package com.gtolib.mixin.oculus;

import com.gtolib.api.data.GTODimensions;
import com.gtolib.utils.RLUtils;
import earth.terrarium.adastra.api.planets.PlanetApi;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.shaderpack.DimensionId;
import net.irisshaders.iris.shaderpack.materialmap.NamespacedId;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Iris.class)
public class IrisMixin {
   @Shadow(remap = false)
   private static boolean initialized;
   @Shadow(remap = false)
   public static NamespacedId lastDimension;

   @ModifyArg(
      method = "createPipeline",
      at = @At(
         value = "INVOKE",
         target = "Lnet/irisshaders/iris/shaderpack/ShaderPack;getProgramSet(Lnet/irisshaders/iris/shaderpack/materialmap/NamespacedId;)Lnet/irisshaders/iris/shaderpack/programs/ProgramSet;"
      ),
      remap = false
   )
   private static NamespacedId getProgramSet(NamespacedId var0) {
      return GTODimensions.PLANET_NAMESPACE.contains(var0.getNamespace())
            && PlanetApi.API.isSpace(GTODimensions.getDimensionKey(RLUtils.fromNamespaceAndPath(var0.getNamespace(), var0.getName())))
         ? DimensionId.END
         : var0;
   }

   @Overwrite(remap = false)
   public static void onLoadingComplete() {
      if (initialized) {
         lastDimension = DimensionId.OVERWORLD;
      }
   }
}
