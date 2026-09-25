package com.gtolib.mixin.lowdraglib;

import appeng.api.stacks.AmountFormat;
import appeng.api.stacks.GenericStack;
import appeng.client.gui.me.common.StackSizeRenderer;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = DrawerHelper.class, remap = false)
public abstract class DrawerHelperMixin {
   @Redirect(
      method = "drawItemStack",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/GuiGraphics;renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
         remap = true
      )
   )
   private static void gtolib$renderLargeItemStackCount(GuiGraphics var0, Font var1, ItemStack var2, int var3, int var4, String var5) {
      if (var2.getCount() > 99) {
         GenericStack var6 = GenericStack.fromItemStack(var2);
         if (var6 == null) {
            var0.renderItemDecorations(var1, var2, var3, var4, var5);
            return;
         }

         String var7 = var6.what().formatAmount(var6.amount(), AmountFormat.SLOT);
         StackSizeRenderer.renderSizeLabel(var0, Minecraft.getInstance().font, var3 + 1, var4 + 1, var7, false);
         ItemStack var8 = var2.copy();
         var8.setCount(1);
         var0.renderItemDecorations(var1, var8, var3, var4, null);
      } else {
         var0.renderItemDecorations(var1, var2, var3, var4, var5);
      }
   }
}
