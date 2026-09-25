package com.gtolib.mixin.mc;

import net.minecraft.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = Util.class, priority = Integer.MAX_VALUE)
public class UtilMixin {
   @Overwrite
   public static long getMillis() {
      return Util.timeSource.getAsLong() / 1000000L;
   }
}
