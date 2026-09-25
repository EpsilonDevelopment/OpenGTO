package com.gtolib.mixin.mc.recipe;

import java.util.Collection;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ServerRecipeBook.class, priority = 0)
public class ServerRecipeBookMixin {
   @Inject(method = "addRecipes", at = @At("HEAD"), cancellable = true)
   private void addRecipes(Collection<Recipe<?>> var1, ServerPlayer var2, CallbackInfoReturnable<Integer> var3) {
      var3.setReturnValue(0);
   }

   @Inject(method = "removeRecipes", at = @At("HEAD"), cancellable = true)
   private void removeRecipes(Collection<Recipe<?>> var1, ServerPlayer var2, CallbackInfoReturnable<Integer> var3) {
      var3.setReturnValue(0);
   }

   @Inject(method = "toNbt", at = @At("HEAD"), cancellable = true)
   private void toNbt(CallbackInfoReturnable<CompoundTag> var1) {
      var1.setReturnValue(new CompoundTag());
   }

   @Inject(method = "fromNbt", at = @At("HEAD"), cancellable = true)
   private void fromNbt(CallbackInfo var1) {
      var1.cancel();
   }
}
