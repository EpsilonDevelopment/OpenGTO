package com.gtolib.api.machine.feature;

import com.gregtechceu.gtceu.api.machine.feature.IOverclockMachine;

public interface IOverclockConfigMachine extends IOverclockMachine {
   default boolean hasOverclockConfig() {
      return true;
   }

   @Override
   default int getOverclockLimit() {
      throw new UnsupportedOperationException("Not implemented");
   }

   default void setOverclockLimit(int number) {
      throw new UnsupportedOperationException("Not implemented");
   }
}
