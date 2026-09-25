package com.gtolib.ae2.crafting.reborn.pipeline;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.IPatternDetails.IInput;
import appeng.api.stacks.AEKey;
import appeng.core.AELog;
import com.gto.fastcollection.fastutil.OpenCacheHashSet;
import com.gtolib.ae2.crafting.models.DependencyGraph;
import com.gtolib.ae2.crafting.models.DependencyNode;
import com.gtolib.ae2.crafting.reborn.Context;
import com.gtolib.ae2.crafting.reborn.api.pipeline.PipelineStep;
import com.gtolib.ae2.crafting.utils.CraftingUtils;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.LinkedList;
import java.util.Queue;

public class PipeStepForGraph implements PipelineStep {
   @Override
   public void execute(Context context) {
      context.graph = generateGraph(context);
   }

   private static DependencyGraph generateGraph(Context context) {
      DependencyGraph graph = new DependencyGraph();
      ObjectSet<AEKey> visited = new OpenCacheHashSet<>();
      Queue<AEKey> queue = new LinkedList<>();
      queue.offer(context.target.what());
      visited.add(context.target.what());

      while (!queue.isEmpty()) {
         AEKey current = queue.poll();
         DependencyNode node = graph.getOrCreate(current);
         if (context.getGrid().isEmpty()) {
            AELog.craftingDebug("(G.T.O Optimized)Grid invalid, skipping pattern search for: " + current);
         } else if (context.getCraftingService().isPresent()) {
            for (IPatternDetails pattern : context.getCraftingService().get().getCraftingFor(current)) {
               if (CraftingUtils.isValidPattern(pattern, context)) {
                  node.addPattern(pattern);

                  for (IInput input : pattern.getInputs()) {
                     AEKey inputKey = context.selectStrategy.selectBestInput(input);
                     if (!visited.contains(inputKey)) {
                        visited.add(inputKey);
                        queue.offer(inputKey);
                     }

                     node.addDependency(inputKey, input.getMultiplier());
                  }
               }
            }
         }
      }

      return graph;
   }
}
