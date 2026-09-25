package com.gtolib.api.machine.trait;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import net.minecraft.core.Direction;

public class NotifiableSafeEnergyContainer extends NotifiableEnergyContainer {
   public NotifiableSafeEnergyContainer(MetaMachine machine, long maxCapacity, long maxInputVoltage, long maxInputAmperage) {
      super(machine, maxCapacity, maxInputVoltage, maxInputAmperage, 0L, 0L);
   }

   @Override
   public long acceptEnergyFromNetwork(Object o, Direction side, long voltage, long energyAdded) {
      if (side == null || this.inputsEnergy(side)) {
         long inputVoltage = this.getInputVoltage();
         long stored = this.getEnergyStored();
         long var11 = Math.min(this.getEnergyCapacity() - stored, Math.min(energyAdded, inputVoltage * this.getInputAmperage()));
         if (var11 > 0L) {
            this.setEnergyStored(stored + var11);
            return var11;
         }
      }

      return 0L;
   }
}
