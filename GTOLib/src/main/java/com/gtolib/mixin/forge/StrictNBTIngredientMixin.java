package com.gtolib.mixin.forge;

import com.gtolib.utils.EmptyStream;
import java.util.stream.Stream;
import net.minecraft.world.item.crafting.Ingredient.Value;
import net.minecraftforge.common.crafting.StrictNBTIngredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(StrictNBTIngredient.class)
public class StrictNBTIngredientMixin {
   @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;of(Ljava/lang/Object;)Ljava/util/stream/Stream;"), remap = false)
   private static Stream<? extends Value> of(Object var0) {
      return EmptyStream.create(new Value[]{(Value)var0});
   }
}
