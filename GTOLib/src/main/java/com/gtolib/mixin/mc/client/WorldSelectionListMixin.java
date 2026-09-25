package com.gtolib.mixin.mc.client;

import com.gtolib.mc.IExtendedLevelSetting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList.Entry;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList.WorldListEntry;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WorldSelectionList.class, priority = 0)
public class WorldSelectionListMixin {
   @Shadow
   @Final
   private SelectWorldScreen screen;

   @Inject(method = "setSelected(Lnet/minecraft/client/gui/screens/worldselection/WorldSelectionList$Entry;)V", at = @At("TAIL"))
   public void setSelected(Entry var1, CallbackInfo var2) {
      if (var1 instanceof WorldListEntry var3) {
         Button var4 = this.screen.selectButton;
         var4.active = IExtendedLevelSetting.matchDifficulty(var3.summary.getSettings());
         if (!var4.active) {
            var4.setMessage(Component.translatable("selectWorld.gto_difficulty.not_current"));
         } else {
            var4.setMessage(Component.translatable("selectWorld.select"));
         }
      }
   }
}
