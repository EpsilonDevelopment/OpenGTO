package com.gtolib.mixin.adastra;

import com.gtolib.mc.ILevel;
import earth.terrarium.adastra.api.planets.Planet;
import earth.terrarium.adastra.common.systems.GravityApiImpl;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(GravityApiImpl.class)
public class GravityApiImplMixin {
   @Overwrite(remap = false)
   public float getGravity(Level var1) {
      Planet var2 = ((ILevel)var1).gtolib$getPlanet();
      return var2 != null ? var2.gravity() / 9.807F : 1.0F;
   }
}
