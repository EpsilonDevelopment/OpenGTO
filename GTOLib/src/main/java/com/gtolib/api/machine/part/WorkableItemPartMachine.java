package com.gtolib.api.machine.part;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IWorkableMultiPart;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import java.util.function.Predicate;
import lombok.Generated;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public abstract class WorkableItemPartMachine extends ItemPartMachine implements IWorkableMultiPart {
   protected RecipeHandlerUnit recipeHandlerUnit;

   public WorkableItemPartMachine(MetaMachineBlockEntity holder, int limit, @Nullable Predicate<ItemStack> filter) {
      super(holder, limit, filter);
   }

   @Generated
   @Override
   public RecipeHandlerUnit getRecipeHandlerUnit() {
      return this.recipeHandlerUnit;
   }

   @Generated
   @Override
   public void setRecipeHandlerUnit(RecipeHandlerUnit recipeHandlerUnit) {
      this.recipeHandlerUnit = recipeHandlerUnit;
   }
}
