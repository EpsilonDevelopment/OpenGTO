package com.gtolib.ae2.crafting.reborn.pipeline;

import com.gtolib.ae2.crafting.reborn.Context;
import com.gtolib.ae2.crafting.reborn.api.pipeline.PipelineStep;

public class PipeStepForReportCycleDetected implements PipelineStep {
   @Override
   public void execute(Context context) {
      if (!context.cycleDetectedResult.cycles.isEmpty()) {
         context.cycleDetectedResult.execute();
      }
   }
}
