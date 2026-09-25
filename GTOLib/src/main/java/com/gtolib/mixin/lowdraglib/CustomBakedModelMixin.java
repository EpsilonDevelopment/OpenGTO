package com.gtolib.mixin.lowdraglib;

import com.gto.fastcollection.cache.primitive.ByteCache;
import com.lowdragmc.lowdraglib.client.bakedpipeline.Quad;
import com.lowdragmc.lowdraglib.client.bakedpipeline.Submap;
import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import com.lowdragmc.lowdraglib.client.model.custommodel.Connections;
import com.lowdragmc.lowdraglib.client.model.custommodel.CustomBakedModel;
import com.lowdragmc.lowdraglib.client.model.custommodel.LDLMetadataSection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CustomBakedModel.class)
public abstract class CustomBakedModelMixin {
   @Shadow(remap = false)
   @Final
   protected List<BakedQuad> noSideCache;
   @Mutable
   @Shadow(remap = false)
   @Final
   protected ConcurrentMap<Direction, ConcurrentMap<Connections, List<BakedQuad>>> sideCache;
   @Shadow(remap = false)
   @Final
   protected BakedModel parent;
   @Unique
   private ByteCache<List<BakedQuad>>[] gtolib$Cache;

   @Shadow(remap = false)
   protected static Quad makeQuad(BakedQuad var0, LDLMetadataSection var1, float var2) {
      return null;
   }

   @Inject(method = "<init>", at = @At("TAIL"), remap = false)
   public void init(BakedModel var1, CallbackInfo var2) {
      this.sideCache = null;
      this.gtolib$Cache = new ByteCache[6];
   }

   @Nonnull
   @Overwrite(remap = false)
   public List<BakedQuad> getCustomQuads(BlockAndTintGetter var1, BlockPos var2, @Nonnull BlockState var3, @Nullable Direction var4, RandomSource var5) {
      Connections var6 = Connections.checkConnections(var1, var2, var3, var4);
      if (var4 == null) {
         if (this.noSideCache.isEmpty()) {
            synchronized (this.noSideCache) {
               if (this.noSideCache.isEmpty()) {
                  this.noSideCache.addAll(buildCustomQuads(var6, this.parent.getQuads(var3, null, var5), 0.0F));
               }
            }
         }

         return this.noSideCache;
      } else {
         int var7 = var4.ordinal();
         ByteCache var8 = this.gtolib$Cache[var7];
         if (var8 == null) {
            var8 = new ByteCache();
            this.gtolib$Cache[var7] = var8;
         }

         return (List<BakedQuad>)var8.getCache((byte)var6.hashCode(), var5x -> buildCustomQuads(var6, this.parent.getQuads(var3, var4, var5), 0.0F));
      }
   }

   @Overwrite(remap = false)
   public static List<BakedQuad> buildCustomQuads(Connections var0, List<BakedQuad> var1, float var2) {
      LinkedList var3 = new LinkedList();

      for (BakedQuad var5 : var1) {
         LDLMetadataSection var6 = LDLMetadataSection.getMetadata(var5.getSprite());
         TextureAtlasSprite var7 = var6.connection == null ? null : ModelFactory.getBlockSprite(var6.connection);
         if (var7 == null) {
            var3.add(makeQuad(var5, var6, var2).rebake());
         } else {
            int[] var8 = var0.getSubmapIndices();

            for (Quad var12 : makeQuad(var5, var6, var2).derotate().subdivide(4)) {
               if (var12 != null) {
                  int var13 = var12.getUvs().normalize().getQuadrant();
                  Quad var14 = var12.grow().transformUVs(var8[var13] > 15 ? var5.getSprite() : var7, Submap.uvs[var8[var13]]);
                  if (var14 != null) {
                     var3.add(var14.rebake());
                  }
               }
            }
         }
      }

      return var3;
   }
}
