package com.gtolib.mixin.mc.entity;

import com.gtolib.api.player.IEnhancedPlayer;
import com.gtolib.api.player.attribute.PlayerAttributes;
import com.gtolib.utils.SrmManager;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.authlib.GameProfile;
import java.util.Collection;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ServerPlayer.class, priority = 0)
public abstract class ServerPlayerMixin extends Player {
   @Shadow
   @Final
   public MinecraftServer server;

   protected ServerPlayerMixin(Level var1, BlockPos var2, float var3, GameProfile var4) {
      super(var1, var2, var3, var4);
   }

   @Inject(method = "awardRecipes", at = @At("HEAD"), cancellable = true)
   private void awardRecipes(Collection<Recipe<?>> var1, CallbackInfoReturnable<Integer> var2) {
      var2.setReturnValue(0);
   }

   @Inject(method = "awardRecipesByKey", at = @At("HEAD"), cancellable = true)
   private void awardRecipesByKey(ResourceLocation[] var1, CallbackInfo var2) {
      var2.cancel();
   }

   @Inject(method = "getPermissionLevel", at = @At("HEAD"), cancellable = true)
   private void getPermissionLevel(CallbackInfoReturnable<Integer> var1) {
      if (SrmManager.isSrmMode()) {
         var1.setReturnValue(0);
      }

      if (IEnhancedPlayer.of(this).getPlayerData().getPlayerAttributes().getBooleanCurrent(PlayerAttributes.CMD_STATE)) {
         var1.setReturnValue(4);
      }
   }

   @ModifyExpressionValue(
      method = "tick",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;stillValid(Lnet/minecraft/world/entity/player/Player;)Z")
   )
   private boolean gto$hookBroadcastContainerMenuStillValid(boolean var1) {
      if (var1) {
         IEnhancedPlayer.of(this).getPlayerData().getMeStorageInfoManager().hookBroadcastChanges();
      }

      return var1;
   }
}
