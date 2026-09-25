package com.gtolib.api.machine.impl.part;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.IParallelHatch;
import com.gtolib.GTOCore;
import com.gtolib.api.machine.part.AmountConfigurationPartMachine;
import it.unimi.dsi.fastutil.ints.Int2LongFunction;

public final class ParallelHatchPartMachine extends AmountConfigurationPartMachine implements IParallelHatch {
   public static final Int2LongFunction PARALLEL_FUNCTION = tier -> 1L << (tier - 3 + (GTOCore.isEasy() ? 1 : 0) << 1);

   public ParallelHatchPartMachine(MetaMachineBlockEntity holder, int tier) {
      super(holder, tier < 0 ? 14 : tier, 1L, tier < 0 ? 9007199254740991L : PARALLEL_FUNCTION.apply(tier));
   }

   @Override
   public long getCurrentParallel() {
      return this.getCurrent();
   }
}
