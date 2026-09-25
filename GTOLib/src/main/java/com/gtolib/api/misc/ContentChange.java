package com.gtolib.api.misc;

public interface ContentChange {
   boolean isChanged();

   default void markClean() {
   }

   default void markDirty() {
   }
}
