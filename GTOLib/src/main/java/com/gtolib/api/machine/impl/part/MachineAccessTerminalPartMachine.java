package com.gtolib.api.machine.impl.part;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import lombok.Generated;
import net.minecraft.world.item.ItemStack;

public final class MachineAccessTerminalPartMachine extends MultiblockPartMachine {
   @SaveToDisk
   private final NotifiableItemStackHandler inventory = new NotifiableItemStackHandler(this, 64, IO.NONE, IO.NONE, CustomItemStackHandler::new);

   public MachineAccessTerminalPartMachine(MetaMachineBlockEntity holder) {
      super(holder);
      this.inventory.setFilter(this::storageFilter);
   }

   private boolean storageFilter(ItemStack itemStack) {
      if (itemStack.getItem() instanceof MetaMachineItem metaMachineItem) {
         MachineDefinition definition = metaMachineItem.getDefinition();
         if (definition instanceof MultiblockMachineDefinition) {
            return false;
         }

         GTRecipeType[] recipeTypes = definition.getRecipeTypes();
         if (recipeTypes != null && recipeTypes.length == 1) {
            return "electric".equals(recipeTypes[0].group);
         }
      }

      return false;
   }

   @Override
   public Widget createUIWidget() {
      int rowSize = 8;
      WidgetGroup group = new WidgetGroup(0, 0, 18 * rowSize + 16, 18 * rowSize + 16);
      WidgetGroup container = new WidgetGroup(4, 4, 18 * rowSize + 8, 18 * rowSize + 8);
      int index = 0;

      for (int y = 0; y < rowSize; y++) {
         for (int x = 0; x < rowSize; x++) {
            container.addWidget(
               new SlotWidget(this.inventory.storage, index++, 4 + x * 18, 4 + y * 18, true, true)
                  .setBackgroundTexture(GuiTextures.SLOT)
                  .setIngredientIO(IngredientIO.INPUT)
            );
         }
      }

      container.setBackground(GuiTextures.BACKGROUND_INVERSE);
      group.addWidget(container);
      return group;
   }

   @Override
   public boolean canShared() {
      return false;
   }

   @Generated
   public NotifiableItemStackHandler getInventory() {
      return this.inventory;
   }
}
