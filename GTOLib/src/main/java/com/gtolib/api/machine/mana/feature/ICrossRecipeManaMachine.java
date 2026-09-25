package com.gtolib.api.machine.mana.feature;

import com.gregtechceu.gtceu.api.machine.feature.IOverclockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gtolib.api.machine.feature.multiblock.ICrossRecipeMachine;
import com.gtolib.api.machine.trait.CrossRecipeTrait;
import com.gtolib.api.recipe.RecipeBuilder;
import com.gtolib.api.recipe.extension.MANATRecipeExtension;
import com.gtolib.api.wireless.WirelessManaContainer;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;

public interface ICrossRecipeManaMachine extends ICrossRecipeMachine, IManaContainerMachine {
   default WirelessManaContainer getWirelessContainer() {
      return null;
   }

   @Override
   default long getOverclockMaxUnit() {
      return this.getManaContainer().getMaxIORate();
   }

   @Override
   default long getRecipeUnit(GTRecipe recipe) {
      return MANATRecipeExtension.getInputMANAt(recipe);
   }

   @Override
   default void setRecipeUnit(long recipeUnit, RecipeBuilder builder) {
      builder.MANAt(recipeUnit);
   }

   @MustBeInvokedByOverriders
   @Override
   default GTRecipe getRealRecipe(RecipeHandlerUnit unit, @NotNull GTRecipe recipe) {
      long contentParallel = ParallelLogic.getMaxContentParallelAmount(this, unit, recipe, 9007199254740991L);
      if (contentParallel == 0L) {
         return null;
      }

      recipe.contentParallel = contentParallel;
      CrossRecipeTrait trait = this.getCrossRecipeTrait();
      double durationFactor = this.getOverclockFactor();
      long maxMana = this.getManaContainer().getMaxIORate();
      int overclockLimit = this instanceof IOverclockMachine overclockMachine ? overclockMachine.getOverclockLimit() : 20;
      if (durationFactor < 0.25) {
         overclocking(recipe, maxMana, MANATRecipeExtension.getInputMANAt(recipe), recipe.duration, durationFactor, overclockLimit);
         if (trait.isSingleThread) {
            this.parallelSingle(trait, recipe, maxMana);
         } else {
            this.parallel(trait, recipe, maxMana);
         }
      } else {
         if (trait.isSingleThread) {
            this.parallelSingle(trait, recipe, maxMana);
         } else {
            this.parallel(trait, recipe, maxMana);
         }

         overclocking(recipe, maxMana, MANATRecipeExtension.getInputMANAt(recipe), recipe.duration, durationFactor, overclockLimit);
      }

      long maxContentMultiplier = contentParallel / trait.lastParallel;
      if (maxContentMultiplier > 1L) {
         long contentMultiplier = 1L;
         long recipeMana = MANATRecipeExtension.getInputMANAt(recipe);
         int ocLevel = 0;

         while (true) {
            long overclockMana = recipeMana << 2;
            if (overclockMana > maxMana || overclockMana < 0L) {
               break;
            }

            long parallel = contentMultiplier << 1;
            if (parallel > maxContentMultiplier) {
               break;
            }

            contentMultiplier = parallel;
            recipeMana = overclockMana;
            ocLevel++;
         }

         MANATRecipeExtension.setMANAt(recipe, recipeMana);
         recipe.ocLevel += ocLevel;
         if (this.isBatchEnabled()) {
            return RecipeModifier.batchProcessing(this, unit, recipe, maxContentMultiplier, contentMultiplier);
         }

         recipe.modifier(contentMultiplier, false);
         recipe.batchParallels = contentMultiplier;
      }

      return recipe;
   }

   private static void overclocking(GTRecipe recipe, long maxMana, long recipeMana, double duration, double durationFactor, int limit) {
      if (duration > 1.0) {
         int ocLevel = 0;

         while (true) {
            long overclockMana = recipeMana << 2;
            if (overclockMana > maxMana || overclockMana < 0L) {
               break;
            }

            double d = duration * durationFactor;
            if (d < limit) {
               break;
            }

            duration = d;
            recipeMana = overclockMana;
            ocLevel++;
         }

         recipe.ocLevel = ocLevel;
         recipe.duration = (int)duration;
      } else {
         recipe.duration = 1;
      }

      MANATRecipeExtension.setMANAt(recipe, recipeMana);
   }

   private void parallelSingle(CrossRecipeTrait trait, GTRecipe recipe, long maxMana) {
      long maxParallel = Math.min(recipe.contentParallel, this.getParallel());
      if (maxParallel > 1L) {
         long mana = MANATRecipeExtension.getInputMANAt(recipe);
         if (mana > 0L) {
            long var9 = Math.min(maxParallel, maxMana / mana);
            if (var9 > 1L) {
               recipe.modifier(var9, true);
            }
         } else {
            recipe.modifier(maxParallel, false);
         }
      }

      trait.lastParallel = recipe.parallels;
   }

   private void parallel(CrossRecipeTrait trait, GTRecipe recipe, long maxMana) {
      boolean separateThread = trait.isSeparateThread;
      long maxParallel = Math.min(recipe.contentParallel, !separateThread && !this.isRepeatedRecipes() ? trait.maxParallel : trait.availableParallel);
      if (maxParallel > 1L) {
         long mana = MANATRecipeExtension.getInputMANAt(recipe);
         if (mana > 0L) {
            long euParallel = maxMana / mana;
            if (separateThread) {
               if (euParallel < trait.maxParallel) {
                  maxParallel = Math.min(maxParallel, euParallel * trait.availableThread);
               } else {
                  euParallel = trait.maxParallel;
               }

               if (maxParallel > euParallel) {
                  trait.useThread = (int)Math.ceil((double)maxParallel / euParallel);
                  MANATRecipeExtension.setMANAt(recipe, mana * euParallel);
               } else {
                  trait.useThread = 1;
                  MANATRecipeExtension.setMANAt(recipe, mana * maxParallel);
               }
            } else {
               maxParallel = Math.min(maxParallel, euParallel);
               MANATRecipeExtension.setMANAt(recipe, mana * maxParallel);
            }

            if (maxParallel > 1L) {
               recipe.modifier(maxParallel, false);
            }
         } else {
            recipe.modifier(maxParallel, false);
         }
      }

      trait.lastParallel = recipe.parallels;
   }
}
