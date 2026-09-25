package com.gtolib.api.player.attribute;

import net.minecraft.server.level.ServerPlayer;

public abstract class AttributeDefinition<T extends AttributeValue<T, D>, D extends AttributeDefinition<T, D>> {
   public final String name;

   protected AttributeDefinition(String name) {
      this.name = name;
   }

   public abstract T createValue(Runnable var1);

   public abstract void apply(ServerPlayer var1, PlayerAttributes var2);

   public String getLangKey() {
      return "player_attribute.gtocore." + this.name;
   }
}
