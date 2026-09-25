package com.gtolib.mixin.adastra;

import earth.terrarium.adastra.common.entities.vehicles.Lander;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Lander.class)
public class LanderMixin {
   @Shadow(remap = false)
   private float speed;

   @Redirect(method = "causeFallDamage", at = @At(value = "INVOKE", target = "Learth/terrarium/adastra/common/entities/vehicles/Lander;onGround()Z"))
   private boolean onGroundRedirect(Lander var1) {
      return this.speed < -0.1 ? var1.onGround() : false;
   }
}
