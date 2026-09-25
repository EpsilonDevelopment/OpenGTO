package com.gtolib.ae2.crafting.reborn.api.pipeline;

import com.gtolib.ae2.crafting.reborn.Context;
import java.util.ArrayList;
import java.util.List;

public class Pipeline {
   private final List<PipelineStep> steps = new ArrayList<>();
   private final List<PipelineStep> forceSteps = new ArrayList<>();

   public final Pipeline addStep(PipelineStep step) {
      this.steps.add(step);
      return this;
   }

   public final Pipeline addForceStep(PipelineStep step) {
      this.forceSteps.add(step);
      return this.addStep(step);
   }

   public final Context execute(Context context) {
      for (PipelineStep step : this.steps) {
         if (!context.isComplete || this.forceSteps.contains(step)) {
            step.execute(context);
         }
      }

      return context;
   }
}
