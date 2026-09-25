package com.gtolib.api.machine.feature.multiblock;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gtolib.api.item.MachineItemStackHandler;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Size;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.item.ItemStack;

public interface IStorageMultiblock extends IMachineLife {
   NotifiableItemStackHandler getMachineStorage();

   default NotifiableItemStackHandler createMachineStorage(@Nullable Predicate<ItemStack> filter) {
      NotifiableItemStackHandler storage = new NotifiableItemStackHandler(
         this.self(), 1, IO.NONE, IO.BOTH, slots -> new MachineItemStackHandler(this::getSlotLimit)
      );
      storage.setFilter(i -> filter != null && !filter.test(i) ? false : this.storageFilter(i));
      storage.addChangedListener(this::onMachineChanged);
      return storage;
   }

   default int getSlotLimit() {
      return 64;
   }

   default boolean storageFilter(ItemStack itemStack) {
      return true;
   }

   default void onMachineChanged() {
   }

   default boolean isEmpty() {
      return this.getMachineStorage().isEmpty();
   }

   default ItemStack getStorageStack() {
      return this.getMachineStorage().getStackInSlot(0);
   }

   default Widget createUIWidget(Widget widget) {
      if (widget instanceof WidgetGroup group) {
         Size size = group.getSize();
         int var10005 = size.width - 30;
         int var10006 = size.height - 30;
         group.addWidget(new SlotWidget(this.getMachineStorage().storage, 0, var10005, var10006, true, true).setBackground(GuiTextures.SLOT));
      }

      return widget;
   }

   @Override
   default void onMachineRemoved() {
      this.self().clearInventory(this.getMachineStorage().storage);
   }
}
