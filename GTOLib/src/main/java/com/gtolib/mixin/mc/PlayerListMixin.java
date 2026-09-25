package com.gtolib.mixin.mc;

import com.gtolib.utils.SrmManager;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerList.class)
public class PlayerListMixin {
   @Inject(method = "respawn", at = @At("RETURN"), cancellable = true)
   private void respawnMixin(ServerPlayer var1, boolean var2, CallbackInfoReturnable<ServerPlayer> var3, @Local(ordinal = 1) ServerPlayer var4) {
      CompoundTag var5 = var1.getPersistentData();
      if (!var5.isEmpty()) {
         var4.getPersistentData().merge(var5);
         var3.setReturnValue(var4);
      }
   }

   @Inject(method = "isOp", at = @At("HEAD"), cancellable = true)
   private void saveAllMixin(GameProfile var1, CallbackInfoReturnable<Boolean> var2) {
      if (SrmManager.isSrmMode()) {
         var2.setReturnValue(false);
      }
   }
}
