package com.gtolib.api.recipe.lookup;

import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntCircuitIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.ItemIngredient;
import com.gto.recipesearch.IntLongMap;
import com.gtolib.api.fluid.IFluid;
import com.gtolib.api.item.IItem;
import com.gtolib.api.recipe.RecipeType;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Ingredient.ItemValue;
import net.minecraft.world.item.crafting.Ingredient.TagValue;
import net.minecraft.world.item.crafting.Ingredient.Value;
import net.minecraft.world.level.material.Fluid;

public final class MapIngredient {
   public static Reference2ObjectOpenHashMap<IItem, Set<Object>> INGREDIENT_BUILDER = new Reference2ObjectOpenHashMap<>();
   public static Reference2IntOpenHashMap<Object> COUNT_MAP = new Reference2IntOpenHashMap<>();
   private static final AtomicInteger COUNT = new AtomicInteger();
   private static final int[] CIRCUIT_INPUTS = new int[33];
   public static final IngredientConverter<Ingredient> INGREDIENT_CONVERTER;
   public static final IngredientConverter<ItemStack> ITEM_CONVERTER;

   public static int getCount(Object object) {
      if (object == null) {
         return COUNT.incrementAndGet();
      }

      synchronized (CIRCUIT_INPUTS) {
         return COUNT_MAP.computeIfAbsent(object, k -> COUNT.incrementAndGet());
      }
   }

   public static void convert(RecipeType type, IntLongMap intMap, Object obj) {
      if (obj instanceof FluidIngredient ingredient) {
         Fluid f = ingredient.getFluid();
         if (f != null) {
            int in = ((IFluid)f).gtolib$getOrCreateMapFluid();
            intMap.add(in, ingredient.amount);
         }
      } else if (obj instanceof ItemIngredient ingredient) {
         if (ingredient instanceof IntCircuitIngredient circuitIngredient) {
            int in = CIRCUIT_INPUTS[circuitIngredient.configuration];
            intMap.add(in, 1L);
         } else {
            type.convertIngredient(ingredient.inner, ingredient.amount, intMap);
         }
      }
   }

   static {
      for (int i = 0; i < 33; i++) {
         CIRCUIT_INPUTS[i] = getCount(null);
      }

      INGREDIENT_CONVERTER = (ingredient, amount, map) -> {
         for (Value value : ingredient.values) {
            if (!(value instanceof TagValue tagValue)) {
               if (value instanceof ItemValue itemValue) {
                  Item item = itemValue.item.getItem();
                  INGREDIENT_BUILDER.computeIfAbsent((IItem)item, k -> new ReferenceOpenHashSet<>()).add(item);
                  int in = getCount(item);
                  map.add(in, amount);
               }
            } else {
               TagKey<Item> tagkey = tagValue.tag;

               for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(tagkey)) {
                  INGREDIENT_BUILDER.computeIfAbsent((IItem)holder.value(), k -> new ReferenceOpenHashSet<>()).add(tagkey);
               }

               int in = getCount(tagkey);
               map.add(in, amount);
            }
         }
      };
      ITEM_CONVERTER = (stack, amount, map) -> {
         Item item = stack.getItem();
         CompoundTag nbt = stack.getTag();
         if (nbt != null && item == IntCircuitIngredient.PROGRAMMED_CIRCUIT && nbt.tags.get("Configuration") instanceof IntTag intTag) {
            map.add(CIRCUIT_INPUTS[intTag.getAsInt()], amount);
         } else {
            int[] arr = ((IItem)item).gtolib$getMapItem();
            if (arr != null) {
               for (int ix : arr) {
                  map.add(ix, amount);
               }
            }
         }
      };
   }
}
