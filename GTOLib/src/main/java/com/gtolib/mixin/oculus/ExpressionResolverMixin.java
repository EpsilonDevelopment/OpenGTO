package com.gtolib.mixin.oculus;

import com.llamalad7.mixinextras.sugar.Local;
import java.util.function.Function;
import kroppeb.stareval.function.Type;
import kroppeb.stareval.resolver.ExpressionResolver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ExpressionResolver.class)
public class ExpressionResolverMixin {
   @Redirect(
      method = "resolveExpressionInternal",
      at = @At(value = "INVOKE", target = "Ljava/util/function/Function;apply(Ljava/lang/Object;)Ljava/lang/Object;"),
      remap = false
   )
   public Object resolveExpressionInternal$apply(Function var1, Object var2, @Local(argsOnly = true) Type var3) {
      Object var4 = var1.apply(var2);
      if (var4 == null) {
         var4 = var3;
      }

      return var4;
   }
}
