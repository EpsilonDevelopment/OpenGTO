package com.gtolib.mixin.mc;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.WaterFluid;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = WaterFluid.class, priority = 0)
public abstract class WaterFluidMixin extends FlowingFluid {
   @Override
   public boolean canConvertToSource(@NotNull FluidState var1, @NotNull Level var2, @NotNull BlockPos var3) {
      return false;
   }
}
