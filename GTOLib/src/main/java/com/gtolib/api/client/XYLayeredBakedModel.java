package com.gtolib.api.client;

import com.lowdragmc.lowdraglib.client.bakedpipeline.Quad;
import com.lowdragmc.lowdraglib.client.bakedpipeline.Submap;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class XYLayeredBakedModel extends BakedModelWrapper<BakedModel> {
   private static final ModelProperty<BlockPos> POS = new ModelProperty<>();
   public static final ModelProperty<ModelData> PARENT_MODEL_DATA = new ModelProperty<>();
   private final List<XYLayeredBakedModel.Layer> layers;

   public XYLayeredBakedModel(BakedModel originalModel, List<XYLayeredBakedModel.Layer> layers) {
      super(originalModel);
      this.layers = layers;
   }

   @NotNull
   @Override
   public ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData modelData) {
      ModelData parentModelData = this.originalModel.getModelData(level, pos, state, modelData);
      return parentModelData.derive().with(POS, pos).with(PARENT_MODEL_DATA, parentModelData).build();
   }

   @NotNull
   @Override
   public List<BakedQuad> getQuads(
      @Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType renderType
   ) {
      ModelData parentModelData = extraData.get(PARENT_MODEL_DATA);
      if (parentModelData == null) {
         parentModelData = ModelData.EMPTY;
      }

      if (this.layers.isEmpty()) {
         return this.originalModel.getQuads(state, side, rand, parentModelData, renderType);
      }

      List<BakedQuad> baseQuads = this.originalModel.getQuads(state, side, rand, parentModelData, null);
      if (this.layers.isEmpty()) {
         return baseQuads;
      }

      BlockPos pos = extraData.get(POS);
      if (pos == null) {
         pos = BlockPos.ZERO;
      }

      List<List<BakedQuad>> matchedByLayer = new ArrayList<>(this.layers.size());
      int addedQuadCount = 0;
      boolean removesSource = false;

      for (XYLayeredBakedModel.Layer layer : this.layers) {
         List<BakedQuad> matched = new ArrayList<>();

         for (BakedQuad quad : baseQuads) {
            if (layer.matches(quad)) {
               matched.add(quad);
            }
         }

         matchedByLayer.add(matched);
         if (!matched.isEmpty()) {
            addedQuadCount += matched.size();
            removesSource |= layer.replaceSource();
         }
      }

      if (addedQuadCount == 0) {
         return baseQuads;
      }

      List<BakedQuad> result = new ArrayList<>(baseQuads.size() + addedQuadCount);
      if (removesSource) {
         for (BakedQuad quad : baseQuads) {
            if (!this.isSourceReplaced(quad)) {
               result.add(quad);
            }
         }
      } else {
         result.addAll(baseQuads);
      }

      int modelX;
      int modelY;
      if (side != null) {
         switch (side) {
            case DOWN:
            case UP:
               modelX = pos.getX();
               modelY = pos.getZ();
               break;
            case SOUTH:
               modelX = pos.getX();
               modelY = -pos.getY();
               break;
            case NORTH:
               modelX = -pos.getX();
               modelY = -pos.getY();
               break;
            case WEST:
               modelX = pos.getZ();
               modelY = -pos.getY();
               break;
            case EAST:
               modelX = -pos.getZ();
               modelY = -pos.getY();
               break;
            default:
               throw new IllegalStateException("Unexpected value: " + side);
         }
      } else {
         modelX = pos.getX();
         modelY = pos.getY();
      }

      for (int i = 0; i < this.layers.size(); i++) {
         XYLayeredBakedModel.Layer layer = this.layers.get(i);
         List<BakedQuad> matched = matchedByLayer.get(i);
         if (!matched.isEmpty()) {
            Submap submap = layer.submapForXY(modelX, modelY);

            for (BakedQuad quad : matched) {
               result.add(Quad.from(quad, layer.depthOffset()).transformUVs(layer.targetSprite(), submap).rebake());
            }
         }
      }

      return result;
   }

   @Override
   public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
      return this.getQuads(state, side, rand, ModelData.EMPTY, null);
   }

   private boolean isSourceReplaced(BakedQuad quad) {
      for (XYLayeredBakedModel.Layer layer : this.layers) {
         if (layer.replaceSource() && layer.matches(quad)) {
            return true;
         }
      }

      return false;
   }

   private static boolean sameSprite(TextureAtlasSprite first, TextureAtlasSprite second) {
      return first == second || first.atlasLocation().equals(second.atlasLocation()) && first.contents().name().equals(second.contents().name());
   }

   public record Layer(
      TextureAtlasSprite sourceSprite,
      TextureAtlasSprite targetSprite,
      @Nullable Integer tintIndex,
      int columns,
      int rows,
      boolean wrap,
      boolean replaceSource,
      float depthOffset
   ) {
      public boolean matches(BakedQuad quad) {
         return !XYLayeredBakedModel.sameSprite(quad.getSprite(), this.sourceSprite) ? false : this.tintIndex == null || quad.getTintIndex() == this.tintIndex;
      }

      public Submap submapForXY(int x, int y) {
         int total = this.columns * this.rows;
         if (total == 1) {
            return Submap.FULL_TEXTURE;
         }

         int indexX = this.wrap ? Math.floorMod(x, this.columns) : Mth.clamp(x, 0, this.columns - 1);
         int indexY = this.wrap ? Math.floorMod(y, this.rows) : Mth.clamp(y, 0, this.rows - 1);
         int column = indexX % this.columns;
         int row = indexY % this.rows;
         float width = 16.0F / this.columns;
         float height = 16.0F / this.rows;
         return new Submap(width, height, column * width, row * height);
      }
   }
}
