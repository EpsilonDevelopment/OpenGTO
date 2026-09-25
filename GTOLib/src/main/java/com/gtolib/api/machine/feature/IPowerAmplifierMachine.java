package com.gtolib.api.machine.feature;

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.IRecipeHandlerHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;

public interface IPowerAmplifierMachine extends IRecipeLogicMachine {
   default double gtolib$getPowerAmplifier() {
      throw new UnsupportedOperationException("Not implemented");
   }

   default void gtolib$setPowerAmplifier(double powerAmplifier) {
      throw new UnsupportedOperationException("Not implemented");
   }

   default double gtolib$getPowerAmplifierDurationMultiplier() {
      return 1.0 / this.gtolib$getPowerAmplifier();
   }

   default double gtolib$getPowerAmplifierEnergyMultiplier() {
      return this.gtolib$getPowerAmplifier();
   }

   default boolean gtolib$noPowerAmplifier() {
      throw new UnsupportedOperationException("Not implemented");
   }

   default void gtolib$setHasPowerAmplifier(boolean hasPowerAmplifier) {
      throw new UnsupportedOperationException("Not implemented");
   }

   static GTRecipe recipeModifier(IRecipeHandlerHolder holder, RecipeHandlerUnit unit, GTRecipe recipe) {
      if (holder instanceof IPowerAmplifierMachine machine) {
         if (machine.gtolib$noPowerAmplifier()) {
            return recipe;
         }

         recipe.euMultiplier(machine.gtolib$getPowerAmplifierEnergyMultiplier());
         recipe.durationMultiplier(machine.gtolib$getPowerAmplifierDurationMultiplier());
      }

      return recipe;
   }
}
