package com.gtolib.ae2.pattern;

import appeng.api.crafting.IPatternDetails.IInput;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.crafting.pattern.AEStonecuttingPattern;
import com.gtolib.api.ae2.pattern.IParallelPatternDetails;
import net.minecraft.world.level.Level;

public final class ParallelAEStonecuttingPattern extends AEStonecuttingPattern implements IParallelPatternDetails {
   private long parallel;
   private IParallelPatternDetails copy;
   private final ParallelInputWrapper[] input;
   private final ParallelGenericStackWrapper output;
   private final Level level;

   public ParallelAEStonecuttingPattern(AEItemKey definition, Level level, long parallel) {
      super(definition, level);
      this.level = level;
      this.parallel = parallel;
      this.input = ParallelInputWrapper.of(super.getInputs(), parallel);
      this.output = ParallelGenericStackWrapper.of(super.getOutputs(), parallel);
   }

   @Override
   public IInput[] getInputs() {
      return this.input;
   }

   @Override
   public GenericStack[] getOutputs() {
      return this.output.stacks;
   }

   @Override
   public void parallel(long parallel) {
      this.parallel = parallel;
      ParallelInputWrapper.setParallel(this.input, parallel);
      this.output.setParallel(parallel);
   }

   @Override
   public IParallelPatternDetails copy(long parallel, Level level) {
      return new ParallelAEStonecuttingPattern(this.getDefinition(), level, parallel);
   }

   @Override
   public IParallelPatternDetails getCopy() {
      return this.copy != null ? this.copy : (this.copy = this.copy(this.parallel, this.level));
   }

   @Override
   public long getParallel() {
      return this.parallel;
   }
}
