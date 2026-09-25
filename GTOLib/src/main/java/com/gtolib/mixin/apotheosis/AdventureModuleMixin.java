package com.gtolib.mixin.apotheosis;

import dev.shadowsoffire.apotheosis.adventure.AdventureModule;
import net.minecraftforge.eventbus.api.IEventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AdventureModule.class)
public class AdventureModuleMixin {
   @Redirect(
      method = "init",
      at = @At(value = "INVOKE", target = "Lnet/minecraftforge/eventbus/api/IEventBus;register(Ljava/lang/Object;)V", ordinal = 1),
      remap = false
   )
   private void register(IEventBus var1, Object var2) {
   }
}
