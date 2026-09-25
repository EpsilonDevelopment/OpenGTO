package com.gtolib.api.wireless;

import com.gregtechceu.gtceu.core.ILevel;
import com.gto.datasynclib.datastream.DataComponentKey;
import com.gtolib.api.network.NetworkPack;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class ReceiverTransmitterHandler {
   private static final ReceiverTransmitterHandler.ConnectionType[] CONNECTION_TYPES = ReceiverTransmitterHandler.ConnectionType.values();
   private static final ReceiverTransmitterHandler.Connection[] EMPTY_CONNECTIONS = new ReceiverTransmitterHandler.Connection[0];
   private static final DataComponentKey<ReceiverTransmitterHandler.ConnectionSet[]> SERVER_CONNECTIONS_KEY = DataComponentKey.createNoCodec(
      "receiver_transmitter_connections"
   );
   private static final Reference2ObjectOpenHashMap<ResourceKey<Level>, ReceiverTransmitterHandler.Connection[]> CLIENT_CONNECTIONS = new Reference2ObjectOpenHashMap<>();
   private static final NetworkPack SYNC = NetworkPack.registerS2C("receiverTransmitterSync", (ignored, buffer) -> readFromBuffer(buffer));

   private ReceiverTransmitterHandler() {
   }

   public static void init() {
   }

   public static void unload() {
      unloadClient();
   }

   public static void unloadClient() {
      CLIENT_CONNECTIONS.clear();
   }

   public static void unload(ResourceKey<Level> dimension, boolean clientSide) {
      if (clientSide) {
         CLIENT_CONNECTIONS.remove(dimension);
      }
   }

   public static void syncToPlayer(ServerPlayer player) {
      SYNC.send(buffer -> writeToBuffer(buffer, player.level()), player);
   }

   private static void writeToBuffer(FriendlyByteBuf buffer, Level level) {
      buffer.writeResourceKey(level.dimension());
      ReceiverTransmitterHandler.ConnectionSet[] sets = level instanceof ServerLevel serverLevel ? getServerConnectionsIfPresent(serverLevel) : null;
      int count = 0;
      if (sets != null) {
         for (ReceiverTransmitterHandler.ConnectionSet set : sets) {
            count += set.size();
         }
      }

      buffer.writeVarInt(count);
      if (sets != null) {
         for (ReceiverTransmitterHandler.ConnectionSet set : sets) {
            set.writeTo(buffer);
         }
      }
   }

   public static void readFromBuffer(FriendlyByteBuf buffer) {
      ResourceKey<Level> dimension = buffer.readResourceKey(Registries.DIMENSION);
      int count = buffer.readVarInt();
      if (count == 0) {
         CLIENT_CONNECTIONS.put(dimension, EMPTY_CONNECTIONS);
      } else {
         ReceiverTransmitterHandler.Connection[] snapshot = new ReceiverTransmitterHandler.Connection[count];

         for (int i = 0; i < count; i++) {
            int type = buffer.readByte();
            snapshot[i] = new ReceiverTransmitterHandler.Connection(
               CONNECTION_TYPES[type], buffer.readBlockPos().immutable(), buffer.readBlockPos().immutable()
            );
         }

         CLIENT_CONNECTIONS.put(dimension, snapshot);
      }
   }

   public static ReceiverTransmitterHandler.Connection[] getClientConnections(ResourceKey<Level> dimension) {
      ReceiverTransmitterHandler.Connection[] connections = CLIENT_CONNECTIONS.get(dimension);
      return connections == null ? EMPTY_CONNECTIONS : connections;
   }

   public static void register(Level level, ReceiverTransmitterHandler.ConnectionType type, BlockPos transmitter, BlockPos receiver) {
      if (level instanceof ServerLevel serverLevel && transmitter != null && receiver != null) {
         ReceiverTransmitterHandler.ConnectionSet[] sets = getServerConnections(serverLevel);
         ReceiverTransmitterHandler.Connection connection = new ReceiverTransmitterHandler.Connection(type, transmitter.immutable(), receiver.immutable());
         if (sets[type.ordinal()].put(connection)) {
            syncDimension(serverLevel);
         }
      }
   }

   public static void unregister(Level level, ReceiverTransmitterHandler.ConnectionType type, BlockPos anyPos) {
      if (level instanceof ServerLevel serverLevel) {
         ReceiverTransmitterHandler.ConnectionSet[] sets = getServerConnectionsIfPresent(serverLevel);
         if (sets != null && sets[type.ordinal()].removeByAny(anyPos)) {
            syncDimension(serverLevel);
         }
      }
   }

   private static ReceiverTransmitterHandler.ConnectionSet[] getServerConnections(ServerLevel level) {
      ReceiverTransmitterHandler.ConnectionSet[] sets = ILevel.getCapability(level, SERVER_CONNECTIONS_KEY);
      if (sets == null) {
         sets = new ReceiverTransmitterHandler.ConnectionSet[CONNECTION_TYPES.length];

         for (int i = 0; i < sets.length; i++) {
            sets[i] = new ReceiverTransmitterHandler.ConnectionSet();
         }

         ILevel.setCapability(level, SERVER_CONNECTIONS_KEY, sets);
      }

      return sets;
   }

   @Nullable
   private static ReceiverTransmitterHandler.ConnectionSet[] getServerConnectionsIfPresent(ServerLevel level) {
      return ILevel.getCapability(level, SERVER_CONNECTIONS_KEY);
   }

   private static void syncDimension(ServerLevel level) {
      if (!level.players().isEmpty()) {
         SYNC.send(buffer -> writeToBuffer(buffer, level), level.players());
      }
   }

   public record Connection(ReceiverTransmitterHandler.ConnectionType type, BlockPos transmitter, BlockPos receiver) {
   }

   private static final class ConnectionSet {
      private final Long2ObjectOpenHashMap<ReceiverTransmitterHandler.Connection> byTransmitter = new Long2ObjectOpenHashMap<>();
      private final Long2ObjectOpenHashMap<ReceiverTransmitterHandler.Connection> byReceiver = new Long2ObjectOpenHashMap<>();

      private int size() {
         return this.byTransmitter.size();
      }

      private ReceiverTransmitterHandler.Connection getByAny(BlockPos pos) {
         ReceiverTransmitterHandler.Connection connection = this.byTransmitter.get(pos.asLong());
         return connection == null ? this.byReceiver.get(pos.asLong()) : connection;
      }

      private boolean put(ReceiverTransmitterHandler.Connection connection) {
         ReceiverTransmitterHandler.Connection current = this.byTransmitter.get(connection.transmitter().asLong());
         if (connection.equals(current)) {
            return false;
         }

         ReceiverTransmitterHandler.Connection oldTransmitter = this.getByAny(connection.transmitter());
         ReceiverTransmitterHandler.Connection oldReceiver = this.getByAny(connection.receiver());
         if (oldTransmitter != null) {
            this.remove(oldTransmitter);
         }

         if (oldReceiver != null && oldReceiver != oldTransmitter) {
            this.remove(oldReceiver);
         }

         this.byTransmitter.put(connection.transmitter().asLong(), connection);
         this.byReceiver.put(connection.receiver().asLong(), connection);
         return true;
      }

      private boolean removeByAny(BlockPos pos) {
         ReceiverTransmitterHandler.Connection connection = this.getByAny(pos);
         if (connection == null) {
            return false;
         }

         this.remove(connection);
         return true;
      }

      private void remove(ReceiverTransmitterHandler.Connection connection) {
         this.byTransmitter.remove(connection.transmitter().asLong());
         this.byReceiver.remove(connection.receiver().asLong());
      }

      private void writeTo(FriendlyByteBuf buffer) {
         for (ReceiverTransmitterHandler.Connection connection : this.byTransmitter.values()) {
            buffer.writeByte(connection.type().ordinal());
            buffer.writeBlockPos(connection.transmitter());
            buffer.writeBlockPos(connection.receiver());
         }
      }
   }

   public enum ConnectionType {
      COMPUTATION,
      DATA;

      // $VF: synthetic method
      private static ReceiverTransmitterHandler.ConnectionType[] $values() {
         return new ReceiverTransmitterHandler.ConnectionType[]{COMPUTATION, DATA};
      }
   }
}
