package com.gtolib.api.machine.trait;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gtolib.api.capability.IIWirelessInteractor;
import com.gtolib.api.machine.impl.DrillingControlCenterMachine;
import com.gtolib.utils.GTOUtils;
import net.minecraft.world.level.Level;

public interface IFluidDrillLogic extends IIWirelessInteractor<DrillingControlCenterMachine> {
   @Override
   default Level getLevel() {
      return this.getMachine().getLevel();
   }

   @Override
   default Class<DrillingControlCenterMachine> getProviderClass() {
      return DrillingControlCenterMachine.class;
   }

   MetaMachine getMachine();

   default boolean firstTestMachine(DrillingControlCenterMachine machine) {
      Level level = machine.getLevel();
      return level == null
         ? false
         : machine.isFormed() && machine.getRecipeLogic().isWorking() && GTOUtils.calculateDistance(machine.getPos(), this.getMachine().getPos()) < 16.0;
   }

   default boolean testMachine(DrillingControlCenterMachine machine) {
      return machine.isFormed() && machine.getRecipeLogic().isWorking();
   }
}
