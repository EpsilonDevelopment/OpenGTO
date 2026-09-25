package com.gtolib.api.machine.multiblock;

import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.ICoilMachine;
import com.gtolib.api.machine.trait.CoilTrait;
import java.util.function.Function;
import java.util.function.ToLongFunction;

public class CoilCustomParallelMultiblockMachine extends CustomParallelMultiblockMachine implements ICoilMachine {
   private final CoilTrait coilTrait;

   public static Function<MetaMachineBlockEntity, CoilCustomParallelMultiblockMachine> createParallelCoil(
      ToLongFunction<CoilCustomParallelMultiblockMachine> parallel, boolean ebf, boolean check
   ) {
      return holder -> new CoilCustomParallelMultiblockMachine(holder, ebf, check, parallel);
   }

   protected CoilCustomParallelMultiblockMachine(
      MetaMachineBlockEntity holder, boolean ebf, boolean check, ToLongFunction<CoilCustomParallelMultiblockMachine> parallel
   ) {
      super(holder, machine -> parallel.applyAsLong((CoilCustomParallelMultiblockMachine)machine));
      this.coilTrait = new CoilTrait(this, ebf, check);
   }

   @Override
   public int getTemperature() {
      return this.coilTrait.getTemperature();
   }

   @Override
   public ICoilType getCoilType() {
      return this.coilTrait.getCoilType();
   }
}
