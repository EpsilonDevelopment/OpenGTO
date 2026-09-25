package com.gtolib.api.beam;

import com.gregtechceu.gtceu.core.ILevel;
import com.gto.datasynclib.datastream.DataComponentKey;
import com.gtolib.api.network.NetworkPack;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.level.ChunkEvent.Load;
import net.minecraftforge.event.level.ChunkWatchEvent.UnWatch;
import net.minecraftforge.event.level.ChunkWatchEvent.Watch;
import org.jetbrains.annotations.Nullable;

public final class BeamManager {
   private static final byte FULL_SYNC = 0;
   private static final byte DELTA_SYNC = 1;
   private static final int MAX_SYNCED_BEAMS = 1048576;
   private static final DataComponentKey<BeamManager> MANAGER_KEY = DataComponentKey.createNoCodec("beam_manager");
   private static boolean initialized;
   private static final NetworkPack SYNC = NetworkPack.registerS2C(
      "rayBeamManagerSync", (args, buffer) -> ((BeamManager.SyncMessage)args[1]).write(buffer), BeamManager::readSync
   );
   private final Level level;
   private final Int2ObjectOpenHashMap<Beam> beams = new Int2ObjectOpenHashMap<>();
   private final Collection<Beam> beamView = Collections.unmodifiableCollection(this.beams.values());
   private final Reference2IntOpenHashMap<Beam> beamIds = new Reference2IntOpenHashMap<>();
   private final Long2ObjectOpenHashMap<IntSet> beamsByChunk = new Long2ObjectOpenHashMap<>();
   private final Int2ObjectOpenHashMap<LongSet> chunksByBeam = new Int2ObjectOpenHashMap<>();
   private final Int2ObjectOpenHashMap<List<BeamManager.ManagedPass>> activePassesByBeam = new Int2ObjectOpenHashMap<>();
   private final Int2ObjectOpenHashMap<BeamManager.DependentOwner> ownerByBeam = new Int2ObjectOpenHashMap<>();
   private final Map<BeamManager.DependentOwner, IntSet> dependentBeamsByOwner = new HashMap<>();
   private final Int2ObjectOpenHashMap<IntSet> dependentBeamsByParent = new Int2ObjectOpenHashMap<>();
   private final Int2ObjectOpenHashMap<ReferenceOpenHashSet<ServerPlayer>> viewersByBeam = new Int2ObjectOpenHashMap<>();
   private final IntSet pendingRebuilds = new IntOpenHashSet();
   private final IntSet pendingPathRebuilds = new IntOpenHashSet();
   private final Int2ObjectOpenHashMap<Beam> pendingUpserts = new Int2ObjectOpenHashMap<>();
   private final IntSet pendingRemovals = new IntOpenHashSet();
   private boolean syncDirty;
   private int nextBeamId = 1;
   private long syncRevision;
   private long clientSyncRevision = -1L;

   private BeamManager(Level level) {
      this.level = level;
      this.beamIds.defaultReturnValue(-1);
   }

   public static synchronized void init() {
      if (!initialized) {
         initialized = true;
         MinecraftForge.EVENT_BUS.addListener(BeamManager::onLevelTick);
         MinecraftForge.EVENT_BUS.addListener(BeamManager::onPlayerLogin);
         MinecraftForge.EVENT_BUS.addListener(BeamManager::onPlayerDimensionChange);
         MinecraftForge.EVENT_BUS.addListener(BeamManager::onPlayerLoggedOut);
         MinecraftForge.EVENT_BUS.addListener(BeamManager::onChunkWatch);
         MinecraftForge.EVENT_BUS.addListener(BeamManager::onChunkUnwatch);
         MinecraftForge.EVENT_BUS.addListener(BeamManager::onChunkLoad);
      }
   }

   public static BeamManager get(Level level) {
      BeamManager manager = ILevel.getCapability(level, MANAGER_KEY);
      if (manager != null) {
         return manager;
      }

      manager = new BeamManager(level);
      ILevel.setCapability(level, MANAGER_KEY, manager);
      return manager;
   }

