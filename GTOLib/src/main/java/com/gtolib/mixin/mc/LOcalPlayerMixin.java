package com.gtolib.mixin.mc;

import com.gtolib.api.player.IEnhancedPlayer;
import com.gtolib.api.player.attribute.PlayerAttributes;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class LOcalPlayerMixin extends AbstractClientPlayer implements IEnhancedPlayer {
   public LOcalPlayerMixin(ClientLevel var1, GameProfile var2) {
      super(var1, var2);
   }

   @Inject(at = @At("RETURN"), method = "isUnderWater", cancellable = true)
   public void isUnderWater(CallbackInfoReturnable<Boolean> var1) {
      var1.setReturnValue((Boolean)var1.getReturnValue() || this.getPlayerData().isNoGravity());
   }

   @Redirect(
      method = "playSound",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/level/Level;playLocalSound(DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFZ)V"
      )
   )
   private void gtolib$redirectPlaySound(
      Level var1, double var2, double var4, double var6, SoundEvent var8, SoundSource var9, float var10, float var11, boolean var12
   ) {
      boolean var13 = var8 == SoundEvents.PLAYER_SWIM || var8 == SoundEvents.PLAYER_SPLASH || var8 == SoundEvents.PLAYER_SPLASH_HIGH_SPEED;
      if (!this.getPlayerData().isNoGravity() || !var13) {
         var1.playLocalSound(var2, var4, var6, var8, var9, var10, var11, var12);
      }
   }

   @WrapMethod(method = "aiStep")
   private void gtolib$wrapAiStep(Operation<Void> var1) {
      if (this.getPlayerData().getPlayerAttributes().getBooleanCurrent(PlayerAttributes.FREE_MOV_STATE) && this.getAbilities().flying) {
         this.noPhysics = true;
         var1.call();
      } else {
         var1.call();
      }
   }
}
