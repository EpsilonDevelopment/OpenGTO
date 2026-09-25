package com.gtolib.api.recipe;

import com.gregtechceu.gtceu.api.capability.IParallelHatch;
import com.gregtechceu.gtceu.api.machine.SimpleGeneratorMachine;
import com.gregtechceu.gtceu.api.machine.feature.IOverclockMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.ICoilMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IWorkableMultiController;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.handler.IRecipeHandlerHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.ingredient.ItemIngredient;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifierList;
import com.gtocore.api.research.techtree.TechTreeSavedData;
import com.gtocore.data.techtree.MachinesNode;
import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.feature.IPowerAmplifierMachine;
import com.gtolib.api.machine.feature.IUpgradeMachine;
import com.gtolib.api.machine.feature.multiblock.IParallelMachine;
import com.gtolib.api.machine.mana.feature.IManaContainerMachine;
import com.gtolib.api.recipe.extension.MANATRecipeExtension;
import com.gtolib.utils.GTOUtils;
import java.util.Collections;
import java.util.UUID;
import lombok.Generated;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@DataGeneratorScanned
public final class GTORecipeModifiers {
   public static RecipeModifier UPGRADE = IUpgradeMachine::recipeModifier;
   public static RecipeModifier POWER_AMPLIFIER = IPowerAmplifierMachine::recipeModifier;
   public static RecipeModifier PARALLEL = GTORecipeModifiers::parallel;
   public static RecipeModifier MANA_OVERCLOCKING = GTORecipeModifiers::manaOverclocking;
   public static RecipeModifier PARALLELIZABLE_MANA_OVERCLOCK = new RecipeModifierList(PARALLEL, MANA_OVERCLOCKING);
   public static RecipeModifier PARALLELIZABLE_OVERCLOCK = new RecipeModifierList(PARALLEL, RecipeModifier.OVERCLOCKING);
   public static RecipeModifier PARALLELIZABLE_PERFECT_OVERCLOCK = new RecipeModifierList(PARALLEL, RecipeModifier.PERFECT_OVERCLOCKING);
   public static RecipeModifier UPGRADE_OVERCLOCK = new RecipeModifierList(UPGRADE, POWER_AMPLIFIER, RecipeModifier.OVERCLOCKING);
   public static RecipeModifier UPGRADE_PERFECT_OVERCLOCK = new RecipeModifierList(UPGRADE, POWER_AMPLIFIER, RecipeModifier.PERFECT_OVERCLOCKING);
   public static RecipeModifier UPGRADE_CRACKER_OVERCLOCK = new RecipeModifierList(UPGRADE, POWER_AMPLIFIER, RecipeModifier.CRACKER_OVERCLOCK);
   public static RecipeModifier UPGRADE_PYROLYSE_OVEN_OVERCLOCK = new RecipeModifierList(UPGRADE, POWER_AMPLIFIER, RecipeModifier.PYROLYSE_OVEN_OVERCLOCK);
   public static RecipeModifier UPGRADE_EBF_OVERCLOCK = new RecipeModifierList(UPGRADE, POWER_AMPLIFIER, RecipeModifier.EBF_OVERCLOCK);
   public static RecipeModifier UPGRADE_MULTI_SMELTER_OVERCLOCK = new RecipeModifierList(UPGRADE, POWER_AMPLIFIER, RecipeModifier.MULTI_SMELTER_OVERCLOCK);
   public static RecipeModifier UPGRADE_PARALLELIZABLE_OVERCLOCK = new RecipeModifierList(UPGRADE, POWER_AMPLIFIER, PARALLEL, RecipeModifier.OVERCLOCKING);
   public static RecipeModifier UPGRADE_PARALLELIZABLE_PERFECT_OVERCLOCK = new RecipeModifierList(
      UPGRADE, POWER_AMPLIFIER, PARALLEL, RecipeModifier.PERFECT_OVERCLOCKING
   );
   public static RecipeModifier UPGRADE_GCYM_OVERCLOCKING = new RecipeModifierList(
      UPGRADE, POWER_AMPLIFIER, PARALLEL, RecipeModifier.overclocking(0.5, 0.8, 0.6)
   );
   public static RecipeModifier SIMPLE_GENERATOR_MACHINEMODIFIER = (machine, unit, recipe) -> {
      if (machine instanceof SimpleGeneratorMachine generator) {
         long EUt = recipe.getOutputEUt();
         if (EUt > 0L) {
            GTRecipe var6 = ParallelLogic.accurateParallel(machine, unit, recipe, generator.getOverclockVoltage() / EUt);
            if (var6 == null) {
               return null;
            }

            var6.duration = var6.duration * GTOUtils.getGeneratorEfficiency(var6.definition.recipeType, generator.getTier()) / 100;
            return var6.duration < 1 ? null : var6;
         } else {
            return recipe;
         }
      } else {
         return null;
      }
   };
   @RegisterLanguage(
      key = "gtocore.recipe.modifier.magneto_resonance_boost",
      cn = "可通过磁共振电路升级提升产量",
      en = "Can be boosted by Magneto Resonance Circuit Upgrade"
   )
   public static final RecipeModifier MAGNETO_RESONANCE_BOOST_MODIFIER = new RecipeModifier() {
      @Override
      public GTRecipe applyModifier(IRecipeHandlerHolder machine, @NotNull RecipeHandlerUnit unit, @NotNull GTRecipe recipe) {
         UUID machineOwner = machine.self().getOwnerUUID();
         if (TechTreeSavedData.isUnlocked(machineOwner, MachinesNode.MagnetoResonaticCircuitUpgrade)) {
            Content<ItemIngredient> prod = recipe.itemOutputs.getFirst().copy();
            prod.amount++;
            recipe.itemOutputs = Collections.singletonList(prod);
         }

         return recipe;
      }

      @Override
      public Component getTooltips() {
         return Component.translatable("gtocore.recipe.modifier.magneto_resonance_boost");
      }
   };

