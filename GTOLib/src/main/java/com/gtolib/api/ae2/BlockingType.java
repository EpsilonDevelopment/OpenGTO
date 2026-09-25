package com.gtolib.api.ae2;

public enum BlockingType {
   NONE,
   ALL,
   CONTAIN,
   NON_CONTAIN,
   PARALLEL;

   // $VF: synthetic method
   private static BlockingType[] $values() {
      return new BlockingType[]{NONE, ALL, CONTAIN, NON_CONTAIN, PARALLEL};
   }
}
