package com.gtolib.api.player.attribute;

public final class IntAttribute extends NumericAttribute<Integer> {
   public IntAttribute(String name, boolean defaultAvailable, float defaultMin, float defaultMax, float defaultCurrent) {
      super(name, Integer.class, defaultAvailable, defaultMin, defaultMax, defaultCurrent);
   }

   public IntAttribute(
      String name, boolean defaultAvailable, float defaultMin, float defaultMax, float defaultCurrent, NumericAttribute.AttributeApplier applier
   ) {
      super(name, Integer.class, defaultAvailable, defaultMin, defaultMax, defaultCurrent, applier);
   }
}