   public static RecipeModifier coilReductionOverclock(double durationFactor) {
      return (holder, unit, recipe) -> {
         if (holder instanceof ICoilMachine coilMachine) {
            GTRecipe r = parallel(holder, unit, recipe);
            return r == null
               ? null
               : RecipeModifier.overclocking(
                  holder, unit, r, false, 1.0 - coilMachine.getCoilTier() * 0.05, 1.0 - coilMachine.getCoilTier() * 0.05, durationFactor
               );
         } else {
            return null;
         }
      };
   }

   @Nullable
   public static GTRecipe parallel(IRecipeHandlerHolder holder, RecipeHandlerUnit unit, GTRecipe recipe) {
      long parallel;
      if (holder instanceof IParallelMachine parallelMachine) {
         parallel = parallelMachine.getParallel();
      } else if (holder instanceof IWorkableMultiController controller) {
         IParallelHatch parallelHatch = controller.getParallelHatch();
         parallel = parallelHatch == null ? 1L : parallelHatch.getCurrentParallel();
      } else {
         parallel = 1L;
      }

      return ParallelLogic.accurateParallel(holder, unit, recipe, parallel);
   }

   @Nullable
   public static GTRecipe externalEnergyOverclocking(
      IRecipeHandlerHolder machine,
      RecipeHandlerUnit unit,
      GTRecipe recipe,
      long recipeVoltage,
      long maxVoltage,
      boolean perfect,
      double reductionEUt,
      double reductionDuration
   ) {
      return RecipeModifier.overclocking(
         machine,
         unit,
         recipe,
         machine instanceof IWorkableMultiController controller && controller.isBatchEnabled(),
         machine instanceof IOverclockMachine overclockMachine ? overclockMachine.getOverclockLimit() : 5,
         recipeVoltage,
         (long)(maxVoltage * reductionEUt),
         false,
         reductionDuration,
         perfect ? 0.25 : 0.5
      );
   }

   @Nullable
   public static GTRecipe manaOverclocking(IRecipeHandlerHolder machine, RecipeHandlerUnit unit, @Nullable GTRecipe recipe) {
      return machine instanceof IManaContainerMachine containerMachine
         ? manaOverclocking(machine, unit, recipe, containerMachine.getManaContainer().getMaxIORate())
         : recipe;
   }

   @Nullable
   public static GTRecipe manaOverclocking(IRecipeHandlerHolder machine, RecipeHandlerUnit unit, @Nullable GTRecipe recipe, long maxMana) {
      if (recipe != null) {
         long maxContentMultiplier = 0L;
         long contentMultiplier = 1L;
         int duration = recipe.duration;
         if (duration <= 0) {
            recipe.duration = 1;
         } else {
            long recipeMana = MANATRecipeExtension.getMANAt(recipe);
            int limit = machine instanceof IOverclockMachine overclockMachine ? overclockMachine.getOverclockLimit() : 5;
            int ocLevel = 0;

            while (true) {
               long overclockMana = recipeMana << 2;
               if (overclockMana > maxMana || overclockMana < 0L) {
                  break;
               }

               int d = duration >> 2;
               if (d < limit) {
                  if (maxContentMultiplier == 0L) {
                     maxContentMultiplier = ParallelLogic.getRemainingMaxParallelAmount(machine, unit, recipe);
                     if (maxContentMultiplier == 0L) {
                        return null;
                     }
                  }

                  long parallel = contentMultiplier << 1;
                  if (parallel > maxContentMultiplier) {
                     contentMultiplier = maxContentMultiplier;
                     break;
                  }

                  contentMultiplier = parallel;
               } else {
                  duration = d;
               }

               recipeMana = overclockMana;
               ocLevel++;
            }

            recipe.ocLevel = ocLevel / 2;
            recipe.duration = Math.max(1, duration);
            MANATRecipeExtension.setMANAt(recipe, recipeMana);
         }

         if (machine instanceof IWorkableMultiController controller && controller.isBatchEnabled()) {
            return RecipeModifier.batchProcessing(machine, unit, recipe, maxContentMultiplier, contentMultiplier);
         }

         if (contentMultiplier > 1L) {
            recipe.modifier(contentMultiplier, false);
            recipe.batchParallels = contentMultiplier;
         }
      }

      return recipe;
   }

   @Generated
   private GTORecipeModifiers() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
