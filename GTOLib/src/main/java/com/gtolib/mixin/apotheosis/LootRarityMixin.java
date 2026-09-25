package com.gtolib.mixin.apotheosis;

import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity.LootRule;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LootRule.class)
public class LootRarityMixin {
   @Redirect(
      method = "execute",
      at = @At(
         value = "INVOKE",
         target = "Lorg/apache/logging/log4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V"
      ),
      remap = false
   )
   private void redirectError(Logger var1, String var2, Object var3, Object var4, Object var5, Object var6) {
   }
}
