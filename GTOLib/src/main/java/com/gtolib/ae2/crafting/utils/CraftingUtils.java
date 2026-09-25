package com.gtolib.ae2.crafting.utils;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.IPatternDetails.IInput;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import com.gto.fastcollection.fastutil.O2LOpenCacheHashMap;
import com.gtolib.ae2.crafting.models.DependencyGraph;
import com.gtolib.ae2.crafting.models.DependencyNode;
import com.gtolib.ae2.crafting.reborn.Context;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2LongMap.Entry;
import java.util.List;
import java.util.Set;

public final class CraftingUtils {
   public static boolean isValidPattern(IPatternDetails pattern, Context context) {
      Set<AEKey> outputs = extractOutputKeys(pattern);
      boolean hasOverlapWithInputs = false;

      for (IInput input : pattern.getInputs()) {
         AEKey inputKey = input.getPossibleInputs()[0].what();
         if (outputs.contains(inputKey)) {
            hasOverlapWithInputs = true;
            context.cycleDetectedResult.cycles.add(List.of(inputKey, inputKey));
         }
      }

      return !hasOverlapWithInputs && pattern.getPrimaryOutput().amount() > 0L;
   }

   private static Set<AEKey> extractOutputKeys(IPatternDetails pattern) {
      GenericStack[] arr = pattern.getOutputs();
      ReferenceOpenHashSet<AEKey> outputs = new ReferenceOpenHashSet<>(arr.length);

      for (GenericStack output : arr) {
         outputs.add(output.what());
      }

      return outputs;
   }

   public static long getPatternOutput(IPatternDetails pattern, AEKey targetItem) {
      for (GenericStack output : pattern.getOutputs()) {
         if (output.what().equals(targetItem)) {
            return output.amount();
         }
      }

      return 0L;
   }

   public static long calculateCraftingTimes(long needToCraft, long outputPerPattern) {
      return (needToCraft + outputPerPattern - 1L) / outputPerPattern;
   }

   public static KeyCounter createSafeKeyCounter(KeyCounter source) {
      source.removeZeros();
      KeyCounter safe = new KeyCounter();
      safe.addAll(source);
      return safe;
   }

   public static Object2LongOpenHashMap<IPatternDetails> createSafePatternMap(Object2LongOpenHashMap<IPatternDetails> source) {
      Object2LongOpenHashMap<IPatternDetails> map = new O2LOpenCacheHashMap<>(source);
      ObjectIterator<Entry<IPatternDetails>> it = map.object2LongEntrySet().fastIterator();

      while (it.hasNext()) {
         if (it.next().getLongValue() < 1L) {
            it.remove();
         }
      }

      return map;
   }

   public static boolean hasMultiplePaths(DependencyGraph dependencyGraph) {
      if (dependencyGraph == null) {
         return false;
      }

      for (it.unimi.dsi.fastutil.objects.Reference2ObjectMap.Entry<AEKey, DependencyNode> entry : dependencyGraph) {
         DependencyNode node = entry.getValue();
         if (node != null && node.getPatterns().size() > 1) {
            return true;
         }
      }

      return false;
   }
}
