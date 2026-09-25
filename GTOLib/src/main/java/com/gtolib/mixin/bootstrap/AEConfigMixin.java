package com.gtolib.mixin.bootstrap;

import appeng.core.AEConfig;
import com.gtolib.forge.ForgeCommonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AEConfig.class)
public class AEConfigMixin {
   @Overwrite(remap = false)
   public boolean isPortableCellDisassemblyEnabled() {
      return false;
   }

   @Inject(method = "syncCommonConfig", at = @At("TAIL"), remap = false)
   public void syncCommonConfig(CallbackInfo var1) {
      ForgeCommonEvent.syncCommonConfig();
   }
}
