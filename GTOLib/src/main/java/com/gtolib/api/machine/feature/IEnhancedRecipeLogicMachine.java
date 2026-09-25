package com.gtolib.api.machine.feature;

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gtolib.GTOCore;
import com.gtolib.api.recipe.IdleReason;
import com.gtolib.api.recipe.RecipeBuilder;
import com.gtolib.gtm.RecipeLogicExt;
import com.gtolib.gtm.RecipeScript;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IEnhancedRecipeLogicMachine extends IRecipeLogicMachine {
   @Nullable
   default GTRecipe getModifyRecipe(@NotNull RecipeHandlerUnit unit, @NotNull GTRecipeDefinition definition) {
      if (!GTRecipeType.available(definition.recipeType, this.getAvailableRecipeTypes())) {
         return null;
      }

      GTRecipe recipe1;
      if (definition.registered) {
         recipe1 = RecipeScript.toRuntime(definition);
         if (recipe1 == null) {
            return null;
         }
      } else {
         recipe1 = new GTRecipe(
            definition,
            definition.itemInputs,
            definition.itemOutputs,
            definition.fluidInputs,
            definition.fluidOutputs,
            definition.data.clone(),
            definition.eut,
            definition.tier,
            definition.duration
         );
      }

      for (RecipeModifier mod : definition.recipeModifiers) {
         recipe1 = mod.applyModifier(this, unit, recipe1);
         if (recipe1 == null) {
            return null;
         }
      }

      GTRecipe recipe = recipe1;
      if (unit.color != -1) {
         recipe.outputColor = unit.color;
      }

      RecipeHelper.trimRecipeOutputs(recipe, this.getOutputLimits());
      return recipe;
   }

   @Override
   default RecipeLogic createRecipeLogic(Object... args) {
      return new RecipeLogicExt(this);
   }

   @Override
   default GTRecipe fullModifyRecipe(RecipeHandlerUnit unit, GTRecipeDefinition definition) {
      GTRecipe recipe = this.getModifyRecipe(unit, definition);
      return recipe == null ? null : this.doModifyRecipe(unit, recipe);
   }

   default RecipeBuilder getRecipeBuilder() {
      return RecipeBuilder.ofRaw();
   }

   @Override
   default void regressRecipe(RecipeLogic recipeLogic) {
      if (GTOCore.isExpert()) {
         this.setWorkingEnabled(false);
         recipeLogic.resetRecipeLogic();
      } else if (this.regressWhenWaiting() && recipeLogic.getProgress() > 1) {
         recipeLogic.setProgress(1);
      }
   }

   default void setIdleReason(IdleReason reason) {
      reason.setReason(this);
   }

   default void setIdleReason(IdleReason reason, Object... args) {
      reason.setReason(this, args);
   }
}
