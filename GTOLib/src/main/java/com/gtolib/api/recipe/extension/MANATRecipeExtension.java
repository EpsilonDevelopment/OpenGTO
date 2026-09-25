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
import com.gtolib.api.machine.mana.feature.IManaContainerMachine;
import com.gtolib.api.recipe.IdleReason;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;

public final class MANATRecipeExtension extends RecipeExtension<Long> {
   public static final MANATRecipeExtension INSTANCE = new MANATRecipeExtension();

   private MANATRecipeExtension() {
      super("manat", DataSyncCodec.LONG_CODEC, true);
   }

   @Override
   public boolean handleTick(@NotNull IRecipeHandlerHolder holder, @NotNull GTRecipe recipe, boolean simulate) {
      long mana = getMANAt(recipe);
      if (mana == 0L) {
         return true;
      }

      boolean result;
      if (holder instanceof IManaContainerMachine machine) {
         result = machine.useMana(mana, simulate);
      } else {
         result = false;
      }

      if (result) {
         return true;
      }

      if (mana > 0L) {
         holder.setIdleReason(IdleReason.NO_MANA::reason);
      } else {
         holder.setIdleReason(ActionResult.FAIL_INSUFFICIENT_OUT);
      }

      return false;
   }

   @Override
   public void extractInput(GTRecipeDefinition recipe, IntLongMap map) {
   }

   @Override
   public long getParallel(IRecipeHandlerHolder holder, RecipeHandlerUnit unit, GTRecipe recipe, long parallel) {
      long mana = getMANAt(recipe);
      if (mana == 0L) {
         return parallel;
      }

      if (holder instanceof IManaContainerMachine machine) {
         parallel = Math.min(parallel, machine.getManaContainer().getMaxIORate() / Math.abs(mana));
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
      long mana = getMANAt(recipe);
      if (mana != 0L) {
         setMANAt(recipe, mana * parallel);
      }
   }

   @Override
   public void addInfo(GTRecipeDefinition recipe, WidgetGroup group, int xOffset, MutableInt yOffset) {
      long mana = getMANAt(recipe);
      group.addWidget(
         new LabelWidget(
            3 - xOffset,
            yOffset.addAndGet(10),
            LocalizationUtils.format(mana > 0L ? "gtocore.machine.mana_input" : "gtocore.machine.mana_output", FormattingUtil.formatNumbers(Math.abs(mana)))
               + " /t"
         )
      );
      group.addWidget(
         new LabelWidget(
            3 - xOffset,
            yOffset.addAndGet(10),
            LocalizationUtils.format("gtocore.machine.mana_stored", FormattingUtil.formatNumbers(Math.abs(mana) * recipe.duration))
         )
      );
   }

   @Override
   public int getInfoHeight(GTRecipeDefinition recipe) {
      return 20;
   }

   public static long getMANAt(GTRecipeDefinition recipe) {
      return recipe.data.getLong(INSTANCE);
   }

   public static long getInputMANAt(GTRecipeDefinition recipe) {
      long mana = recipe.data.getLong(INSTANCE);
      return mana > 0L ? mana : 0L;
   }

   public static long getOutputMANAt(GTRecipeDefinition recipe) {
      long mana = recipe.data.getLong(INSTANCE);
      return mana < 0L ? -mana : 0L;
   }

   public static long getMANAt(GTRecipe recipe) {
      return recipe.data.getLong(INSTANCE);
   }

   public static long getInputMANAt(GTRecipe recipe) {
      long mana = recipe.data.getLong(INSTANCE);
      return mana > 0L ? mana : 0L;
   }

   public static long getOutputMANAt(GTRecipe recipe) {
      long mana = recipe.data.getLong(INSTANCE);
      return mana < 0L ? -mana : 0L;
   }

   public static void setMANAt(GTRecipe recipe, long mana) {
      recipe.data.put(INSTANCE, mana);
   }
}