   @Nullable
   public static BeamManager getIfPresent(Level level) {
      return ILevel.getCapability(level, MANAGER_KEY);
   }

   public Level level() {
      return this.level;
   }

   public Collection<Beam> beams() {
      return this.beamView;
   }

   public Map<Integer, Beam> beamsById() {
      return Map.copyOf(this.beams);
   }

   @Nullable
   public Beam getBeam(int id) {
      return this.beams.get(id);
   }

   public int register(Beam beam) {
      return this.register(beam, null);
   }

   public int registerDependent(BeamPassContext ownerContext, Beam beam) {
      if (!this.isPassActive(ownerContext)) {
         throw new IllegalStateException("Dependent ray beam owner pass is no longer active: " + ownerContext.key());
      } else {
         return this.register(beam, BeamManager.DependentOwner.from(ownerContext));
      }
   }

   public boolean isPassActive(BeamPassContext context) {
      if (context.level() != this.level) {
         return false;
      } else {
         List<BeamManager.ManagedPass> passes = this.activePassesByBeam.get(context.beamId());
         int passIndex = context.passIndex();
         if (passes != null && passIndex >= 0 && passIndex < passes.size()) {
            BeamPassContext active = passes.get(passIndex).context();
            return active.key().equals(context.key()) && active.operatorPos().equals(context.operatorPos());
         } else {
            return false;
         }
      }
   }

   private int register(Beam beam, @Nullable BeamManager.DependentOwner owner) {
      int existingId = this.beamIds.getInt(beam);
      if (existingId >= 0) {
         BeamManager.DependentOwner existingOwner = this.ownerByBeam.get(existingId);
         if (!Objects.equals(owner, existingOwner)) {
            throw new IllegalArgumentException("Ray beam is already registered with another owner");
         } else {
            return existingId;
         }
      } else {
         int id = this.nextBeamId++;
         beam.rebuildPath(this.level, id);
         this.beams.put(id, beam);
         this.beamIds.put(beam, id);
         if (owner != null) {
            this.linkOwner(id, owner);
         }

         this.index(id, beam);
         this.reconcilePasses(id, beam);
         this.sendUpsert(id, beam);
         return id;
      }
   }

   public void update(int id, Beam beam) {
      this.update(id, beam, false);
   }

   public void updatePath(int id, Beam beam) {
      this.update(id, beam, true);
   }

   private void update(int id, Beam beam, boolean invalidateDependents) {
      Beam oldBeam = this.beams.get(id);
      if (oldBeam == null) {
         throw new IllegalArgumentException("Unknown ray beam id: " + id);
      }

      if (invalidateDependents) {
         this.removeDependentsOwnedBy(id);
      }

      this.unindex(id);
      this.beamIds.removeInt(oldBeam);
      beam.rebuildPath(this.level, id);
      this.beams.put(id, beam);
      this.beamIds.put(beam, id);
      this.index(id, beam);
      this.reconcilePasses(id, beam);
      this.pendingRebuilds.remove(id);
      this.pendingPathRebuilds.remove(id);
      this.sendUpsert(id, beam);
   }

   public boolean unregister(int id) {
      Beam beam = this.beams.remove(id);
      if (beam == null) {
         return false;
      }

      this.unindex(id);
      this.removePasses(id);
      this.removeDependentsOwnedBy(id);
      this.unlinkOwner(id);
      this.beamIds.removeInt(beam);
      this.pendingRebuilds.remove(id);
      this.pendingPathRebuilds.remove(id);
      this.sendRemove(id);
      return true;
   }

   public boolean unregister(Beam beam) {
      int id = this.beamIds.getInt(beam);
      return id >= 0 && this.unregister(id);
   }

   public void requestRebuild(int id) {
      if (this.beams.containsKey(id)) {
         this.pendingRebuilds.add(id);
      }
   }

