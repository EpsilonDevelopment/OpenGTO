package com.gtolib.ae2.crafting.utils;

import appeng.api.stacks.AEKey;
import com.gtolib.ae2.crafting.models.DependencyGraph;
import com.gtolib.ae2.crafting.models.DependencyNode;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap.Entry;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public final class TopologicalSortUtils {
   public static ArrayList<AEKey> tryKahnSort(DependencyGraph graph) throws InterruptedException {
      Reference2IntOpenHashMap<AEKey> inDegree = calculateInDegrees(graph);
      Queue<AEKey> zeroInDegreeQueue = findZeroInDegreeNodes(inDegree);
      ArrayList<AEKey> sortedItems = new ArrayList<>();
      int processedCount = 0;

      while (!zeroInDegreeQueue.isEmpty()) {
         if (++processedCount % 10 == 0) {
            checkInterruption();
         }

         AEKey current = zeroInDegreeQueue.poll();
         sortedItems.add(current);
         updateInDegreesAfterProcessing(current, graph, inDegree, zeroInDegreeQueue);
      }

      return sortedItems.size() == graph.size() ? sortedItems : null;
   }

   private static Reference2IntOpenHashMap<AEKey> calculateInDegrees(DependencyGraph graph) {
      Reference2IntOpenHashMap<AEKey> inDegree = new Reference2IntOpenHashMap<>();

      for (Entry<AEKey, DependencyNode> entry : graph) {
         DependencyNode node = entry.getValue();
         inDegree.addTo(entry.getKey(), node == null ? 0 : node.getDependencies().size());
      }

      return inDegree;
   }

   private static Queue<AEKey> findZeroInDegreeNodes(Reference2IntOpenHashMap<AEKey> inDegree) {
      Queue<AEKey> queue = new LinkedList<>();
      ObjectIterator<it.unimi.dsi.fastutil.objects.Reference2IntMap.Entry<AEKey>> it = inDegree.reference2IntEntrySet().fastIterator();

      while (it.hasNext()) {
         it.unimi.dsi.fastutil.objects.Reference2IntMap.Entry<AEKey> entry = it.next();
         if (entry.getIntValue() == 0) {
            queue.offer(entry.getKey());
         }
      }

      return queue;
   }

   private static void updateInDegreesAfterProcessing(
      AEKey current, DependencyGraph graph, Reference2IntOpenHashMap<AEKey> inDegree, Queue<AEKey> zeroInDegreeQueue
   ) {
      for (Entry<AEKey, DependencyNode> entry : graph) {
         DependencyNode node = entry.getValue();
         if (node != null && node.getDependencies().containsKey(current)) {
            AEKey item = entry.getKey();
            int newInDegree = inDegree.addTo(item, -1);
            if (newInDegree == 1) {
               zeroInDegreeQueue.offer(item);
            }
         }
      }
   }

   private static void checkInterruption() throws InterruptedException {
      if (Thread.interrupted()) {
         throw new InterruptedException();
      }
   }
}
