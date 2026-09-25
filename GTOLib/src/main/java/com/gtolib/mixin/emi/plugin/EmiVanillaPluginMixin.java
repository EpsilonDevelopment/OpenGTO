package com.gtolib.mixin.emi.plugin;

import dev.emi.emi.VanillaPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.runtime.EmiReloadLog;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = VanillaPlugin.class, remap = false)
public abstract class EmiVanillaPluginMixin {
   @Unique
   private static final ResourceLocation INFINITE_WATER_RECIPE_ID = ResourceLocation.fromNamespaceAndPath("emi", "/world/fluid_spring/minecraft/water");

   @Redirect(
      method = "addWorldInteraction",
      at = @At(value = "INVOKE", target = "Ldev/emi/emi/VanillaPlugin;addRecipeSafe(Ldev/emi/emi/api/EmiRegistry;Ljava/util/function/Supplier;)V")
   )
   private static void cancelInfiniteWaterRecipe(EmiRegistry var0, Supplier<EmiRecipe> var1) {
      EmiRecipe var2 = (EmiRecipe)var1.get();
      if (var2 == null || var2.getId() == null || !var2.getId().equals(INFINITE_WATER_RECIPE_ID)) {
         try {
            var0.addRecipe(var2);
         } catch (Throwable var4) {
            EmiReloadLog.warn("Exception thrown when parsing EMI recipe (no ID available)", var4);
         }
      }
   }
}
