package com.gtolib.api.machine;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gtolib.api.annotation.dynamic.DynamicInitialData;
import com.gtolib.api.registries.ScanningClass;

public interface IGTOMachineDefinition {
   DynamicInitialData getDynamicInitialData();

   void setDynamicInitialData(DynamicInitialData var1);

   boolean canWorkInSpaceIndependently();

   void setCanWorkInSpaceIndependently(boolean var1);

   static void update(MetaMachine machine) {
      IGTOMachineDefinition definition = (IGTOMachineDefinition)machine.getDefinition();
      DynamicInitialData config = definition.getDynamicInitialData();
      if (config != null) {
         if (config != DynamicInitialData.DEFAULT) {
            config.update(machine);
         }
      } else {
         config = ScanningClass.OBJECT_VALUES.get(machine.getClass());
         if (config == null) {
            definition.setDynamicInitialData(DynamicInitialData.DEFAULT);
         } else {
            config.update(machine);
            definition.setDynamicInitialData(config);
         }
      }
   }
}
