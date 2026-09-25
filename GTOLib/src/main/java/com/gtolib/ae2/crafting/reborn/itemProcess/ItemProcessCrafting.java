package com.gtolib.ae2.crafting.reborn.itemProcess;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.IPatternDetails.IInput;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import com.gtolib.ae2.crafting.reborn.Context;
import com.gtolib.ae2.crafting.reborn.api.ItemProcess;
import com.gtolib.ae2.crafting.utils.CraftingUtils;
import java.util.List;

public class ItemProcessCrafting extends ItemProcess {
   @Override
   protected boolean canHandle(Context context, AEKey item) {
      return context.getCraftingService().map(s -> s.isCraftable(item)).orElse(false);
   }

   @Override
   protected void handle(Context context, AEKey item, long needed) {
      List<IPatternDetails> patterns = context.graph.getNode(item).getPatterns();
      IPatternDetails pattern = context.selectStrategy.selectBestPattern(patterns);
      if (pattern == null) {
         if (this.next != null) {
            this.next.process(context, item, needed);
         } else {
            context.missingItems.add(item, needed);
            context.requireItems.remove(item, needed);
         }
      } else {
         long patternOutput = CraftingUtils.getPatternOutput(pattern, item);
         long craftingTimes = CraftingUtils.calculateCraftingTimes(needed, patternOutput);
         context.patternTimes.addTo(pattern, craftingTimes);

         for (IInput input : pattern.getInputs()) {
            AEKey inputKey = context.selectStrategy.selectBestInput(input);

            for (GenericStack possibleInput : input.getPossibleInputs()) {
               if (possibleInput.what().equals(inputKey)) {
                  long inputNeeded = possibleInput.amount() * input.getMultiplier() * craftingTimes;
                  context.requireItems.add(inputKey, inputNeeded);
                  break;
               }
            }
         }

         context.requireItems.remove(item, needed);
         if (this.next != null) {
            this.next.process(context, item, 0L);
         }
      }
   }
}
