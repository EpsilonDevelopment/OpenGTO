package com.gtolib.mixin.adastra;

import com.gtolib.mc.ILevel;
import earth.terrarium.adastra.api.planets.Planet;
import earth.terrarium.adastra.common.systems.TemperatureApiImpl;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(TemperatureApiImpl.class)
public class TemperatureApiImplMixin {
   @Overwrite(remap = false)
   public short getTemperature(Level var1) {
      Planet var2 = ((ILevel)var1).gtolib$getPlanet();
      return var2 != null ? var2.temperature() : 15;
   }
}
