package com.gtolib.utils.reflect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import lombok.Generated;

public final class MethodReference {
   private final Method method;
   private final Object instance;

   public static MethodReference fromInstance(Object instance, String methodName, Class<?>... parameterTypes) {
      return new MethodReference(instance, instance.getClass(), methodName, parameterTypes);
   }

   public static MethodReference fromClass(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
      return new MethodReference(null, clazz, methodName, parameterTypes);
   }

   private MethodReference(Object instance, Class<?> clazz, String methodName, Class<?>... parameterTypes) {
      this.instance = instance;

      try {
         this.method = clazz.getDeclaredMethod(methodName, parameterTypes);
         this.method.setAccessible(true);
      } catch (NoSuchMethodException e) {
         throw new RuntimeException(e);
      }
   }

   public void invoke(Object... args) throws InvocationTargetException, IllegalAccessException {
      this.method.invoke(this.instance, args);
   }

   @Generated
   public Method getMethod() {
      return this.method;
   }
}
