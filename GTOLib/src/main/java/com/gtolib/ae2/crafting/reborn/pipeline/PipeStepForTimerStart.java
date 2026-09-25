package com.gtolib.ae2.crafting.reborn.pipeline;

import com.gtolib.ae2.crafting.reborn.Context;
import com.gtolib.ae2.crafting.reborn.api.pipeline.PipelineStep;
import java.time.Instant;

public class PipeStepForTimerStart implements PipelineStep {
   @Override
   public void execute(Context context) {
      context.startTime = Instant.now();
   }
}
