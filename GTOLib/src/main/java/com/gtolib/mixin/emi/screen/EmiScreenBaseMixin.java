package com.gtolib.mixin.emi.screen;

import appeng.client.gui.me.crafting.CraftConfirmScreen;
import appeng.client.gui.me.crafting.CraftingCPUScreen;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.emi.emi.screen.EmiScreenBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EmiScreenBase.class)
public class EmiScreenBaseMixin {
   @ModifyExpressionValue(method = "of", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;isEmpty()Z", remap = false), remap = false)
   private static boolean addCustomScreenSupport(boolean var0) {
      return !gto$isCustomScreen() && var0;
   }

   @Unique
   private static boolean gto$isCustomScreen() {
      Screen var0 = Minecraft.getInstance().screen;
      return var0 instanceof CraftingCPUScreen || var0 instanceof CraftConfirmScreen;
   }
}
