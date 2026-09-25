package com.gtolib.mixin.emi.plugin;

import com.gtolib.emi.EMIManager;
import mezz.jei.common.config.file.FileWatcher;
import mezz.jei.library.startup.JeiStarter;
import mezz.jei.library.startup.StartData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = JeiStarter.class, remap = false)
public abstract class JeiStarterMixin {
   @Mutable
   @Shadow
   @Final
   private FileWatcher fileWatcher;

   @Shadow
   public abstract void start();

   @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lmezz/jei/common/config/file/FileWatcher;start()V"))
   private void disableFileWatcher(FileWatcher var1) {
   }

   @Inject(method = "<init>", at = @At("TAIL"))
   private void init(StartData var1, CallbackInfo var2) {
      this.fileWatcher = null;
   }

   @Inject(method = "start", at = @At("HEAD"), remap = false, cancellable = true)
   private void gtolib$deferStart(CallbackInfo var1) {
      if (EMIManager.JeiStarter == null) {
         var1.cancel();
      }

      EMIManager.JeiStarter = this::start;
   }
}
