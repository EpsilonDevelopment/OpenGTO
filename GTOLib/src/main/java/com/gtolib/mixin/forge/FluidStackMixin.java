package com.gtolib.mixin.forge;

import com.gtolib.utils.RLUtils;
import net.minecraft.core.Holder.Reference;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.IForgeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FluidStack.class)
public class FluidStackMixin {
   @Shadow(remap = false)
   private boolean isEmpty;
   @Shadow(remap = false)
   private int amount;
   @Unique
   private Fluid gtolib$fluid;

   @Redirect(
      method = "<init>(Lnet/minecraft/world/level/material/Fluid;I)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraftforge/registries/IForgeRegistry;getKey(Ljava/lang/Object;)Lnet/minecraft/resources/ResourceLocation;",
         ordinal = 0
      ),
      remap = false
   )
   private <V> ResourceLocation getKey(IForgeRegistry var1, V var2) {
      return RLUtils.EMPTY;
   }

   @Redirect(
      method = "<init>(Lnet/minecraft/world/level/material/Fluid;I)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraftforge/registries/IForgeRegistry;getDelegateOrThrow(Ljava/lang/Object;)Lnet/minecraft/core/Holder$Reference;"
      ),
      remap = false
   )
   private <V> Reference<V> getDelegateOrThrow(IForgeRegistry var1, V var2) {
      this.gtolib$fluid = (Fluid)var2;
      return null;
   }

   @Overwrite(remap = false)
   public final Fluid getFluid() {
      return this.isEmpty ? Fluids.EMPTY : this.gtolib$fluid;
   }

   @Overwrite(remap = false)
   public final Fluid getRawFluid() {
      return this.gtolib$fluid;
   }

   @Overwrite(remap = false)
   public void setAmount(int var1) {
      if (this.gtolib$fluid == Fluids.EMPTY) {
         throw new IllegalStateException("Can't modify the empty stack.");
      }

      this.amount = var1;
      this.isEmpty = var1 <= 0;
   }
}
