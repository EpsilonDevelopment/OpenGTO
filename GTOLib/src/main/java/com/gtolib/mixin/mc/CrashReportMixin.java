package com.gtolib.mixin.mc;

import com.gtolib.MixinConfigPlugin;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.CrashReport;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.forgespi.language.IModFileInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CrashReport.class, priority = 0)
public class CrashReportMixin {
   @Inject(
      method = "getFriendlyReport",
      at = @At(value = "INVOKE", target = "Ljava/lang/StringBuilder;append(Ljava/lang/String;)Ljava/lang/StringBuilder;", shift = Shift.AFTER, ordinal = 6)
   )
   private void gto$onGetFriendlyReport(CallbackInfoReturnable<String> var1, @Local StringBuilder var2) {
      if (FMLLoader.getLoadingModList() != null) {
         IModFileInfo var3 = FMLLoader.getLoadingModList().getModFileById("gtocore").getFile().getModFileInfo();
         var2.append("GTO Core Version: ")
            .append(var3.versionString())
            .append("-")
            .append(MixinConfigPlugin.hash == null ? "unknown" : MixinConfigPlugin.hash)
            .append("\n");
      }
   }
}
