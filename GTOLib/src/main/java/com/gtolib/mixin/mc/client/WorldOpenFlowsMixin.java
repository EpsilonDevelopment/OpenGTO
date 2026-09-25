package com.gtolib.mixin.mc.client;

import com.gtocore.config.GTOConfig;
import com.gtocore.config.SparkRange;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import org.embeddedt.modernfix.spark.SparkLaunchProfiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldOpenFlows.class)
public class WorldOpenFlowsMixin {
   @Inject(method = "loadLevel", at = @At("HEAD"))
   private void loadLevel(Screen var1, String var2, CallbackInfo var3) {
      if (GTOConfig.INSTANCE.devMode.startSpark == SparkRange.WORLD) {
         SparkLaunchProfiler.start("all");
      }
   }
}
