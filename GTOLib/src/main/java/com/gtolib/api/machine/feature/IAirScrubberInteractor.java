package com.gtolib.api.machine.feature;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.machine.electric.AirScrubberMachine;
import com.gtolib.mc.ILevel;
import com.gtolib.utils.GTOUtils;
import it.unimi.dsi.fastutil.objects.ReferenceSet;

public interface IAirScrubberInteractor {
   AirScrubberMachine getAirScrubberMachineCache();

   void setAirScrubberMachineCache(AirScrubberMachine var1);

   default AirScrubberMachine getAirScrubberMachine() {
      if (this.getAirScrubberMachineCache() == null && this instanceof MetaMachine metaMachine && metaMachine.getLevel() != null) {
         ReferenceSet machines = ((ILevel)metaMachine.getLevel()).gtolib$getMachineNet().get(AirScrubberMachine.class);
         if (machines == null) {
            return null;
         }

         for (Object m : machines) {
            if (m instanceof AirScrubberMachine machine
               && machine.getRecipeLogic().isWorking()
               && machine.getLevel() != null
               && GTOUtils.calculateDistance(machine.getPos(), metaMachine.getPos()) < 1 << machine.getTier() << 3) {
               this.setAirScrubberMachineCache(machine);
               return machine;
            }
         }
      }

      AirScrubberMachine machine = this.getAirScrubberMachineCache();
      if (machine != null && machine.getRecipeLogic().isWorking()) {
         return machine;
      }

      this.setAirScrubberMachineCache(null);
      return null;
   }
}
