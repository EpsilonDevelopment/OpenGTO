package com.gtolib.mixin.emi.plugin;

import com.gtolib.emi.EMIManager;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.jemi.JemiPlugin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = JemiPlugin.class, remap = false)
public abstract class JemiPluginMixin {
   @Inject(method = "register", at = @At("HEAD"), remap = false)
   private void gtolib$startJeiBeforeWait(EmiRegistry var1, CallbackInfo var2) {
      EMIManager.startDeferredJei();
   }
}
