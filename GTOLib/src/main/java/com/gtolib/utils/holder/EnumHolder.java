package com.gtolib.utils.holder;

public class EnumHolder<T extends Enum<T>> {
   public T value;

   public EnumHolder(T value) {
      this.value = value;
   }
}
