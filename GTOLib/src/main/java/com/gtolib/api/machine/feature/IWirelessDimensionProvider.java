package com.gtolib.api.machine.feature;

import com.gtocore.common.data.GTORecipeDataKeys;
import com.gtolib.api.capability.IExtendWirelessEnergyContainerHolder;
import com.gtolib.api.machine.feature.multiblock.ITierCasingMachine;
import com.gtolib.api.wireless.ExtendWirelessEnergyContainer;
import net.minecraft.world.level.Level;

public interface IWirelessDimensionProvider extends IExtendWirelessEnergyContainerHolder, ITierCasingMachine {
   default void loadContainer() {
      if (!this.isRemote()) {
         Level level = this.getLevel();
         if (level != null) {
            ExtendWirelessEnergyContainer container = this.getWirelessEnergyContainer();
            if (container != null) {
               container.getDimension().put(level.dimension().location(), this.getCasingTier(GTORecipeDataKeys.INTEGRAL_FRAMEWORK_TIER));
            }
         }
      }
   }

   boolean isRemote();

   Level getLevel();

   default void unloadContainer() {
      if (!this.isRemote()) {
         Level level = this.getLevel();
         if (level != null) {
            ExtendWirelessEnergyContainer container = this.getWirelessEnergyContainer();
            if (container != null) {
               container.getDimension().put(level.dimension().location(), 0);
            }
         }
      }
   }
}
