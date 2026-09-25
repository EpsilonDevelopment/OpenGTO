package com.gtolib.api.machine.feature;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.machine.feature.IElectricMachine;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMufflerMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IWorkableMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.registry.registrate.MultiblockMachineBuilder.MufflerProductionGenerator;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.item.ItemMap;
import com.gtocore.config.GTOConfig;
import com.gtocore.data.IdleReason;
import com.gtolib.GTOCore;
import net.minecraft.world.item.ItemStack;

public interface IGTOMufflerMachine extends IMufflerMachine, IControllable, ITieredMachine {
   int gtolib$getRecoveryChance();

   default boolean isMufflerPulseDisabled() {
      return GTOConfig.INSTANCE.gamePlay.disableMufflerPart && !GTOCore.isExpert() || this.gtolib$getRecoveryChance() >= 100;
   }

   @Override
   default GTRecipe modifyRecipe(IWorkableMultiController controller, RecipeHandlerUnit unit, GTRecipe recipe) {
      int tier = this.getTier();
      if (GTOCore.isExpert() && controller instanceof IElectricMachine machine && machine.getTier() < tier - 1) {
         IdleReason.MUFFLER_NOT_SUPPORTED.setReason(controller);
         return null;
      } else if (this.isMufflerPulseDisabled()) {
         return recipe;
      } else if (GTOCore.isExpert() && controller instanceof ITieredMachine machine && machine.getTier() > tier + 1) {
         IdleReason.MUFFLER_INSUFFICIENT.setReason(controller);
         return null;
      } else {
         if (!this.gtolib$checkAshFull() && this.isFrontFaceFree()) {
            return recipe;
         }

         IdleReason.MUFFLER_OBSTRUCTED.setReason(controller);
         return null;
      }
   }

   @Override
   default void onWorking(IWorkableMultiController controller) {
      IMufflerMachine.super.onWorking(controller);
      if (!this.isMufflerPulseDisabled()) {
         if (this.self().getOffsetTimer() % 80 == 0) {
            this.gtolib$addMufflerEffect();
            if (!this.gtolib$checkAshFull()) {
               this.gtolib$insertAsh(controller.self(), controller.getRecipeLogic().getLastRecipe());
            }
         }
      }
   }

   @Override
   default void afterWorking(IWorkableMultiController controller) {
   }

   default boolean gtolib$checkAshFull() {
      return false;
   }

   default void gtolib$insertAsh(MultiblockControllerMachine controller, GTRecipe lastRecipe) {
      if (!GTOCore.isEasy()) {
         if (!GTOConfig.INSTANCE.gamePlay.disableMufflerPart || GTOCore.isExpert()) {
            int count = this.gtolib$getRecoveryChance();
            if (count < 100 && count < GTValues.RNG.nextInt(100)) {
               if (GTOCore.isExpert() || GTValues.RNG.nextBoolean()) {
                  this.recoverItemsTable(ItemMap.ASH);
               }
            } else if (this.isWorkingEnabled()) {
               if (lastRecipe != null && lastRecipe.getInputEUt() >= GTValues.V[8] && GTValues.RNG.nextFloat() < 1.0E-5F * count) {
                  ItemStack ash = GTOItems.NEUTRON_PILE.asStack();
                  if (count > 100000.0) {
                     ash.setCount((int)(count / 100000.0) + (count >= GTValues.RNG.nextInt(100000) ? 0 : 1));
                  }

                  this.recoverItemsTable(ash);
               }

               if (GTValues.RNG.nextBoolean()) {
                  MufflerProductionGenerator supplier = controller.getDefinition().getRecoveryItems();
                  if (supplier != null) {
                     ItemStack ash = supplier.getMuffledProduction(controller, lastRecipe).copy();
                     if (count > 100) {
                        ash.setCount(count / 100 + (count >= GTValues.RNG.nextInt(100) ? 0 : 1));
                     }

                     this.recoverItemsTable(ash);
                  }
               }
            }
         }
      }
   }

   default void gtolib$addMufflerEffect() {
   }
}
