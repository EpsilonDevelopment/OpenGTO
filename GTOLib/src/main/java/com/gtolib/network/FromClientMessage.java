package com.gtolib.network;

import com.gregtechceu.gtceu.GTCEu;
import com.gtolib.GTOCore;
import com.gtolib.api.network.NetworkPack;
import dev.architectury.networking.NetworkManager.PacketContext;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public final class FromClientMessage extends BaseC2SMessage {
   private static final NetworkPack NULL_PACK = (a, b) -> {};
   private static final MessageType SEND_DATA_FROM_CLIENT = GTOCore.NETWORK_MANAGER.registerC2S("from_client", FromClientMessage::new);
   private static final Int2ObjectOpenHashMap<BiConsumer<ServerPlayer, FriendlyByteBuf>> NETWORK_HANDLE = new Int2ObjectOpenHashMap<>();
   private final int channel;
   private Consumer<FriendlyByteBuf> consumer;
   private FriendlyByteBuf buf;

   public static synchronized NetworkPack register(int id, BiConsumer<Object[], FriendlyByteBuf> write, BiConsumer<ServerPlayer, FriendlyByteBuf> read) {
      if (NETWORK_HANDLE.put(id, read) != null) {
         throw new IllegalArgumentException("Duplicate channel id: " + id);
      } else {
         return GTCEu.isClientSide() ? (c, a) -> new FromClientMessage(id, buf -> {
            write.accept(a, buf);
            c.accept(buf);
         }).sendToServer() : NULL_PACK;
      }
   }

   private FromClientMessage(int channel, @NotNull Consumer<FriendlyByteBuf> consumer) {
      this.channel = channel;
      this.consumer = consumer;
   }

   private FromClientMessage(FriendlyByteBuf buf) {
      this.channel = buf.readVarInt();
      this.buf = new FriendlyByteBuf(buf.copy());
   }

   @Override
   public MessageType getType() {
      return SEND_DATA_FROM_CLIENT;
   }

   @Override
   public void write(FriendlyByteBuf buf) {
      buf.writeVarInt(this.channel);
      this.consumer.accept(buf);
   }

   @Override
   public void handle(PacketContext context) {
      if (this.buf != null && context.getPlayer() instanceof ServerPlayer serverPlayer) {
         NETWORK_HANDLE.get(this.channel).accept(serverPlayer, this.buf);
      }
   }
}
