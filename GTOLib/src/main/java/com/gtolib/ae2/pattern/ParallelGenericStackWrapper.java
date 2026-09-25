package com.gtolib.ae2.pattern;

import appeng.api.stacks.GenericStack;
import com.gtolib.api.ae2.stacks.IGenericStack;

final class ParallelGenericStackWrapper {
   private final GenericStack[] src;
   final GenericStack[] stacks;

   private ParallelGenericStackWrapper(GenericStack[] src, long parallel) {
      this.src = src;
      this.stacks = new GenericStack[src.length];

      for (int i = 0; i < src.length; i++) {
         if (src[i] == null) {
            this.stacks[i] = null;
         } else {
            this.stacks[i] = new GenericStack(src[i].what(), src[i].amount() * parallel);
         }
      }
   }

   static ParallelGenericStackWrapper of(GenericStack[] src, long parallel) {
      return new ParallelGenericStackWrapper(src, parallel);
   }

   void setParallel(long parallel) {
      for (int i = 0; i < this.src.length; i++) {
         GenericStack sr = this.src[i];
         if (sr != null) {
            IGenericStack.of(this.stacks[i]).setAmount(sr.amount() * parallel);
         }
      }
   }
}
