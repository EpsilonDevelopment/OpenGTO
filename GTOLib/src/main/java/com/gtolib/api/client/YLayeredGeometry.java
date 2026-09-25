package com.gtolib.api.client;

import com.google.gson.JsonParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.BlockGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import net.minecraftforge.client.model.geometry.UnbakedGeometryHelper;

public final class YLayeredGeometry implements IUnbakedGeometry<YLayeredGeometry> {
   private final List<YLayeredGeometry.LayerSpec> layerSpecs;

   public YLayeredGeometry(List<YLayeredGeometry.LayerSpec> layerSpecs) {
      this.layerSpecs = layerSpecs;
   }

   @Override
   public BakedModel bake(
      IGeometryBakingContext context,
      ModelBaker baker,
      Function<Material, TextureAtlasSprite> spriteGetter,
      ModelState modelState,
      ItemOverrides overrides,
      ResourceLocation modelLocation
   ) {
      if (!(context instanceof BlockGeometryBakingContext blockContext)) {
         throw new IllegalStateException("gtocore:y_layered requires a block model baking context");
      } else {
         BlockModel owner = blockContext.owner;
         if (owner.parent == null) {
            throw new IllegalStateException("gtocore:y_layered requires a parent model to supply base geometry");
         }

         ModelState bakedState = UnbakedGeometryHelper.composeRootTransformIntoModelState(modelState, context.getRootTransform());
         BlockModel proxyModel = createProxyModel(owner, context);
         BakedModel baseModel = proxyModel.bake(baker, proxyModel, spriteGetter, bakedState, modelLocation, context.isGui3d());
         List<XYLayeredBakedModel.Layer> bakedLayers = new ArrayList<>(this.layerSpecs.size());

         for (YLayeredGeometry.LayerSpec layerSpec : this.layerSpecs) {
            bakedLayers.add(layerSpec.bake(owner, spriteGetter));
         }

         return new XYLayeredBakedModel(baseModel, List.copyOf(bakedLayers));
      }
   }

   private static BlockModel createProxyModel(BlockModel owner, IGeometryBakingContext context) {
      BlockModel proxyModel = new BlockModel(
         owner.getParentLocation(),
         Collections.emptyList(),
         new HashMap<>(owner.textureMap),
         owner.hasAmbientOcclusion,
         owner.getGuiLight(),
         owner.getTransforms(),
         List.copyOf(owner.getOverrides())
      );
      proxyModel.parent = cloneParentChain(owner.parent, new IdentityHashMap<>());
      proxyModel.name = owner.name + "#gtocore_y_layered";
      if (context.getRenderTypeHint() != null) {
         proxyModel.customData.setRenderTypeHint(context.getRenderTypeHint());
      }

      if (context.getRenderTypeFastHint() != null) {
         proxyModel.customData.setRenderTypeFastHint(context.getRenderTypeFastHint());
      }

      proxyModel.customData.setGui3d(context.isGui3d());
      return proxyModel;
   }

   private static BlockModel cloneParentChain(BlockModel original, Map<BlockModel, BlockModel> clones) {
      if (original == null) {
         return null;
      }

      BlockModel existing = clones.get(original);
      if (existing != null) {
         return existing;
      }

      BlockModel clone = new BlockModel(
         original.getParentLocation(),
         List.copyOf(original.getElements()),
         new HashMap<>(original.textureMap),
         original.hasAmbientOcclusion,
         original.getGuiLight(),
         original.getTransforms(),
         List.copyOf(original.getOverrides())
      );
      clone.name = original.name + "#gtocore_y_layered_clone";
      clones.put(original, clone);
      clone.parent = cloneParentChain(original.parent, clones);
      return clone;
   }

   static final class LayerSpec {
      private final String source;
      private final String texture;
      private final Integer tintIndex;
      private final int columns;
      private final int rows;
      private final boolean wrap;
      private final boolean replaceSource;
      private final float depthOffset;

      LayerSpec(String source, String texture, Integer tintIndex, int columns, int rows, boolean wrap, boolean replaceSource, float depthOffset) {
         this.source = source;
         this.texture = texture;
         this.tintIndex = tintIndex;
         this.columns = columns;
         this.rows = rows;
         this.wrap = wrap;
         this.replaceSource = replaceSource;
         this.depthOffset = depthOffset;
      }

      XYLayeredBakedModel.Layer bake(BlockModel owner, Function<Material, TextureAtlasSprite> spriteGetter) {
         return new XYLayeredBakedModel.Layer(
            resolveSprite(owner, spriteGetter, this.source),
            resolveSprite(owner, spriteGetter, this.texture),
            this.tintIndex,
            this.columns,
            this.rows,
            this.wrap,
            this.replaceSource,
            this.depthOffset
         );
      }

      private static TextureAtlasSprite resolveSprite(BlockModel owner, Function<Material, TextureAtlasSprite> spriteGetter, String reference) {
         if (isTextureKey(reference)) {
            String key = reference.startsWith("#") ? reference.substring(1) : reference;
            return spriteGetter.apply(owner.getMaterial(key));
         } else {
            ResourceLocation texture = ResourceLocation.tryParse(reference);
            if (texture == null) {
               throw new JsonParseException("Invalid texture reference for gtocore:y_layered: " + reference);
            } else {
               return spriteGetter.apply(new Material(TextureAtlas.LOCATION_BLOCKS, texture));
            }
         }
      }

      private static boolean isTextureKey(String reference) {
         return reference.startsWith("#") || !reference.contains(":") && !reference.contains("/");
      }
   }
}
