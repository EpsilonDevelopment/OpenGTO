package com.gtolib.mixin.mc;

import com.gtolib.utils.MathUtil;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = Mth.class, priority = 2000)
public final class MthMixin {
   @Overwrite
   public static float sin(float var0) {
      return MathUtil.sin(var0);
   }

   @Overwrite
   public static float cos(float var0) {
      return MathUtil.cos(var0);
   }
}
