package com.gtolib.api.ae2.me2in1.panel;

public class MePanelGridSize {
   public static final int DEFAULT_SIZE = 4;
   public int rows;
   public int columns;

   public MePanelGridSize() {
      this(4, 4);
   }

   public MePanelGridSize(int rows, int columns) {
      this.rows = Math.max(4, rows);
      this.columns = Math.max(4, columns);
   }
}
