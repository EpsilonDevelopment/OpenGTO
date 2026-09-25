package com.gtolib.utils;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap.FastEntrySet;
import java.util.Iterator;
import java.util.Map;

public final class MapUtils {
   private MapUtils() {
   }

   public static <K, V> Iterator<java.util.Map.Entry<K, V>> fastIterator(Map<K, V> map) {
      if (map instanceof Object2ObjectMap<K, V> object2ObjectMap) {
         ObjectSet<Entry<K, V>> set = object2ObjectMap.object2ObjectEntrySet();
         return set instanceof FastEntrySet<K, V> fastEntrySet ? (Iterator<java.util.Map.Entry<K, V>>) (Iterator<?>) fastEntrySet.fastIterator() : (Iterator<java.util.Map.Entry<K, V>>) (Iterator<?>) set.iterator();
      } else if (map instanceof Reference2ObjectMap<K, V> reference2ObjectMap) {
         ObjectSet<it.unimi.dsi.fastutil.objects.Reference2ObjectMap.Entry<K, V>> set = reference2ObjectMap.reference2ObjectEntrySet();
         return set instanceof it.unimi.dsi.fastutil.objects.Reference2ObjectMap.FastEntrySet<K, V> fastEntrySet ? (Iterator<java.util.Map.Entry<K, V>>) (Iterator<?>) fastEntrySet.fastIterator() : (Iterator<java.util.Map.Entry<K, V>>) (Iterator<?>) set.iterator();
      } else {
         return map.entrySet().iterator();
      }
   }
}
