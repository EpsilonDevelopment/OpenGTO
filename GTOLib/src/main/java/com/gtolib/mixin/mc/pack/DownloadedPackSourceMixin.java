package com.gtolib.mixin.mc.pack;

import com.gtolib.cache.pack.FastFilePackResources;
import java.io.File;
import net.minecraft.client.resources.DownloadedPackSource;
import net.minecraft.server.packs.PackResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = DownloadedPackSource.class, priority = 0)
public class DownloadedPackSourceMixin {
   @Inject(method = "lambda$setServerPack$8", at = @At("HEAD"), cancellable = true)
   private static void fastfilepackresources(File var0, String var1, CallbackInfoReturnable<PackResources> var2) {
      var2.setReturnValue(new FastFilePackResources(var1, var0, false));
   }
}
