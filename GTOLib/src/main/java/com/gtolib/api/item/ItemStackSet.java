package com.gtolib.api.item;

import com.gto.datasynclib.util.ItemStackHashStrategy;
import com.gto.fastcollection.fastutil.O2OOpenCustomCacheHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class ItemStackSet implements Set<ItemStack> {
   private final Object2ObjectOpenCustomHashMap<ItemStack, ItemStack> map = new O2OOpenCustomCacheHashMap<>(ItemStackHashStrategy.ITEM_AND_TAG);

   @Override
   public int size() {
      return this.map.size();
   }

   @Override
   public boolean isEmpty() {
      return this.map.isEmpty();
   }

   @Override
   public boolean contains(Object o) {
      return o instanceof ItemStack stack ? this.map.containsKey(stack) : false;
   }

   @NotNull
   @Override
   public Iterator<ItemStack> iterator() {
      return this.map.values().iterator();
   }

   @NotNull
   @Override
   public Object[] toArray() {
      return this.map.values().toArray();
   }

   @NotNull
   @Override
   public <T> T[] toArray(@NotNull T[] a) {
      return (T[])this.map.values().toArray(a);
   }

   public boolean add(ItemStack itemStack) {
      if (itemStack.isEmpty()) {
         return false;
      }

      ItemStack stack = this.map.get(itemStack);
      if (stack == null) {
         stack = itemStack.copy();
         this.map.put(stack, stack);
      } else {
         stack.setCount(stack.getCount() + itemStack.getCount());
      }

      return true;
   }

   @Override
   public boolean remove(Object o) {
      return false;
   }

   @Override
   public boolean containsAll(@NotNull Collection<?> c) {
      return false;
   }

   @Override
   public boolean addAll(@NotNull Collection<? extends ItemStack> c) {
      c.forEach(this::add);
      return true;
   }

   @Override
   public boolean retainAll(@NotNull Collection<?> c) {
      return false;
   }

   @Override
   public boolean removeAll(@NotNull Collection<?> c) {
      return false;
   }

   @Override
   public void clear() {
      this.map.clear();
   }
}
