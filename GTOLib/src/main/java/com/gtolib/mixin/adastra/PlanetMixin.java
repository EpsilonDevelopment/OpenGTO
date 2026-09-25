package com.gtolib.mixin.adastra;

import com.gtolib.api.data.Dimension;
import com.gtolib.api.data.GTODimensions;
import earth.terrarium.adastra.api.planets.Planet;
import java.util.Optional;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Planet.class)
public class PlanetMixin {
   @Overwrite(remap = false)
   public Optional<ResourceKey<Level>> getOrbitPlanet() {
      return Optional.ofNullable(GTODimensions.getDimensionIncludingOrbits(((Planet)(Object)this).dimension())).map(Dimension::getResourceKey);
   }
}
