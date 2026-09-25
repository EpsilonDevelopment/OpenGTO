package com.gtolib.mixin.mc.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EnderMan.class, priority = 0)
public class EnderManMixin extends Monster {
   protected EnderManMixin(EntityType<? extends Monster> var1, Level var2) {
      super(var1, var2);
   }

   @Inject(method = "teleport()Z", at = @At("HEAD"), cancellable = true)
   private void teleport(CallbackInfoReturnable<Boolean> var1) {
      if (this.isNoAi()) {
         var1.setReturnValue(false);
      }
   }
}
