package com.gtolib.api.machine.heat;

import com.gtolib.api.data.Dimension;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;

public final class AmbientTemperature {
   public static final double BASE_TEMPERATURE = 293.15;
   private static final double BIOME_TEMP_FACTOR = 50.0;
   private static final double ALTITUDE_FACTOR = -0.0065;

   public static double calculate(@Nullable Level level, BlockPos pos) {
      if (level == null) {
         return 293.15;
      }

      Dimension dim = Dimension.from(level);
      if (dim == null) {
         return calculateOverworld(level, pos);
      }

      return switch (dim) {
         case OVERWORLD -> calculateOverworld(level, pos);
         case MOON -> 100.0;
         case MARS -> 210.0;
         case VENUS -> 737.0;
         case MERCURY -> 440.0;
         case CERES -> 168.0;
         case IO -> 130.0;
         case GANYMEDE -> 110.0;
         case ENCELADUS -> 75.0;
         case TITAN -> 94.0;
         case PLUTO -> 44.0;
         case GLACIO -> 200.0;
         case BARNARDA_C -> 300.0;
         case THE_NETHER -> 400.0;
         case THE_END -> 50.0;
         case OTHERSIDE -> 280.0;
         case ALFHEIM -> 295.0;
         case CREATE -> 3.0;
         case ANCIENT_WORLD -> 310.0;
         case FLAT, VOID -> 293.15;
      };
   }

   private static double calculateOverworld(Level level, BlockPos pos) {
      double temp = 293.15;
      Biome biome = level.getBiome(pos).value();
      float biomeTemp = biome.getBaseTemperature();
      temp += (biomeTemp - 0.8) * 50.0;
      return temp + (pos.getY() - 64) * -0.0065;
   }
}
