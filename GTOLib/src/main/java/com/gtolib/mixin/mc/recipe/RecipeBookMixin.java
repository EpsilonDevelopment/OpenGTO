package com.gtolib.mixin.mc.recipe;

import net.minecraft.stats.RecipeBook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RecipeBook.class, priority = 0)
public class RecipeBookMixin {
   @Inject(method = "copyOverData", at = @At("HEAD"), cancellable = true)
   private void copyOverData(RecipeBook var1, CallbackInfo var2) {
      var2.cancel();
   }
}
