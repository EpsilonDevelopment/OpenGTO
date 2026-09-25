package com.gtolib.mixin.emi;

import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.registry.EmiComparisonDefaults;
import java.util.Map;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EmiComparisonDefaults.class)
public class EmiComparisonDefaultsMixin {
   @Shadow(remap = false)
   public static Map<Object, Comparison> comparisons;

   @Overwrite(remap = false)
   public static Comparison get(Object var0) {
      Comparison var1 = comparisons.get(var0);
      return var1 != null ? var1 : Comparison.DEFAULT_COMPARISON;
   }
}
