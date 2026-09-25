package com.gtolib.ae2.crafting.reborn;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.IPatternDetails.IInput;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import com.gtolib.ae2.crafting.utils.CraftingUtils;
import java.util.Collection;

public class SelectStrategy {
   private Context context;

   public SelectStrategy(Context context) {
      this.context = context;
   }

   public SelectStrategy setContext(Context context) {
      this.context = context;
      return this;
   }

   public AEKey selectBestInput(IInput input) {
      long maxAvailable = 0L;
      AEKey maxAeKey = input.getPossibleInputs()[0].what();

      for (GenericStack stack : input.getPossibleInputs()) {
         long storage = this.context.getUnOccupiedStorage(stack.what());
         if (storage > maxAvailable) {
            maxAvailable = storage;
            maxAeKey = stack.what();
         }
      }

      return maxAeKey;
   }

   public IPatternDetails selectBestPattern(Collection<IPatternDetails> patterns) {
      for (IPatternDetails pattern : patterns) {
         if (CraftingUtils.isValidPattern(pattern, this.context)) {
            return pattern;
         }
      }

      return null;
   }
}
