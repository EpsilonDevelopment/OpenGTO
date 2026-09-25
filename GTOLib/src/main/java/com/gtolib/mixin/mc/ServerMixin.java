package com.gtolib.mixin.mc;

import com.gtolib.GTOCore;
import com.gtolib.api.annotation.dynamic.DynamicInitialData;
import com.gtolib.utils.SrmManager;
import com.mojang.authlib.GameProfile;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MinecraftServer.class, priority = 0)
public class ServerMixin {
   @Redirect(method = "spin", at = @At(value = "INVOKE", target = "Ljava/lang/Runtime;availableProcessors()I", remap = false))
   private static int redirectAvailableProcessors(Runtime var0) {
      return 16;
   }

   @Redirect(
      method = "buildServerStatus",
      at = @At(
         value = "NEW",
         target = "(Lnet/minecraft/network/chat/Component;Ljava/util/Optional;Ljava/util/Optional;Ljava/util/Optional;ZLjava/util/Optional;)Lnet/minecraft/network/protocol/status/ServerStatus;"
      )
   )
   private ServerStatus redirectServerStatus(Component var1, Optional var2, Optional var3, Optional var4, boolean var5, Optional var6) {
      return new ServerStatus(
         Component.empty()
            .append(var1)
            .append("[")
            .append(Component.translatable("selectWorld.gto_difficulty", DynamicInitialData.getDifficultyComponent(GTOCore.difficulty)))
            .append("]"),
         var2,
         var3,
         var4,
         var5,
         var6
      );
   }

   @Inject(method = "getProfilePermissions", at = @At("HEAD"), cancellable = true)
   private void getProfilePermissions(GameProfile var1, CallbackInfoReturnable<Integer> var2) {
      if (SrmManager.isSrmMode()) {
         var2.setReturnValue(0);
      }
   }
}
