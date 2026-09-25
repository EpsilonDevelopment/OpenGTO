package com.gtolib.api.machine.trait;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.ingredient.ItemIngredient;
import com.gregtechceu.gtceu.utils.function.ObjLongPredicate;
import com.gto.recipesearch.IntLongMap;
import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.GTOItems;
import java.util.Iterator;
import java.util.List;
import java.util.function.ObjLongConsumer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class NotifiableCatalystHandler extends NotifiableNotConsumableItemHandler {
   private final boolean damage;

   public NotifiableCatalystHandler(MetaMachine machine, int slots, boolean damage) {
      super(machine, slots, IO.BOTH);
      this.damage = damage;
      this.setFilter(i -> ChemicalHelper.getPrefix(i.getItem()) == GTOTagPrefix.CATALYST || i.is((Item)GTOItems.CATALYST_BASE.get()));
      if (!damage) {
         this.storage.isInputLimited = true;
      }
   }

   @Override
   public boolean isNotConsumable() {
      return false;
   }

   @Override
   public boolean forEachItems(ObjLongPredicate<ItemStack> function) {
      for (int i = 0; i < this.storage.size; i++) {
         ItemStack stack = this.storage.stacks[i];
         int amount = stack.getCount();
         if (amount > 0) {
            if (this.damage) {
               long damage = 10000 - stack.getDamageValue();
               if (function.test(stack, damage * damage)) {
                  return true;
               }
            } else if (function.test(stack, Long.MAX_VALUE)) {
               return true;
            }
         }
      }

      return false;
   }

   @Override
   public void fastForEachItems(ObjLongConsumer<ItemStack> function) {
      for (int i = 0; i < this.storage.size; i++) {
         ItemStack stack = this.storage.stacks[i];
         int amount = stack.getCount();
         if (amount > 0) {
            if (this.damage) {
               long damage = 10000 - stack.getDamageValue();
               function.accept(stack, damage * damage);
            } else {
               function.accept(stack, Long.MAX_VALUE);
            }
         }
      }
   }

   @Override
   public void fillSearchMap(@NotNull GTRecipeType type, @NotNull IntLongMap map) {
      for (int i = 0; i < this.storage.size; i++) {
         ItemStack stack = this.storage.stacks[i];
         int amount = stack.getCount();
         if (amount > 0) {
            if (this.damage) {
               long damage = 10000 - stack.getDamageValue();
               type.convertItem(stack, damage * damage, map);
            } else {
               type.convertItem(stack, Long.MAX_VALUE, map);
            }
         }
      }
   }

   @Override
   public boolean handleRecipeItem(IO io, GTRecipe recipe, List<Content<ItemIngredient>> items, boolean simulate) {
      if (io == IO.IN && !this.isEmpty()) {
         Iterator<Content<ItemIngredient>> it = items.iterator();

         while (it.hasNext()) {
            Content<ItemIngredient> ingredient = it.next();
            if (ingredient.isEmpty()) {
               it.remove();
            } else {
               int slots = this.storage.getSlots();

               for (int slot = 0; slot < slots; slot++) {
                  ItemStack stored = this.storage.getStackInSlot(slot);
                  if (ingredient.inner.test(stored)) {
                     if (!this.damage) {
                        it.remove();
                        break;
                     }

                     int damageValue = stored.getDamageValue();
                     if (damageValue > 9999) {
                        this.storage.setStackInSlot(slot, GTOItems.CATALYST_BASE.asStack());
                     } else {
                        long damage = 10000 - damageValue;
                        long amount = Math.min(ingredient.amount, damage * damage);
                        if (!simulate) {
                           stored.setDamageValue(damageValue + (int)(Math.sqrt(amount) + 0.5));
                        }

                        ingredient.shrink(amount);
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
