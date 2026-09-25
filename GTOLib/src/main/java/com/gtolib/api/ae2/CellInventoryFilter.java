package com.gtolib.api.ae2;

import appeng.api.inventories.InternalInventory;
import appeng.api.storage.StorageCells;
import appeng.util.inv.filter.IAEItemFilter;
import net.minecraft.world.item.ItemStack;

public class CellInventoryFilter implements IAEItemFilter {
   private final Runnable runnable;

   public CellInventoryFilter(Runnable runnable) {
      this.runnable = runnable;
   }

   @Override
   public boolean allowExtract(InternalInventory inv, int slot, int amount) {
      this.runnable.run();
      return true;
   }

   @Override
   public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
      return StorageCells.getHandler(stack) != null;
   }
}
