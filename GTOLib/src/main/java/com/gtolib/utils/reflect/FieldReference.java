package com.gtolib.utils.reflect;

import java.lang.reflect.Field;
import lombok.Generated;

public final class FieldReference<T> {
   private final Field field;
   private final Object instance;

   public static <T> FieldReference<T> fromInstance(Object instance, Class<?> clazz, String fieldName) {
      return new FieldReference<>(instance, clazz, fieldName);
   }

   public static <T> FieldReference<T> fromInstance(Object instance, String fieldName) {
      return new FieldReference<>(instance, instance.getClass(), fieldName);
   }

   public static <T> FieldReference<T> fromClass(Class<?> clazz, String fieldName) {
      return new FieldReference<>(null, clazz, fieldName);
   }

   private FieldReference(Object instance, Class<?> clazz, String fieldName) {
      this.instance = instance;

      Field field;
      try {
         field = clazz.getDeclaredField(fieldName);
         field.setAccessible(true);
      } catch (NoSuchFieldException e) {
         throw new RuntimeException(e);
      }

      this.field = field;
   }

   public T get() throws IllegalAccessException {
      return (T)this.field.get(this.instance);
   }

   public void set(T value) throws IllegalAccessException {
      this.field.set(this.instance, value);
   }

   @Generated
   public Field getField() {
      return this.field;
   }
}
