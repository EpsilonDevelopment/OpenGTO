package com.gtolib.ae2.crafting.reborn.pipeline;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.CraftingPlan;
import com.gtolib.ae2.crafting.reborn.Context;
import com.gtolib.ae2.crafting.reborn.api.pipeline.PipelineStep;
import com.gtolib.ae2.crafting.utils.CraftingUtils;
import it.unimi.dsi.fastutil.objects.Object2LongMap;

public class PipeStepForFinalPlan implements PipelineStep {
   @Override
   public void execute(Context context) {
      KeyCounter safeUsedItems = CraftingUtils.createSafeKeyCounter(context.usedItems);
      KeyCounter safeEmittedItems = CraftingUtils.createSafeKeyCounter(context.emittedItems);
      KeyCounter safeMissingItems = CraftingUtils.createSafeKeyCounter(context.missingItems);
      Object2LongMap<IPatternDetails> safePatternTimes = CraftingUtils.createSafePatternMap(context.patternTimes);
      context.plan = new CraftingPlan(
         context.target, context.bytes, context.isSimulate, false, safeUsedItems, safeEmittedItems, safeMissingItems, safePatternTimes
      );
   }
}
