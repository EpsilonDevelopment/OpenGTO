package com.gtolib.mixin.mc.client;

import com.gregtechceu.gtceu.GTCEu;
import com.gtocore.config.GTOConfig;
import com.gtolib.MixinConfigPlugin;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.server.IntegratedServer;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.LoadingModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Minecraft.class, priority = 0)
public abstract class MinecraftMixin {
   @Shadow
   @Nullable
   private IntegratedServer singleplayerServer;

   @Shadow
   public abstract boolean isConnectedToRealms();

   @Shadow
   @Nullable
   public abstract ServerData getCurrentServer();

   @Shadow
   @Nullable
   public abstract ClientPacketListener getConnection();

   @Redirect(method = "run", at = @At(value = "INVOKE", target = "Ljava/lang/Runtime;availableProcessors()I", remap = false))
   private int redirectAvailableProcessors(Runtime var1) {
      return 16;
   }

   @Inject(method = "createTitle", at = @At("HEAD"), cancellable = true)
   private void createTitle(CallbackInfoReturnable<String> var1) {
      StringBuilder var2 = new StringBuilder("GregTech Odyssey");
      LoadingModList var3 = FMLLoader.getLoadingModList();
      if (var3 != null) {
         var2.append(" | ").append(var3.getModFileById("gtocore").versionString());
         if (MixinConfigPlugin.hash != null) {
            var2.append("-").append(MixinConfigPlugin.hash);
         }

         var2.append(" |");
      }

      var2.append(" [").append(GTOConfig.INSTANCE.gamePlay.difficulty).append(" Mode]").append(GTCEu.isDev() ? " [Dev]" : "");
      ClientPacketListener var4 = this.getConnection();
      if (var4 != null && var4.getConnection().isConnected()) {
         var2.append(" - ");
         if (this.singleplayerServer != null && !this.singleplayerServer.isPublished()) {
            var2.append(I18n.get("title.singleplayer"));
         } else if (this.isConnectedToRealms()) {
            var2.append(I18n.get("title.multiplayer.realms"));
         } else if (this.singleplayerServer != null || this.getCurrentServer() != null && this.getCurrentServer().isLan()) {
            var2.append(I18n.get("title.multiplayer.lan"));
         } else {
            var2.append(I18n.get("title.multiplayer.other"));
         }
      }

      var1.setReturnValue(var2.toString());
   }
}
