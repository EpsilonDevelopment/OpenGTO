package com.gtolib.mixin.adastra;

import com.gtolib.mc.ILevel;
import earth.terrarium.adastra.api.planets.Planet;
import earth.terrarium.adastra.common.systems.OxygenApiImpl;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(OxygenApiImpl.class)
public class OxygenApiImplMixin {
   @Overwrite(remap = false)
   public boolean hasOxygen(Level var1) {
      Planet var2 = ((ILevel)var1).gtolib$getPlanet();
      return var2 != null ? var2.oxygen() : true;
   }
}
