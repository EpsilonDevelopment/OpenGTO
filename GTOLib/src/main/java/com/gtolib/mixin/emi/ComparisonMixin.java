package com.gtolib.mixin.emi;

import com.gtolib.emi.IEmiStack;
import dev.emi.emi.api.stack.Comparison;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Comparison.class)
public class ComparisonMixin {
   @Mutable
   @Shadow(remap = false)
   @Final
   private static Comparison COMPARE_NBT;

   @Inject(method = "<clinit>", at = @At("TAIL"))
   private static void init(CallbackInfo var0) {
      COMPARE_NBT = Comparison.compareData(var0x -> var0x instanceof IEmiStack var1 ? var1.getUniqueNbt() : var0x.getNbt());
   }
}
