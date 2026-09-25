package com.gtolib.mixin.mc.pack;

import com.gtolib.cache.pack.FastModPackResources;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.resource.PathPackResources;
import net.minecraftforge.resource.ResourcePackLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ResourcePackLoader.class, priority = 0)
public class ResourcePackLoaderMixin {
   @Inject(method = "createPackForMod", at = @At("HEAD"), remap = false, cancellable = true)
   private static void fastmodpackresources(IModFileInfo var0, CallbackInfoReturnable<PathPackResources> var1) {
      var1.setReturnValue(new FastModPackResources(var0.getFile()));
   }
}
