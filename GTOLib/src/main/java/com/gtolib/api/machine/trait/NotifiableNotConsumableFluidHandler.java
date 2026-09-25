package com.gtolib.api.machine.trait;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import java.util.Iterator;
import java.util.List;
import net.minecraftforge.fluids.FluidStack;

public final class NotifiableNotConsumableFluidHandler extends NotifiableFluidTank {
   public NotifiableNotConsumableFluidHandler(MetaMachine machine, int slots, int capacity) {
      super(machine, slots, capacity, IO.IN, IO.NONE);
   }

   @Override
   public boolean isNotConsumable() {
      return true;
   }

   @Override
   public boolean handleRecipeFluid(IO io, GTRecipe recipe, List<Content<FluidIngredient>> fluids, boolean simulate) {
      if (simulate && io == IO.IN && !this.isEmpty()) {
         Iterator<Content<FluidIngredient>> it = fluids.iterator();

         while (it.hasNext()) {
            Content<FluidIngredient> ingredient = it.next();
            if (ingredient.chance == 0) {
               if (ingredient.isEmpty()) {
                  it.remove();
               } else {
                  for (int tank = 0; tank < this.storages.length; tank++) {
                     FluidStack stored = this.getFluidInTank(tank);
                     int count = stored.getAmount();
                     if (count != 0 && ingredient.inner.test(stored)) {
                        ingredient.shrink(count);
                        if (ingredient.amount <= 0L) {
                           it.remove();
                           break;
                        }
                     }
                  }
               }
            }
         }
      }

      return fluids.isEmpty();
   }
}
