package com.gtolib.api.machine;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gtolib.api.annotation.dynamic.DynamicInitialData;
import lombok.Generated;
import net.minecraft.resources.ResourceLocation;

public final class BasicMachineDefinition extends MachineDefinition implements IGTOMachineDefinition {
   private DynamicInitialData dynamicInitialData;
   private boolean canWorkInSpaceIndependently;

   private BasicMachineDefinition(ResourceLocation id) {
      super(id);
   }

   public static BasicMachineDefinition createDefinition(ResourceLocation id) {
      return new BasicMachineDefinition(id);
   }

   @Override
   public boolean canWorkInSpaceIndependently() {
      return this.canWorkInSpaceIndependently;
   }

   @Generated
   @Override
   public void setDynamicInitialData(DynamicInitialData dynamicInitialData) {
      this.dynamicInitialData = dynamicInitialData;
   }

   @Generated
   @Override
   public void setCanWorkInSpaceIndependently(boolean canWorkInSpaceIndependently) {
      this.canWorkInSpaceIndependently = canWorkInSpaceIndependently;
   }

   @Generated
   @Override
   public DynamicInitialData getDynamicInitialData() {
      return this.dynamicInitialData;
   }
}
