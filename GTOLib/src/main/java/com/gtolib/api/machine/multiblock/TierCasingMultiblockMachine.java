package com.gtolib.api.machine.multiblock;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gtolib.api.machine.feature.multiblock.ITierCasingMachine;
import com.gtolib.api.machine.trait.TierCasingTrait;
import com.gtolib.api.recipe.TierDataKey;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.function.Function;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TierCasingMultiblockMachine extends ElectricMultiblockMachine implements ITierCasingMachine {
   private final TierCasingTrait tierCasingTrait;

   public static Function<MetaMachineBlockEntity, TierCasingMultiblockMachine> createMachine(TierDataKey... tierTypes) {
      return holder -> new TierCasingMultiblockMachine(holder, tierTypes);
   }

   protected TierCasingMultiblockMachine(MetaMachineBlockEntity holder, TierDataKey... tierTypes) {
      super(holder);
      this.tierCasingTrait = new TierCasingTrait(this, tierTypes);
   }

   public Reference2IntOpenHashMap<TierDataKey> getCasingTiers() {
      return this.tierCasingTrait.getCasingTiers();
   }
}
