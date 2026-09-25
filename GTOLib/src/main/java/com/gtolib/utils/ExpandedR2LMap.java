package com.gtolib.utils;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.objects.Reference2LongMap;
import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;
import java.util.Iterator;
import org.jetbrains.annotations.NotNull;

public class ExpandedR2LMap<K> extends Reference2LongOpenHashMap<K> implements Iterable<Reference2LongMap.Entry<K>> {
   public ExpandedR2LMap() {
      super(16, 0.75F);
   }

   public ExpandedR2LMap(int size) {
      super(size, 0.75F);
   }

   @NotNull
   @Override
   public Iterator<Entry<K>> iterator() {
      return this.reference2LongEntrySet().fastIterator();
   }

   @Override
   public long addTo(K k, long incr) {
      if (k == null) {
         return 0L;
      }

      K[] key = this.key;
      int pos;
      K curr;
      if ((curr = key[pos = HashCommon.mix(System.identityHashCode(k)) & this.mask]) != null) {
         do {
            if (curr == k) {
               long oldValue = this.value[pos];
               long newValue = oldValue + incr;
               if (newValue < 0L && incr >= 0L && oldValue >= 0L) {
                  this.value[pos] = Long.MAX_VALUE;
               } else {
                  this.value[pos] = newValue;
               }

               return oldValue;
            }
         } while ((curr = key[pos = pos + 1 & this.mask]) != null);
      }

      key[pos] = k;
      this.value[pos] = incr;
      if (this.size++ >= this.maxFill) {
         this.rehash(HashCommon.arraySize(this.size + 1, this.f));
      }

      return 0L;
   }

   public void ensureCapacity(int capacity) {
      int needed = (int)Math.min(1073741824L, Math.max(2L, HashCommon.nextPowerOfTwo((long)Math.ceil((capacity + this.size) / this.f))));
      if (needed > this.n) {
         this.rehash(needed);
      }
   }

   public void reset() {
      int i = 0;

      for (int len = this.value.length; i < len; i++) {
         this.value[i] = 0L;
      }
   }
}
