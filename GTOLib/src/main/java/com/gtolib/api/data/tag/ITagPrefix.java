package com.gtolib.api.data.tag;

public interface ITagPrefix {
   default boolean gtolib$isTagInput() {
      throw new UnsupportedOperationException("");
   }
}
