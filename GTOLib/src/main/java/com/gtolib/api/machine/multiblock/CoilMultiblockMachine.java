package com.gtolib.api.machine.multiblock;

import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.ICoilMachine;
import com.gtolib.api.machine.trait.CoilTrait;
import java.util.function.Function;

public class CoilMultiblockMachine extends ElectricMultiblockMachine implements ICoilMachine {
   private final CoilTrait coilTrait;

   public static Function<MetaMachineBlockEntity, CoilMultiblockMachine> createCoilMachine(boolean ebf, boolean check) {
      return h -> new CoilMultiblockMachine(h, ebf, check);
   }

   protected CoilMultiblockMachine(MetaMachineBlockEntity holder, boolean ebf, boolean check) {
      super(holder);
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
