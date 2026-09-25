package com.gtolib.api.machine.feature.multiblock;

import com.gtolib.api.recipe.TierDataKey;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;

public interface ITierCasingMachine {
   Reference2IntMap<TierDataKey> getCasingTiers();

   default int getCasingTier(TierDataKey type) {
      return this.getCasingTiers().getInt(type);
   }
}
