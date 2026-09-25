package com.gtolib.api.machine.feature;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.IRecipeHandlerHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;

public interface IUpgradeMachine {
   default void gtolib$setSpeed(double speed) {
      throw new UnsupportedOperationException("Not implemented");
   }

   default void gtolib$setEnergy(double energy) {
      throw new UnsupportedOperationException("Not implemented");
   }

   default double gtolib$getSpeed() {
      throw new UnsupportedOperationException("Not implemented");
   }

   default double gtolib$getEnergy() {
      throw new UnsupportedOperationException("Not implemented");
   }

   default boolean gtolib$canUpgraded() {
      throw new UnsupportedOperationException("Not implemented");
   }

   static GTRecipe recipeModifier(IRecipeHandlerHolder holder, RecipeHandlerUnit unit, GTRecipe recipe) {
      if (holder instanceof IUpgradeMachine machine) {
         double energy = machine.gtolib$getEnergy();
         if (energy < 1.0 && recipe.eut > 0L) {
            recipe.eut = Math.max(1L, (long)(recipe.eut * energy));
         }

         double speed = machine.gtolib$getSpeed();
         if (speed < 1.0) {
            recipe.durationMultiplier(speed);
         }
      }

      return recipe;
   }
}
