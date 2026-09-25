package com.gtolib.api.machine.multiblock;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gtolib.api.machine.feature.multiblock.ITierCasingMachine;
import com.gtolib.api.machine.trait.TierCasingTrait;
import com.gtolib.api.recipe.TierDataKey;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.function.Function;
import java.util.function.ToLongFunction;

public class TierCasingParallelMultiblockMachine extends CustomParallelMultiblockMachine implements ITierCasingMachine {
   private final TierCasingTrait tierCasingTrait;

   public static Function<MetaMachineBlockEntity, TierCasingParallelMultiblockMachine> createParallel(
      ToLongFunction<TierCasingParallelMultiblockMachine> parallel, TierDataKey... tierTypes
   ) {
      return holder -> new TierCasingParallelMultiblockMachine(holder, parallel, tierTypes);
   }

   protected TierCasingParallelMultiblockMachine(
      MetaMachineBlockEntity holder, ToLongFunction<TierCasingParallelMultiblockMachine> parallel, TierDataKey... tierTypes
   ) {
      super(holder, machine -> parallel.applyAsLong((TierCasingParallelMultiblockMachine)machine));
      this.tierCasingTrait = new TierCasingTrait(this, tierTypes);
   }

   public Reference2IntOpenHashMap<TierDataKey> getCasingTiers() {
      return this.tierCasingTrait.getCasingTiers();
   }
}
