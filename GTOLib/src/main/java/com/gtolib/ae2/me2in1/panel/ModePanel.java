package com.gtolib.ae2.me2in1.panel;

import appeng.client.Point;
import appeng.client.gui.widgets.TabButton.Style;
import com.gtolib.ae2.me2in1.NoShadowsTabButton;
import com.gtolib.api.ae2.me2in1.Me2in1Menu;
import com.gtolib.api.ae2.me2in1.Me2in1Screen;
import com.gtolib.api.ae2.me2in1.encoding.BatchEncodingPanel;
import com.gtolib.api.ae2.me2in1.encoding.CraftingEncodingPanel;
import com.gtolib.api.ae2.me2in1.encoding.EncodingSubPanel;
import com.gtolib.api.ae2.me2in1.encoding.ExtendedEncodingMode;
import com.gtolib.api.ae2.me2in1.encoding.ProcessingEncodingPanel;
import com.gtolib.api.ae2.me2in1.encoding.SmithingTableEncodingPanel;
import com.gtolib.api.ae2.me2in1.encoding.StonecuttingEncodingPanel;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.renderer.Rect2i;

public class ModePanel extends Panel {
   private static final int DRAG_MARK_PANEL_X = -8;
   private static final int DRAG_MARK_PANEL_Y = -10;
   private static final int TAB_BUTTON_OFFSET_X = 0;
   private static final int TAB_BUTTON_OFFSET_Y = -33;
   private static final int TAB_BUTTON_GAP = 22;
   private final Map<ExtendedEncodingMode, EncodingSubPanel> modePanels = new EnumMap<>(ExtendedEncodingMode.class);
   private final Map<ExtendedEncodingMode, NoShadowsTabButton> modeTabButtons = new EnumMap<>(ExtendedEncodingMode.class);

   public ModePanel(Me2in1Screen<?> screen) {
      super(screen, -156, 180, "encodingModePanel");

      for (ExtendedEncodingMode mode : ExtendedEncodingMode.values()) {
         EncodingSubPanel panel = switch (mode) {
            case STONECUTTING -> new StonecuttingEncodingPanel(this);
            case CRAFTING -> new CraftingEncodingPanel(this);
            case SMITHING_TABLE -> new SmithingTableEncodingPanel(this);
            case PROCESSING -> new ProcessingEncodingPanel(this);
            case BATCH -> new BatchEncodingPanel(this);
         };
         NoShadowsTabButton tabButton = new NoShadowsTabButton(panel.getTabIconItem(), panel.getTabTooltip(), btn -> this.getMenu().setMode(mode));
         tabButton.setStyle(Style.HORIZONTAL);
         int modeIndex = this.modeTabButtons.size();
         this.modeTabButtons.put(mode, tabButton);
         screen.getSubWidgets().put("modeTabButton_" + mode.name(), tabButton);
         this.modePanels.put(mode, panel);
      }

      for (Entry<ExtendedEncodingMode, EncodingSubPanel> entry : this.modePanels.entrySet()) {
         ExtendedEncodingMode mode = entry.getKey();
         EncodingSubPanel panel = entry.getValue();
         screen.getWidgets().add("encodingModePanel_" + mode.name(), panel);
      }
   }

   private Me2in1Menu getMenu() {
      return this.screen.getMenu();
   }

   @Override
   public void updateBeforeRender() {
      super.updateBeforeRender();
      if (!this.isCollapsed()) {
         AtomicInteger atomicX = new AtomicInteger(this.x + 0);

         for (ExtendedEncodingMode mode : ExtendedEncodingMode.values()) {
            boolean selected = this.getMenu().getMode() == mode;
            NoShadowsTabButton tabButton = this.modeTabButtons.get(mode);
            tabButton.visible = true;
            tabButton.active = true;
            tabButton.setSelected(selected);
            tabButton.setPosition(this.getGuiLeft() + atomicX.getAndAdd(22), this.getGuiTop() + this.y + -33);
            this.modePanels.get(mode).setVisible(false);
         }

         EncodingSubPanel selectedPanel = this.modePanels.get(this.getMenu().getMode());
         selectedPanel.setPosition(new Point(this.x, this.y));
         selectedPanel.setVisible(true);
         selectedPanel.updateBeforeRender();
      } else {
         for (ExtendedEncodingMode mode : ExtendedEncodingMode.values()) {
            NoShadowsTabButton tabButton = this.modeTabButtons.get(mode);
            tabButton.visible = false;
            tabButton.active = false;
            this.modePanels.get(mode).setVisible(false);
         }
      }
   }

   @Override
   int getDragMarkShiftX() {
      return -8;
   }

   @Override
   int getDragMarkShiftY() {
      return -10;
   }

   @Override
   public Rect2i getBounds() {
      if (this.isCollapsed()) {
         return this.getCollapsedBounds();
      } else {
         return this.modePanels.get(this.getMenu().getMode()) instanceof ProcessingEncodingPanel
            ? new Rect2i(this.getPanelOriginX(), this.getPanelOriginY(), this.getPanelWidth(), this.getPanelHeight())
            : new Rect2i(this.getPanelOriginX(), this.getPanelOriginY(), this.getBasePanelWidth(), this.getBasePanelHeight());
      }
   }

   @Override
   protected int getBasePanelWidth() {
      return this.getMenu().getMode() == ExtendedEncodingMode.BATCH ? 178 : 140;
   }

   @Override
   protected int getBasePanelHeight() {
      return this.getMenu().getMode() == ExtendedEncodingMode.BATCH ? 152 : 112;
   }

   @Override
   protected int getPanelOriginX() {
      return this.getMenu().getMode() == ExtendedEncodingMode.BATCH ? this.x - 45 : this.x - 5;
   }

   @Override
   protected int getPanelOriginY() {
      return this.y - 12 - 22;
   }

   @Override
   protected int getResizeGripOffsetX() {
      return -6;
   }

   @Override
   protected int getResizeGripOffsetY() {
      return -6;
   }

   @Override
   protected boolean hasResizeHandle() {
      return this.getMenu().getMode() == ExtendedEncodingMode.PROCESSING || this.getMenu().getMode() == ExtendedEncodingMode.BATCH;
   }

   @Override
   protected int getMaxColumns() {
      return 6;
   }

   @Override
   protected int getMaxRows() {
      return 6;
   }
}
