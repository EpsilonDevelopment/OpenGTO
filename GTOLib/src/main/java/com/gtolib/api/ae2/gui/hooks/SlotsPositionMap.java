package com.gtolib.api.ae2.gui.hooks;

import appeng.client.gui.layout.SlotGridLayout;
import appeng.client.gui.style.SlotPosition;
import java.util.HashMap;
import java.util.Map;

public class SlotsPositionMap extends HashMap<String, SlotPosition> {
   public SlotsPositionMap(Map<String, SlotPosition> slots) {
      this.putAll(slots);
   }

   public SlotPosition get(Object key) {
      return this.computeIfAbsent((String)key, k -> {
         SlotPosition pos = new SlotPosition();
         pos.setGrid(SlotGridLayout.BREAK_AFTER_9COLS);
         return pos;
      });
   }
}
