package com.gtolib.mixin.farmersdelight;

import com.gtolib.api.item.IItem;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ToolAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import vectorwing.farmersdelight.common.crafting.ingredient.ToolActionIngredient;

@Mixin(ToolActionIngredient.class)
public class ToolActionIngredientMixin {
   @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/util/Collection;stream()Ljava/util/stream/Stream;"), remap = false)
   private static Stream<Item> redirect(Collection<Item> var0, @Local(argsOnly = true) ToolAction var1) {
      return var0.stream().filter(var1x -> var1x.canPerformAction(ItemStack.EMPTY, var1));
   }

   @Redirect(
      method = "<init>",
      at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;map(Ljava/util/function/Function;)Ljava/util/stream/Stream;", ordinal = 0),
      remap = false
   )
   private static Stream<ItemStack> redirect(Stream<Item> var0, Function<? super Item, ? extends ItemStack> var1) {
      return var0.map(var0x -> ((IItem)var0x).gtolib$getReadOnlyStack());
   }
}
