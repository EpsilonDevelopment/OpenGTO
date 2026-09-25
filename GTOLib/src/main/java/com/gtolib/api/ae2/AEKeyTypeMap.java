package com.gtolib.api.ae2;

import appeng.api.stacks.AEKeyType;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class AEKeyTypeMap<T> implements Map<AEKeyType, T> {
   public static final AEKeyType ITEM_TYPE = AEKeyType.items();
   public static final AEKeyType FLUID_TYPE = AEKeyType.fluids();
   public T item;
   public T fluid;
   private AEKeyTypeMap<T>.ItemEntry itemEntry;
   private AEKeyTypeMap<T>.FluidEntry fluidEntry;
   private AEKeyTypeMap<T>.EntryIterator entryIterator;
   private Set<Entry<AEKeyType, T>> entries;

   public AEKeyTypeMap(T itemValue, T fluidValue) {
      this.item = itemValue;
      this.fluid = fluidValue;
   }

   @Override
   public String toString() {
      return "AEKeyTypeMap{" + this.item + ", " + this.fluid + "}";
   }

   @Override
   public int size() {
      boolean hasItem = this.item != null;
      boolean hasFluid = this.fluid != null;
      return hasItem && hasFluid ? 2 : (!hasItem && !hasFluid ? 0 : 1);
   }

   @Override
   public boolean isEmpty() {
      return this.item == null && this.fluid == null;
   }

   @Override
   public boolean containsKey(Object key) {
      if (key == ITEM_TYPE) {
         return this.item != null;
      } else {
         return key == FLUID_TYPE ? this.fluid != null : false;
      }
   }

   @Override
   public boolean containsValue(Object value) {
      return this.item == value || this.fluid == value;
   }

   @Override
   public T get(Object key) {
      if (key == ITEM_TYPE) {
         return this.item;
      } else {
         return key == FLUID_TYPE ? this.fluid : null;
      }
   }

   @Override
   public T getOrDefault(Object key, T defaultValue) {
      if (key == ITEM_TYPE) {
         return this.item != null ? this.item : defaultValue;
      } else if (key == FLUID_TYPE) {
         return this.fluid != null ? this.fluid : defaultValue;
      } else {
         return defaultValue;
      }
   }

   @Override
   public void forEach(BiConsumer<? super AEKeyType, ? super T> action) {
      if (this.item != null) {
         action.accept(ITEM_TYPE, this.item);
      }

      if (this.fluid != null) {
         action.accept(FLUID_TYPE, this.fluid);
      }
   }

   @Nullable
   public T put(AEKeyType key, T value) {
      if (key == ITEM_TYPE) {
         T old = this.item;
         this.item = value;
         return old;
      } else if (key == FLUID_TYPE) {
         T old = this.fluid;
         this.fluid = value;
         return old;
      } else {
         return null;
      }
   }

   @Override
   public T remove(Object key) {
      if (key == ITEM_TYPE) {
         T old = this.item;
         this.item = null;
         return old;
      } else if (key == FLUID_TYPE) {
         T old = this.fluid;
         this.fluid = null;
         return old;
      } else {
         return null;
      }
   }

   @Override
   public void putAll(@NotNull Map<? extends AEKeyType, ? extends T> m) {
      for (Entry<? extends AEKeyType, ? extends T> entry : m.entrySet()) {
         if (entry.getKey() == ITEM_TYPE) {
            this.item = (T)entry.getValue();
         } else if (entry.getKey() == FLUID_TYPE) {
            this.fluid = (T)entry.getValue();
         }
      }
   }

   @Override
   public void clear() {
      this.item = null;
      this.fluid = null;
   }

   @NotNull
   @Override
   public Set<AEKeyType> keySet() {
      boolean hasItem = this.item != null;
      boolean hasFluid = this.fluid != null;
      if (hasItem && hasFluid) {
         return Set.of(ITEM_TYPE, FLUID_TYPE);
      } else {
         return !hasItem && !hasFluid ? Collections.emptySet() : Collections.singleton(hasItem ? ITEM_TYPE : FLUID_TYPE);
      }
   }

   @NotNull
   @Override
   public Collection<T> values() {
      boolean hasItem = this.item != null;
      boolean hasFluid = this.fluid != null;
      if (hasItem && hasFluid) {
         return Set.of(this.item, this.fluid);
      } else {
         return !hasItem && !hasFluid ? Collections.emptyList() : Collections.singleton(hasItem ? this.item : this.fluid);
      }
   }

   @NotNull
   @Override
   public Set<Entry<AEKeyType, T>> entrySet() {
      if (this.entries == null) {
         this.entries = new AEKeyTypeMap.EntrySet();
      }

      return this.entries;
   }

   private class EntryIterator implements Iterator<Entry<AEKeyType, T>> {
      private boolean hasNext;
      private boolean complete;

      @Override
      public boolean hasNext() {
         return this.hasNext;
      }

      public Entry<AEKeyType, T> next() {
         if (this.complete) {
            this.complete = false;
            if (AEKeyTypeMap.this.itemEntry == null) {
               AEKeyTypeMap.this.itemEntry = AEKeyTypeMap.this.new ItemEntry();
            }

            return AEKeyTypeMap.this.itemEntry;
         } else {
            this.hasNext = false;
            if (AEKeyTypeMap.this.fluid != null) {
               if (AEKeyTypeMap.this.fluidEntry == null) {
                  AEKeyTypeMap.this.fluidEntry = AEKeyTypeMap.this.new FluidEntry();
               }

               return AEKeyTypeMap.this.fluidEntry;
            } else {
               if (AEKeyTypeMap.this.itemEntry == null) {
                  AEKeyTypeMap.this.itemEntry = AEKeyTypeMap.this.new ItemEntry();
               }

               return AEKeyTypeMap.this.itemEntry;
            }
         }
      }
   }

   private class EntrySet implements Set<Entry<AEKeyType, T>> {
      @Override
      public int size() {
         boolean hasItem = AEKeyTypeMap.this.item != null;
         boolean hasFluid = AEKeyTypeMap.this.fluid != null;
         return hasItem && hasFluid ? 2 : (!hasItem && !hasFluid ? 0 : 1);
      }

      @Override
      public boolean isEmpty() {
         return AEKeyTypeMap.this.item == null && AEKeyTypeMap.this.fluid == null;
      }

      @Override
      public boolean contains(Object o) {
         throw new UnsupportedOperationException();
      }

      @NotNull
      @Override
      public Iterator<Entry<AEKeyType, T>> iterator() {
         boolean hasItem = AEKeyTypeMap.this.item != null;
         boolean hasFluid = AEKeyTypeMap.this.fluid != null;
         if (!hasItem && !hasFluid) {
            return Collections.emptyIterator();
         }

         if (AEKeyTypeMap.this.entryIterator == null) {
            AEKeyTypeMap.this.entryIterator = AEKeyTypeMap.this.new EntryIterator();
         }

         AEKeyTypeMap.this.entryIterator.hasNext = true;
         AEKeyTypeMap.this.entryIterator.complete = hasItem && hasFluid;
         return AEKeyTypeMap.this.entryIterator;
      }

      @NotNull
      @Override
      public Object[] toArray() {
         throw new UnsupportedOperationException();
      }

      @NotNull
      @Override
      public <O> O[] toArray(@NotNull O[] a) {
         throw new UnsupportedOperationException();
      }

      public boolean add(Entry<AEKeyType, T> entry) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean remove(Object o) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean containsAll(@NotNull Collection<?> c) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean addAll(@NotNull Collection<? extends Entry<AEKeyType, T>> c) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean retainAll(@NotNull Collection<?> c) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean removeAll(@NotNull Collection<?> c) {
         throw new UnsupportedOperationException();
      }

      @Override
      public void clear() {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean equals(Object o) {
         throw new UnsupportedOperationException();
      }

      @Override
      public int hashCode() {
         throw new UnsupportedOperationException();
      }
   }

   private class FluidEntry implements Entry<AEKeyType, T> {
      public AEKeyType getKey() {
         return AEKeyTypeMap.FLUID_TYPE;
      }

      @Override
      public T getValue() {
         return AEKeyTypeMap.this.fluid;
      }

      @Override
      public T setValue(T value) {
         T old = AEKeyTypeMap.this.fluid;
         AEKeyTypeMap.this.fluid = value;
         return old;
      }
   }

   private class ItemEntry implements Entry<AEKeyType, T> {
      public AEKeyType getKey() {
         return AEKeyTypeMap.ITEM_TYPE;
      }

      @Override
      public T getValue() {
         return AEKeyTypeMap.this.item;
      }

      @Override
      public T setValue(T value) {
         T old = AEKeyTypeMap.this.item;
         AEKeyTypeMap.this.item = value;
         return old;
      }
   }
}
