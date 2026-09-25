package com.gtolib.api.machine.part;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IWorkableMultiPart;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import lombok.Generated;
import org.jetbrains.annotations.Nullable;

public abstract class WorkableAmountConfigurationPartMachine extends AmountConfigurationPartMachine implements IWorkableMultiPart {
   @Nullable
   protected RecipeHandlerUnit recipeHandlerUnit;

   protected WorkableAmountConfigurationPartMachine(MetaMachineBlockEntity holder, int tier, long min, long max) {
      super(holder, tier, min, max);
   }

   @Nullable
   @Generated
   @Override
   public RecipeHandlerUnit getRecipeHandlerUnit() {
      return this.recipeHandlerUnit;
   }

   @Generated
   @Override
   public void setRecipeHandlerUnit(@Nullable RecipeHandlerUnit recipeHandlerUnit) {
      this.recipeHandlerUnit = recipeHandlerUnit;
   }
}
