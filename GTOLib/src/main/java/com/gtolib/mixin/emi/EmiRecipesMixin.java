package com.gtolib.mixin.emi;

import com.gtolib.emi.EMIManager;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.registry.EmiRecipes;
import java.util.List;
import java.util.Map;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EmiRecipes.class)
public final class EmiRecipesMixin {
   @Shadow(remap = false)
   private static Map<EmiRecipeCategory, List<EmiIngredient>> workstations;
   @Shadow(remap = false)
   private static List<EmiRecipe> recipes;

   @Overwrite(remap = false)
   public static void bake() {
      EmiRecipes.manager = new EMIManager(EmiRecipes.categories, workstations, recipes);
   }
}
