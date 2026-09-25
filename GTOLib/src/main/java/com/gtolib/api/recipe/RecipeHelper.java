package com.gtolib.api.recipe;

import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.ItemIngredient;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public final class RecipeHelper extends com.gregtechceu.gtceu.api.recipe.RecipeHelper {
   public static List<ItemStack> getConsumeInputItems(GTRecipeDefinition recipe) {
      return recipe.itemInputs
         .stream()
         .filter(content -> content.chance > 0)
         .map(i -> i.inner)
         .map(ItemIngredient::getInnerItemStack)
         .collect(Collectors.toList());
   }

   public static List<FluidStack> getConsumeInputFluids(GTRecipeDefinition recipe) {
      return recipe.fluidInputs
         .stream()
         .filter(content -> content.chance > 0)
         .map(i -> i.inner)
         .map(FluidIngredient::getFluidStack)
         .collect(Collectors.toList());
   }
}
