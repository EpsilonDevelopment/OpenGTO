package com.gtolib.api.machine.trait;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.ingredient.ItemIngredient;
import com.gregtechceu.gtceu.api.transfer.item.ICustomItemStackHandler;
import com.gregtechceu.gtceu.integration.ae2.utils.KeyStorage;
import com.gregtechceu.gtceu.utils.TaskHandler;
import com.gtocore.common.machine.multiblock.part.ae.StatusTrackedMEPartMachine;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import gto_ae.helpers.facility_management.WorkingStatus;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class MEOutputItemHandler extends NotifiableRecipeHandlerTrait implements ICustomItemStackHandler {
   private TickableSubscription updateSubs;
   private final StatusTrackedMEPartMachine machine;
   private final KeyStorage internalBuffer;

   public MEOutputItemHandler(MetaMachine holder, KeyStorage internalBuffer) {
      super(holder);
      this.machine = (StatusTrackedMEPartMachine)holder;
      this.internalBuffer = internalBuffer;
   }

   public void updateAutoOutputSubscription() {
      if (this.machine.isWorkingEnabled() && this.machine.isOnline()) {
         this.updateSubs = this.getMachine().subscribeServerTick(this.updateSubs, this::updateTick, 20);
      } else if (this.updateSubs != null) {
         this.machine.setStatus(WorkingStatus.IDLE);
         this.updateSubs.unsubscribe();
         this.updateSubs = null;
      }
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
   public void onMachineLoad() {
      super.onMachineLoad();
      if (this.machine.getLevel() instanceof ServerLevel serverLevel) {
         TaskHandler.enqueueTask(serverLevel, this::updateAutoOutputSubscription);
      }
   }

   @Override
   public void onMachineUnLoad() {
      super.onMachineUnLoad();
      if (this.updateSubs != null) {
         this.updateSubs.unsubscribe();
         this.updateSubs = null;
      }
   }

   private void updateTick() {
      if (this.machine.isWorkingEnabled() && !this.internalBuffer.isEmpty() && this.machine.updateMEStatus() && this.internalBuffer.lock.tryLock()) {
         try {
            final IGrid grid = this.machine.getMainNode().getGrid();
            if (grid != null) {
               this.internalBuffer.insertInventory(new MEStorage() {
                  @Override
                  public Component getDescription() {
                     return null;
                  }

                  @Override
                  public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
                     long add = grid.getStorageService().getInventory().insert(what, amount, mode, source);
                     MEOutputItemHandler.this.machine.getThroughputCounter().add(what, add);
                     return add;
                  }
               }, this.machine.getActionSource());
            }
         } finally {
            this.internalBuffer.lock.unlock();
         }
      }

      this.machine.setStatus(this.machine.getThroughputCounter().map.isEmpty() ? WorkingStatus.IDLE : WorkingStatus.WORKING);
      this.machine.getThroughputCounter().tickRefresh();
   }

   @Override
   public boolean isInfiniteOutputItem() {
      return true;
   }

   @Override
   public boolean handleRecipeItem(IO io, GTRecipe recipe, List<Content<ItemIngredient>> items, boolean simulate) {
      if (io == IO.OUT) {
         if (simulate) {
            return true;
         }

         TaskHandler.enqueueAsyncTask(this.machine.getLevel(), () -> this.handle(items), 0);
         return true;
      } else {
         throw new IllegalStateException("IO is not the same");
      }
   }

   private void handle(List<Content<ItemIngredient>> items) {
      this.internalBuffer.lock.lock();

      try {
         for (Content<ItemIngredient> ingredient : items) {
            if (!ingredient.isEmpty()) {
               long amount = ingredient.amount;
               if (amount > 0L) {
                  this.internalBuffer.storage.insert(AEItemKey.of(ingredient.inner.getInnerItemStack()), amount);
               }
            }
         }
      } finally {
         this.internalBuffer.lock.unlock();
      }
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
      int count = stack.getCount();
      if (count > 0 && !simulate) {
         this.internalBuffer.lock.lock();

         try {
            this.internalBuffer.storage.insert(AEItemKey.of(stack), count);
            this.machine.onChanged();
         } finally {
            this.internalBuffer.lock.unlock();
         }
      }

      return ItemStack.EMPTY;
   }

   @NotNull
   @Override
   public ItemStack extractItem(int slot, int amount, boolean simulate) {
      return ItemStack.EMPTY;
   }
}
