package com.gtolib.api.machine.multiblock;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.gtolib.api.gui.ParallelConfigurator;
import com.gtolib.api.machine.feature.multiblock.IParallelMachine;
import com.gtolib.api.machine.trait.CustomParallelTrait;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import org.jetbrains.annotations.NotNull;

public class NoEnergyCustomParallelMultiblockMachine extends NoEnergyMultiblockMachine implements IParallelMachine {
   @SaveToDisk
   protected final CustomParallelTrait customParallelTrait;

   public static Function<MetaMachineBlockEntity, NoEnergyCustomParallelMultiblockMachine> createParallel(
      ToLongFunction<NoEnergyCustomParallelMultiblockMachine> parallel
   ) {
      return holder -> new NoEnergyCustomParallelMultiblockMachine(holder, parallel);
   }

   protected NoEnergyCustomParallelMultiblockMachine(
      MetaMachineBlockEntity holder, @NotNull ToLongFunction<NoEnergyCustomParallelMultiblockMachine> getMaxParallel
   ) {
      this(holder, getMaxParallel, m -> 1L);
   }

   protected NoEnergyCustomParallelMultiblockMachine(
      MetaMachineBlockEntity holder,
      ToLongFunction<NoEnergyCustomParallelMultiblockMachine> getMaxParallel,
      ToLongFunction<NoEnergyCustomParallelMultiblockMachine> getMinParallel
   ) {
      super(holder);
      this.customParallelTrait = new CustomParallelTrait(
         this,
         machine -> getMaxParallel.applyAsLong((NoEnergyCustomParallelMultiblockMachine)machine),
         machine -> getMinParallel.applyAsLong((NoEnergyCustomParallelMultiblockMachine)machine)
      );
   }

   @Override
   public void attachConfigurators(@NotNull ConfiguratorPanel configuratorPanel) {
      super.attachConfigurators(configuratorPanel);
      configuratorPanel.attachConfigurators(new ParallelConfigurator(this));
   }

   @Override
   public long getMaxParallel() {
      return this.customParallelTrait.getMaxParallel();
   }

   @Override
   public long getMinParallel() {
      return this.customParallelTrait.getMinParallel();
   }

   @Override
   public long getParallel() {
      return this.customParallelTrait.getParallel();
   }

   @Override
   public void setParallel(long number) {
      this.customParallelTrait.setParallel(number);
   }
}
