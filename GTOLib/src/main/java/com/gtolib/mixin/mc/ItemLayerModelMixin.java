package com.gtolib.mixin.mc;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(value = ItemModelGenerator.class, priority = 0)
public final class ItemLayerModelMixin {
   @Shadow
   @Final
   @Mutable
   public static List<String> LAYERS;

   @Inject(method = "<clinit>", at = @At("RETURN"))
   private static void extendLayers(CallbackInfo var0) {
      LAYERS.clear();
      ArrayList var1 = Lists.newArrayList();

      for (int var2 = 0; var2 < 10; var2++) {
         var1.add("layer" + var2);
      }

      LAYERS.addAll(var1);
   }
}
