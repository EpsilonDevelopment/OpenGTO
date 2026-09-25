package com.gtolib.api.machine.trait;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.transfer.fluid.ICustomFluidStackHandler;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import lombok.Generated;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import org.jetbrains.annotations.NotNull;

public final class VoidOutputFluidHandler extends NotifiableRecipeHandlerTrait implements ICustomFluidStackHandler {
   private Predicate<FluidStack> filter = GTUtil.FAVORABLE;

   public VoidOutputFluidHandler(MetaMachine holder) {
      super(holder);
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
   public boolean isInfiniteOutputFluid() {
      return this.filter == GTUtil.FAVORABLE;
   }

   @Override
   public boolean handleRecipeFluid(IO io, GTRecipe recipe, List<Content<FluidIngredient>> fluids, boolean simulate) {
      if (io != IO.OUT) {
         throw new IllegalStateException("IO is not the same");
      }

      if (this.filter == GTUtil.FAVORABLE) {
         return true;
      }

      Iterator<Content<FluidIngredient>> it = fluids.iterator();

      while (it.hasNext()) {
         Content<FluidIngredient> ingredient = it.next();
         if (ingredient.isEmpty()) {
            it.remove();
         } else {
            FluidStack stack = ingredient.inner.getFluidStack();
            if (stack.isEmpty() || this.filter.test(stack)) {
               it.remove();
            }
         }
      }

      return fluids.isEmpty();
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
      return resource.getAmount();
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

   @Generated
   public void setFilter(Predicate<FluidStack> filter) {
      this.filter = filter;
   }
}
