package com.gtolib.utils.iostream;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

public interface IOStreamEncoder<T> {
   void encode(DataIOStream var1, T var2) throws IOException;

   static <K, V> IOStreamEncoder<V> convert(IOStreamEncoder<? super K> encoder, Function<V, K> converter) {
      return (dos, obj) -> encoder.encode(dos, converter.apply(obj));
   }

   static <K, V> IOStreamEncoder<Map<? extends K, ? extends V>> map(IOStreamEncoder<? super K> keySerializer, IOStreamEncoder<? super V> valueSerializer) {
      return (dos, map) -> {
         dos.writeVarInt(map.size());
         map.forEach((k, v) -> {
            try {
               keySerializer.encode(dos, k);
               valueSerializer.encode(dos, v);
            } catch (IOException e) {
               throw new RuntimeException(e);
            }
         });
      };
   }

   static <E> IOStreamEncoder<Collection<? extends E>> collection(IOStreamEncoder<? super E> encoder) {
      return (dos, list) -> {
         dos.writeVarInt(list.size());
         list.forEach(o -> {
            try {
               encoder.encode(dos, o);
            } catch (IOException e) {
               throw new RuntimeException(e);
            }
         });
      };
   }

   static <E> IOStreamEncoder<E[]> array(IOStreamEncoder<? super E> encoder) {
      return (dos, list) -> {
         dos.writeVarInt(list.length);

         for (E o : list) {
            encoder.encode(dos, o);
         }
      };
   }

   static <K, V> IOStreamEncoder<Map<? extends K, ? extends V>> old_map(IOStreamEncoder<? super K> keyEncoder, IOStreamEncoder<? super V> valueEncoder) {
      return (dos, map) -> {
         dos.writeInt(map.size());
         map.forEach((k, v) -> {
            try {
               keyEncoder.encode(dos, k);
               valueEncoder.encode(dos, v);
            } catch (IOException e) {
               throw new RuntimeException(e);
            }
         });
      };
   }

   static <E> IOStreamEncoder<Collection<? extends E>> old_collection(IOStreamEncoder<? super E> encoder) {
      return (dos, list) -> {
         dos.writeInt(list.size());
         list.forEach(o -> {
            try {
               encoder.encode(dos, o);
            } catch (IOException e) {
               throw new RuntimeException(e);
            }
         });
      };
   }
}
