package com.gtolib.mixin.mc.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Bat.class, priority = 0)
public abstract class BatMixin extends AmbientCreature {
   protected BatMixin(EntityType<? extends AmbientCreature> var1, Level var2) {
      super(var1, var2);
   }

   @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
   private void tick(CallbackInfo var1) {
      this.discard();
      var1.cancel();
   }
}
