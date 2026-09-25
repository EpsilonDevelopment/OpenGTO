package com.gtolib.api.recipe.extension;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.extension.RecipeExtension;
import com.gregtechceu.gtceu.api.recipe.handler.IRecipeHandlerHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gto.datasynclib.DataSyncCodec;
import com.gto.recipesearch.IntLongMap;
import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.capability.IHeatContainer;
import com.gtolib.api.recipe.IdleReason;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;

@DataGeneratorScanned
public class HUTRecipeExtension extends RecipeExtension<Long> {
   @RegisterLanguage(cn = "热量输入：%s HU", en = "Heat Input: %s HU")
   public static final String HU_INPUT = "gtocore.machine.hu_input";
   @RegisterLanguage(cn = "热量输出：%s HU", en = "Heat Output: %s HU")
   public static final String HU_OUTPUT = "gtocore.machine.hu_output";
   @RegisterLanguage(cn = "总热量：%s HU", en = "Total Heat: %s HU")
   public static final String HU_STORED = "gtocore.machine.hu_stored";
   public static final HUTRecipeExtension INSTANCE = new HUTRecipeExtension();

   private HUTRecipeExtension() {
      super("hut", DataSyncCodec.LONG_CODEC, false);
   }

   public static boolean useHeat(IHeatContainer container, long hu, boolean simulate) {
      if (hu < 0L) {
         if (!simulate) {
            container.addHeat(-hu, 1, false);
         }

         return true;
      } else {
         return container.removeHeat(hu, 1, simulate) == hu;
      }
   }

   @Override
   public boolean handleTick(@NotNull IRecipeHandlerHolder holder, @NotNull GTRecipe recipe, boolean simulate) {
      long hu = getHUT(recipe);
      if (hu == 0L) {
         return true;
      }

      IHeatContainer container = IHeatContainer.getCapability(holder.self().holder);
      boolean result;
      if (container != null) {
         result = useHeat(container, hu, simulate);
      } else {
         result = !simulate && hu < 0L;
      }

      if (result) {
         return true;
      }

      if (hu > 0L) {
         holder.setIdleReason(IdleReason.INSUFFICIENT_TEMPERATURE::reason);
      } else {
         holder.setIdleReason(IdleReason.HEAT_ACCUMULATION::reason);
      }

      return false;
   }

   @Override
   public void extractInput(GTRecipeDefinition recipe, IntLongMap map) {
   }

   @Override
   public long getParallel(IRecipeHandlerHolder holder, RecipeHandlerUnit unit, GTRecipe recipe, long parallel) {
      long hu = getHUT(recipe);
      if (hu <= 0L) {
         return parallel;
      }

      IHeatContainer container = IHeatContainer.getCapability(holder.self().holder);
      if (container != null) {
         parallel = Math.min(parallel, (long)container.getBaseTransferRate() / Math.abs(hu));
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
      long hu = getHUT(recipe);
      if (hu != 0L) {
         setHUT(recipe, hu * parallel);
      }
   }

   @Override
   public void addInfo(GTRecipeDefinition recipe, WidgetGroup group, int xOffset, MutableInt yOffset) {
      long hu = getHUT(recipe);
      group.addWidget(
         new LabelWidget(
            3 - xOffset,
            yOffset.addAndGet(10),
            LocalizationUtils.format(hu > 0L ? "gtocore.machine.hu_input" : "gtocore.machine.hu_output", FormattingUtil.formatNumbers(Math.abs(hu))) + " /t"
         )
      );
      group.addWidget(
         new LabelWidget(
            3 - xOffset,
            yOffset.addAndGet(10),
            LocalizationUtils.format("gtocore.machine.hu_stored", FormattingUtil.formatNumbers(Math.abs(hu) * recipe.duration))
         )
      );
   }

   @Override
   public int getInfoHeight(GTRecipeDefinition recipe) {
      return 20;
   }

   public static long getHUT(GTRecipeDefinition recipe) {
      return recipe.data.getLong(INSTANCE);
   }

   public static long getInputHUT(GTRecipeDefinition recipe) {
      long mana = recipe.data.getLong(INSTANCE);
      return mana > 0L ? mana : 0L;
   }

   public static long getOutputHUT(GTRecipeDefinition recipe) {
      long mana = recipe.data.getLong(INSTANCE);
      return mana < 0L ? -mana : 0L;
   }

   public static long getHUT(GTRecipe recipe) {
      return recipe.data.getLong(INSTANCE);
   }

   public static long getInputHUT(GTRecipe recipe) {
      long mana = recipe.data.getLong(INSTANCE);
      return mana > 0L ? mana : 0L;
   }

   public static long getOutputHUT(GTRecipe recipe) {
      long mana = recipe.data.getLong(INSTANCE);
      return mana < 0L ? -mana : 0L;
   }

   public static void setHUT(GTRecipe recipe, long mana) {
      recipe.data.put(INSTANCE, mana);
   }
}
