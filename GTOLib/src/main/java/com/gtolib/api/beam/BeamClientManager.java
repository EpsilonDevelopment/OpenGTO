package com.gtolib.api.beam;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.ArrayList;
import java.util.Collection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public final class BeamClientManager {
   private static final int[] COLOR_WAVELENGTHS = new int[]{300, 425, 550, 675, 800};
   private static final int[] WAVELENGTH_COLORS = new int[]{8388863, 255, 65280, 16776960, 16711680};
   private static final double MIN_SEGMENT_LENGTH_SQUARED = 1.0E-12;
   private static boolean initialized;

   private BeamClientManager() {
   }

   public static synchronized void init() {
      if (!initialized) {
         initialized = true;
         MinecraftForge.EVENT_BUS.addListener(BeamClientManager::renderLevel);
      }
   }

   private static void renderLevel(RenderLevelStageEvent event) {
      if (event.getStage() == Stage.AFTER_TRANSLUCENT_BLOCKS) {
         ClientLevel level = Minecraft.getInstance().level;
         if (level != null) {
            BeamManager manager = BeamManager.getIfPresent(level);
            if (manager != null) {
               Collection<Beam> beams = manager.beams();
               if (!beams.isEmpty()) {
                  PoseStack poseStack = event.getPoseStack();
                  Vec3 cameraPosition = event.getCamera().getPosition();
                  poseStack.pushPose();
                  poseStack.translate(-cameraPosition.x, -cameraPosition.y, -cameraPosition.z);
                  RenderSystem.enableBlend();
                  RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
                  RenderSystem.depthMask(false);
                  RenderSystem.disableCull();
                  RenderSystem.setShader(GameRenderer::getPositionColorShader);
                  Tesselator tesselator = Tesselator.getInstance();
                  BufferBuilder buffer = tesselator.getBuilder();
                  buffer.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                  Matrix4f matrix = poseStack.last().pose();

                  for (Beam beam : beams) {
                     BeamClientManager.RenderData renderData = renderData(beam);

                     for (BeamClientManager.RenderSegment segment : renderData.segments) {
                        if (event.getFrustum().isVisible(segment.bounds)) {
                           renderSegment(matrix, buffer, segment);
                        }
                     }
                  }

                  tesselator.end();
                  RenderSystem.enableCull();
                  RenderSystem.depthMask(true);
                  RenderSystem.defaultBlendFunc();
                  RenderSystem.disableBlend();
                  poseStack.popPose();
               }
            }
         }
      }
   }

   private static void renderSegment(Matrix4f matrix, BufferBuilder buffer, BeamClientManager.RenderSegment segment) {
      putVertex(buffer, matrix, segment.startPositive, segment.red, segment.green, segment.blue, segment.startAlpha);
      putVertex(buffer, matrix, segment.end, segment.red, segment.green, segment.blue, segment.endAlpha);
      putVertex(buffer, matrix, segment.end, segment.red, segment.green, segment.blue, segment.endAlpha);
      putVertex(buffer, matrix, segment.startNegative, segment.red, segment.green, segment.blue, segment.startAlpha);
   }

   private static BeamClientManager.RenderData renderData(Beam beam) {
      if (beam.clientRenderData() instanceof BeamClientManager.RenderData data) {
         return data;
      } else {
         BeamClientManager.RenderData data = BeamClientManager.RenderData.create(beam);
         beam.clientRenderData(data);
         return data;
      }
   }

   private static double halfWidth(double intensity) {
      return Math.log(intensity + 1.0) * 0.025 / 16.0;
   }

   private static float alpha(double intensity) {
      return Math.max((float)(0.8 - 0.8 * Math.exp(-intensity * 0.005 / 16.0)), 0.02F);
   }

   private static void putVertex(BufferBuilder buffer, Matrix4f matrix, Vec3 position, float red, float green, float blue, float alpha) {
      buffer.vertex(matrix, (float)position.x, (float)position.y, (float)position.z).color(red, green, blue, alpha).endVertex();
   }

   private static Vec3 rotateAroundAxis(Vec3 vector, Vec3 axis, float angle) {
      if (Math.abs(angle) <= 1.0E-7F) {
         return vector;
      }

      double sin = Math.sin(angle);
      double cos = Math.cos(angle);
      return vector.scale(cos).add(axis.cross(vector).scale(sin)).add(axis.scale(axis.dot(vector) * (1.0 - cos)));
   }

   public static int wavelengthToColor(int wavelength) {
      if (wavelength <= COLOR_WAVELENGTHS[0]) {
         return WAVELENGTH_COLORS[0];
      }

      int last = COLOR_WAVELENGTHS.length - 1;
      if (wavelength >= COLOR_WAVELENGTHS[last]) {
         return WAVELENGTH_COLORS[last];
      }

      for (int i = 0; i < last; i++) {
         int fromWavelength = COLOR_WAVELENGTHS[i];
         int toWavelength = COLOR_WAVELENGTHS[i + 1];
         if (wavelength <= toWavelength) {
            float progress = (float)(wavelength - fromWavelength) / (toWavelength - fromWavelength);
            return interpolateColor(WAVELENGTH_COLORS[i], WAVELENGTH_COLORS[i + 1], progress);
         }
      }

      return WAVELENGTH_COLORS[last];
   }

   private static int interpolateColor(int from, int to, float progress) {
      int red = (int)Mth.lerp(progress, from >> 16 & 0xFF, to >> 16 & 0xFF);
      int green = (int)Mth.lerp(progress, from >> 8 & 0xFF, to >> 8 & 0xFF);
      int blue = (int)Mth.lerp(progress, from & 0xFF, to & 0xFF);
      return red << 16 | green << 8 | blue;
   }

   private record RenderData(BeamClientManager.RenderSegment[] segments) {
      private static BeamClientManager.RenderData create(Beam beam) {
         ArrayList<BeamClientManager.RenderSegment> segments = new ArrayList<>(beam.segments().size());

         for (BeamSegment segment : beam.segments()) {
            BeamClientManager.RenderSegment renderSegment = BeamClientManager.RenderSegment.create(segment);
            if (renderSegment != null) {
               segments.add(renderSegment);
            }
         }

         return new BeamClientManager.RenderData(segments.toArray(BeamClientManager.RenderSegment[]::new));
      }
   }

   private record RenderSegment(
      AABB bounds, Vec3 startPositive, Vec3 end, Vec3 startNegative, float red, float green, float blue, float startAlpha, float endAlpha
   ) {
      @Nullable
      private static BeamClientManager.RenderSegment create(BeamSegment segment) {
         BeamProperties properties = segment.start.propertiesSnapshot();
         if (properties.intensity <= 0L) {
            return null;
         } else {
            Vec3 start = segment.start.position();
            Vec3 end = segment.endPosition;
            Vec3 offset = end.subtract(start);
            double lengthSquared = offset.lengthSqr();
            if (lengthSquared <= 1.0E-12) {
               return null;
            } else {
               Vec3 direction = offset.scale(1.0 / Math.sqrt(lengthSquared));
               double maxHalfWidth = BeamClientManager.halfWidth(properties.intensity);
               if (Double.isFinite(maxHalfWidth) && !(maxHalfWidth <= 0.0)) {
                  Vec3 reference = Math.abs(direction.y) < 0.999 ? new Vec3(0.0, 1.0, 0.0) : new Vec3(1.0, 0.0, 0.0);
                  Vec3 lateral = direction.cross(reference).normalize();
                  Vec3 polarizedLateral = BeamClientManager.rotateAroundAxis(lateral, direction, properties.polarization);
                  Vec3 startLateral = polarizedLateral.scale(maxHalfWidth);
                  int color = BeamClientManager.wavelengthToColor(properties.waveLength);
                  float red = (color >> 16 & 0xFF) / 255.0F;
                  float green = (color >> 8 & 0xFF) / 255.0F;
                  float blue = (color & 0xFF) / 255.0F;
                  return new BeamClientManager.RenderSegment(
                     new AABB(start, end).inflate(maxHalfWidth),
                     start.add(startLateral),
                     end,
                     start.subtract(startLateral),
                     red,
                     green,
                     blue,
                     BeamClientManager.alpha(properties.intensity),
                     BeamClientManager.alpha(0.0)
                  );
               } else {
                  return null;
               }
            }
         }
      }
   }
}