   public void requestPathRebuild(int id) {
      if (this.beams.containsKey(id)) {
         this.pendingRebuilds.add(id);
         this.pendingPathRebuilds.add(id);
      }
   }

   public void requestRebuild(Beam beam) {
      int id = this.beamIds.getInt(beam);
      if (id >= 0) {
         this.requestRebuild(id);
      }
   }

   public static void requestRebuildAt(Level level, BlockPos pos) {
      BeamManager manager = getIfPresent(level);
      if (manager != null && !level.isClientSide()) {
         manager.invalidate(pos, false);
      }
   }

   public static void requestPathRebuildAt(Level level, BlockPos pos) {
      BeamManager manager = getIfPresent(level);
      if (manager != null && !level.isClientSide()) {
         manager.invalidate(pos, true);
      }
   }

   public static void onBlockChanged(Level level, BlockPos pos) {
      requestPathRebuildAt(level, pos);
   }

   private void invalidate(BlockPos pos, boolean pathChanged) {
      IntSet affected = this.beamsByChunk.get(ChunkPos.asLong(pos));
      if (affected != null) {
         long blockPos = pos.asLong();
         IntIterator iterator = affected.iterator();

         while (iterator.hasNext()) {
            int id = iterator.nextInt();
            Beam beam = this.beams.get(id);
            if (beam != null && crosses(beam, blockPos)) {
               this.pendingRebuilds.add(id);
               if (pathChanged) {
                  this.pendingPathRebuilds.add(id);
               }
            }
         }
      }
   }

   private void invalidateChunk(long chunkPos) {
      IntSet affected = this.beamsByChunk.get(chunkPos);
      if (affected != null) {
         this.pendingRebuilds.addAll(affected);
         this.pendingPathRebuilds.addAll(affected);
      }
   }

   private void flushRebuilds() {
      if (!this.pendingRebuilds.isEmpty()) {
         int[] ids = this.pendingRebuilds.toIntArray();
         this.pendingRebuilds.clear();

         for (int id : ids) {
            Beam beam = this.beams.get(id);
            if (beam != null) {
               if (this.pendingPathRebuilds.remove(id)) {
                  this.removeDependentsOwnedBy(id);
               }

               this.unindex(id);
               beam.rebuildPath(this.level, id);
               this.index(id, beam);
               this.reconcilePasses(id, beam);
               this.sendUpsert(id, beam);
            }
         }
      }
   }

   private void index(int id, Beam beam) {
      LongOpenHashSet chunks = new LongOpenHashSet();
      BeamNode initialNode = beam.initialNode();
      this.addChunk(id, chunks, ChunkPos.asLong(Mth.floor(initialNode.x) >> 4, Mth.floor(initialNode.z) >> 4));

      for (BeamSegment segment : beam.segments()) {
         LongIterator iterator = segment.passedBlocks.iterator();

         while (iterator.hasNext()) {
            this.addChunk(id, chunks, chunkKey(iterator.nextLong()));
         }
      }

      this.chunksByBeam.put(id, chunks);
   }

   private void addChunk(int beamId, LongSet chunks, long chunkPos) {
      if (chunks.add(chunkPos)) {
         this.beamsByChunk.computeIfAbsent(chunkPos, ignored -> new IntOpenHashSet()).add(beamId);
      }
   }

   private void unindex(int id) {
      LongSet chunks = this.chunksByBeam.remove(id);
      if (chunks != null) {
         LongIterator iterator = chunks.iterator();

         while (iterator.hasNext()) {
            this.removeChunk(iterator.nextLong(), id);
         }
      }
   }

   private void removeChunk(long chunkPos, int beamId) {
      IntSet ids = this.beamsByChunk.get(chunkPos);
      if (ids != null && ids.remove(beamId) && ids.isEmpty()) {
         this.beamsByChunk.remove(chunkPos);
      }
   }

