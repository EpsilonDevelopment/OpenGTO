package com.gtolib.api.fluid;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public interface IFluid {
   @NotNull
   ResourceLocation gtolib$getIdLocation();

   @NotNull
   default String gtolib$getIdString() {
      return this.gtolib$getIdLocation().toString();
   }

   FluidStack gtolib$getReadOnlyStack();

   int gtolib$getOrCreateMapFluid();

   int gtolib$getMapFluid();
}
