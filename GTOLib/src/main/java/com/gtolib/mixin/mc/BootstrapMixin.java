package com.gtolib.mixin.mc;

import com.gtolib.cache.CacheManager;
import net.minecraft.server.Bootstrap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Bootstrap.class)
public class BootstrapMixin {
   @Inject(method = "bootStrap", at = @At(value = "FIELD", opcode = 179, target = "Lnet/minecraft/server/Bootstrap;isBootstrapped:Z", ordinal = 0))
   private static void recordStartTime(CallbackInfo var0) {
      CacheManager.init();
   }
}
