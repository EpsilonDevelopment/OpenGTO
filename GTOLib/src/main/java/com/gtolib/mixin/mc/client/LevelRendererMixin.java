package com.gtolib.mixin.mc.client;

import com.gtolib.api.player.IEnhancedPlayer;
import com.gtolib.api.player.attribute.PlayerAttributes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
   @ModifyArg(
      method = "renderLevel",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/LevelRenderer;setupRender(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/culling/Frustum;ZZ)V"
      ),
      index = 2
   )
   private boolean gtolib$modifySetupRenderArg(boolean var1) {
      LocalPlayer var2 = Minecraft.getInstance().player;
      IEnhancedPlayer var3 = IEnhancedPlayer.of(var2);
      return var3 == null
         ? var1
         : var3.getPlayerData().getPlayerAttributes().getBooleanCurrent(PlayerAttributes.FREE_MOV_STATE) && var2.getAbilities().flying || var1;
   }
}
