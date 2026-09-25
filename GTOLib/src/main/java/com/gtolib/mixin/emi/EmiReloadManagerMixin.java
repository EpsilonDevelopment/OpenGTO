package com.gtolib.mixin.emi;

import com.gregtechceu.gtceu.utils.GTUtil;
import com.gtocore.config.GTOConfig;
import com.gtocore.config.SparkRange;
import com.gtolib.emi.EMIManager;
import dev.emi.emi.search.EmiSearch;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import org.embeddedt.modernfix.spark.SparkLaunchProfiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "dev.emi.emi.runtime.EmiReloadManager$ReloadWorker")
public class EmiReloadManagerMixin {
   @Redirect(method = "run", at = @At(value = "INVOKE", target = "Ldev/emi/emi/search/EmiSearch;bake()V"), remap = false)
   private void bake() {
      Thread var1 = new Thread(() -> {
         EMIManager.searchBaking = true;
         EmiSearch.bake();
         EMIManager.searchBaking = false;
         EMIManager.searchBake = true;
         Level var0 = GTUtil.getClientLevel();
         if (var0 != null) {
            Minecraft.getInstance().execute(EmiSearch::update);
         }

         try {
            Thread.sleep(5000L);
         } catch (InterruptedException var2) {
            throw new RuntimeException(var2);
         }

         if (GTOConfig.INSTANCE.devMode.startSpark == SparkRange.WORLD || GTOConfig.INSTANCE.devMode.startSpark == SparkRange.ALL) {
            SparkLaunchProfiler.stop("all");
         }
      });
      var1.setPriority(1);
      var1.setDaemon(true);
      var1.start();
   }
}
