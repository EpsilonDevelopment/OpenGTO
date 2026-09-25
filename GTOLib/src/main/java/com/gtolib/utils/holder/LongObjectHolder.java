package com.gtolib.utils.holder;

public class LongObjectHolder<T> {
   public long number;
   public T obj;

   public LongObjectHolder(long number, T obj) {
      this.number = number;
      this.obj = obj;
   }
}
