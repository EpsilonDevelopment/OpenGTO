package com.gtolib.mixin.mc.pack;

import com.gtolib.cache.pack.FastFilePackResources;
import com.gtolib.cache.pack.FastPathPackResources;
import java.io.File;
import java.nio.file.Path;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FolderRepositorySource.class, priority = 0)
public class FolderRepositorySourceMixin {
   @Inject(method = "lambda$detectPackResources$1", at = @At("HEAD"), cancellable = true)
   private static void fastpathpackresources(Path var0, boolean var1, String var2, CallbackInfoReturnable<PackResources> var3) {
      var3.setReturnValue(new FastPathPackResources(var2, var0, var1));
   }

   @Inject(method = "lambda$detectPackResources$2", at = @At("HEAD"), cancellable = true)
   private static void fastfilepackresources(File var0, boolean var1, String var2, CallbackInfoReturnable<PackResources> var3) {
      var3.setReturnValue(new FastFilePackResources(var2, var0, var1));
   }
}
