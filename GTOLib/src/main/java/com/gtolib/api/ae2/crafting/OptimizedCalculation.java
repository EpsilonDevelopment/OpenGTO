package com.gtolib.api.ae2.crafting;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingSimulationRequester;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import com.gtolib.ae2.crafting.reborn.Context;
import com.gtolib.ae2.crafting.reborn.SelectStrategy;
import com.gtolib.ae2.crafting.reborn.api.pipeline.Pipeline;
import com.gtolib.ae2.crafting.reborn.pipeline.PipeStepForFinalPlan;
import com.gtolib.ae2.crafting.reborn.pipeline.PipeStepForFind;
import com.gtolib.ae2.crafting.reborn.pipeline.PipeStepForGetBytes;
import com.gtolib.ae2.crafting.reborn.pipeline.PipeStepForGraph;
import com.gtolib.ae2.crafting.reborn.pipeline.PipeStepForReportCycleDetected;
import com.gtolib.ae2.crafting.reborn.pipeline.PipeStepForSortedItem;
import com.gtolib.ae2.crafting.reborn.pipeline.PipeStepForTimerEnd;
import com.gtolib.ae2.crafting.reborn.pipeline.PipeStepForTimerStart;
import com.gtolib.ae2.crafting2.logic.MainLogic;

public final class OptimizedCalculation {
   public static ICraftingPlan execute(IGrid grid, ICraftingSimulationRequester simRequester, AEKey what, long amount, CalculationStrategy strategy) {
      Pipeline pipeline = new Pipeline();
      pipeline.addForceStep(new PipeStepForTimerStart())
         .addStep(new PipeStepForGraph())
         .addStep(new PipeStepForSortedItem())
         .addStep(new PipeStepForFind())
         .addStep(new PipeStepForGetBytes())
         .addForceStep(new PipeStepForFinalPlan())
         .addForceStep(new PipeStepForReportCycleDetected())
         .addForceStep(new PipeStepForTimerEnd());
      return pipeline.execute(Context.create(simRequester, strategy, grid, new GenericStack(what, amount), new SelectStrategy(null), false)).plan;
   }

   public static ICraftingPlan executeV2(
      IGrid grid, KeyCounter keyCounter, ICraftingSimulationRequester simRequester, AEKey what, long amount, CalculationStrategy strategy
   ) {
      return MainLogic.INSTANCE.executeV2(grid, keyCounter, simRequester, what, amount, strategy);
   }
}
