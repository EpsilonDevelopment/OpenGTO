package com.gtolib.mixin.apotheosis;

import com.gtolib.emi.EMIManager;
import dev.emi.emi.api.stack.EmiStack;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "dev.shadowsoffire.placebo.reload.DynamicRegistry$SyncManagement")
public abstract class DynamicRegistryMixin {
   @Inject(method = "endSync", at = @At("RETURN"), remap = false)
   private static void endSync(String var0, CallbackInfo var1) {
      if (var0.equals("gems")) {
         for (Gem var3 : GemRegistry.INSTANCE.getValues()) {
            LootRarity var4 = RarityRegistry.getMinRarity().get();

            for (LootRarity var5 = RarityRegistry.getMaxRarity().get(); var4 != var5; var4 = var4.next()) {
               EMIManager.stacks.add(EmiStack.of(GemRegistry.createGemStack(var3, var4)));
            }
         }
      }
   }
}
