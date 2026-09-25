package com.gtolib.api.machine.trait;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.IRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.ItemIngredient;
import com.gregtechceu.gtceu.utils.function.ObjLongPredicate;
import com.gto.recipesearch.IntLongMap;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import java.util.List;
import java.util.function.ObjLongConsumer;
import lombok.Generated;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class ProxyRecipeHandler extends NotifiableRecipeHandlerTrait {
   protected IRecipeHandlerTrait proxy = null;
   protected ISubscription proxySub = null;
   protected boolean canHandleItem;
   protected boolean canHandleFluid;
   protected boolean isAvailable = true;

   public ProxyRecipeHandler(MetaMachine machine) {
      super(machine);
   }

   public void setProxy(IRecipeHandlerTrait proxy) {
      this.proxy = proxy;
      if (this.proxySub != null) {
         this.proxySub.unsubscribe();
         this.proxySub = null;
      }

      if (proxy != null) {
         this.proxySub = proxy.addChangedListener(this::notifyListeners);
      }
   }

   @Override
   public boolean canHandleItem() {
      return this.canHandleItem;
   }

   @Override
   public boolean canHandleFluid() {
      return this.canHandleFluid;
   }

   @Override
   public boolean isAvailable() {
      return this.isAvailable;
   }

   @Override
   public boolean handleRecipeItem(IO io, GTRecipe recipe, List<Content<ItemIngredient>> items, boolean simulate) {
      return this.proxy == null ? false : this.proxy.handleRecipeItem(io, recipe, items, simulate);
   }

   @Override
   public boolean handleRecipeFluid(IO io, GTRecipe recipe, List<Content<FluidIngredient>> fluids, boolean simulate) {
      return this.proxy == null ? false : this.proxy.handleRecipeFluid(io, recipe, fluids, simulate);
   }

   @Override
   public boolean forEachFluids(ObjLongPredicate<FluidStack> function) {
      return this.proxy == null ? false : this.proxy.forEachFluids(function);
   }

   @Override
   public void fastForEachFluids(ObjLongConsumer<FluidStack> function) {
      if (this.proxy != null) {
         this.proxy.fastForEachFluids(function);
      }
   }

   @Override
   public boolean forEachItems(ObjLongPredicate<ItemStack> function) {
      return this.proxy == null ? false : this.proxy.forEachItems(function);
   }

   @Override
   public void fastForEachItems(ObjLongConsumer<ItemStack> function) {
      if (this.proxy != null) {
         this.proxy.fastForEachItems(function);
      }
   }

   @Override
   public IntLongMap getSearchMap(@NotNull GTRecipeType type) {
      return this.proxy == null ? IntLongMap.EMPTY : this.proxy.getSearchMap(type);
   }

   @Override
   public boolean isNotConsumable() {
      return this.proxy == null ? true : this.proxy.isNotConsumable();
   }

   @Override
   public boolean test(Object ingredient) {
      return this.proxy == null ? false : this.proxy.test(ingredient);
   }

   @Override
   public IO getHandlerIO() {
      return IO.IN;
   }

   @Generated
   public ProxyRecipeHandler setCanHandleItem(boolean canHandleItem) {
      this.canHandleItem = canHandleItem;
      return this;
   }

   @Generated
   public ProxyRecipeHandler setCanHandleFluid(boolean canHandleFluid) {
      this.canHandleFluid = canHandleFluid;
      return this;
   }

   @Generated
   public ProxyRecipeHandler setAvailable(boolean isAvailable) {
      this.isAvailable = isAvailable;
      return this;
   }
}
