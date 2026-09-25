package com.gtolib.api.machine.feature.multiblock;

import com.gregtechceu.gtceu.api.machine.feature.IMachineFeature;

public interface IParallelMachine extends IMachineFeature {
   long MAX_PARALLEL = 9007199254740991L;
   long MIN_PARALLEL = 1L;

   long getMaxParallel();

   long getMinParallel();

   default long getParallel() {
      throw new UnsatisfiedLinkError("Not Impl");
   }

   default void setParallel(long var1) {
      throw new UnsatisfiedLinkError("Not Impl");
   }
}
