package com.gtolib.api.machine.part;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.gtolib.api.item.MachineItemStackHandler;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import lombok.Generated;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemPartMachine extends MultiblockPartMachine implements IMachineLife {
   @SyncToClient
   @SaveToDisk
   protected NotifiableItemStackHandler inventory;
   private final int limit;

   public ItemPartMachine(MetaMachineBlockEntity holder, int limit, @Nullable Predicate<ItemStack> filter) {
      super(holder);
      this.limit = limit;
      this.inventory = this.createMachineStorage(filter);
   }

   protected NotifiableItemStackHandler createMachineStorage(@Nullable Predicate<ItemStack> filter) {
      NotifiableItemStackHandler storage = new NotifiableItemStackHandler(this, 1, IO.NONE, IO.BOTH, slots -> new MachineItemStackHandler(this::getSlotLimit));
      storage.setFilter(i -> filter != null && !filter.test(i) ? false : this.storageFilter(i));
      storage.addChangedListener(this::onMachineChanged);
      return storage;
   }

   protected int getSlotLimit() {
      return this.limit;
   }

   protected boolean storageFilter(ItemStack itemStack) {
      return true;
   }

   protected void onMachineChanged() {
   }

   public static Widget createSLOTWidget(NotifiableItemStackHandler inventory) {
      WidgetGroup group = new WidgetGroup(0, 0, 34, 34);
      WidgetGroup container = new WidgetGroup(4, 4, 26, 26);
      container.addWidget(new SlotWidget(inventory.storage, 0, 4, 4, true, true).setBackground(GuiTextures.SLOT));
      group.addWidget(container);
      return group;
   }

   @Override
   public Widget createUIWidget() {
      return createSLOTWidget(this.getInventory());
   }

   @Override
   public void onMachineRemoved() {
      this.clearInventory(this.getInventory().storage);
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
