package com.gtolib.mixin.mc.client;

import com.gtolib.mc.ICreateWorldScreen;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.GridLayout.RowHelper;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = CreateWorldScreen.class, priority = 0)
public abstract class CreateWorldScreenMixin implements ICreateWorldScreen {
   @Unique
   private LayoutElement gtolib$createWorldButton;

   @Shadow
   protected abstract void onCreate();

   @Override
   public void gto$onCreate() {
      this.onCreate();
   }

   @Redirect(
      method = "init",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;",
         ordinal = 0
      )
   )
   private <T extends LayoutElement> T gtolib$addCreateWorldButton(RowHelper var1, LayoutElement var2) {
      return (T)(this.gtolib$createWorldButton = var1.addChild(var2));
   }

   @Override
   public LayoutElement gtolib$getCreateWorldButton() {
      return this.gtolib$createWorldButton;
   }
}