   private static boolean crosses(Beam beam, long blockPos) {
      BeamNode initialNode = beam.initialNode();
      if (BlockPos.asLong(Mth.floor(initialNode.x), Mth.floor(initialNode.y), Mth.floor(initialNode.z)) == blockPos) {
         return true;
      }

      for (BeamSegment segment : beam.segments()) {
         if (segment.passedBlocks.contains(blockPos)) {
            return true;
         }
      }

      return false;
   }

   private static long chunkKey(long blockPos) {
      return ChunkPos.asLong(BlockPos.getX(blockPos) >> 4, BlockPos.getZ(blockPos) >> 4);
   }

   private void reconcilePasses(int beamId, Beam beam) {
      ArrayList<BeamManager.ManagedPass> current = new ArrayList<>();

      for (BeamSegment segment : beam.segments()) {
         if (segment.operator != null && segment.passContext != null) {
            current.add(new BeamManager.ManagedPass(segment.operator, segment.passContext));
         }
      }

      List<BeamManager.ManagedPass> previous = this.activePassesByBeam.put(beamId, current);
      if (previous != null) {
         for (BeamManager.ManagedPass oldPass : previous) {
            int passIndex = oldPass.context().passIndex();
            boolean remains = false;
            if (passIndex >= 0 && passIndex < current.size()) {
               BeamManager.ManagedPass newPass = current.get(passIndex);
               remains = newPass.operator() == oldPass.operator() && newPass.context().operatorPos().equals(oldPass.context().operatorPos());
            }

            if (!remains) {
               this.removeDependents(oldPass.context());
               oldPass.operator().onRayBeamPassRemoved(oldPass.context());
            }
         }
      }
   }

   private void removePasses(int beamId) {
      List<BeamManager.ManagedPass> passes = this.activePassesByBeam.remove(beamId);
      if (passes != null) {
         for (BeamManager.ManagedPass pass : passes) {
            this.removeDependents(pass.context());
            pass.operator().onRayBeamPassRemoved(pass.context());
         }
      }
   }

   private void linkOwner(int beamId, BeamManager.DependentOwner owner) {
      this.ownerByBeam.put(beamId, owner);
      this.dependentBeamsByOwner.computeIfAbsent(owner, ignored -> new IntOpenHashSet()).add(beamId);
      this.dependentBeamsByParent.computeIfAbsent(owner.passKey().beamId(), ignored -> new IntOpenHashSet()).add(beamId);
   }

   private void unlinkOwner(int beamId) {
      BeamManager.DependentOwner owner = this.ownerByBeam.remove(beamId);
      if (owner != null) {
         IntSet ids = this.dependentBeamsByOwner.get(owner);
         if (ids != null && ids.remove(beamId) && ids.isEmpty()) {
            this.dependentBeamsByOwner.remove(owner);
         }

         IntSet parentIds = this.dependentBeamsByParent.get(owner.passKey().beamId());
         if (parentIds != null && parentIds.remove(beamId) && parentIds.isEmpty()) {
            this.dependentBeamsByParent.remove(owner.passKey().beamId());
         }
      }
   }

   private void removeDependents(BeamPassContext context) {
      this.removeDependents(BeamManager.DependentOwner.from(context));
   }

   private void removeDependents(BeamManager.DependentOwner owner) {
      IntSet ids = this.dependentBeamsByOwner.remove(owner);
      if (ids != null) {
         IntIterator iterator = ids.iterator();

         while (iterator.hasNext()) {
            this.unregister(iterator.nextInt());
         }
      }
   }

   private void removeDependentsOwnedBy(int beamId) {
      IntSet ids = this.dependentBeamsByParent.remove(beamId);
      if (ids != null) {
         IntIterator iterator = ids.iterator();

         while (iterator.hasNext()) {
            this.unregister(iterator.nextInt());
         }
      }
   }

