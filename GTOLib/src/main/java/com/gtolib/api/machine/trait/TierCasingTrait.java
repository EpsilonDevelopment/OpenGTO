package com.gtolib.api.machine.trait;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gtolib.api.machine.feature.multiblock.IMultiblockTraitHolder;
import com.gtolib.api.machine.feature.multiblock.ITierCasingMachine;
import com.gtolib.api.recipe.IdleReason;
import com.gtolib.api.recipe.TierDataKey;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap.Entry;
import java.util.List;
import lombok.Generated;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class TierCasingTrait extends MultiblockTrait {
   private final Reference2IntOpenHashMap<TierDataKey> casingTiers = new Reference2IntOpenHashMap<>(2);

   public TierCasingTrait(ITierCasingMachine machine, TierDataKey... tierTypes) {
      super((IMultiblockTraitHolder)machine);

      for (TierDataKey type : tierTypes) {
         this.casingTiers.put(type, 0);
      }
   }

   @Override
   public void onStructureFormed() {
      this.casingTiers.replaceAll((t, v) -> this.getMachine().getMultiblockState().getMatchContext().getOrDefault(t, 0));
   }

   @Override
   public void onStructureInvalid() {
      this.casingTiers.replaceAll((t, v) -> 0);
   }

   @Override
   public GTRecipe modifyRecipe(@NotNull RecipeHandlerUnit unit, @NotNull GTRecipe recipe) {
      if (!recipe.data.isEmpty()) {
         ObjectIterator<Entry<TierDataKey>> it = this.casingTiers.reference2IntEntrySet().fastIterator();

         while (it.hasNext()) {
            Entry<TierDataKey> entry = it.next();
            TierDataKey type = entry.getKey();
            if (recipe.data.getInt(type) > entry.getIntValue()) {
               IdleReason.BLOCK_TIER_NOT_SATISFIES.setReason(this.machine);
               return null;
            }
         }
      }

      return recipe;
   }

   @Override
   public void customText(@NotNull List<Component> textList) {
      this.casingTiers
         .reference2IntEntrySet()
         .fastForEach(entry -> textList.add(Component.translatable(getTierTranslationKey(entry.getKey().name), entry.getIntValue())));
   }

   public static String getTierTranslationKey(String type) {
      return "gtocore.tier." + type;
   }

   @Generated
   public Reference2IntOpenHashMap<TierDataKey> getCasingTiers() {
      return this.casingTiers;
   }
}
