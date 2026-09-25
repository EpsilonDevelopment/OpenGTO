package com.gtolib.api.machine.feature;

public interface ICustomElectricMachine {
   double getTotalEu();

   boolean isActivated();

   default boolean isGenerator() {
      return false;
   }
}
