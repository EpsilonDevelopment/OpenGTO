package com.gtolib.mixin.mc;

import com.gtocore.config.Difficulty;
import com.gtocore.config.GTOConfig;
import com.gtolib.GTOCore;
import com.gtolib.api.annotation.dynamic.DynamicInitialData;
import com.gtolib.mc.ICreateWorldScreen;
import com.llamalad7.mixinextras.sugar.Local;
import dev.architectury.hooks.client.screen.forge.ScreenHooksImpl;
import dev.toma.configuration.client.ConfigurationClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.GridLayout.RowHelper;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.gui.screens.worldselection.CreateWorldScreen$GameTab", priority = 0)
public class GameTabMixin {
   @Inject(method = "<init>", at = @At("RETURN"))
   private void init(CreateWorldScreen var1, CallbackInfo var2, @Local(name = "gridlayout$rowhelper") RowHelper var3) {
      CycleButton var4 = CycleButton.<Difficulty>builder(
            var0 -> Component.empty()
               .append(DynamicInitialData.getDifficultyComponent(var0.ordinal() + 1))
               .append(var0 != gto$getCurrentDifficulty() ? "[⚠]" : "")
         )
         .withTooltip(
            var0 -> Tooltip.create(
               Component.translatable("selectWorld.gto_difficulty.tooltip." + var0.name().toLowerCase())
                  .append("\n")
                  .append(Component.translatable("selectWorld.gto_difficulty.tooltip.generic"))
                  .append(var0 != gto$getCurrentDifficulty() ? "\n" : "")
                  .append(var0 != gto$getCurrentDifficulty() ? Component.translatable("text.cloth-config.restart_required_sub") : Component.empty())
            )
         )
         .withValues(Difficulty.values())
         .withInitialValue(gto$getCurrentDifficulty())
         .create(0, 0, 186, 20, Component.translatable("selectWorld.gto_difficulty.no_suffix"), (var1x, var2x) -> {
            GTOConfig.set("difficulty", var2x, new String[]{"gamePlay"});
            if (((ICreateWorldScreen)var1).gtolib$getCreateWorldButton() instanceof Button var3x) {
               boolean var5x = var2x == gto$getCurrentDifficulty();
               var3x.setMessage(var5x ? Component.translatable("selectWorld.create") : Component.translatable("gui.jade.save_and_quit"));
               var3x.onPress = var5x ? var1xx -> ((ICreateWorldScreen)var1).gto$onCreate() : var0x -> gto$quitGame();
            }
         });
      Button var5 = Button.builder(Component.literal("..."), var1x -> var1.getMinecraft().setScreen(ConfigurationClient.getConfigScreen("gtocore", var1)))
         .bounds(0, 0, 20, 20)
         .createNarration(var0 -> var0.get().append(Component.translatable("config.screen.gtocore")))
         .tooltip(Tooltip.create(Component.translatable("gui.open").append(Component.translatable("config.screen.gtocore"))))
         .build();
      ScreenHooksImpl.addRenderableWidget(var1, var5);
      GridLayout var6 = new GridLayout().columnSpacing(2);
      var3.addChild(var6);
      var6.addChild(var4, 0, 0);
      var6.addChild(var5, 0, 1);
      var1.getUiState().addListener(var0 -> {});
   }

   @Unique
   private static void gto$quitGame() {
      Minecraft.getInstance().stop();
   }

   @Unique
   private static Difficulty gto$getCurrentDifficulty() {
      return Difficulty.values()[GTOCore.difficulty - 1];
   }
}
