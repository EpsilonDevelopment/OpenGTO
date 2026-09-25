package com.gtolib.mixin.modernfix;

import com.gregtechceu.gtceu.GTCEu;
import com.gtocore.common.CommonProxy;
import com.gtolib.emi.EMIManager;
import com.gtolib.forge.ForgeCommonEvent;
import org.embeddedt.modernfix.util.ClassInfoManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClassInfoManager.class)
public class ClassInfoManagerMixin {
   @Shadow(remap = false)
   private static boolean hasRun;

   @Inject(method = "clear", at = @At("HEAD"), remap = false, cancellable = true)
   private static void clear(CallbackInfo var0) {
      if (!hasRun) {
         ForgeCommonEvent.clear();
         CommonProxy.afterStartup();
         if (GTCEu.isClientSide()) {
            EMIManager.addStacks();
         } else {
            var0.cancel();
         }
      }
   }
}
