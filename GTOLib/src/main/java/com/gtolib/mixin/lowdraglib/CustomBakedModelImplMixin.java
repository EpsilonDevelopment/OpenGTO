package com.gtolib.mixin.lowdraglib;

import com.gtolib.utils.register.BlockRegisterUtils;
import com.lowdragmc.lowdraglib.client.model.custommodel.CustomBakedModel;
import com.lowdragmc.lowdraglib.client.model.forge.CustomBakedModelImpl;
import com.lowdragmc.lowdraglib.client.model.forge.LDLRendererModel.RendererBakedModel;
import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CustomBakedModelImpl.class)
public abstract class CustomBakedModelImplMixin extends CustomBakedModel {
   @Shadow(remap = false)
   @NotNull
   public abstract List<BakedQuad> getCustomQuads(
      BlockAndTintGetter var1, BlockPos var2, @NotNull BlockState var3, @Nullable Direction var4, RandomSource var5, ModelData var6, @Nullable RenderType var7
   );

   public CustomBakedModelImplMixin(BakedModel var1) {
      super(var1);
   }

   @Overwrite(remap = false)
   @NotNull
   @Override
   public List<BakedQuad> getQuads(
      @Nullable BlockState var1, @Nullable Direction var2, @NotNull RandomSource var3, @NotNull ModelData var4, @Nullable RenderType var5
   ) {
      BlockAndTintGetter var6 = var4.get(RendererBakedModel.WORLD);
      BlockPos var7 = var4.get(RendererBakedModel.POS);
      if (var1 != null && BlockRegisterUtils.CUSTOM_MODEL_DATA_BLOCKS.contains(var1.getBlock())) {
         ModelData var8 = var4.get(RendererBakedModel.MODEL_DATA);
         if (var8 == null) {
            var8 = ModelData.EMPTY;
         }

         return var6 != null && var7 != null
            ? this.getCustomQuads(var6, var7, var1, var2, var3, var8, var5)
            : this.parent.getQuads(var1, var2, var3, var8, var5);
      } else {
         return var6 != null && var7 != null && var1 != null ? this.getCustomQuads(var6, var7, var1, var2, var3) : this.parent.getQuads(var1, var2, var3);
      }
   }

   @Overwrite(remap = false)
   @NotNull
   @Override
   public ModelData getModelData(@NotNull BlockAndTintGetter var1, @NotNull BlockPos var2, @NotNull BlockState var3, @NotNull ModelData var4) {
      return BlockRegisterUtils.CUSTOM_MODEL_DATA_BLOCKS.contains(var3.getBlock())
         ? var4.derive()
            .with(RendererBakedModel.WORLD, var1)
            .with(RendererBakedModel.POS, var2)
            .with(RendererBakedModel.MODEL_DATA, this.parent.getModelData(var1, var2, var3, var4))
            .build()
         : var4.derive().with(RendererBakedModel.WORLD, var1).with(RendererBakedModel.POS, var2).build();
   }

   static {
      BlockRegisterUtils.loadCustomModelDataBlocks();
   }
}
