package com.gtolib.api.machine.mana.feature;

import com.gregtechceu.gtceu.api.machine.feature.IDummyEnergyMachine;
import com.gregtechceu.gtceu.api.machine.feature.IDummyEnergyMachine.DummyContainer;
import com.gtolib.api.capability.IManaContainer;

public interface IManaEnergyMachine extends IDummyEnergyMachine {
   final class ManaEnergyContainer extends DummyContainer {
      private final IManaContainer container;

      public ManaEnergyContainer(long eut, IManaContainer container) {
         super(eut);
         this.container = container;
      }

      @Override
      public long changeEnergy(long differenceAmount) {
         return -this.container.removeMana(-differenceAmount, 1, false);
      }

      @Override
      public long getEnergyStored() {
         return this.container.getCurrentMana();
      }
   }
}
