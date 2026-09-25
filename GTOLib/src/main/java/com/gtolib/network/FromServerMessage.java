package com.gtolib.network;

import com.gtolib.GTOCore;
import com.gtolib.api.network.NetworkPack;
import dev.architectury.networking.NetworkManager.PacketContext;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public final class FromServerMessage extends BaseS2CMessage {
   private static final MessageType SEND_DATA_FROM_SERVER = GTOCore.NETWORK_MANAGER.registerS2C("from_server", FromServerMessage::new);
   private static final Int2ObjectOpenHashMap<BiConsumer<Player, FriendlyByteBuf>> NETWORK_HANDLE = new Int2ObjectOpenHashMap<>();
   private final int channel;
   private Consumer<FriendlyByteBuf> consumer;
   private FriendlyByteBuf buf;

   public static synchronized NetworkPack register(int id, BiConsumer<Object[], FriendlyByteBuf> write, BiConsumer<Player, FriendlyByteBuf> read) {
      if (NETWORK_HANDLE.put(id, read) != null) {
         throw new IllegalArgumentException("Duplicate channel id: " + id);
      } else {
         return (c, a) -> {
            FromServerMessage message = new FromServerMessage(id, buf -> {
               write.accept(a, buf);
               c.accept(buf);
            });
            switch (a[0]) {
               case MinecraftServer server:
                  message.sendToAll(server);
                  break;
               case ServerPlayer player:
                  message.sendTo(player);
                  break;
               case Iterable players:
                  message.sendTo(players);
                  break;
               default:
                  throw new IllegalArgumentException("Invalid type: " + a[0].getClass().getName());
            }
         };
      }
   }

   private FromServerMessage(int channel, @NotNull Consumer<FriendlyByteBuf> consumer) {
      this.channel = channel;
      this.consumer = consumer;
   }

   private FromServerMessage(FriendlyByteBuf buf) {
      this.channel = buf.readVarInt();
      this.buf = new FriendlyByteBuf(buf.copy());
   }

   @Override
   public MessageType getType() {
      return SEND_DATA_FROM_SERVER;
   }

   @Override
   public void write(FriendlyByteBuf buf) {
      buf.writeVarInt(this.channel);
      this.consumer.accept(buf);
   }

   @Override
   public void handle(PacketContext context) {
      if (this.buf != null) {
         NETWORK_HANDLE.get(this.channel).accept(context.getPlayer(), this.buf);
      }
   }
}
