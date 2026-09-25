package com.gtolib.mixin.emi.screen;

import com.gtolib.emi.EMIManager;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.runtime.EmiLog;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.search.EmiSearch;
import dev.emi.emi.search.EmiSearch.CompiledQuery;
import java.util.ArrayList;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EmiSearch.class)
public class EmiSearchMixin {
   @Inject(method = "bake", at = @At("HEAD"), remap = false, cancellable = true)
   private static void bake(CallbackInfo var0) {
      if (EMIManager.searchBake) {
         var0.cancel();
      }
   }

   @Overwrite(remap = false)
   public static void search(String var0) {
      try {
         List<? extends EmiIngredient> var1 = EmiScreenManager.getSearchSource();
         if (!EMIManager.searchBake) {
            EmiSearch.stacks = var1;
            return;
         }

         CompiledQuery var2 = new CompiledQuery(var0);
         EmiSearch.compiledQuery = var2;
         if (var2.isEmpty()) {
            EmiSearch.stacks = var1;
            return;
         }

         ArrayList<EmiIngredient> var3 = new ArrayList<>(var1.size());
         var1.forEach(var2x -> {
            List<EmiStack> var3x = var2x.getEmiStacks();
            if (var3x.size() == 1 && var2.test((EmiStack)var3x.getFirst())) {
               var3.add(var2x);
            }
         });
         EmiSearch.stacks = var3;
      } catch (Exception var4) {
         EmiLog.error("Error when attempting to search:", var4);
      }
   }
}
