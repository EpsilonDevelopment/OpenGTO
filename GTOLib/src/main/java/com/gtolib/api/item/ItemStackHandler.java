package com.gtolib.api.item;

import com.gregtechceu.gtceu.api.transfer.item.ICustomItemStackHandler;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemStackHandler implements ICustomItemStackHandler {
   private ItemStack itemStack;

   public ItemStackHandler(ItemStack stack) {
      this.itemStack = stack;
   }

   @Override
   public void setStackInSlot(int slot, @NotNull ItemStack stack) {
      this.itemStack = stack;
   }

   @Override
   public int getSlots() {
      return 1;
   }

   @NotNull
   @Override
   public ItemStack getStackInSlot(int slot) {
      return this.itemStack;
   }

   @NotNull
   @Override
   public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
      if (this.itemStack.is(stack.getItem())) {
         this.itemStack.setCount(this.itemStack.getCount() + stack.getCount());
      } else {
         this.itemStack = stack;
      }

      return ItemStack.EMPTY;
   }

   @NotNull
   @Override
   public ItemStack extractItem(int slot, int amount, boolean simulate) {
      int count = this.itemStack.getCount() - amount;
      if (count < 0) {
         return ItemStack.EMPTY;
      }

      ItemStack stack = this.itemStack.copyWithCount(count);
      if (count == 0) {
         this.itemStack = ItemStack.EMPTY;
      } else {
         this.itemStack.setCount(count);
      }

      return stack;
   }

   @Override
   public int getSlotLimit(int slot) {
      return Integer.MAX_VALUE;
   }

   @Override
   public boolean isItemValid(int slot, @NotNull ItemStack stack) {
      return this.itemStack.isEmpty();
   }
}
