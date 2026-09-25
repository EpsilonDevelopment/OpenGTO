package com.gtolib.api.machine.feature.multiblock;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IWorkableMultiController;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gtolib.api.machine.feature.IEnhancedRecipeLogicMachine;

public interface IEnhancedMultiblockMachine extends IEnhancedRecipeLogicMachine, IWorkableMultiController, IMultiblockTraitHolder {
   default void onContentChanges(RecipeHandlerUnit var1) {
      throw new UnsatisfiedLinkError("Not Impl");
   }

   default void onPartScan(IMultiPart var1) {
      throw new UnsatisfiedLinkError("Not Impl");
   }
}
