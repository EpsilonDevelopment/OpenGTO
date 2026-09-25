package com.gtolib.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gto.datasynclib.annotations.SaveToDisk;
import lombok.Generated;
import net.minecraft.core.Direction;

public class EnergyContainerTrait extends MachineTrait implements IEnergyContainer {
   @SaveToDisk(defaultValue = "0")
   protected long energyStored;
   private long energyCapacity;

   public EnergyContainerTrait(MetaMachine machine, long maxCapacity) {
      super(machine);
      this.energyCapacity = maxCapacity;
   }

   public void resetBasicInfo(long maxCapacity) {
      this.energyCapacity = maxCapacity;
   }

   @Override
   public long acceptEnergyFromNetwork(Object o, Direction side, long voltage, long amperage) {
      return 0L;
   }

   @Override
   public boolean inputsEnergy(Direction side) {
      return false;
   }

   @Override
   public long changeEnergy(long energyToAdd) {
      long oldEnergyStored = this.energyStored;
      long newEnergyStored = this.energyCapacity - oldEnergyStored < energyToAdd ? this.energyCapacity : oldEnergyStored + energyToAdd;
      if (newEnergyStored < 0L) {
         newEnergyStored = 0L;
      }

      this.energyStored = newEnergyStored;
      return newEnergyStored - oldEnergyStored;
   }

   @Override
   public long getInputAmperage() {
      return 0L;
   }

   @Override
   public long getInputVoltage() {
      return 0L;
   }

   @Generated
   @Override
   public long getEnergyStored() {
      return this.energyStored;
   }

   @Generated
   @Override
   public long getEnergyCapacity() {
      return this.energyCapacity;
   }

   @Generated
   public void setEnergyStored(long energyStored) {
      this.energyStored = energyStored;
   }
}
