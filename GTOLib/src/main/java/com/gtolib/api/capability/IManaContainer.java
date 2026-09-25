package com.gtolib.api.capability;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gtolib.api.machine.ManaDistributorMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface IManaContainer extends IIWirelessInteractor<ManaDistributorMachine> {
   @Override
   default Level getLevel() {
      return this.getMachine().getLevel();
   }

   @Override
   default Class<ManaDistributorMachine> getProviderClass() {
      return ManaDistributorMachine.class;
   }

   MetaMachine getMachine();

   default boolean firstTestMachine(ManaDistributorMachine machine) {
      if (!this.acceptDistributor()) {
         return false;
      }

      BlockPos pos = this.getMachine().getPos();
      Level level = machine.getLevel();
      return level == null ? false : machine.isFormed() && machine.add(pos);
   }

   boolean acceptDistributor();

   long getCurrentMana();

   default boolean testMachine(ManaDistributorMachine machine) {
      return machine.isFormed() && machine.isWorkingEnabled();
   }

   @Override
   default void removeNetMachineCache() {
      ManaDistributorMachine distributor = this.getNetMachineCache();
      if (distributor != null) {
         distributor.remove();
         this.setNetMachineCache(null);
      }
   }

   long getMaxIORate();

   void setCurrentMana(long var1);

   default long addManaUnrestricted(long amount, boolean simulate) {
      long change = Math.min(this.getMaxMana() - this.getCurrentMana(), amount);
      if (change <= 0L) {
         return 0L;
      }

      if (!simulate) {
         this.setCurrentMana(this.getCurrentMana() + change);
      }

      return change;
   }

   default long removeManaUnrestricted(long amount, boolean simulate) {
      long change = Math.min(this.getCurrentMana(), amount);
      if (change <= 0L) {
         return 0L;
      }

      if (!simulate) {
         this.setCurrentMana(this.getCurrentMana() - change);
      }

      return change;
   }

   default long removeMana(long amount, int rateMultiplier, boolean simulate) {
      long change = Math.min(this.getCurrentMana(), Math.min(rateMultiplier * this.getMaxIORate(), amount));
      if (change <= 0L) {
         return 0L;
      }

      if (!simulate) {
         this.setCurrentMana(this.getCurrentMana() - change);
      }

      return change;
   }

   default long addMana(long amount, int rateMultiplier, boolean simulate) {
      long change = Math.min(this.getMaxMana() - this.getCurrentMana(), Math.min(rateMultiplier * this.getMaxIORate(), amount));
      if (change <= 0L) {
         return 0L;
      }

      if (!simulate) {
         this.setCurrentMana(this.getCurrentMana() + change);
      }

      return change;
   }

   long getMaxMana();
}
