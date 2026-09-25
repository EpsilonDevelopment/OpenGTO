package com.gtolib.api.machine.impl.part;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.IGridNodeListener.State;
import appeng.api.networking.events.GridCraftingCpuChange;
import appeng.api.networking.security.IActionHost;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.helpers.MachineSource;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.pattern.Predicates.DataKey;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.common.network.packets.SCPacketUpdateActiveBlock;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.IGridConnectedMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.trait.GridNodeHolder;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.gtolib.api.ae2.crafting.ICraftingCPUCluster;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public final class CraftingInterfacePartMachine extends MultiblockPartMachine implements IMachineLife, IGridConnectedMachine, IActionHost {
   @SaveToDisk
   private final GridNodeHolder nodeHolder;
   @SaveToDisk(defaultValue = "0")
   public long storage;
   @SaveToDisk(defaultValue = "0")
   public int thread;
   @SaveToDisk(defaultValue = "0")
   public int accelerator;
   private boolean activated;
   private boolean isOnline;
   private CompoundTag tag;
   private final MachineSource source;
   private final ObjectArrayList<CraftingCPUCluster> clusters = new ObjectArrayList<>();
   private final List<CraftingCPUCluster> list = new ArrayList<>();
   private final ConditionalSubscriptionHandler tickSubs;

   public CraftingInterfacePartMachine(MetaMachineBlockEntity holder) {
      super(holder);
      this.nodeHolder = new GridNodeHolder(this);
      this.source = new MachineSource(this);
      this.tickSubs = new ConditionalSubscriptionHandler(this, this::tickUpdate, 10, () -> true);
   }

   @Override
   public void saveCustomPersistedData(@NotNull CompoundTag tag, boolean forDrop) {
      super.saveCustomPersistedData(tag, forDrop);
      ListTag list = new ListTag();

      for (CraftingCPUCluster cluster : this.clusters) {
         CompoundTag c = new CompoundTag();
         cluster.writeToNBT(c);
         list.add(c);
      }

      tag.put("clusters", list);
   }

   @Override
   public void loadCustomPersistedData(@NotNull CompoundTag tag) {
      super.loadCustomPersistedData(tag);
      this.tag = tag;
   }

   @Override
   public void onLoad() {
      super.onLoad();
      if (this.getLevel() instanceof ServerLevel) {
         this.tickSubs.initialize(this.getLevel());
         GridHelper.onFirstTick(this.holder, b -> this.updateList());
      }
   }

   @Override
   public void onUnload() {
      super.onUnload();
      this.tickSubs.unsubscribe();
   }

   @Override
   public void onMachineRemoved() {
      if (this.getLevel() instanceof ServerLevel) {
         for (CraftingCPUCluster cluster : this.clusters) {
            cluster.setDestroyed(true);
         }

         this.clusters.clear();
         this.updateList();
      }
   }

   @Override
   public void onMainNodeStateChanged(State reason) {
      IGridConnectedMachine.super.onMainNodeStateChanged(reason);
      this.updateList();
   }

   private void tickUpdate() {
      this.updateList();
      if (this.list.size() > 1) {
         if (this.activated) {
            return;
         }

         this.activated = true;
         IMultiController c = this.getController();
         if (c != null) {
            GTNetwork.NETWORK.sendToAll(new SCPacketUpdateActiveBlock(c.getMultiblockState().matchContext.get(DataKey.ACTIVE_BLOCKS), true));
         }
      } else if (this.activated) {
         this.activated = false;
         IMultiController c = this.getController();
         if (c != null) {
            GTNetwork.NETWORK.sendToAll(new SCPacketUpdateActiveBlock(c.getMultiblockState().matchContext.get(DataKey.ACTIVE_BLOCKS), false));
         }
      }
   }

   private void updateList() {
      IGrid grid = this.nodeHolder.getMainNode().getGrid();
      if (grid != null) {
         this.onChanged();
         if (this.tag != null) {
            ListTag list = this.tag.getList("clusters", 10);
            this.clusters.clear();

            for (int i = 0; i < list.size(); i++) {
               CraftingCPUCluster cluster = ICraftingCPUCluster.create(this, this.source, this.storage, this.accelerator);
               cluster.readFromNBT(list.getCompound(i));
               this.clusters.add(cluster);
            }

            this.tag = null;
         }

         int ida = 0;
         int size = this.list.size();
         if (size == 0) {
            if (this.clusters.isEmpty()) {
               return;
            }
         } else {
            for (CraftingCPUCluster cluster : this.list) {
               if (!cluster.isDestroyed() && !cluster.isBusy()) {
                  ida++;
               }
            }
         }

         if (ida != 1) {
            this.list.clear();
            CraftingCPUCluster idle = null;

            for (CraftingCPUCluster cluster : this.clusters) {
               if (cluster.isBusy()) {
                  this.list.add(cluster);
               } else if (idle == null) {
                  idle = cluster;
               }
            }

            if (idle != null) {
               this.list.add(idle);
            }

            grid.postEvent(new GridCraftingCpuChange(this.nodeHolder.getMainNode().getNode()));
         }

         if (size < this.list.size()) {
            this.tickSubs.setCycle(0);
         } else if (this.tickSubs.getCycle() < 20) {
            this.tickSubs.setCycle(this.tickSubs.getCycle() + 1);
         }
      }
   }

   public void setStorage(long storage) {
      this.storage = storage;

      for (CraftingCPUCluster cluster : this.clusters) {
         cluster.setStorage(storage);
      }
   }

   public void setAccelerator(int accelerator) {
      this.accelerator = accelerator;

      for (CraftingCPUCluster cluster : this.clusters) {
         cluster.setAccelerator(accelerator);
      }
   }

   public void setThread(int thread) {
      this.thread = thread;
      int size = this.clusters.size();
      if (size != thread) {
         int var5 = Math.max(size, thread);
         if (var5 != 0) {
            this.clusters.size(var5);

            for (int i = 0; i < var5; i++) {
               CraftingCPUCluster cluster = this.clusters.get(i);
               if (cluster == null) {
                  cluster = ICraftingCPUCluster.create(this, this.source, this.storage, this.accelerator);
                  this.clusters.set(i, cluster);
               } else if (i >= thread) {
                  cluster.setDestroyed(true);
                  this.clusters.set(i, null);
               }
            }

            this.clusters.removeIf(Objects::isNull);
         }
      }
   }

   public List<CraftingCPUCluster> getClusters() {
      return this.list;
   }

   @Override
   public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
      return false;
   }

   @Override
   public IManagedGridNode getMainNode() {
      return this.nodeHolder.getMainNode();
   }

   @Override
   public boolean isOnline() {
      return this.isOnline;
   }

   @Override
   public void setOnline(boolean online) {
      this.isOnline = online;
   }
}
