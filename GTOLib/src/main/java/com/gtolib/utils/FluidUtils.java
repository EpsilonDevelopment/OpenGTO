package com.gtolib.utils;

import com.gtolib.api.fluid.IFluid;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.registries.ForgeRegistries;

public final class FluidUtils {
   private FluidUtils() {
   }

   public static String getUnicodeMillibuckets(long amount) {
      return amount < 1000L ? NumberUtils.formatLong(amount) + " mB" : NumberUtils.formatLong(amount / 1000L) + " B";
   }

   public static String getId(Fluid fluid) {
      return ((IFluid)fluid).gtolib$getIdString();
   }

   public static ResourceLocation getIdLocation(Fluid fluid) {
      return ((IFluid)fluid).gtolib$getIdLocation();
   }

   public static Fluid getFluid(String fluidName) {
      Fluid fluid = ForgeRegistries.FLUIDS.getValue(RLUtils.parse(fluidName));
      return fluid == null ? Fluids.EMPTY : fluid;
   }
}
