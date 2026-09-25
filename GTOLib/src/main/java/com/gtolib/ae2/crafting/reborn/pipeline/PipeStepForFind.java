package com.gtolib.ae2.crafting.reborn.pipeline;

import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import com.gtolib.ae2.crafting.reborn.Context;
import com.gtolib.ae2.crafting.reborn.api.ItemProcess;
import com.gtolib.ae2.crafting.reborn.api.pipeline.PipelineStep;
import com.gtolib.ae2.crafting.reborn.itemProcess.ItemProcessCrafting;
import com.gtolib.ae2.crafting.reborn.itemProcess.ItemProcessEmittable;
import com.gtolib.ae2.crafting.reborn.itemProcess.ItemProcessInventory;
import it.unimi.dsi.fastutil.objects.Reference2LongMap.Entry;
import java.util.Iterator;

public class PipeStepForFind implements PipelineStep {
   @Override
   public void execute(Context context) {
      if (context.strategy.equals(CalculationStrategy.REPORT_MISSING_ITEMS)) {
         context.copyDataFrom(attempt(context, context.target.amount()).temporaryContext);
      } else if (context.strategy.equals(CalculationStrategy.CRAFT_LESS)) {
         long l = 0L;
         long r = context.target.amount();
         PipeStepForFind.AttemptResult result = attempt(context, r);
         if (result.success) {
            context.copyDataFrom(result.temporaryContext);
            return;
         }

         while (l <= r) {
            long mid = l + (r - l + 1L) / 2L;
            PipeStepForFind.AttemptResult current = attempt(context, mid);
            if (current.success) {
               result = current;
               l = mid + 1L;
            } else {
               r = mid - 1L;
            }
         }

         context.copyDataFrom(result.temporaryContext);
      }
   }

   private static PipeStepForFind.AttemptResult attempt(Context context, long number) {
      Context temporaryContext = context.shallowCopy();
      temporaryContext.target = new GenericStack(temporaryContext.target.what(), number);
      temporaryContext.requireItems.add(temporaryContext.target.what(), number);
      ItemProcess chain = ItemProcess.Factory.createChain(new ItemProcessEmittable(), new ItemProcessInventory(), new ItemProcessCrafting());

      for (AEKey sortedItem : context.sortedItems) {
         long l = temporaryContext.requireItems.get(sortedItem);
         chain.process(temporaryContext, sortedItem, l);
      }

      for (AEKey sortedItem : context.sortedItems) {
         long l = temporaryContext.requireItems.get(sortedItem);
         temporaryContext.requireItems.remove(sortedItem, l);
         temporaryContext.missingItems.add(sortedItem, l);
      }

      temporaryContext.missingItems.removeZeros();
      Iterator<Entry<AEKey>> it = temporaryContext.missingItems.iterator();

      while (it.hasNext()) {
         Entry<AEKey> missingItem = it.next();
         if (missingItem.getLongValue() <= 0L) {
            it.remove();
         }
      }

      boolean res;
      if (temporaryContext.missingItems.isEmpty() && number > 0L) {
         temporaryContext.isSimulate = false;
         res = true;
      } else {
         temporaryContext.isSimulate = true;
         res = false;
      }

      return new PipeStepForFind.AttemptResult(res, temporaryContext);
   }

   record AttemptResult(boolean success, Context temporaryContext) {
   }
}
