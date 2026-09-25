package com.gtolib.api.ae2.pattern;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.KeyCounter;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;

public interface IDetails extends IPatternDetails {
   default KeyCounter[] gtolib$getInputHolder() {
      throw new UnsatisfiedLinkError("Not Impl");
   }

   default GTRecipeDefinition getRecipe() {
      throw new UnsatisfiedLinkError("Not Impl");
   }
}
