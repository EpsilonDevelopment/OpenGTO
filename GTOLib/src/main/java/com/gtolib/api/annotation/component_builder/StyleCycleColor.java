package com.gtolib.api.annotation.component_builder;

public final class StyleCycleColor {
   public static final StyleCycleColor WARM = new StyleCycleColor(new int[]{16775885, 16767673, 15787660, 15787660});
   public static final StyleCycleColor PROGRESSIVE = new StyleCycleColor(new int[]{8900331, 9498256, 16777184, 16770229, 16758465});
   private final int[] colors;
   private final int maxIndex;
   private int index = 0;

   private StyleCycleColor(int[] colors) {
      this.colors = colors;
      this.maxIndex = colors.length - 1;
   }

   public int getNextColor() {
      if (this.index > this.maxIndex) {
         this.index = 0;
      }

      return this.colors[this.index++];
   }
}
