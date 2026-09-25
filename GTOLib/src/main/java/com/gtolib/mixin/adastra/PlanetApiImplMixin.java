package com.gtolib.mixin.adastra;

import com.gtolib.mc.ILevel;
import earth.terrarium.adastra.api.planets.Planet;
import earth.terrarium.adastra.common.planets.PlanetApiImpl;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(PlanetApiImpl.class)
public class PlanetApiImplMixin {
   @Overwrite(remap = false)
   @Nullable
   public Planet getPlanet(Level var1) {
      return ((ILevel)var1).gtolib$getPlanet();
   }

   @Overwrite(remap = false)
   public boolean isPlanet(Level var1) {
      return ((ILevel)var1).gtolib$getPlanet() != null;
   }

   @Overwrite(remap = false)
   public boolean isSpace(Level var1) {
      Planet var2 = ((ILevel)var1).gtolib$getPlanet();
      return var2 != null ? var2.isSpace() : false;
   }

   @Overwrite(remap = false)
   public long getSolarPower(Level var1) {
      Planet var2 = ((ILevel)var1).gtolib$getPlanet();
      return var2 != null ? var2.solarPower() : 16L;
   }
}
