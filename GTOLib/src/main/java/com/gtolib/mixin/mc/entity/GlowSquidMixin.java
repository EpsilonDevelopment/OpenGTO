package com.gtolib.mixin.mc.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.GlowSquid;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GlowSquid.class)
public abstract class GlowSquidMixin extends Squid {
   protected GlowSquidMixin(EntityType<? extends Squid> var1, Level var2) {
      super(var1, var2);
   }

   @Override
   public void tick() {
      this.discard();
   }
}
