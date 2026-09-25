package com.gtolib.api.machine.mana.feature;

import com.gtolib.api.capability.IManaContainer;
import org.jetbrains.annotations.NotNull;

public interface IManaContainerMachine {
   @NotNull
   IManaContainer getManaContainer();

   default boolean useMana(long mana, boolean simulate) {
      if (mana < 0L) {
         if (!simulate) {
            this.getManaContainer().addMana(-mana, 1, false);
         }

         return true;
      } else {
         return this.getManaContainer().removeMana(mana, 1, simulate) == mana;
      }
   }

   default boolean useManaUnrestricted(long mana, boolean simulate) {
      if (mana < 0L) {
         if (!simulate) {
            this.getManaContainer().addManaUnrestricted(-mana, false);
         }

         return true;
      } else {
         return this.getManaContainer().removeManaUnrestricted(mana, simulate) == mana;
      }
   }
}
