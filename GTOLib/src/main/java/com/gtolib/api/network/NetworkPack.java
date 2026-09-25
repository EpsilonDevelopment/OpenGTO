package com.gtolib.api.network;

import com.gregtechceu.gtceu.utils.GTUtil;
import com.gtolib.network.FromClientMessage;
import com.gtolib.network.FromServerMessage;
import com.gtolib.utils.GTOUtils;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public interface NetworkPack {
   static NetworkPack registerS2C(String id, BiConsumer<Object[], FriendlyByteBuf> write, BiConsumer<Player, FriendlyByteBuf> read) {
      return FromServerMessage.register(id.hashCode(), write, read);
   }

   static NetworkPack registerC2S(String id, BiConsumer<Object[], FriendlyByteBuf> write, BiConsumer<ServerPlayer, FriendlyByteBuf> read) {
      return FromClientMessage.register(id.hashCode(), write, read);
   }

   static NetworkPack registerS2C(String id, BiConsumer<Player, FriendlyByteBuf> read) {
      return FromServerMessage.register(id.hashCode(), GTOUtils.NOOP_BI_CONSUMER, read);
   }

   static NetworkPack registerC2S(String id, BiConsumer<ServerPlayer, FriendlyByteBuf> read) {
      return FromClientMessage.register(id.hashCode(), GTOUtils.NOOP_BI_CONSUMER, read);
   }

   static NetworkPack registerS2C(int id, BiConsumer<Object[], FriendlyByteBuf> write, BiConsumer<Player, FriendlyByteBuf> read) {
      return FromServerMessage.register(id, write, read);
   }

   static NetworkPack registerC2S(int id, BiConsumer<Object[], FriendlyByteBuf> write, BiConsumer<ServerPlayer, FriendlyByteBuf> read) {
      return FromClientMessage.register(id, write, read);
   }

   static NetworkPack registerS2C(int id, BiConsumer<Player, FriendlyByteBuf> read) {
      return FromServerMessage.register(id, GTOUtils.NOOP_BI_CONSUMER, read);
   }

   static NetworkPack registerC2S(int id, BiConsumer<ServerPlayer, FriendlyByteBuf> read) {
      return FromClientMessage.register(id, GTOUtils.NOOP_BI_CONSUMER, read);
   }

   void send(Consumer<FriendlyByteBuf> var1, Object... var2);

   default void send(Object... args) {
      this.send(GTUtil.NOOP_CONSUMER, args);
   }
}
