package com.gtolib.gtm;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.ItemIngredient;
import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.api.research.IResearchPointsOperation;
import com.gtocore.api.research.ResearchTag;
import com.gtolib.GTOCore;
import com.gtolib.forge.ForgeCommonEvent;
import java.security.ProtectionDomain;

public class RecipeLogicExt extends RecipeLogic {
   private int pgbkup = 0;
   private long gt = 0L;

   public RecipeLogicExt(IRecipeLogicMachine machine) {
      super(machine);
   }

   public static boolean isCallerTrusted() {
      return AutoBuildSetting.stackWalker.walk(frames -> frames.skip(2L).limit(1L).allMatch(f -> {
         ProtectionDomain pd = f.getDeclaringClass().getProtectionDomain();
         return pd == AutoBuildSetting.LIB || pd == AutoBuildSetting.GTO || pd == AutoBuildSetting.GTM;
      }));
   }

   @Override
   public final void setProgress(int p) {
      if (p - this.progress > 0 && !isCallerTrusted()) {
         this.resetRecipeLogic();
      } else {
         super.setProgress(p);
         this.pgbkup = this.progress ^ 16205;
      }
   }

   @Override
   public final void handleRecipeWorking() {
      try {
         if (ForgeCommonEvent.tickCount != this.gt) {
            if (this.lastRecipe != null) {
               this.duration = this.lastRecipe.duration;
            }

            if (this.gt != 0L && (this.pgbkup ^ 16205) < this.progress) {
               this.progress = 0;
            }

            super.handleRecipeWorking();
            this.pgbkup = this.progress ^ 16205;
            return;
         }
      } finally {
         this.gt = ForgeCommonEvent.tickCount;
      }
   }

   @Override
   public boolean onRecipeFinish() {
      if (this.lastRecipe != null && this.lastOriginRecipe != null && this.machine instanceof IMultiController controller) {
         this.addResearchData(controller, this.lastRecipe, this.lastOriginRecipe);
      }

      return super.onRecipeFinish();
   }

   public void addResearchData(IMultiController controller, GTRecipe recipe, GTRecipeDefinition definition) {
      if (controller != null && recipe != null && definition != null) {
         boolean hasCatalyst = false;

         for (Content<ItemIngredient> input : definition.itemInputs) {
            if (ChemicalHelper.getMaterialEntry(input.inner.getInnerItemStack().getItem()).tagPrefix() == GTOTagPrefix.CATALYST) {
               hasCatalyst = true;
               break;
            }
         }

         if (hasCatalyst) {
            double effMod = Math.max(1.0E-5, recipe.duration * recipe.eut / (definition.duration * definition.eut + 1.0E-5));
            IResearchPointsOperation.findHatchAndAddResearchData(controller, ResearchTag.CATALYSIS, definition.tier / effMod * recipe.parallels);
         }

         boolean isGenerator = recipe.eut < -4194304L && this.totalContinuousRunningTime > 300L * (GTOCore.difficulty + 1);
         if (isGenerator) {
            double p = Math.floor(Math.log(-recipe.eut) / Math.log(4.0)) - 10.0;
            IResearchPointsOperation.findHatchAndAddResearchData(controller, ResearchTag.ENERGY, p * p);
         }
      }
   }
}
