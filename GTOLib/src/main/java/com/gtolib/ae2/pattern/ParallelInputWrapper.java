package com.gtolib.ae2.pattern;

import appeng.api.crafting.IPatternDetails.IInput;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import com.gtolib.api.ae2.stacks.IGenericStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

final class ParallelInputWrapper implements IInput {
   private final IInput delegation;
   private final GenericStack[] possibleInputs;

   private ParallelInputWrapper(IInput delegation, long parallel) {
      this.delegation = delegation;
      GenericStack[] inputs = delegation.getPossibleInputs();
      this.possibleInputs = new GenericStack[inputs.length];

      for (int i = 0; i < inputs.length; i++) {
         this.possibleInputs[i] = new GenericStack(inputs[i].what(), inputs[i].amount() * parallel);
      }
   }

   static ParallelInputWrapper[] of(IInput[] inputs, long parallel) {
      ParallelInputWrapper[] input = new ParallelInputWrapper[inputs.length];

      for (int i = 0; i < inputs.length; i++) {
         input[i] = new ParallelInputWrapper(inputs[i], parallel);
      }

      return input;
   }

   static void setParallel(ParallelInputWrapper[] inputs, long parallel) {
      for (ParallelInputWrapper input : inputs) {
         GenericStack[] stacks = input.delegation.getPossibleInputs();

         for (int i = 0; i < stacks.length; i++) {
            IGenericStack.of(input.possibleInputs[i]).setAmount(stacks[i].amount() * parallel);
         }
      }
   }

   @Override
   public GenericStack[] getPossibleInputs() {
      return this.possibleInputs;
   }

   @Override
   public long getMultiplier() {
      return this.delegation.getMultiplier();
   }

   @Override
   public boolean isValid(AEKey input, Level level) {
      return this.delegation.isValid(input, level);
   }

   @Nullable
   @Override
   public AEKey getRemainingKey(AEKey template) {
      return null;
   }
}
