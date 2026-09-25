package com.gtolib.api.emi.stack;

import appeng.api.client.AEKeyRenderHandler;
import appeng.api.stacks.AEFluidKey;
import appeng.client.gui.style.FluidBlitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKey;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.utils.memoization.GTMemoizer;
import com.gtocore.common.data.GTOMaterials;
import com.gtolib.GTOCore;
import com.gtolib.api.ae2.stacks.TagPrefixKey;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

public class TagPrefixRenderer {
   @OnlyIn(Dist.CLIENT)
   private static Minecraft minecraft;
   private static final Supplier<ItemStack> TinIngotStack = GTMemoizer.memoize(() -> ChemicalHelper.get(TagPrefix.ingot, GTMaterials.Tin, 1));
   private static final Supplier<Map<MaterialIconType, Fluid>> fluidRenderSamples = GTMemoizer.memoize(() -> {
      Builder<MaterialIconType, Fluid> builder = ImmutableMap.builder();
      builder.put(MaterialIconType.liquid, GTMaterials.Tin.getFluid());
      builder.put(MaterialIconType.molten, GTOMaterials.HastelloyN.getFluid(FluidStorageKeys.MOLTEN));
      builder.put(MaterialIconType.plasma, GTMaterials.Silver.getFluid(FluidStorageKeys.PLASMA));
      builder.put(MaterialIconType.gas, GTOMaterials.Aether.getFluid(FluidStorageKeys.GAS));
      return builder.build();
   });

   @OnlyIn(Dist.CLIENT)
   public static void render(GuiGraphics draw, int x, int y, float partialTicks, TagPrefix tagPrefix) {
      MaterialIconType type = tagPrefix.materialIconType();
      boolean isOre = type == MaterialIconType.ore;
      boolean isCable = tagPrefix.name.startsWith("wireGt") || tagPrefix.name.startsWith("cableGt");
      boolean isFluidPipe = tagPrefix.name.startsWith("pipe") && tagPrefix.name.endsWith("Fluid");
      boolean isItemPipe = tagPrefix.name.startsWith("pipe") && !isFluidPipe;
      if (isOre) {
         draw.renderItem(TagPrefix.ORES.get(tagPrefix).stoneType().get().getBlock().asItem().getDefaultInstance(), x, y);
      } else if (isCable || isItemPipe) {
         draw.renderItem(ChemicalHelper.get(tagPrefix, GTMaterials.Tin), x, y);
      } else if (isFluidPipe) {
         draw.renderItem(ChemicalHelper.get(tagPrefix, GTMaterials.StainlessSteel), x, y);
      } else if (type == null) {
         ItemStack woodenItem = ChemicalHelper.get(tagPrefix, GTMaterials.TreatedWood);
         if (!woodenItem.isEmpty()) {
            draw.renderItem(woodenItem, x, y);
         }
      } else {
         if (minecraft == null) {
            minecraft = Minecraft.getInstance();
         }

         BakedModel model = minecraft.getModelManager().getModel(GTOCore.id("item/" + tagPrefix.getLowerCaseName()));
         if (model == minecraft.getModelManager().getMissingModel()) {
            GTOCore.LOGGER.error("Missing model for tagprefix {}", tagPrefix);
         } else {
            draw.pose().pushPose();
            draw.pose().translate(x + 8, y + 8, 150.0F);

            try {
               draw.pose().mulPoseMatrix(new Matrix4f().scaling(1.0F, -1.0F, 1.0F));
               draw.pose().scale(16.0F, 16.0F, 16.0F);
               boolean flag = !model.usesBlockLight();
               if (flag) {
                  Lighting.setupForFlatItems();
               }

               ItemStack stackToRender = TinIngotStack.get();
               minecraft.getItemRenderer()
                  .render(stackToRender, ItemDisplayContext.GUI, false, draw.pose(), draw.bufferSource(), 15728880, OverlayTexture.NO_OVERLAY, model);
               draw.flush();
               if (flag) {
                  Lighting.setupFor3DItems();
               }
            } catch (Throwable throwable) {
               GTOCore.LOGGER.error("Error rendering tagprefix stack for tagprefix {}", tagPrefix, throwable);
            }

            draw.pose().popPose();
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static void render(GuiGraphics draw, int x, int y, float partialTicks, FluidStorageKey storageKey) {
      MaterialIconType type = storageKey.getIconType();
      if (type != null) {
         Fluid what = fluidRenderSamples.get().get(type);
         if (what != null) {
            FluidBlitter.create(AEFluidKey.of(what)).dest(x, y, 16, 16).blit(draw);
         }
      }
   }

   public static class AEKeyHandler implements AEKeyRenderHandler<TagPrefixKey> {
      public void drawInGui(Minecraft minecraft, GuiGraphics guiGraphics, int x, int y, TagPrefixKey stack) {
         if (stack.isFluidKey()) {
            TagPrefixRenderer.render(guiGraphics, x, y, 0.0F, stack.getStorageKey());
         } else {
            TagPrefixRenderer.render(guiGraphics, x, y, 0.0F, stack.getPrefix());
         }
      }

      public void drawOnBlockFace(PoseStack poseStack, MultiBufferSource buffers, TagPrefixKey what, float scale, int combinedLight, Level level) {
      }

      public Component getDisplayName(TagPrefixKey stack) {
         return stack.getDisplayName();
      }
   }
}
