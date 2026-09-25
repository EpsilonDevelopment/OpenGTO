package com.gtolib.api.machine.trait;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.ingredient.ItemIngredient;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import java.util.Iterator;
import java.util.List;
import java.util.function.IntFunction;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class NotifiableNotConsumableItemHandler extends NotifiableItemStackHandler {
   NotifiableNotConsumableItemHandler(MetaMachine machine, int slots, @NotNull IO capabilityIO, IntFunction<CustomItemStackHandler> storageFactory) {
      super(machine, slots, IO.IN, capabilityIO, storageFactory);
   }

   public NotifiableNotConsumableItemHandler(MetaMachine machine, int slots, @NotNull IO capabilityIO) {
      this(machine, slots, capabilityIO, CustomItemStackHandler::new);
   }

   @Override
   public boolean isNotConsumable() {
      return true;
   }

   @Override
   public void exportToNearby(@NotNull Direction... facings) {
   }

   @Override
   public void importFromNearby(@NotNull Direction... facings) {
   }

   @Override
   public boolean handleRecipeItem(IO io, GTRecipe recipe, List<Content<ItemIngredient>> items, boolean simulate) {
      if (simulate && io == IO.IN && !this.isEmpty()) {
         Iterator<Content<ItemIngredient>> it = items.iterator();

         while (it.hasNext()) {
            Content<ItemIngredient> ingredient = it.next();
            if (ingredient.chance == 0) {
               if (ingredient.isEmpty()) {
                  it.remove();
               } else {
                  int slots = this.storage.getSlots();

                  for (int slot = 0; slot < slots; slot++) {
                     ItemStack stored = this.storage.getStackInSlot(slot);
                     int count = stored.getCount();
                     if (count != 0 && ingredient.inner.test(stored)) {
                        ingredient.shrink(count);
                        if (ingredient.amount < 1L) {
                           it.remove();
                           break;
                        }
                     }
                  }
               }
            }
         }
      }

      return items.isEmpty();
   }
}
