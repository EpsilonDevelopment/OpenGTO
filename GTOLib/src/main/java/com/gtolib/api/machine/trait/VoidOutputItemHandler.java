package com.gtolib.api.machine.trait;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.ingredient.ItemIngredient;
import com.gregtechceu.gtceu.api.transfer.item.ICustomItemStackHandler;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import lombok.Generated;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class VoidOutputItemHandler extends NotifiableRecipeHandlerTrait implements ICustomItemStackHandler {
   private Predicate<ItemStack> filter = GTUtil.FAVORABLE;

   public VoidOutputItemHandler(MetaMachine holder) {
      super(holder);
   }

   @Override
   public boolean canHandleItem() {
      return true;
   }

   @Override
   public IO getHandlerIO() {
      return IO.OUT;
   }

   @Override
   public ISubscription addChangedListener(Runnable listener) {
      return () -> {};
   }

   @Override
   public boolean isInfiniteOutputItem() {
      return this.filter == GTUtil.FAVORABLE;
   }

   @Override
   public boolean handleRecipeItem(IO io, GTRecipe recipe, List<Content<ItemIngredient>> items, boolean simulate) {
      if (io != IO.OUT) {
         throw new IllegalStateException("IO is not the same");
      }

      if (this.filter == GTUtil.FAVORABLE) {
         return true;
      }

      Iterator<Content<ItemIngredient>> it = items.iterator();

      while (it.hasNext()) {
         Content<ItemIngredient> ingredient = it.next();
         if (ingredient.isEmpty()) {
            it.remove();
         } else {
            ItemStack stack = ingredient.inner.getInnerItemStack();
            if (stack.isEmpty() || this.filter.test(stack)) {
               it.remove();
            }
         }
      }

      return items.isEmpty();
   }

   @Override
   public int getSlots() {
      return 1;
   }

   @Override
   public int getSlotLimit(int slot) {
      return Integer.MAX_VALUE;
   }

   @Override
   public boolean isItemValid(int i, @NotNull ItemStack itemStack) {
      return true;
   }

   @NotNull
   @Override
   public ItemStack getStackInSlot(int slot) {
      return ItemStack.EMPTY;
   }

   @Override
   public void setStackInSlot(int slot, @NotNull ItemStack stack) {
   }

   @NotNull
   @Override
   public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
      return ItemStack.EMPTY;
   }

   @NotNull
   @Override
   public ItemStack extractItem(int slot, int amount, boolean simulate) {
      return ItemStack.EMPTY;
   }

   @Generated
   public void setFilter(Predicate<ItemStack> filter) {
      this.filter = filter;
   }
}
