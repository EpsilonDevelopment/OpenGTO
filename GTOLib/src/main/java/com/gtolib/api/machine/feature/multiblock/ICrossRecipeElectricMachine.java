package com.gtolib.api.machine.feature.multiblock;

import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine;
import com.gregtechceu.gtceu.api.machine.feature.IOverclockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gtocore.common.data.GTOItems;
import com.gtolib.GTOCore;
import com.gtolib.api.machine.feature.ICustomElectricMachine;
import com.gtolib.api.machine.feature.IPowerAmplifierMachine;
import com.gtolib.api.machine.trait.CrossRecipeTrait;
import com.gtolib.api.player.IEnhancedPlayer;
import com.gtolib.api.recipe.RecipeBuilder;
import com.gtolib.api.wireless.ExtendWirelessEnergyContainer;
import com.gtolib.utils.MathUtil;
import java.math.BigInteger;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;

public interface ICrossRecipeElectricMachine extends ICrossRecipeMachine, IOverclockMachine, IPowerAmplifierMachine, ICustomElectricMachine, IInteractedMachine {
   default ExtendWirelessEnergyContainer getWirelessContainer() {
      CrossRecipeTrait trait = this.getCrossRecipeTrait();
      return trait.wirelessEnergyContainer != null ? trait.wirelessEnergyContainer.getWirelessEnergyContainer() : null;
   }

   @Override
   default long getOverclockMaxUnit() {
      return this.getOverclockVoltage();
   }

   @Override
   default long getRecipeUnit(GTRecipe recipe) {
      return recipe.eut;
   }

   @Override
   default void setRecipeUnit(long recipeUnit, RecipeBuilder builder) {
      builder.EUt(recipeUnit);
   }

   @Override
   default InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      if (this.isActivated() && player.getItemInHand(hand).is(GTOItems.TIME_TWISTER.asItem())) {
         long duration = 0L;

         for (ICrossRecipeMachine.Thread t : this.getThreads()) {
            duration += (t.duration - t.progress) / 2;
         }

         BigInteger eu = BigInteger.valueOf(MathUtil.saturatedCast(this.getTotalEu()));
         BigInteger var14 = eu.multiply(BigInteger.valueOf(duration)).multiply(BigInteger.valueOf(2L << GTOCore.difficulty));
         ExtendWirelessEnergyContainer c = IEnhancedPlayer.of(player).getPlayerData().getWirelessEnergyContainer();
         if (c.getStorage().compareTo(var14) >= 0) {
            c.setStorage(c.getStorage().subtract(var14));

            for (ICrossRecipeMachine.Thread t : this.getThreads()) {
               t.progress = t.progress + (t.duration - t.progress) / 2;
            }

            player.displayClientMessage(Component.translatable("gtocore.item.time_twister.consumed_eu", FormattingUtil.formatNumbers(var14), duration), true);
            return InteractionResult.SUCCESS;
         }
      }

      return IInteractedMachine.super.onUse(state, world, pos, player, hand, hit);
   }

   @Override
   default boolean isActivated() {
      return !this.getThreads().isEmpty();
   }

   @Override
   default double getTotalEu() {
      double eu = 0.0;

      for (ICrossRecipeMachine.Thread t : this.getThreads()) {
         eu += (double)t.use * t.recipe.eut;
      }

      return eu;
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
      long maxVoltage = this.getOverclockVoltage();
      double energyMultiplier = this.gtolib$getPowerAmplifierEnergyMultiplier();
      double durationMultiplier = this.gtolib$getPowerAmplifierDurationMultiplier();
      if (durationFactor < 0.25) {
         overclocking(recipe, maxVoltage, (long)(recipe.eut * energyMultiplier), recipe.duration * durationMultiplier, durationFactor, this.getOverclockLimit());
         if (trait.isSingleThread) {
            this.parallelSingle(trait, recipe, maxVoltage);
         } else {
            this.parallel(trait, recipe, maxVoltage);
         }
      } else {
         if (trait.isSingleThread) {
            this.parallelSingle(trait, recipe, maxVoltage);
         } else {
            this.parallel(trait, recipe, maxVoltage);
         }

         overclocking(recipe, maxVoltage, (long)(recipe.eut * energyMultiplier), recipe.duration * durationMultiplier, durationFactor, this.getOverclockLimit());
      }

      long maxContentMultiplier = contentParallel / trait.lastParallel;
      if (maxContentMultiplier > 1L) {
         long contentMultiplier = 1L;
         long recipeVoltage = recipe.eut;
         int ocLevel = 0;

         while (true) {
            long overclockVoltage = recipeVoltage << 2;
            if (overclockVoltage > maxVoltage || overclockVoltage < 0L) {
               break;
            }

            long parallel = contentMultiplier << 1;
            if (parallel > maxContentMultiplier) {
               break;
            }

            contentMultiplier = parallel;
            recipeVoltage = overclockVoltage;
            ocLevel++;
         }

         recipe.eut = recipeVoltage;
         recipe.ocLevel += ocLevel;
         if (this.isBatchEnabled()) {
            return RecipeModifier.batchProcessing(this, unit, recipe, maxContentMultiplier, contentMultiplier);
         }

         recipe.modifier(contentMultiplier, false);
         recipe.batchParallels = contentMultiplier;
      }

      return recipe;
   }

   private static void overclocking(GTRecipe recipe, long maxVoltage, long recipeVoltage, double duration, double durationFactor, int limit) {
      if (duration > 1.0) {
         int ocLevel = 0;

         while (true) {
            long overclockVoltage = recipeVoltage << 2;
            if (overclockVoltage > maxVoltage || overclockVoltage < 0L) {
               break;
            }

            double d = duration * durationFactor;
            if (d < limit) {
               break;
            }

            duration = d;
            recipeVoltage = overclockVoltage;
            ocLevel++;
         }

         recipe.ocLevel = ocLevel;
         recipe.duration = (int)duration;
      } else {
         recipe.duration = 1;
      }

      recipe.eut = recipeVoltage;
   }

   private void parallelSingle(CrossRecipeTrait trait, GTRecipe recipe, long maxVoltage) {
      long maxParallel = Math.min(recipe.contentParallel, this.getParallel());
      if (maxParallel > 1L) {
         long eu = recipe.eut;
         if (eu > 0L) {
            long var9 = Math.min(maxParallel, maxVoltage / eu);
            if (var9 > 1L) {
               recipe.modifier(var9, true);
            }
         } else {
            recipe.modifier(maxParallel, false);
         }
      }

      trait.lastParallel = recipe.parallels;
   }

   private void parallel(CrossRecipeTrait trait, GTRecipe recipe, long maxVoltage) {
      boolean separateThread = trait.isSeparateThread;
      long maxParallel = Math.min(recipe.contentParallel, !separateThread && !this.isRepeatedRecipes() ? trait.maxParallel : trait.availableParallel);
      if (maxParallel > 1L) {
         long eu = recipe.eut;
         if (eu > 0L) {
            long euParallel = maxVoltage / eu;
            if (separateThread) {
               if (euParallel < trait.maxParallel) {
                  maxParallel = Math.min(maxParallel, euParallel * trait.availableThread);
               } else {
                  euParallel = trait.maxParallel;
               }

               if (maxParallel > euParallel) {
                  trait.useThread = (int)Math.ceil((double)maxParallel / euParallel);
                  recipe.eut = eu * euParallel;
               } else {
                  trait.useThread = 1;
                  recipe.eut = eu * maxParallel;
               }
            } else {
               maxParallel = Math.min(maxParallel, euParallel);
               recipe.eut = eu * maxParallel;
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
