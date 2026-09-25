package com.gtolib.api.ae2.gui;

import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.TextureTransform;
import com.gtolib.GTOCore;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class BlitterGroup {
   private final Blitter head;
   private final Blitter tail;
   private final Blitter middleSegment;
   private int r = 255;
   private int g = 255;
   private int b = 255;
   private int a = 255;
   private Rect2i destRect = new Rect2i(0, 0, 0, 0);
   private boolean blending = true;
   private TextureTransform transform = TextureTransform.NONE;
   private int zOffset;

   BlitterGroup(Blitter head, Blitter tail, Blitter middleSegment) {
      this.head = head;
      this.tail = tail;
      this.middleSegment = middleSegment;
   }

   public static BlitterGroup createFromSplittingBlitter(Blitter blitter, int headHeight, int tailHeight) {
      int srcX = blitter.getSrcX();
      int srcY = blitter.getSrcY();
      int srcWidth = blitter.getSrcWidth();
      int srcHeight = blitter.getSrcHeight();
      return new BlitterGroup(
         blitter.copy().src(srcX, srcY, srcWidth, headHeight),
         blitter.copy().src(srcX, srcY + srcHeight - tailHeight, srcWidth, tailHeight),
         blitter.copy().src(srcX, srcY + headHeight, srcWidth, 1)
      );
   }

   public BlitterGroup dest(int x, int y) {
      this.destRect = new Rect2i(x, y, 0, 0);
      return this;
   }

   public void vBlit(GuiGraphics gui, int segments) {
      if (segments > 0) {
         this.vBlit(gui, this.head);
         if (segments > 1) {
            for (int i = 0; i < segments - 2; i++) {
               this.vBlit(gui, this.middleSegment);
            }
         }

         this.vBlit(gui, this.tail);
      }
   }

   public void vBlitFixedHeight(GuiGraphics gui, int fixedHeight) {
      if (fixedHeight <= this.head.getSrcHeight() + this.tail.getSrcHeight()) {
         GTOCore.LOGGER
            .warn(
               "Fixed height {} is too small for the blitter group with head height {} and tail height {}",
               fixedHeight,
               this.head.getSrcHeight(),
               this.tail.getSrcHeight()
            );
      } else {
         int segments = fixedHeight - this.head.getSrcHeight() - this.tail.getSrcHeight();
         this.vBlit(gui, segments);
      }
   }

   private void vBlit(GuiGraphics gui, Blitter blitter) {
      gui.pose().pushPose();
      gui.pose().translate(0.0F, 0.0F, this.zOffset);
      blitter.dest(this.destRect).color(this.r, this.g, this.b, this.a).blending(this.blending).transform(this.transform).blit(gui);
      gui.pose().popPose();
      this.destRect = new Rect2i(this.destRect.getX(), this.destRect.getY() + blitter.getSrcHeight(), this.destRect.getWidth(), 0);
   }

   public BlitterGroup copy() {
      BlitterGroup bg = new BlitterGroup(this.head.copy(), this.tail.copy(), this.middleSegment.copy());
      bg.r = this.r;
      bg.g = this.g;
      bg.b = this.b;
      bg.a = this.a;
      bg.destRect = this.destRect;
      bg.blending = this.blending;
      bg.transform = this.transform;
      bg.zOffset = this.zOffset;
      return bg;
   }
}
