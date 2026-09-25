package com.gtolib.mixin.apotheosis;

import com.llamalad7.mixinextras.sugar.Local;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.spawn.enchantment.CapturingEnchant;
import dev.shadowsoffire.placebo.registry.RegistryEvent.Register;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.eventbus.api.IEventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Apotheosis.class)
public class ApotheosisMixin {
   @Inject(method = "<clinit>", at = @At("TAIL"), remap = false)
   private static void clinit(CallbackInfo var0) {
      Apotheosis.enableSpawner = false;
   }

   @Inject(method = "<init>", at = @At("TAIL"), remap = false)
   private void init(CallbackInfo var1, @Local IEventBus var2) {
      var2.addGenericListener(Enchantment.class, ApotheosisMixin::gtolib$enchants);
   }

   @Unique
   private static void gtolib$enchants(Register<Enchantment> var0) {
      var0.getRegistry().register(new CapturingEnchant(), "capturing");
   }
}
