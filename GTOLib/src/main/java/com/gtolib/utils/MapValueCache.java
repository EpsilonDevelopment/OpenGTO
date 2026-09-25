package com.gtolib.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public final class MapValueCache<K, V> extends ConcurrentHashMap<K, V> {
   private final Function<K, V> mapFunction;

   public MapValueCache(Function<K, V> mapFunction) {
      this.mapFunction = mapFunction;
   }

   public V getCache(K k) {
      return super.computeIfAbsent(k, this.mapFunction);
   }
}
