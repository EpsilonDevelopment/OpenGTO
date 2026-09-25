package com.gtolib.api.recipe.extension;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.extension.RecipeExtension;
import com.gregtechceu.gtceu.api.recipe.handler.IRecipeHandlerHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gto.datasynclib.DataSyncCodec;
import com.gto.recipesearch.IntLongMap;
import com.gtolib.api.capability.IHeatContainer;
import com.gtolib.api.recipe.IdleReason;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;

public class HURecipeExtension extends RecipeExtension<Long> {
   public static final HURecipeExtension INSTANCE = new HURecipeExtension();

   private HURecipeExtension() {
      super("hu", DataSyncCodec.LONG_CODEC, false);
   }

   public static boolean useHeatUnrestricted(IHeatContainer container, long hu, boolean simulate) {
      if (hu < 0L) {
         if (!simulate) {
            container.addHeatUnrestricted(-hu, false);
         }

         return true;
      } else {
         return container.removeHeatUnrestricted(hu, simulate) == hu;
      }
   }

   @Override
   public boolean handleInput(@NotNull IRecipeHandlerHolder holder, @NotNull RecipeHandlerUnit unit, @NotNull GTRecipe recipe, boolean simulate) {
      long hu = getHU(recipe);
      if (hu <= 0L) {
         return true;
      }

      IHeatContainer container = IHeatContainer.getCapability(holder.self().holder);
      boolean result;
      if (container != null) {
         result = useHeatUnrestricted(container, hu, simulate);
      } else {
         result = false;
      }

      if (result) {
         return true;
      }

      holder.setIdleReason(IdleReason.INSUFFICIENT_TEMPERATURE::reason);
      return false;
   }

   @Override
   public boolean handleOutput(@NotNull IRecipeHandlerHolder holder, @NotNull GTRecipe recipe, boolean simulate) {
      long hu = getHU(recipe);
      if (hu >= 0L) {
         return true;
      }

      IHeatContainer container = IHeatContainer.getCapability(holder.self().holder);
      boolean result;
      if (container != null) {
         result = useHeatUnrestricted(container, hu, simulate);
      } else {
         result = !simulate;
      }

      if (result) {
         return true;
      }

      holder.setIdleReason(IdleReason.HEAT_ACCUMULATION::reason);
      return false;
   }

   @Override
   public void extractInput(GTRecipeDefinition recipe, IntLongMap map) {
   }

   @Override
   public long getParallel(IRecipeHandlerHolder holder, RecipeHandlerUnit unit, GTRecipe recipe, long parallel) {
      long hu = getHU(recipe);
      if (hu <= 0L) {
         return parallel;
      }

      IHeatContainer container = IHeatContainer.getCapability(holder.self().holder);
      if (container != null) {
         parallel = Math.min(parallel, container.getCurrentHeat() / hu);
      } else {
         parallel = 0L;
      }

      if (parallel > 0L) {
         return parallel;
      }

      holder.setIdleReason(IdleReason.INSUFFICIENT_TEMPERATURE::reason);
      return 0L;
   }

   @Override
   public void setParallel(GTRecipe recipe, long parallel) {
      long hu = getHU(recipe);
      if (hu != 0L) {
         setHU(recipe, hu * parallel);
      }
   }

   @Override
   public void addInfo(GTRecipeDefinition recipe, WidgetGroup group, int xOffset, MutableInt yOffset) {
      long hu = getHU(recipe);
      group.addWidget(
         new LabelWidget(
            3 - xOffset,
            yOffset.addAndGet(10),
            LocalizationUtils.format(hu > 0L ? "gtocore.machine.hu_input" : "gtocore.machine.hu_output", FormattingUtil.formatNumbers(Math.abs(hu)))
         )
      );
   }

   @Override
   public int getInfoHeight(GTRecipeDefinition recipe) {
      return 10;
   }

   public static long getHU(GTRecipeDefinition recipe) {
      return recipe.data.getLong(INSTANCE);
   }

   public static long getInputHU(GTRecipeDefinition recipe) {
      long mana = recipe.data.getLong(INSTANCE);
      return mana > 0L ? mana : 0L;
   }

   public static long getOutputHU(GTRecipeDefinition recipe) {
      long mana = recipe.data.getLong(INSTANCE);
      return mana < 0L ? -mana : 0L;
   }

   public static long getHU(GTRecipe recipe) {
      return recipe.data.getLong(INSTANCE);
   }

   public static long getInputHU(GTRecipe recipe) {
      long mana = recipe.data.getLong(INSTANCE);
      return mana > 0L ? mana : 0L;
   }

   public static long getOutputHU(GTRecipe recipe) {
      long mana = recipe.data.getLong(INSTANCE);
      return mana < 0L ? -mana : 0L;
   }

   public static void setHU(GTRecipe recipe, long mana) {
      recipe.data.put(INSTANCE, mana);
   }
}