   private void sendUpsert(int id, Beam beam) {
      if (!this.level.isClientSide()) {
         this.pendingRemovals.remove(id);
         this.pendingUpserts.put(id, beam);
         this.syncDirty = true;
      }
   }

   private void sendRemove(int id) {
      if (!this.level.isClientSide()) {
         this.pendingUpserts.remove(id);
         this.pendingRemovals.add(id);
         this.syncDirty = true;
      }
   }

   private void flushSync() {
      if (this.syncDirty && this.level instanceof ServerLevel serverLevel) {
         this.syncDirty = false;
         Reference2ObjectOpenHashMap<ServerPlayer, List<BeamManager.BeamEntry>> var11 = new Reference2ObjectOpenHashMap<>();
         Reference2ObjectOpenHashMap<ServerPlayer, IntArrayList> removalsByPlayer = new Reference2ObjectOpenHashMap<>();
         ObjectIterator revision = this.pendingUpserts.int2ObjectEntrySet().fastIterator();

         while (revision.hasNext()) {
            Entry<Beam> entry = (Entry<Beam>)revision.next();
            int id = entry.getIntKey();
            ReferenceOpenHashSet<ServerPlayer> currentViewers = this.trackingPlayers(id, serverLevel);
            ReferenceOpenHashSet<ServerPlayer> knownViewers = this.viewersByBeam.computeIfAbsent(id, ignored -> new ReferenceOpenHashSet<>());

            for (ServerPlayer player : currentViewers) {
               knownViewers.add(player);
               var11.computeIfAbsent(player, ignored -> new ArrayList<>()).add(new BeamManager.BeamEntry(id, entry.getValue()));
            }

            ObjectIterator<ServerPlayer> iterator2 = knownViewers.iterator();

            while (iterator2.hasNext()) {
               ServerPlayer player = iterator2.next();
               if (!currentViewers.contains(player)) {
                  iterator2.remove();
                  removalsByPlayer.computeIfAbsent(player, ignored -> new IntArrayList()).add(id);
               }
            }
         }

         IntIterator iterator = this.pendingRemovals.iterator();

         while (iterator.hasNext()) {
            int id = iterator.nextInt();
            ReferenceOpenHashSet<ServerPlayer> viewers = this.viewersByBeam.remove(id);
            if (viewers != null) {
               for (ServerPlayer player : viewers) {
                  removalsByPlayer.computeIfAbsent(player, ignored -> new IntArrayList()).add(id);
               }
            }
         }

         if (!var11.isEmpty() || !removalsByPlayer.isEmpty()) {
            long revisionx = ++this.syncRevision;
            ReferenceOpenHashSet<ServerPlayer> players = new ReferenceOpenHashSet<>();
            players.addAll(var11.keySet());
            players.addAll(removalsByPlayer.keySet());

            for (ServerPlayer player : players) {
               List<BeamManager.BeamEntry> entries = (List<BeamManager.BeamEntry>)var11.get(player);
               if (entries == null) {
                  entries = Collections.emptyList();
               }

               IntArrayList removals = (IntArrayList)removalsByPlayer.get(player);
               SYNC.send(player, new BeamManager.SyncMessage((byte)1, revisionx, removals == null ? new int[0] : removals.toIntArray(), entries));
            }
         }

         this.pendingUpserts.clear();
         this.pendingRemovals.clear();
      }
   }

   private void sendFull(ServerPlayer player) {
      List<BeamManager.BeamEntry> entries = this.snapshot();

      for (BeamManager.BeamEntry entry : entries) {
         this.viewersByBeam.computeIfAbsent(entry.id(), ignored -> new ReferenceOpenHashSet<>()).add(player);
      }

      SYNC.send(player, new BeamManager.SyncMessage((byte)0, this.syncRevision, new int[0], entries));
   }

