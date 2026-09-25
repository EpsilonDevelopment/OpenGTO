package com.gtolib.mixin.lowdraglib;

import com.lowdragmc.lowdraglib.client.model.custommodel.LDLMetadataSection;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LDLMetadataSection.class)
public class LDLMetadataSectionMixin {
   @Shadow(remap = false)
   @Final
   @Mutable
   private static Map<ResourceLocation, LDLMetadataSection> METADATA_CACHE;

   @Overwrite(remap = false)
   public static void clearCache() {
   }

   @Overwrite(remap = false)
   public static LDLMetadataSection getMetadata(ResourceLocation var0) {
      return METADATA_CACHE.computeIfAbsent(var0, var1 -> {
         LDLMetadataSection var2 = LDLMetadataSection.MISSING;
         Optional var3 = Minecraft.getInstance().getResourceManager().getResource(var0);
         if (var3.isPresent()) {
            Resource var4 = (Resource)var3.get();

            try {
               var2 = var4.metadata().getSection(LDLMetadataSectionAccessor.instance()).get();
            } catch (Exception var6) {
            }
         }

         return var2;
      });
   }
}
