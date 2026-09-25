package com.gtolib.api.player.attribute;

import net.minecraft.server.level.ServerPlayer;

public class NumericAttribute<T extends Number> extends AttributeDefinition<NumericValue, NumericAttribute<?>> {
   public final Class<T> kind;
   public final boolean defaultAvailable;
   public final float defaultMin;
   public final float defaultMax;
   public final float defaultCurrent;
   private final NumericAttribute.AttributeApplier applier;

   protected NumericAttribute(String name, Class<T> kind, boolean defaultAvailable, float defaultMin, float defaultMax, float defaultCurrent) {
      this(name, kind, defaultAvailable, defaultMin, defaultMax, defaultCurrent, (serverPlayer, playerAttributes, value) -> {});
   }

   protected NumericAttribute(
      String name, Class<T> kind, boolean defaultAvailable, float defaultMin, float defaultMax, float defaultCurrent, NumericAttribute.AttributeApplier applier
   ) {
      super(name);
      this.kind = kind;
      this.defaultAvailable = defaultAvailable;
      this.defaultMin = defaultMin;
      this.defaultMax = defaultMax;
      this.defaultCurrent = defaultCurrent;
      this.applier = applier;
   }

   public NumericValue createValue(Runnable dirtyCallback) {
      return new NumericValue(this, dirtyCallback, this.defaultAvailable, this.defaultMin, this.defaultMax, this.defaultCurrent);
   }

   @Override
   public void apply(ServerPlayer serverPlayer, PlayerAttributes playerAttributes) {
      this.applier.apply(serverPlayer, playerAttributes, playerAttributes.get(this));
   }

   @FunctionalInterface
   public interface AttributeApplier {
      void apply(ServerPlayer var1, PlayerAttributes var2, NumericValue var3);
   }
}
