package com.gtolib.api.machine.trait;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gtolib.api.machine.feature.multiblock.IMultiblockTraitHolder;
import com.gtolib.api.machine.feature.multiblock.IParallelMachine;
import java.util.function.ToLongFunction;
import lombok.Generated;
import org.jetbrains.annotations.NotNull;

public class CustomParallelTrait extends MultiblockTrait {
   @SaveToDisk(defaultValue = "0")
   private long parallelNumber;
   private long runtimeParallelNumber;
   private boolean modified = true;
   private boolean isFormed;
   private boolean defaultMax = true;
   private final ToLongFunction<IParallelMachine> getMaxParallel;
   private final ToLongFunction<IParallelMachine> getMinParallel;

   public CustomParallelTrait(
      IParallelMachine machine, @NotNull ToLongFunction<IParallelMachine> getMaxParallel, ToLongFunction<IParallelMachine> getMinParallel
   ) {
      super((IMultiblockTraitHolder)machine);
      this.getMaxParallel = getMaxParallel;
      this.getMinParallel = getMinParallel;
   }

   public CustomParallelTrait(IParallelMachine machine, @NotNull ToLongFunction<IParallelMachine> getMaxParallel) {
      this(machine, getMaxParallel, m -> 1L);
   }

   @Override
   public void onStructureFormed() {
      this.modified = true;
      this.isFormed = true;
   }

   @Override
   public void onStructureInvalid() {
      this.modified = true;
      this.isFormed = false;
   }

   public long getMaxParallel() {
      return Math.clamp(this.getMaxParallel.applyAsLong((IParallelMachine)this.getMachine()), 1L, 9007199254740991L);
   }

   public long getMinParallel() {
      return Math.clamp(this.getMinParallel.applyAsLong((IParallelMachine)this.getMachine()), 1L, this.getMaxParallel());
   }

   public long getDefaultParallel() {
      return this.defaultMax ? this.getMaxParallel() : this.getMinParallel();
   }

   public long getParallel() {
      if (this.modified) {
         this.setRuntime();
      }

      return this.runtimeParallelNumber;
   }

   public void setParallel(long number) {
      if (this.isFormed) {
         if (number == this.getDefaultParallel()) {
            this.parallelNumber = 0L;
         } else {
            this.parallelNumber = number;
         }

         this.machine.onChanged();
         this.modified = true;
      } else if (this.machine.isRemote()) {
         this.runtimeParallelNumber = number;
      }
   }

   protected void setRuntime() {
      if (this.isFormed) {
         this.modified = false;
         if (this.parallelNumber == 0L) {
            this.runtimeParallelNumber = this.getDefaultParallel();
         } else {
            this.runtimeParallelNumber = Math.clamp(this.parallelNumber, this.getMinParallel(), this.getMaxParallel());
         }
      } else if (!this.machine.isRemote()) {
         this.runtimeParallelNumber = 1L;
      }
   }

   @Generated
   public void setDefaultMax(boolean defaultMax) {
      this.defaultMax = defaultMax;
   }
}
