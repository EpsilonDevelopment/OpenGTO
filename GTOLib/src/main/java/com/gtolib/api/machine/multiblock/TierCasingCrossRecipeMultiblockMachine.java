package com.gtolib.api.machine.multiblock;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gtocore.common.data.GTORecipeDataKeys;
import com.gtolib.api.machine.feature.multiblock.ITierCasingMachine;
import com.gtolib.api.machine.trait.TierCasingTrait;
import com.gtolib.api.recipe.TierDataKey;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import org.jetbrains.annotations.NotNull;

public final class TierCasingCrossRecipeMultiblockMachine extends CrossRecipeMultiblockMachine implements ITierCasingMachine {
   private final TierCasingTrait tierCasingTrait;

   public static Function<MetaMachineBlockEntity, TierCasingCrossRecipeMultiblockMachine> createParallel(
      ToLongFunction<TierCasingCrossRecipeMultiblockMachine> parallel, TierDataKey... tierTypes
   ) {
      return holder -> new TierCasingCrossRecipeMultiblockMachine(holder, parallel, tierTypes);
   }

   public static Function<MetaMachineBlockEntity, TierCasingCrossRecipeMultiblockMachine> createGasTieredParallel() {
      return createParallel(m -> 1L << 2 * m.getCasingTier(GTORecipeDataKeys.GLASS_TIER), GTORecipeDataKeys.GLASS_TIER);
   }

   private TierCasingCrossRecipeMultiblockMachine(
      MetaMachineBlockEntity holder, @NotNull ToLongFunction<TierCasingCrossRecipeMultiblockMachine> parallel, TierDataKey... tierTypes
   ) {
      super(holder, false, false, m -> parallel.applyAsLong((TierCasingCrossRecipeMultiblockMachine)m));
      this.tierCasingTrait = new TierCasingTrait(this, tierTypes);
   }

   public Reference2IntOpenHashMap<TierDataKey> getCasingTiers() {
      return this.tierCasingTrait.getCasingTiers();
   }
}
