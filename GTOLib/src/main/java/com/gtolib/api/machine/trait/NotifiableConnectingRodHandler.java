package com.gtolib.api.machine.trait;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.ingredient.ItemIngredient;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.utils.function.ObjLongPredicate;
import com.gtocore.api.data.tag.GTOTagPrefix;
import java.util.Iterator;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class NotifiableConnectingRodHandler extends NotifiableNotConsumableItemHandler {
   @Nullable
   private Material rodMaterial;
   private boolean rodMaterialInitialized;

   public NotifiableConnectingRodHandler(MetaMachine machine) {
      super(machine, 1, IO.BOTH);
      this.setFilter(i -> ChemicalHelper.getPrefix(i.getItem()) == GTOTagPrefix.CONNECTING_ROD);
   }

   @Nullable
   public Material getRodMaterial() {
      if (!this.rodMaterialInitialized) {
         this.refreshRodMaterial();
      }

      return this.rodMaterial;
   }

   @Override
   public void onContentsChanged() {
      this.refreshRodMaterial();
      super.onContentsChanged();
   }

   private void refreshRodMaterial() {
      this.rodMaterial = this.storage.stacks[0] != null ? ChemicalHelper.getMaterialStack(this.storage.stacks[0].getItem()).material() : null;
      if (this.rodMaterial == GTMaterials.NULL) {
         this.rodMaterial = null;
      }

      this.rodMaterialInitialized = true;
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
         if (amount > 0 && function.test(stack, Long.MAX_VALUE)) {
            return true;
         }
      }

      return false;
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
                     int damageValue = stored.getDamageValue();
                     if (damageValue <= stored.getMaxDamage() - 1) {
                        if (!simulate) {
                           stored.setDamageValue(damageValue + 1);
                        }

                        it.remove();
                        break;
                     }

                     this.storage.setStackInSlot(slot, ItemStack.EMPTY);
                  }
               }
            }
         }
      }

      return items.isEmpty();
   }
}
