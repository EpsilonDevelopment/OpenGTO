package com.gtolib.ae2.crafting.utils;

import appeng.api.stacks.AEKey;
import com.gtolib.ae2.crafting.models.DependencyGraph;
import com.gtolib.ae2.crafting.models.DependencyNode;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap.Entry;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class CycleDetectionUtils {
   public static List<List<AEKey>> detectAllCycles(DependencyGraph graph) throws InterruptedException {
      Set<AEKey> visited = new ReferenceOpenHashSet<>();
      Set<AEKey> recursionStack = new ReferenceOpenHashSet<>();
      List<List<AEKey>> cycles = new ArrayList<>();

      for (Entry<AEKey, DependencyNode> entry : graph) {
         AEKey item = entry.getKey();
         if (!visited.contains(item)) {
            List<AEKey> currentPath = new ArrayList<>();
            dfsFindCycle(item, entry.getValue(), graph, visited, recursionStack, currentPath, cycles, 0);
            if (cycles.size() >= 10) {
               break;
            }
         }

         if (cycles.size() % 5 == 0) {
            checkInterruption();
         }
      }

      return cycles;
   }

   private static boolean dfsFindCycle(
      AEKey current,
      DependencyNode node,
      DependencyGraph graph,
      Set<AEKey> visited,
      Set<AEKey> recursionStack,
      List<AEKey> currentPath,
      List<List<AEKey>> cycles,
      int depth
   ) {
      if (depth > 50) {
         return false;
      }

      visited.add(current);
      recursionStack.add(current);
      currentPath.add(current);
      if (node != null) {
         for (AEKey dependency : node.getDependencies().keySet()) {
            if (!visited.contains(dependency)) {
               if (dfsFindCycle(dependency, graph.getNode(dependency), graph, visited, recursionStack, currentPath, cycles, depth + 1) && cycles.size() >= 10) {
                  return true;
               }
            } else if (recursionStack.contains(dependency)) {
               processCycleFound(dependency, currentPath, cycles);
            }
         }
      }

      recursionStack.remove(current);
      currentPath.remove(currentPath.size() - 1);
      return false;
   }

   private static void processCycleFound(AEKey cycleStart, List<AEKey> currentPath, List<List<AEKey>> cycles) {
      List<AEKey> cycle = extractCycle(cycleStart, currentPath);
      if (cycle.size() > 1 && !isDuplicateCycle(cycle, cycles)) {
         cycles.add(cycle);
      }
   }

   private static List<AEKey> extractCycle(AEKey cycleStart, List<AEKey> currentPath) {
      List<AEKey> cycle = new ArrayList<>();
      int startIndex = findCycleStartIndex(cycleStart, currentPath);
      if (startIndex >= 0) {
         int size = currentPath.size();

         for (int i = startIndex; i < size; i++) {
            cycle.add(currentPath.get(i));
         }
      }

      return cycle;
   }

   private static int findCycleStartIndex(AEKey cycleStart, List<AEKey> currentPath) {
      for (int i = currentPath.size() - 1; i >= 0; i--) {
         if (currentPath.get(i).equals(cycleStart)) {
            return i;
         }
      }

      return -1;
   }

   private static boolean isDuplicateCycle(List<AEKey> newCycle, List<List<AEKey>> existingCycles) {
      Set<AEKey> newCycleSet = new ReferenceOpenHashSet<>(newCycle);

      for (List<AEKey> existing : existingCycles) {
         if (existing.size() == newCycle.size()) {
            Set<AEKey> existingSet = new ReferenceOpenHashSet<>(existing);
            if (existingSet.equals(newCycleSet)) {
               return true;
            }
         }
      }

      return false;
   }

   private static void checkInterruption() throws InterruptedException {
      if (Thread.interrupted()) {
         throw new InterruptedException();
      }
   }
}