   private ReferenceOpenHashSet<ServerPlayer> trackingPlayers(int beamId, ServerLevel serverLevel) {
      ReferenceOpenHashSet<ServerPlayer> players = new ReferenceOpenHashSet<>();
      LongSet chunks = this.chunksByBeam.get(beamId);
      if (chunks == null) {
         return players;
      }

      LongIterator iterator = chunks.iterator();

      while (iterator.hasNext()) {
         players.addAll(serverLevel.getChunkSource().chunkMap.getPlayers(new ChunkPos(iterator.nextLong()), false));
      }

      return players;
   }

   private boolean isTrackedBy(ServerPlayer player, long chunkPos, ServerLevel serverLevel) {
      return serverLevel.getChunkSource().chunkMap.getPlayers(new ChunkPos(chunkPos), false).contains(player);
   }

   private void handleChunkWatch(ServerPlayer player, ChunkPos chunkPos, boolean watched) {
      if (this.level instanceof ServerLevel serverLevel) {
         IntSet ids = this.beamsByChunk.get(chunkPos.toLong());
         if (ids != null) {
            ArrayList<BeamManager.BeamEntry> updates = new ArrayList<>();
            IntArrayList removals = new IntArrayList();
            IntIterator iterator = ids.iterator();

            while (iterator.hasNext()) {
               int id = iterator.nextInt();
               Beam beam = this.beams.get(id);
               if (beam != null) {
                  ReferenceOpenHashSet<ServerPlayer> viewers = this.viewersByBeam.computeIfAbsent(id, ignored -> new ReferenceOpenHashSet<>());
                  if (watched) {
                     if (viewers.add(player)) {
                        updates.add(new BeamManager.BeamEntry(id, beam));
                     }
                  } else if (viewers.contains(player) && !this.isTrackedByAnyChunk(player, id, serverLevel)) {
                     viewers.remove(player);
                     removals.add(id);
                  }
               }
            }

            if (!updates.isEmpty() || !removals.isEmpty()) {
               SYNC.send(player, new BeamManager.SyncMessage((byte)1, ++this.syncRevision, removals.toIntArray(), updates));
            }
         }
      }
   }

   private boolean isTrackedByAnyChunk(ServerPlayer player, int beamId, ServerLevel serverLevel) {
      LongSet chunks = this.chunksByBeam.get(beamId);
      if (chunks == null) {
         return false;
      }

      LongIterator iterator = chunks.iterator();

      while (iterator.hasNext()) {
         if (this.isTrackedBy(player, iterator.nextLong(), serverLevel)) {
            return true;
         }
      }

      return false;
   }

   private List<BeamManager.BeamEntry> snapshot() {
      ArrayList<BeamManager.BeamEntry> entries = new ArrayList<>(this.beams.size());
      ObjectIterator<Entry<Beam>> iterator = this.beams.int2ObjectEntrySet().fastIterator();

      while (iterator.hasNext()) {
         Entry<Beam> entry = iterator.next();
         entries.add(new BeamManager.BeamEntry(entry.getIntKey(), entry.getValue()));
      }

      return entries;
   }

   private static void readSync(Player player, FriendlyByteBuf buffer) {
      byte operation = buffer.readByte();
      long revision = buffer.readVarLong();
      int removedCount = buffer.readVarInt();
      if (removedCount >= 0 && removedCount <= 1048576) {
         int[] removedIds = new int[removedCount];

         for (int i = 0; i < removedCount; i++) {
            removedIds[i] = buffer.readVarInt();
         }

         int count = buffer.readVarInt();
         if (count < 0 || count > 1048576) {
            throw new IllegalArgumentException("Invalid synchronized ray beam count: " + count);
         }

         if (player.level().isClientSide()) {
            BeamManager manager = get(player.level());
            if (revision > manager.clientSyncRevision) {
               if (operation == 0) {
                  manager.clearClient();
               } else if (operation != 1) {
                  throw new IllegalArgumentException("Unknown ray beam sync operation: " + operation);
               }

               for (int id : removedIds) {
                  manager.beams.remove(id);
               }

               for (int i = 0; i < count; i++) {
                  manager.beams.put(buffer.readVarInt(), Beam.readFromNetwork(buffer));
               }

               manager.clientSyncRevision = revision;
            }
         }
      } else {
         throw new IllegalArgumentException("Invalid removed ray beam count: " + removedCount);
      }
   }

