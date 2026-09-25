package com.gtolib.mixin.emi;

import com.gto.fastcollection.fastutil.OpenCacheHashSet;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.registry.EmiStackList;
import dev.emi.emi.runtime.EmiFavorite;
import dev.emi.emi.runtime.EmiFavorite.Craftable;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EmiPlayerInventory.class)
public abstract class EmiPlayerInventoryMixin {
   @Shadow(remap = false)
   public Map<EmiStack, EmiStack> inventory;
   @Shadow(remap = false)
   @Final
   private Comparison strict;

   @Shadow(remap = false)
   public abstract Predicate<EmiRecipe> getPredicate();

   @Overwrite(remap = false)
   public List<EmiIngredient> getCraftables() {
      Predicate var1 = this.getPredicate();
      if (var1 == null) {
         return Collections.emptyList();
      }

      OpenCacheHashSet<EmiRecipe> var2 = new OpenCacheHashSet<>(this.inventory.size());

      for (EmiStack var4 : this.inventory.keySet()) {
         var2.addAll(EmiApi.getRecipeManager().getRecipesByInput(var4));
      }

      return var2.stream()
         .filter(var1x -> !var1x.hideCraftable() && !var1x.getOutputs().isEmpty() && var1.test(var1x))
         .map(Craftable::new)
         .sorted(Comparator.<Craftable>comparingInt(var0 -> EmiStackList.getIndex(var0.getStack())).thenComparingLong(EmiFavorite::getAmount))
         .collect(Collectors.toList());
   }

   @Overwrite(remap = false)
   public List<Boolean> getCraftAvailability(EmiRecipe var1) {
      Object2LongOpenHashMap var2 = new Object2LongOpenHashMap();
      ArrayList var3 = new ArrayList();

      label25:
      for (EmiIngredient var5 : var1.getInputs()) {
         for (EmiStack var7 : var5.getEmiStacks()) {
            long var8 = var7.getAmount();
            EmiStack var10 = this.inventory.get(var7);
            if (var10 != null) {
               long var11 = var2.getLong(var10);
               long var13 = var10.getAmount() - var11;
               if (var13 >= var8) {
                  var2.put(var10, var8 + var11);
                  var3.add(true);
                  continue label25;
               }
            }
         }

         var3.add(false);
      }

      return var3;
   }

   @Overwrite(remap = false)
   public boolean canCraft(EmiRecipe var1, long var2) {
      Object2LongOpenHashMap var4 = new Object2LongOpenHashMap();

      label31:
      for (EmiIngredient var6 : var1.getInputs()) {
         if (!var6.isEmpty()) {
            for (EmiStack var8 : var6.getEmiStacks()) {
               long var9 = var8.getAmount() * var2;
               EmiStack var11 = this.inventory.get(var8);
               if (var11 != null) {
                  long var12 = var4.getLong(var11);
                  long var14 = var11.getAmount() - var12;
                  if (var14 >= var9) {
                     var4.put(var11, var9 + var12);
                     continue label31;
                  }
               }
            }

            return false;
         }
      }

      return true;
   }

   @Overwrite(remap = false)
   public boolean isEqual(EmiPlayerInventory var1) {
      if (var1 == null) {
         return false;
      }

      Comparison var2 = Comparison.of((var1x, var2x) -> this.strict.compare(var1x, var2x) && var1x.getAmount() == var2x.getAmount());
      if (var1.inventory.size() != this.inventory.size()) {
         return false;
      }

      for (EmiStack var4 : this.inventory.keySet()) {
         EmiStack var5 = var1.inventory.get(var4);
         if (var5 == null || !var5.isEqual(var4, var2)) {
            return false;
         }
      }

      return true;
   }
}
