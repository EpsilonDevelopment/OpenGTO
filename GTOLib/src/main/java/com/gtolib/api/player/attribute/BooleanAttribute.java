package com.gtolib.api.player.attribute;

import net.minecraft.server.level.ServerPlayer;

public final class BooleanAttribute extends AttributeDefinition<BooleanValue, BooleanAttribute> {
   public final boolean defaultAvailable;
   public final boolean defaultCurrent;
   private final BooleanAttribute.AttributeApplier applier;

   public BooleanAttribute(String name, boolean defaultAvailable, boolean defaultCurrent) {
      this(name, defaultAvailable, defaultCurrent, (serverPlayer, playerAttributes, value) -> {});
   }

   public BooleanAttribute(String name, boolean defaultAvailable, boolean defaultCurrent, BooleanAttribute.AttributeApplier applier) {
      super(name);
      this.defaultAvailable = defaultAvailable;
      this.defaultCurrent = defaultCurrent;
      this.applier = applier;
   }

   public BooleanValue createValue(Runnable dirtyCallback) {
      return new BooleanValue(this, dirtyCallback, this.defaultAvailable, this.defaultCurrent);
   }

   @Override
   public void apply(ServerPlayer serverPlayer, PlayerAttributes playerAttributes) {
      this.applier.apply(serverPlayer, playerAttributes, playerAttributes.get(this));
   }

   @FunctionalInterface
   public interface AttributeApplier {
      void apply(ServerPlayer var1, PlayerAttributes var2, BooleanValue var3);
   }
}
