package com.gtolib.api.machine.multiblock;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gtocore.common.data.GTORecipeDataKeys;
import com.gtolib.api.machine.feature.multiblock.ITierCasingMachine;
import com.gtolib.api.machine.trait.TierCasingTrait;
import com.gtolib.api.recipe.TierDataKey;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.function.Function;
import java.util.function.ToLongFunction;

public class CoilTieredCasingMultiblockMachine extends CoilCrossRecipeMultiblockMachine implements ITierCasingMachine {
   private final TierCasingTrait tierCasingTrait;

   public static Function<MetaMachineBlockEntity, CoilTieredCasingMultiblockMachine> createEBFTieredParallel(
      ToLongFunction<CoilTieredCasingMultiblockMachine> parallel, boolean check, TierDataKey... tierTypes
   ) {
      return createTieredParallel(parallel, true, check, tierTypes);
   }

   public static Function<MetaMachineBlockEntity, CoilTieredCasingMultiblockMachine> createGlassTieredParallel(boolean ebf, boolean check) {
      return createTieredParallel(m -> 1L << 2 * (m.getCasingTier(GTORecipeDataKeys.GLASS_TIER) - 1), ebf, check, GTORecipeDataKeys.GLASS_TIER);
   }

   public static Function<MetaMachineBlockEntity, CoilTieredCasingMultiblockMachine> createTieredParallel(
      ToLongFunction<CoilTieredCasingMultiblockMachine> parallel, boolean ebf, boolean check, TierDataKey... tierTypes
   ) {
      return holder -> new CoilTieredCasingMultiblockMachine(holder, false, false, ebf, check, parallel, tierTypes);
   }

   protected CoilTieredCasingMultiblockMachine(
      MetaMachineBlockEntity holder,
      boolean infinite,
      boolean isHatchParallel,
      boolean ebf,
      boolean check,
      ToLongFunction<CoilTieredCasingMultiblockMachine> parallel,
      TierDataKey... tierTypes
   ) {
      super(holder, infinite, isHatchParallel, ebf, check, p -> parallel.applyAsLong((CoilTieredCasingMultiblockMachine)p));
      this.tierCasingTrait = new TierCasingTrait(this, tierTypes);
   }

   public Reference2IntOpenHashMap<TierDataKey> getCasingTiers() {
      return this.tierCasingTrait.getCasingTiers();
   }
}
