package com.gtolib.mixin.mc;

import java.util.List;
import net.minecraft.client.resources.SplashManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SplashManager.class, priority = 0)
public class SplashManagerMixin {
   @Inject(
      at = @At("RETURN"),
      method = "apply(Ljava/util/List;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
      cancellable = true
   )
   private void addSplashes(List<String> var1, ResourceManager var2, ProfilerFiller var3, CallbackInfo var4) {
      var4.cancel();
   }
}
