package com.gtolib.utils.iostream;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntFunction;

public interface IOStreamDecoder<T> {
   T decode(DataIOStream var1) throws IOException;

   static <K, V> IOStreamDecoder<V> convert(IOStreamDecoder<? extends K> decoder, Function<K, V> converter) {
      return stream -> converter.apply((K)decoder.decode(stream));
   }

   static <K, V> IOStreamDecoder<Reference2ReferenceOpenHashMap<K, V>> map(
      IOStreamDecoder<? extends K> keySerializer, IOStreamDecoder<? extends V> valueSerializer
   ) {
      return stream -> {
         int size = stream.readVarInt();
         Reference2ReferenceOpenHashMap<K, V> map = new Reference2ReferenceOpenHashMap<>(size);

         for (int i = 0; i < size; i++) {
            map.put((K)keySerializer.decode(stream), (V)valueSerializer.decode(stream));
         }

         return map;
      };
   }

   static <K, V, M extends Map<K, V>> IOStreamDecoder<M> map(
      IntFunction<M> function, IOStreamDecoder<? extends K> keySerializer, IOStreamDecoder<? extends V> valueSerializer
   ) {
      return stream -> {
         int size = stream.readVarInt();
         M map = function.apply(size);

         for (int i = 0; i < size; i++) {
            map.put((K)keySerializer.decode(stream), (V)valueSerializer.decode(stream));
         }

         return map;
      };
   }

   static <E, C extends Collection<E>> IOStreamDecoder<C> collection(IntFunction<C> function, IOStreamDecoder<? extends E> decoder) {
      return stream -> {
         int size = stream.readVarInt();
         C list = function.apply(size);

         for (int i = 0; i < size; i++) {
            list.add((E)decoder.decode(stream));
         }

         return list;
      };
   }

   static <E> IOStreamDecoder<List<E>> list(IOStreamDecoder<E> decoder) {
      return stream -> {
         int size = stream.readVarInt();
         Object[] array = new Object[size];

         for (int i = 0; i < size; i++) {
            array[i] = decoder.decode(stream);
         }

         return (List<E>)Arrays.asList(array);
      };
   }

   static <E> IOStreamDecoder<ReferenceOpenHashSet<E>> set(IOStreamDecoder<? extends E> decoder) {
      return stream -> {
         int size = stream.readVarInt();
         ReferenceOpenHashSet<E> set = new ReferenceOpenHashSet<>(size);

         for (int i = 0; i < size; i++) {
            set.add((E)decoder.decode(stream));
         }

         return set;
      };
   }

   static <K, V> IOStreamDecoder<Reference2ReferenceOpenHashMap<K, V>> old_map(
      IOStreamDecoder<? extends K> keySerializer, IOStreamDecoder<? extends V> valueSerializer
   ) {
      return stream -> {
         int size = stream.readInt();
         Reference2ReferenceOpenHashMap<K, V> map = new Reference2ReferenceOpenHashMap<>(size);

         for (int i = 0; i < size; i++) {
            map.put((K)keySerializer.decode(stream), (V)valueSerializer.decode(stream));
         }

         return map;
      };
   }

   static <E, C extends Collection<E>> IOStreamDecoder<C> old_collection(IntFunction<C> function, IOStreamDecoder<? extends E> decoder) {
      return stream -> {
         int size = stream.readInt();
         C list = function.apply(size);

         for (int i = 0; i < size; i++) {
            list.add((E)decoder.decode(stream));
         }

         return list;
      };
   }
}
