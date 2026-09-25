package com.gtolib.api.recipe.extension;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.extension.RecipeExtension;
import com.gregtechceu.gtceu.api.recipe.handler.ActionResult;
import com.gregtechceu.gtceu.api.recipe.handler.IRecipeHandlerHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gto.datasynclib.DataSyncCodec;
import com.gto.recipesearch.IntLongMap;
import com.gtolib.api.capability.IManaContainer;
import com.gtolib.api.machine.mana.feature.IManaContainerMachine;
import com.gtolib.api.recipe.IdleReason;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;

public final class MANARecipeExtension extends RecipeExtension<Long> {
   public static final MANARecipeExtension INSTANCE = new MANARecipeExtension();

   private MANARecipeExtension() {
      super("mana", DataSyncCodec.LONG_CODEC, false);
   }

   @Override
   public boolean handleInput(@NotNull IRecipeHandlerHolder holder, @NotNull RecipeHandlerUnit unit, @NotNull GTRecipe recipe, boolean simulate) {
      long mana = getMANA(recipe);
      if (mana <= 0L) {
         return true;
      }

      boolean result;
      if (holder instanceof IManaContainerMachine machine) {
         result = machine.useManaUnrestricted(mana, simulate);
      } else {
         result = false;
      }

      if (result) {
         return true;
      }

      holder.setIdleReason(IdleReason.NO_MANA::reason);
      return false;
   }

   @Override
   public boolean handleOutput(@NotNull IRecipeHandlerHolder holder, @NotNull GTRecipe recipe, boolean simulate) {
      long mana = getMANA(recipe);
      if (mana >= 0L) {
         return true;
      }

      boolean result;
      if (holder instanceof IManaContainerMachine machine) {
         result = machine.useManaUnrestricted(mana, simulate);
      } else {
         result = false;
      }

      if (result) {
         return true;
      }

      holder.setIdleReason(ActionResult.FAIL_INSUFFICIENT_OUT);
      return false;
   }

   @Override
   public void extractInput(GTRecipeDefinition recipe, IntLongMap map) {
   }

   @Override
   public long getParallel(IRecipeHandlerHolder holder, RecipeHandlerUnit unit, GTRecipe recipe, long parallel) {
      long mana = getMANA(recipe);
      if (mana == 0L) {
         return parallel;
      }

      if (holder instanceof IManaContainerMachine machine) {
         IManaContainer container = machine.getManaContainer();
         if (mana > 0L) {
            parallel = Math.min(parallel, container.getCurrentMana() / mana);
         } else {
            mana = -mana;
            parallel = Math.min(parallel, (container.getMaxMana() - container.getCurrentMana()) / mana);
         }
      } else {
         parallel = 0L;
      }

      if (parallel > 0L) {
         return parallel;
      }

      if (mana > 0L) {
         holder.setIdleReason(IdleReason.NO_MANA::reason);
      } else {
         holder.setIdleReason(ActionResult.FAIL_INSUFFICIENT_OUT);
      }

      return 0L;
   }

   @Override
   public void setParallel(GTRecipe recipe, long parallel) {
      long mana = getMANA(recipe);
      if (mana != 0L) {
         setMANA(recipe, mana * parallel);
      }
   }

   @Override
   public void addInfo(GTRecipeDefinition recipe, WidgetGroup group, int xOffset, MutableInt yOffset) {
      long mana = getMANA(recipe);
      group.addWidget(
         new LabelWidget(
            3 - xOffset,
            yOffset.addAndGet(10),
            LocalizationUtils.format(mana > 0L ? "gtocore.machine.mana_input" : "gtocore.machine.mana_output", FormattingUtil.formatNumbers(Math.abs(mana)))
         )
      );
   }

   @Override
   public int getInfoHeight(GTRecipeDefinition recipe) {
      return 10;
   }

   public static long getMANA(GTRecipeDefinition recipe) {
      return recipe.data.getLong(INSTANCE);
   }

   public static long getInputMANA(GTRecipeDefinition recipe) {
      long mana = recipe.data.getLong(INSTANCE);
      return mana > 0L ? mana : 0L;
   }

   public static long getOutputMANA(GTRecipeDefinition recipe) {
      long mana = recipe.data.getLong(INSTANCE);
      return mana < 0L ? -mana : 0L;
   }

   public static long getMANA(GTRecipe recipe) {
      return recipe.data.getLong(INSTANCE);
   }

   public static long getInputMANA(GTRecipe recipe) {
      long mana = recipe.data.getLong(INSTANCE);
      return mana > 0L ? mana : 0L;
   }

   public static long getOutputMANA(GTRecipe recipe) {
      long mana = recipe.data.getLong(INSTANCE);
      return mana < 0L ? -mana : 0L;
   }

   public static void setMANA(GTRecipe recipe, long mana) {
      recipe.data.put(INSTANCE, mana);
   }
}
