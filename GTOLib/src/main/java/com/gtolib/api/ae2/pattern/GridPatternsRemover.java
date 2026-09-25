package com.gtolib.api.ae2.pattern;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.crafting.IPatternDetails.IInput;
import appeng.api.networking.IGrid;
import appeng.api.stacks.AEKey;
import appeng.core.definitions.AEItems;
import appeng.helpers.patternprovider.PatternContainer;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Multiset;
import com.google.common.collect.SetMultimap;
import com.gto.fastcollection.fastutil.OpenCacheHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class GridPatternsRemover {
   private final SetMultimap<AEKey, GridPatternsRemover.PatternLocator> whereIsThePatterns = Multimaps.newSetMultimap(
      new Reference2ReferenceOpenHashMap<>(), ReferenceOpenHashSet::new
   );
   private final SetMultimap<AEKey, AEKey> whatDoesThisProductConsistOf = Multimaps.newSetMultimap(
      new Reference2ReferenceOpenHashMap<>(), ReferenceOpenHashSet::new
   );
   private final Multiset<AEKey> howManyUsagesDoesThisIngredientHave = HashMultiset.create();

   public GridPatternsRemover(IGrid grid, Level level) {
      for (Class<?> machineClass : grid.getMachineClasses()) {
         Class<? extends PatternContainer> containerClass = tryCastMachineToContainer(machineClass);
         if (containerClass != null) {
            ObjectSet<GridPatternsRemover.PatternDetailsNoAmount> patternDetailsNoAmountSet = new OpenCacheHashSet<>();

            for (PatternContainer container : grid.getActiveMachines(containerClass)) {
               for (int i = 0; i < container.getTerminalPatternInventory().size(); i++) {
                  ItemStack maybePattern = container.getTerminalPatternInventory().getStackInSlot(i);
                  if (maybePattern.is(AEItems.PROCESSING_PATTERN.asItem())) {
                     IPatternDetails pattern = PatternDetailsHelper.decodePattern(maybePattern, level);
                     if (pattern != null) {
                        AEKey output = pattern.getPrimaryOutput().what();
                        List<AEKey> inputs = Stream.of(pattern.getInputs()).map(IInput::getPossibleInputs).map(ii -> ii[0].what()).toList();
                        this.whereIsThePatterns.put(output, new GridPatternsRemover.PatternLocator(container, i));
                        if (patternDetailsNoAmountSet.add(new GridPatternsRemover.PatternDetailsNoAmount(output, inputs))) {
                           this.whatDoesThisProductConsistOf.putAll(output, inputs);
                           this.howManyUsagesDoesThisIngredientHave.addAll(inputs);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public static List<GridPatternsRemover.PatternLocator> collectPatternToRemove(IGrid grid, Level level, AEKey key, boolean recursive) {
      return recursive
         ? new GridPatternsRemover(grid, level).collectPatternToRemove(key, new OpenCacheHashSet<>())
         : (new GridPatternsRemover(grid, level)).whereIsThePatterns.get(key).stream().toList();
   }

   List<GridPatternsRemover.PatternLocator> collectPatternToRemove(AEKey key, Set<AEKey> visited) {
      ArrayList<GridPatternsRemover.PatternLocator> toRemove = new ArrayList<>(this.whereIsThePatterns.get(key));
      if (!visited.add(key)) {
         return toRemove;
      }

      for (AEKey input : this.whatDoesThisProductConsistOf.get(key)) {
         if (this.howManyUsagesDoesThisIngredientHave.count(input) <= 1) {
            toRemove.addAll(this.collectPatternToRemove(input, visited));
         }
      }

      return toRemove;
   }

   private static Class<? extends PatternContainer> tryCastMachineToContainer(Class<?> machineClass) {
      return PatternContainer.class.isAssignableFrom(machineClass) ? machineClass.asSubclass(PatternContainer.class) : null;
   }

   private record PatternDetailsNoAmount(AEKey key, List<AEKey> inputs) {
   }

   public record PatternLocator(PatternContainer container, int slot) {
   }
}
