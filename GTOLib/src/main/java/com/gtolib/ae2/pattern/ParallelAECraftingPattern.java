package com.gtolib.ae2.pattern;

import appeng.api.crafting.IPatternDetails.IInput;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.crafting.pattern.AECraftingPattern;
import com.gtolib.api.ae2.pattern.IParallelPatternDetails;
import net.minecraft.world.level.Level;

public final class ParallelAECraftingPattern extends AECraftingPattern implements IParallelPatternDetails {
   private long parallel;
   private IParallelPatternDetails copy;
   private final ParallelInputWrapper[] input;
   private final ParallelGenericStackWrapper output;
   private final ParallelGenericStackWrapper sparseInputs;
   private final ParallelGenericStackWrapper sparseOutputs;
   private final Level level;

   public ParallelAECraftingPattern(AEItemKey definition, Level level, long parallel) {
      super(definition, level);
      this.level = level;
      this.parallel = parallel;
      this.input = ParallelInputWrapper.of(super.getInputs(), parallel);
      this.output = ParallelGenericStackWrapper.of(super.getOutputs(), parallel);
      this.sparseInputs = ParallelGenericStackWrapper.of(super.getSparseInputs(), parallel);
      this.sparseOutputs = ParallelGenericStackWrapper.of(super.getSparseOutputs(), parallel);
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
   public GenericStack[] getSparseInputs() {
      return this.sparseInputs.stacks;
   }

   @Override
   public GenericStack[] getSparseOutputs() {
      return this.sparseOutputs.stacks;
   }

   @Override
   public void parallel(long parallel) {
      this.parallel = parallel;
      ParallelInputWrapper.setParallel(this.input, parallel);
      this.output.setParallel(parallel);
   }

   @Override
   public IParallelPatternDetails copy(long parallel, Level level) {
      return new ParallelAECraftingPattern(this.getDefinition(), level, parallel);
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
