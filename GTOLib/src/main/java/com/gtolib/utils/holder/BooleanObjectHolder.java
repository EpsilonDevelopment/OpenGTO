package com.gtolib.utils.holder;

public class BooleanObjectHolder<T> {
   public T obj;
   public boolean flag;

   public BooleanObjectHolder(T obj, boolean flag) {
      this.obj = obj;
      this.flag = flag;
   }
}
