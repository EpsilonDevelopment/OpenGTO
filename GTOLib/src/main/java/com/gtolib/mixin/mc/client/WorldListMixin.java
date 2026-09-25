package com.gtolib.mixin.mc.client;

import com.gtolib.mc.IExtendedLevelSetting;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList.WorldListEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.storage.LevelStorageSource.LevelCandidates;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldSelectionList.class)
public class WorldListMixin {
   @Redirect(
      method = "loadLevels",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/screens/worldselection/CreateWorldScreen;openFresh(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/gui/screens/Screen;)V"
      )
   )
   private void redirectLoadLevels(Minecraft var1, Screen var2) {
   }

   @Redirect(
      method = "loadLevels",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/level/storage/LevelStorageSource;loadLevelSummaries(Lnet/minecraft/world/level/storage/LevelStorageSource$LevelCandidates;)Ljava/util/concurrent/CompletableFuture;"
      )
   )
   private CompletableFuture<List<LevelSummary>> redirectLevelCandidates(LevelStorageSource var1, LevelCandidates var2) {
      return var1.loadLevelSummaries(var2).thenApply(var0 -> var0.stream().sorted((var0x, var1x) -> {
         boolean var2x = IExtendedLevelSetting.matchDifficulty(var0x.getSettings());
         boolean var3 = IExtendedLevelSetting.matchDifficulty(var1x.getSettings());
         if (var2x && !var3) {
            return -1;
         } else {
            return !var2x && var3 ? 1 : var0x.compareTo(var1x);
         }
      }).toList());
   }

   @Mixin(WorldListEntry.class)
   public static class WorldListEntryMixin {
      @Redirect(
         method = "render",
         at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)I"
         )
      )
      private int redirectDrawString(
         GuiGraphics var1,
         Font var2,
         Component var3,
         int var4,
         int var5,
         int var6,
         boolean var7,
         @Local(argsOnly = true, ordinal = 3) int var8,
         @Local(argsOnly = true, ordinal = 0) float var9
      ) {
         if (var2.width(var3) > var8 - 30) {
            var1.enableScissor(var4, var5, var4 + var8 - 30, var5 + 9 + 1);
            int var10 = Math.toIntExact(System.currentTimeMillis() / 100L % (var2.width(var3) + 30));
            int var11 = var1.drawString(var2, var3, var4 - var10, var5, var6, var7);
            var1.drawString(var2, var3, var4 + var2.width(var3) + 30 - var10, var5, var6, var7);
            var1.drawString(var2, var3, var4 - var2.width(var3) - 30 - var10, var5, var6, var7);
            var1.disableScissor();
            return var11;
         } else {
            return var1.drawString(var2, var3, var4, var5, var6, var7);
         }
      }
   }
}
