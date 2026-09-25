package com.gtolib.mixin.mc;

import net.minecraft.FileUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FileUtil.class)
public class FileUtilMixin {
   @Inject(method = "validatePath", at = @At("HEAD"), cancellable = true)
   private static void validatePath(String[] var0, CallbackInfo var1) {
      var1.cancel();
   }
}
