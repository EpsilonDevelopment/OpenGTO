package com.gtolib.ae2.pattern;

import appeng.api.crafting.IPatternDetails.IInput;
import appeng.api.crafting.IPatternDetails.PatternInputSink;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.pattern.AEProcessingPattern;
import com.gtolib.api.ae2.pattern.IParallelPatternDetails;
import it.unimi.dsi.fastutil.objects.Reference2LongMap.Entry;
import net.minecraft.world.level.Level;

public final class ParallelAEProcessingPattern extends AEProcessingPattern implements IParallelPatternDetails {
   private long parallel;
   private IParallelPatternDetails copy;
   private final ParallelInputWrapper[] input;
   private final ParallelGenericStackWrapper output;
   private final ParallelGenericStackWrapper sparseInputs;
   private final ParallelGenericStackWrapper sparseOutputs;

   public ParallelAEProcessingPattern(AEItemKey definition, long parallel) {
      super(definition);
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
      return new ParallelAEProcessingPattern(this.getDefinition(), parallel);
   }

   @Override
   public IParallelPatternDetails getCopy() {
      return this.copy != null ? this.copy : (this.copy = this.copy(this.parallel, null));
   }

   @Override
   public long getParallel() {
      return this.parallel;
   }

   @Override
   public void pushInputsToExternalInventory(KeyCounter[] inputHolder, PatternInputSink inputSink) {
      KeyCounter[] var3 = inputHolder;
      int var4 = var3.length;

      for (int var5 = 0; var5 < var4; var5++) {
         for (Entry<AEKey> input : var3[var5]) {
            inputSink.pushInput(input.getKey(), input.getLongValue());
         }
      }
   }
}
