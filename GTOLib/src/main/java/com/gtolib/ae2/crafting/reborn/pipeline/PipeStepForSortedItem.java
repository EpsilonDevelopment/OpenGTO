package com.gtolib.ae2.crafting.reborn.pipeline;

import appeng.api.stacks.AEKey;
import com.gtolib.ae2.crafting.reborn.Context;
import com.gtolib.ae2.crafting.reborn.CycleDetectedResult;
import com.gtolib.ae2.crafting.reborn.api.pipeline.PipelineStep;
import com.gtolib.ae2.crafting.utils.CycleDetectionUtils;
import com.gtolib.ae2.crafting.utils.TopologicalSortUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PipeStepForSortedItem implements PipelineStep {
   @Override
   public void execute(Context context) {
      try {
         ArrayList<AEKey> items = TopologicalSortUtils.tryKahnSort(context.graph);
         if (items != null) {
            Collections.reverse(items);
            context.sortedItems = items;
         }

         if (items == null) {
            context.isSimulate = true;
            context.isComplete = true;
            if (context.getPlayer().isEmpty()) {
               context.cycleDetectedResult = new CycleDetectedResult(context, Collections.emptyList());
            } else {
               List<List<AEKey>> cycles = Collections.emptyList();

               try {
                  cycles = CycleDetectionUtils.detectAllCycles(context.graph);
               } catch (InterruptedException var5) {
               }

               context.cycleDetectedResult.cycles.addAll(cycles);
            }
         }
      } catch (InterruptedException var6) {
      }
   }
}
