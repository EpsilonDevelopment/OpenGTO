package com.gtolib.mixin.lowdraglib;

import com.gtolib.cache.ClientCacheManager;
import com.lowdragmc.lowdraglib.utils.ResourceHelper;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ResourceHelper.class)
public class ResourceHelperMixin {
   @Overwrite(remap = false)
   public static boolean isResourceExist(ResourceLocation var0) {
      return FMLEnvironment.dist == Dist.CLIENT
         ? ClientCacheManager.RESOURCE_EXIST.computeIfAbsent(var0, var1 -> Minecraft.getInstance().getResourceManager().getResource(var0).isPresent())
         : false;
   }

   @Overwrite(remap = false)
   public static boolean isTextureExist(@Nonnull ResourceLocation var0) {
      ResourceLocation var1 = new ResourceLocation(var0.getNamespace(), "textures/%s.png".formatted(var0.getPath()), null);
      return isResourceExist(var1);
   }

   @Overwrite(remap = false)
   public static boolean isModelExist(@Nonnull ResourceLocation var0) {
      ResourceLocation var1 = new ResourceLocation(var0.getNamespace(), "models/%s.json".formatted(var0.getPath()), null);
      return isResourceExist(var1);
   }
}
