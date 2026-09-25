package com.gtolib.api.player.attribute;

public final class FloatAttribute extends NumericAttribute<Float> {
   public FloatAttribute(String name, boolean defaultAvailable, float defaultMin, float defaultMax, float defaultCurrent) {
      super(name, Float.class, defaultAvailable, defaultMin, defaultMax, defaultCurrent);
   }

   public FloatAttribute(
      String name, boolean defaultAvailable, float defaultMin, float defaultMax, float defaultCurrent, NumericAttribute.AttributeApplier applier
   ) {
      super(name, Float.class, defaultAvailable, defaultMin, defaultMax, defaultCurrent, applier);
   }
}
