package com.gtolib.ae2.crafting.reborn.pipeline;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEKey;
import com.gtolib.ae2.crafting.reborn.Context;
import com.gtolib.ae2.crafting.reborn.api.pipeline.PipelineStep;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Reference2LongMap.Entry;

public class PipeStepForGetBytes implements PipelineStep {
   @Override
   public void execute(Context context) {
      for (Entry<AEKey> neededItem : context.usedItems) {
         context.bytes = context.bytes + neededItem.getLongValue() / neededItem.getKey().getAmountPerByte();
      }

      ObjectIterator<it.unimi.dsi.fastutil.objects.Object2LongMap.Entry<IPatternDetails>> it = context.patternTimes.object2LongEntrySet().fastIterator();

      while (it.hasNext()) {
         context.bytes = context.bytes + it.next().getLongValue();
      }
   }
}
