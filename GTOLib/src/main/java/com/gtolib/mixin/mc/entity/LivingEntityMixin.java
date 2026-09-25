package com.gtolib.mixin.mc.entity;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = LivingEntity.class, priority = 0)
public class LivingEntityMixin {
   @Redirect(
      method = "decreaseAirSupply",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getRespiration(Lnet/minecraft/world/entity/LivingEntity;)I")
   )
   private int getRespiration(LivingEntity var1) {
      return 0;
   }
}
