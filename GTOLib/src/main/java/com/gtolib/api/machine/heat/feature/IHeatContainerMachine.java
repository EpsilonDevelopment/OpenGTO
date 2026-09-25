package com.gtolib.api.machine.heat.feature;

import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.api.machine.feature.IMachineFeature;
import com.gtolib.api.capability.IHeatContainer;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IHeatContainerMachine extends IMachineFeature {
   IHeatContainer getHeatContainer();

   default boolean testHeatCapability(@Nullable Direction side) {
      if (side == null) {
         return true;
      }

      IHeatContainer container = this.getHeatContainer();
      return container.heatIO(side);
   }

   @Nullable
   @Override
   default <T> Object getGTCapability(@NotNull Class<T> cap, @Nullable Direction side) {
      if (cap == IHeatContainer.class) {
         return this.testHeatCapability(side) ? this.getHeatContainer() : GTCapability.EMPTY;
      } else {
         return null;
      }
   }
}
