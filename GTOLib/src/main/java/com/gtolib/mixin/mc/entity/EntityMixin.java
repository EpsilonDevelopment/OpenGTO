package com.gtolib.mixin.mc.entity;

import java.util.Collections;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Entity.class, priority = 0)
public abstract class EntityMixin {
   @Shadow
   public int tickCount;

   @Inject(method = "push(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
   private void onPush(@NotNull Entity var1, CallbackInfo var2) {
      if (!(((Object)this) instanceof Player)) {
         var2.cancel();
      }
   }

   @Redirect(
      method = "collide",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/level/Level;getEntityCollisions(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"
      )
   )
   private List getEntityCollisions(Level var1, Entity var2, AABB var3) {
      return var2 instanceof Player ? var1.getEntityCollisions(var2, var3) : Collections.emptyList();
   }

   @Inject(method = "updateInWaterStateAndDoFluidPushing", at = @At("HEAD"), cancellable = true)
   private void updateInWaterStateAndDoFluid(CallbackInfoReturnable<Boolean> var1) {
      if (this.tickCount % 5 != 0) {
         var1.setReturnValue(false);
      }
   }
}
