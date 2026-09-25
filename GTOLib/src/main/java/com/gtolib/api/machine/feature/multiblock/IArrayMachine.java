package com.gtolib.api.machine.feature.multiblock;

import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IWorkableMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IWorkableMultiPart;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gtocore.common.data.GTORecipeTypes;
import com.gtolib.api.machine.multiblock.ElectricMultiblockMachine;
import net.minecraft.world.item.Item;

public interface IArrayMachine extends IWorkableMultiController {
   default ElectricMultiblockMachine multiblockMachine() {
      return (ElectricMultiblockMachine)this;
   }

   default MachineDefinition getMachineDefinition() {
      if (this.getMachineDefinitionCache() == null && this.getStorageItem() instanceof MetaMachineItem metaMachineItem) {
         this.setMachineDefinitionCache(metaMachineItem.getDefinition());
      }

      return this.getMachineDefinitionCache();
   }

   @Override
   default GTRecipeType[] getAvailableRecipeTypes() {
      GTRecipeType[] cache = this.getAvailableRecipeTypesCache();
      if (cache == null) {
         MachineDefinition definition = this.getMachineDefinition();
         cache = definition == null ? new GTRecipeType[]{GTORecipeTypes.DUMMY_RECIPES} : definition.getRecipeTypes();
         this.setAvailableRecipeTypesCache(cache);

         for (IMultiPart p : this.getParts()) {
            if (p instanceof IWorkableMultiPart part) {
               part.setAvailableRecipeTypes(cache);
            }
         }
      }

      return cache;
   }

   default void onStorageChanged() {
      this.setMachineDefinitionCache(null);
      this.setAvailableRecipeTypesCache(null);

      for (IMultiPart p : this.getParts()) {
         p.removedFromController(this);
         p.addedToController(this);
      }

      if (this.multiblockMachine().isFormed()) {
         this.multiblockMachine().getRecipeLogic().markLastRecipeDirty();
         this.multiblockMachine().getRecipeLogic().updateTickSubscription();
      }
   }

   Item getStorageItem();

   void setMachineDefinitionCache(MachineDefinition var1);

   MachineDefinition getMachineDefinitionCache();
}
