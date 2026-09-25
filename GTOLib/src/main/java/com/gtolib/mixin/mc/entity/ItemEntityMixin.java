package com.gtolib.mixin.mc.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ItemEntity.class, priority = 0)
public abstract class ItemEntityMixin extends Entity {
   protected ItemEntityMixin(EntityType<?> var1, Level var2) {
      super(var1, var2);
   }

   @Redirect(
      method = "tick",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;noCollision(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Z")
   )
   private boolean noCollision(Level var1, Entity var2, AABB var3) {
      return var2.tickCount % 5 == 3 ? var1.noCollision(var2, var3) : true;
   }

   @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;tick()V"))
   private void tick(Entity var1) {
      if (var1.tickCount % 20 == 9) {
         this.baseTick();
      }
   }

   @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;updateInWaterStateAndDoFluidPushing()Z"))
   private boolean updateInWaterStateAndDoFluid(ItemEntity var1) {
      return var1.tickCount % 20 == 0 ? this.updateInWaterStateAndDoFluidPushing() : false;
   }

   @Redirect(
      method = "tick",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/entity/item/ItemEntity;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V"
      )
   )
   private void move(ItemEntity var1, MoverType var2, Vec3 var3) {
      if (var3.x != 0.0 || var3.z != 0.0 || var1.tickCount % 5 == 3) {
         this.move(var2, var3);
      }
   }
}
