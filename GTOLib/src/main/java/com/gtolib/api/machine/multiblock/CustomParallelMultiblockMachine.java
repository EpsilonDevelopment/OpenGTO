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

public class CustomParallelMultiblockMachine extends ElectricMultiblockMachine implements IParallelMachine {
   @SaveToDisk
   protected final CustomParallelTrait customParallelTrait;

   public static Function<MetaMachineBlockEntity, CustomParallelMultiblockMachine> createParallel(ToLongFunction<CustomParallelMultiblockMachine> parallel) {
      return holder -> new CustomParallelMultiblockMachine(holder, parallel);
   }

   protected CustomParallelMultiblockMachine(MetaMachineBlockEntity holder, @NotNull ToLongFunction<CustomParallelMultiblockMachine> parallel) {
      super(holder);
      this.customParallelTrait = new CustomParallelTrait(this, machine -> parallel.applyAsLong((CustomParallelMultiblockMachine)machine));
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
