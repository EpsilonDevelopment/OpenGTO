package com.gtolib.mixin.emi;

import com.gtolib.emi.EMIFavouriteAEKeyCache;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.runtime.EmiFavorites;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EmiFavorites.class)
public class EmiFavoritesMixin {
   @Inject(method = "addFavorite(Ldev/emi/emi/api/stack/EmiIngredient;Ldev/emi/emi/api/recipe/EmiRecipe;)V", at = @At("TAIL"), remap = false)
   private static void onAddFavorite(EmiIngredient var0, EmiRecipe var1, CallbackInfo var2) {
      EMIFavouriteAEKeyCache.recalculate();
   }

   @Inject(method = "removeFavorite", at = @At("RETURN"), remap = false)
   private static void onRemoveFavorite(EmiIngredient var0, CallbackInfoReturnable<Boolean> var1) {
      if ((Boolean)var1.getReturnValue()) {
         EMIFavouriteAEKeyCache.recalculate();
      }
   }

   @Inject(method = "addFavoriteAt", at = @At("TAIL"), remap = false)
   private static void onAddFavoriteAt(EmiIngredient var0, int var1, CallbackInfo var2) {
      EMIFavouriteAEKeyCache.recalculate();
   }
}
