package com.gtolib.api.machine.mana.feature;

import com.gtolib.api.misc.ManaContainerList;
import org.jetbrains.annotations.NotNull;

public interface IManaMultiblock extends IManaContainerMachine {
   @NotNull
   ManaContainerList getManaContainer();

   boolean isGeneratorMana();

   default long removeMana(long amount, int rateMultiplier, boolean simulate) {
      return this.getManaContainer().removeMana(amount, rateMultiplier, simulate);
   }
}
