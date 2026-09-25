package com.gtolib.api.machine.feature.multiblock;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

public interface IFluidRendererMachine {
   Set<BlockPos> getFluidBlockOffsets();

   @Nullable
   Fluid getCachedFluid();

   @Nullable
   static Fluid getFluid(GTRecipe recipe) {
      if (recipe != null) {
         for (Content<FluidIngredient> c : recipe.fluidInputs) {
            Fluid f = c.inner.getFluid();
            if (f != null) {
               return f;
            }
         }

         for (Content<FluidIngredient> c : recipe.fluidOutputs) {
            Fluid f = c.inner.getFluid();
            if (f != null) {
               return f;
            }
         }
      }

      return null;
   }
}
