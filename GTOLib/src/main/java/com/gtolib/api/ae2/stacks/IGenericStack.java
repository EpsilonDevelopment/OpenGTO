package com.gtolib.api.ae2.stacks;

import appeng.api.stacks.GenericStack;
import org.jetbrains.annotations.NotNull;

public interface IGenericStack {
   void setAmount(long var1);

   static IGenericStack of(@NotNull GenericStack stack) {
      return (IGenericStack)(Object)stack;
   }
}
