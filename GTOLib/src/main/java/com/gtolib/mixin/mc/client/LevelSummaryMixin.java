package com.gtolib.mixin.mc.client;

import com.gtolib.GTOCore;
import com.gtolib.api.annotation.dynamic.DynamicInitialData;
import com.gtolib.mc.IExtendedLevelSetting;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.HoverEvent.Action;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LevelSummary.class, priority = 0)
public class LevelSummaryMixin {
   @Shadow
   @Final
   private LevelSettings settings;

   @Redirect(method = "createInfo", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelSummary;hasCheats()Z"))
   private boolean gtolib$redirectHasCheats(LevelSummary var1, @Local MutableComponent var2) {
      if (((IExtendedLevelSetting)(Object)this.settings).gto$isSrm()) {
         var2.append(", ").append(Component.translatable("selectWorld.self_restraint_mode.enabled").withStyle(ChatFormatting.RED));
         return false;
      } else {
         return var1.hasCheats();
      }
   }

   @Inject(method = "createInfo", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelSummary;isExperimental()Z"))
   private void gtolib$injectDifficultyInfo(CallbackInfoReturnable<Component> var1, @Local MutableComponent var2) {
      int var3 = ((IExtendedLevelSetting)(Object)this.settings).gto$getGTODifficulty();
      Component var4 = var3 > 0
         ? DynamicInitialData.getDifficultyComponent(var3)
         : Component.translatable("gtocore.tooltip.unknown").withStyle(ChatFormatting.DARK_AQUA);
      var2.append(", ").append(Component.translatable("selectWorld.gto_difficulty", var4));
      var2.append("[")
         .append(
            GTOCore.difficulty == var3
               ? Component.literal("✔")
                  .withStyle(ChatFormatting.GREEN)
                  .setStyle(Style.EMPTY.withHoverEvent(new HoverEvent(Action.SHOW_TEXT, Component.translatable("selectWorld.gto_difficulty.current"))))
               : Component.literal("×")
                  .withStyle(ChatFormatting.RED)
                  .setStyle(Style.EMPTY.withHoverEvent(new HoverEvent(Action.SHOW_TEXT, Component.translatable("selectWorld.gto_difficulty.not_current"))))
         )
         .append("]");
      if (((IExtendedLevelSetting)(Object)this.settings).gto$isDevMode()) {
         var2.append(Component.literal(" ["));
         var2.append(Component.translatable("selectWorld.dev_mode").withStyle(ChatFormatting.AQUA));
         var2.append(Component.literal("]"));
      }
   }

   @Inject(method = "isDisabled", at = @At("RETURN"), cancellable = true)
   private void gtolib$injectIsDisabled(CallbackInfoReturnable<Boolean> var1) {
      if (!IExtendedLevelSetting.matchDifficulty(this.settings)) {
         var1.setReturnValue(true);
      }
   }
}
