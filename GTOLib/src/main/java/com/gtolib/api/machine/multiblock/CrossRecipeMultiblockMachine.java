package com.gtolib.api.machine.multiblock;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.gtolib.api.machine.feature.multiblock.ICrossRecipeElectricMachine;
import com.gtolib.api.machine.trait.CrossRecipeTrait;
import com.gtolib.utils.MachineUtils;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import org.jetbrains.annotations.NotNull;

public class CrossRecipeMultiblockMachine extends ElectricMultiblockMachine implements ICrossRecipeElectricMachine {
   @SaveToDisk
   private final CrossRecipeTrait crossRecipeTrait;

   public static CrossRecipeMultiblockMachine createHatchParallel(MetaMachineBlockEntity holder) {
      return new CrossRecipeMultiblockMachine(holder, false, true, MachineUtils::getHatchParallel);
   }

   public static Function<MetaMachineBlockEntity, CrossRecipeMultiblockMachine> createParallel(
      boolean infinite, boolean isHatchParallel, ToLongFunction<CrossRecipeMultiblockMachine> parallel
   ) {
      return holder -> new CrossRecipeMultiblockMachine(holder, infinite, isHatchParallel, parallel);
   }

   protected CrossRecipeMultiblockMachine(
      MetaMachineBlockEntity holder, boolean infinite, boolean isHatchParallel, @NotNull ToLongFunction<CrossRecipeMultiblockMachine> parallel
   ) {
      super(holder);
      this.crossRecipeTrait = new CrossRecipeTrait(this, infinite, isHatchParallel, machine -> parallel.applyAsLong((CrossRecipeMultiblockMachine)machine));
   }

   @Override
   public CrossRecipeTrait getCrossRecipeTrait() {
      return this.crossRecipeTrait;
   }

   @Override
   public void attachConfigurators(@NotNull ConfiguratorPanel configuratorPanel) {
      super.attachConfigurators(configuratorPanel);
      this.crossRecipeTrait.attachConfigurators(configuratorPanel);
   }

   @Override
   public GTRecipe getRealRecipe(@NotNull RecipeHandlerUnit unit, @NotNull GTRecipe recipe) {
      return ICrossRecipeElectricMachine.super.getRealRecipe(unit, recipe);
   }
}
