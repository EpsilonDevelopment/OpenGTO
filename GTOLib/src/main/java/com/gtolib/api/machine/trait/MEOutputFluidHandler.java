package com.gtolib.api.machine.trait;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.transfer.fluid.ICustomFluidStackHandler;
import com.gregtechceu.gtceu.integration.ae2.utils.KeyStorage;
import com.gregtechceu.gtceu.utils.TaskHandler;
import com.gtocore.common.machine.multiblock.part.ae.StatusTrackedMEPartMachine;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import gto_ae.helpers.facility_management.WorkingStatus;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import org.jetbrains.annotations.NotNull;

public final class MEOutputFluidHandler extends NotifiableRecipeHandlerTrait implements ICustomFluidStackHandler {
   private TickableSubscription updateSubs;
   private final StatusTrackedMEPartMachine machine;
   private final KeyStorage internalBuffer;

   public MEOutputFluidHandler(MetaMachine holder, KeyStorage internalBuffer) {
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
                     MEOutputFluidHandler.this.machine.getThroughputCounter().add(what, add);
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

   public void fillInternal(FluidStack resource, long amount) {
      if (amount >= 1L && !resource.isEmpty()) {
         this.internalBuffer.lock.lock();

         try {
            this.internalBuffer.storage.insert(AEFluidKey.of(resource), amount);
         } finally {
            this.internalBuffer.lock.unlock();
         }
      }
   }

   @Override
   public boolean isInfiniteOutputFluid() {
      return true;
   }

   @Override
   public boolean handleRecipeFluid(IO io, GTRecipe recipe, List<Content<FluidIngredient>> fluids, boolean simulate) {
      if (io == IO.OUT) {
         if (simulate) {
            return true;
         }

         TaskHandler.enqueueAsyncTask(this.machine.getLevel(), () -> this.handle(fluids), 0);
         return true;
      } else {
         throw new IllegalStateException("IO is not the same");
      }
   }

   private void handle(List<Content<FluidIngredient>> fluids) {
      this.internalBuffer.lock.lock();

      try {
         for (Content<FluidIngredient> ingredient : fluids) {
            Fluid f = ingredient.inner.getFluid();
            if (f != null) {
               long amount = ingredient.amount;
               if (amount > 0L) {
                  this.internalBuffer.storage.insert(AEFluidKey.of(f, ingredient.inner.nbt), amount);
               }
            }
         }
      } finally {
         this.internalBuffer.lock.unlock();
      }
   }

   @Override
   public boolean canHandleFluid() {
      return true;
   }

   @Override
   public int getTanks() {
      return 1;
   }

   @NotNull
   @Override
   public FluidStack getFluidInTank(int tank) {
      return FluidStack.EMPTY;
   }

   @Override
   public void setFluidInTank(int tank, @NotNull FluidStack fluidStack) {
   }

   @Override
   public int getTankCapacity(int tank) {
      return Integer.MAX_VALUE;
   }

   @Override
   public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
      return true;
   }

   @Override
   public int fill(FluidStack resource, FluidAction action) {
      int amount = resource.getAmount();
      if (amount > 0 && action.execute()) {
         this.internalBuffer.lock.lock();

         try {
            this.internalBuffer.storage.insert(AEFluidKey.of(resource.getFluid(), resource.getTag()), amount);
            this.machine.onChanged();
         } finally {
            this.internalBuffer.lock.unlock();
         }
      }

      return amount;
   }

   @NotNull
   @Override
   public FluidStack drain(FluidStack fluidStack, FluidAction fluidAction) {
      return FluidStack.EMPTY;
   }

   @NotNull
   @Override
   public FluidStack drain(int i, FluidAction fluidAction) {
      return FluidStack.EMPTY;
   }

   @Override
   public boolean supportsFill(int tank) {
      return true;
   }

   @Override
   public boolean supportsDrain(int tank) {
      return false;
   }
}