   private void clearClient() {
      this.beams.clear();
      this.beamIds.clear();
      this.beamsByChunk.clear();
      this.chunksByBeam.clear();
      this.activePassesByBeam.clear();
      this.ownerByBeam.clear();
      this.dependentBeamsByOwner.clear();
      this.dependentBeamsByParent.clear();
      this.viewersByBeam.clear();
      this.pendingRebuilds.clear();
      this.pendingPathRebuilds.clear();
      this.pendingUpserts.clear();
      this.pendingRemovals.clear();
      this.syncDirty = false;
   }

   private static void onLevelTick(LevelTickEvent event) {
      if (event.phase == Phase.END) {
         BeamManager manager = getIfPresent(event.level);
         if (manager != null && !manager.level.isClientSide()) {
            manager.flushRebuilds();
            manager.flushSync();
         }
      }
   }

   private static void onPlayerLogin(PlayerLoggedInEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         get(player.serverLevel()).sendFull(player);
      }
   }

   private static void onPlayerDimensionChange(PlayerChangedDimensionEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         ServerLevel oldLevel = player.server.getLevel(event.getFrom());
         if (oldLevel != null) {
            BeamManager oldManager = getIfPresent(oldLevel);
            if (oldManager != null) {
               oldManager.removeViewer(player);
            }
         }

         get(player.serverLevel()).sendFull(player);
      }
   }

   private static void onPlayerLoggedOut(PlayerLoggedOutEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         BeamManager manager = getIfPresent(player.serverLevel());
         if (manager != null) {
            manager.removeViewer(player);
         }
      }
   }

   private static void onChunkWatch(Watch event) {
      if (event.getPlayer() instanceof ServerPlayer player) {
         BeamManager manager = getIfPresent(event.getLevel());
         if (manager != null) {
            manager.handleChunkWatch(player, event.getPos(), true);
         }
      }
   }

   private static void onChunkUnwatch(UnWatch event) {
      if (event.getPlayer() instanceof ServerPlayer player) {
         BeamManager manager = getIfPresent(event.getLevel());
         if (manager != null) {
            manager.handleChunkWatch(player, event.getPos(), false);
         }
      }
   }

   private static void onChunkLoad(Load event) {
      if (event.getLevel() instanceof ServerLevel level) {
         BeamManager manager = getIfPresent(level);
         if (manager != null) {
            manager.invalidateChunk(event.getChunk().getPos().toLong());
         }
      }
   }

   private void removeViewer(ServerPlayer player) {
      for (ReferenceOpenHashSet<ServerPlayer> viewers : this.viewersByBeam.values()) {
         viewers.remove(player);
      }
   }

   private record BeamEntry(int id, @Nullable Beam beam) {
   }

   private record DependentOwner(BeamPassKey passKey, BlockPos operatorPos) {
      private static BeamManager.DependentOwner from(BeamPassContext context) {
         return new BeamManager.DependentOwner(context.key(), context.operatorPos());
      }
   }

   private record ManagedPass(IBeamOperator operator, BeamPassContext context) {
   }

   private record SyncMessage(byte operation, long revision, int[] removedIds, List<BeamManager.BeamEntry> entries) {
      private void write(FriendlyByteBuf buffer) {
         buffer.writeByte(this.operation);
         buffer.writeVarLong(this.revision);
         buffer.writeVarInt(this.removedIds.length);

         for (int id : this.removedIds) {
            buffer.writeVarInt(id);
         }

         buffer.writeVarInt(this.entries.size());

         for (BeamManager.BeamEntry entry : this.entries) {
            buffer.writeVarInt(entry.id());
            entry.beam().writeToNetwork(buffer);
         }
      }
   }
}
