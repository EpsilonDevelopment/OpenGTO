package com.gtolib.api.ae2.gui;

import appeng.client.gui.style.Blitter;
import net.minecraft.client.gui.GuiGraphics;

public final class BlitterHelper {
   private final GuiGraphics guiGraphics;
   private static final Blitter SLOTS_GENERIC = Blitter.texture("guis/slots_generic.png", 72, 72);
   private static final Blitter[] SLOTS_GENERICS = new Blitter[]{
      SLOTS_GENERIC.copy().src(0, 0, 9, 9),
      SLOTS_GENERIC.copy().src(9, 0, 18, 9),
      SLOTS_GENERIC.copy().src(27, 0, 9, 9),
      SLOTS_GENERIC.copy().src(0, 9, 9, 18),
      SLOTS_GENERIC.copy().src(9, 9, 18, 18),
      SLOTS_GENERIC.copy().src(27, 9, 9, 18),
      SLOTS_GENERIC.copy().src(0, 27, 9, 9),
      SLOTS_GENERIC.copy().src(9, 27, 18, 9),
      SLOTS_GENERIC.copy().src(27, 27, 9, 9)
   };

   private BlitterHelper(GuiGraphics guiGraphics) {
      this.guiGraphics = guiGraphics;
   }

   public static BlitterHelper of(GuiGraphics guiGraphics) {
      return new BlitterHelper(guiGraphics);
   }

   public void hBlit(Blitter left, Blitter middle, Blitter right, int x, int y, int totalWidth, int totalHeight) {
      int leftWidth = left.getSrcWidth();
      int rightWidth = right.getSrcWidth();
      int middleWidth = Math.max(0, totalWidth - leftWidth - rightWidth);
      this.blit(left, x, y, leftWidth, totalHeight);
      this.blit(middle, x + leftWidth, y, middleWidth, totalHeight);
      this.blit(right, x + leftWidth + middleWidth, y, rightWidth, totalHeight);
   }

   public void vBlit(Blitter top, Blitter middle, Blitter bottom, int x, int y, int totalWidth, int totalHeight) {
      int topHeight = top.getSrcHeight();
      int bottomHeight = bottom.getSrcHeight();
      int middleHeight = Math.max(0, totalHeight - topHeight - bottomHeight);
      this.blit(top, x, y, totalWidth, topHeight);
      this.blit(middle, x, y + topHeight, totalWidth, middleHeight);
      this.blit(bottom, x, y + topHeight + middleHeight, totalWidth, bottomHeight);
   }

   public void gridBlit(Blitter[] blitters, int x, int y, int cols, int rows) {
      validate3x3(blitters);
      if (cols > 0 && rows > 0) {
         int leftWidth = blitters[0].getSrcWidth();
         int middleWidth = blitters[1].getSrcWidth();
         int rightWidth = blitters[2].getSrcWidth();
         int topHeight = blitters[0].getSrcHeight();
         int middleHeight = blitters[3].getSrcHeight();
         int bottomHeight = blitters[6].getSrcHeight();
         int currentY = y;

         for (int row = 0; row < rows; row++) {
            int rowBase = row == 0 ? 0 : (row == rows - 1 ? 6 : 3);
            int rowHeight = row == 0 ? topHeight : (row == rows - 1 ? bottomHeight : middleHeight);
            int currentX = x;

            for (int col = 0; col < cols; col++) {
               int colOffset = col == 0 ? 0 : (col == cols - 1 ? 2 : 1);
               int colWidth = col == 0 ? leftWidth : (col == cols - 1 ? rightWidth : middleWidth);
               this.blit(blitters[rowBase + colOffset], currentX, currentY, colWidth, rowHeight);
               currentX += colWidth;
            }

            currentY += rowHeight;
         }
      }
   }

   public void slotBlit(int x, int y, int cols, int rows) {
      this.gridBlit(SLOTS_GENERICS, x, y, cols + 1, rows + 1);
   }

   public void bgBlit(Blitter[] blitters, int x, int y, int totalWidth, int totalHeight) {
      validate3x3(blitters);
      int topHeight = blitters[0].getSrcHeight();
      int bottomHeight = blitters[6].getSrcHeight();
      int middleHeight = Math.max(0, totalHeight - topHeight - bottomHeight);
      this.hBlit(blitters[0], blitters[1], blitters[2], x, y, totalWidth, topHeight);
      this.hBlit(blitters[3], blitters[4], blitters[5], x, y + topHeight, totalWidth, middleHeight);
      this.hBlit(blitters[6], blitters[7], blitters[8], x, y + topHeight + middleHeight, totalWidth, bottomHeight);
   }

   private static void validate3x3(Blitter[] blitters) {
      if (blitters == null || blitters.length != 9) {
         throw new IllegalArgumentException("Expected exactly 9 blitters");
      }
   }

   private void blit(Blitter blitter, int x, int y, int width, int height) {
      if (width > 0 && height > 0) {
         blitter.copy().dest(x, y, width, height).blit(this.guiGraphics);
      }
   }
}
