package com.gtolib.api.machine.feature;

import com.gregtechceu.gtceu.api.machine.feature.IMachineFeature;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.utils.GTUtil;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.Level;

public interface IVacuumMachine extends IMachineFeature {
   int getVacuumTier();

   default void update() {
      if (this.getVacuumTier() > 0) {
         Level level = this.self().getLevel();
         if (level == null) {
            return;
         }

         for (Direction side : GTUtil.DIRECTIONS) {
            if (side.getAxis() != Axis.Y && this.self().getNeighborMachine(side) instanceof IRecipeLogicMachine recipeLogicMachine) {
               recipeLogicMachine.getRecipeLogic().updateTickSubscription();
            }
         }
      }
   }
}
