package com.gtolib.api.machine.multiblock;

import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.ICoilMachine;
import com.gtolib.api.machine.trait.CoilTrait;
import com.gtolib.utils.MachineUtils;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CoilCrossRecipeMultiblockMachine extends CrossRecipeMultiblockMachine implements ICoilMachine {
   private final CoilTrait coilTrait;

   public static CrossRecipeMultiblockMachine createCoilParallel(MetaMachineBlockEntity holder) {
      return createCoilParallel(holder, false);
   }

   public static CrossRecipeMultiblockMachine createInfiniteCoilParallel(MetaMachineBlockEntity holder) {
      return createCoilParallel(holder, true);
   }

   private static CrossRecipeMultiblockMachine createCoilParallel(MetaMachineBlockEntity holder, boolean infinite) {
      return new CoilCrossRecipeMultiblockMachine(holder, infinite, false, false, false, CoilCrossRecipeMultiblockMachine::temperatureParallel);
   }

   public static Function<MetaMachineBlockEntity, CrossRecipeMultiblockMachine> createHatchParallel(boolean ebf) {
      return holder -> new CoilCrossRecipeMultiblockMachine(holder, false, true, ebf, true, MachineUtils::getHatchParallel);
   }

   public static Function<MetaMachineBlockEntity, CrossRecipeMultiblockMachine> createCoilParallelEBF() {
      return holder -> new CoilCrossRecipeMultiblockMachine(holder, false, false, true, false, CoilCrossRecipeMultiblockMachine::temperatureParallel);
   }

   public static Function<MetaMachineBlockEntity, CrossRecipeMultiblockMachine> createCheckedTemperatureParallel(boolean ebf) {
      return holder -> new CoilCrossRecipeMultiblockMachine(holder, false, false, ebf, true, CoilCrossRecipeMultiblockMachine::temperatureParallel);
   }

   protected CoilCrossRecipeMultiblockMachine(
      MetaMachineBlockEntity holder,
      boolean infinite,
      boolean isHatchParallel,
      boolean ebf,
      boolean check,
      ToLongFunction<CoilCrossRecipeMultiblockMachine> parallel
   ) {
      super(holder, infinite, isHatchParallel, machine -> parallel.applyAsLong((CoilCrossRecipeMultiblockMachine)machine));
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

   private long temperatureParallel() {
      return this.isFormed() ? 1L << Math.min(60, (int)(this.getTemperature() / 900.0)) : 0L;
   }
}
