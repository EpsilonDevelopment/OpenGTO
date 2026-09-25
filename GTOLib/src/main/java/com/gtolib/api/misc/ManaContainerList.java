package com.gtolib.api.misc;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gtolib.api.capability.IManaContainer;
import com.gtolib.api.machine.ManaDistributorMachine;

public final class ManaContainerList implements IManaContainer {
   public static final ManaContainerList EMPTY = new ManaContainerList();
   private final IManaContainer[] containers;
   private final long maxMana;
   private final long maxIORate;

   public ManaContainerList(IManaContainer... containers) {
      this.containers = containers;
      long maxMana = 0L;

      for (IManaContainer container : containers) {
         maxMana += container.getMaxMana();
      }

      this.maxMana = maxMana;
      long getMaxProductionRate = 0L;

      for (IManaContainer container : containers) {
         getMaxProductionRate += container.getMaxIORate();
      }

      this.maxIORate = getMaxProductionRate;
   }

   @Override
   public boolean acceptDistributor() {
      return false;
   }

   @Override
   public MetaMachine getMachine() {
      return null;
   }

   @Override
   public long getMaxMana() {
      return this.maxMana;
   }

   @Override
   public long getCurrentMana() {
      long currentMana = 0L;

      for (IManaContainer container : this.containers) {
         currentMana += container.getCurrentMana();
      }

      return currentMana;
   }

   @Override
   public long getMaxIORate() {
      return this.maxIORate;
   }

   @Override
   public void setCurrentMana(long mana) {
   }

   public ManaDistributorMachine getNetMachineCache() {
      return null;
   }

   public void setNetMachineCache(ManaDistributorMachine cache) {
   }

   @Override
   public long addMana(long amount, int rateMultiplier, boolean simulate) {
      long change = 0L;

      for (IManaContainer container : this.containers) {
         if (amount <= 0L) {
            return change;
         }

         long mana = container.addMana(amount, rateMultiplier, simulate);
         change += mana;
         amount -= mana;
      }

      return change;
   }

   @Override
   public long removeMana(long amount, int rateMultiplier, boolean simulate) {
      long change = 0L;

      for (IManaContainer container : this.containers) {
         if (amount <= 0L) {
            return change;
         }

         long mana = container.removeMana(amount, rateMultiplier, simulate);
         change += mana;
         amount -= mana;
      }

      return change;
   }

   @Override
   public long addManaUnrestricted(long amount, boolean simulate) {
      long change = 0L;

      for (IManaContainer container : this.containers) {
         if (amount <= 0L) {
            return change;
         }

         long mana = container.addManaUnrestricted(amount, simulate);
         change += mana;
         amount -= mana;
      }

      return change;
   }

   @Override
   public long removeManaUnrestricted(long amount, boolean simulate) {
      long change = 0L;

      for (IManaContainer container : this.containers) {
         if (amount <= 0L) {
            return change;
         }

         long mana = container.removeManaUnrestricted(amount, simulate);
         change += mana;
         amount -= mana;
      }

      return change;
   }
}
