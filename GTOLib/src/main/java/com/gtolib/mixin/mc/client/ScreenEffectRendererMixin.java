package com.gtolib.mixin.mc.client;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ScreenEffectRenderer.class)
public class ScreenEffectRendererMixin {
   @Redirect(method = "renderScreenEffect", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Player;noPhysics:Z", opcode = 180))
   private static boolean gtolib$redirectNoPhysics(Player var0) {
      return var0.noPhysics || var0 instanceof LocalPlayer var1 && var1.getAbilities().flying;
   }
}
