package com.gtolib.api.machine.trait;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gtolib.api.machine.feature.multiblock.IMultiblockTraitHolder;
import java.util.List;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class MultiblockTrait extends MachineTrait {
   protected MultiblockTrait(IMultiblockTraitHolder machine) {
      super(machine.self());
      machine.getMultiblockTraits().add(this);
   }

   @Nullable
   public GTRecipe modifyRecipe(@NotNull RecipeHandlerUnit unit, @NotNull GTRecipe recipe) {
      return recipe;
   }

   public void afterWorking() {
   }

   public void customText(@NotNull List<Component> textList) {
   }

   public void onPartScan(IMultiPart part) {
   }

   public void onStructureFormed() {
   }

   public void onStructureInvalid() {
   }

   public MultiblockControllerMachine getMachine() {
      return (MultiblockControllerMachine)this.machine;
   }
}
