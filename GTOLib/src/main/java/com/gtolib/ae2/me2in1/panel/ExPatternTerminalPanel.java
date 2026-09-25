package com.gtolib.ae2.me2in1.panel;

import appeng.client.Point;
import appeng.client.gui.me.patternaccess.PatternSlot;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.widgets.AETextField;
import appeng.client.gui.widgets.Scrollbar;
import appeng.client.gui.widgets.TabButton.Style;
import com.gtolib.api.ae2.gui.BlitterHelper;
import com.gtolib.api.ae2.me2in1.Me2in1Screen;
import com.gtolib.api.ae2.me2in1.panel.PanelSizeMap;
import lombok.Generated;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;

public class ExPatternTerminalPanel extends Panel {
   private final Scrollbar scrollbar = new Scrollbar(Scrollbar.DEFAULT);
   private static final int PANEL_WIDTH = 209;
   private static final int HEADER_HEIGHT = 51;
   private static final int FOOTER_HEIGHT = 12;
   private static final int ROW_HEIGHT = 18;
   private static final int MIN_VISIBLE_ROWS = 2;
   private static final int DEFAULT_VISIBLE_ROWS = 2;
   private static final int SEARCH_PROVIDER_X = 34;
   private static final int SEARCH_PROVIDER_Y = 27;
   private static final int SEARCH_PROVIDER_WIDTH = 141;
   private static final int DRAG_MARK_X = 3;
   private static final int DRAG_MARK_Y = 3;
   private static final int COLUMNS = 9;
   private static final int GUI_WIDTH = 209;
   private static final int GUI_WIDTH_0Slots = 47;
   private static final int lMarginWidth = 15;
   private static final int TEXT_FIELD_ICON_X = 21;
   private static final int TEXT_FIELD_ICON_Y = 25;
   private static final Blitter BASE = Blitter.texture("guis/ex_pattern_access_terminal.png");
   private static final Blitter FRAME_LU_CORNER_6_6 = BASE.copy().src(0, 0, 6, 6);
   private static final Blitter FRAME_RU_CORNER_6_6 = BASE.copy().src(203, 0, 6, 6);
   private static final Blitter FRAME_LD_CORNER_6_6 = BASE.copy().src(0, 250, 6, 6);
   private static final Blitter FRAME_RD_CORNER_6_6 = BASE.copy().src(184, 250, 6, 6);
   private static final Blitter FRAME_L_SIDE_6_6 = BASE.copy().src(0, 6, 6, 6);
   private static final Blitter FRAME_R_SIDE_6_6 = BASE.copy().src(203, 6, 6, 6);
   private static final Blitter FRAME_U_SIDE_6_6 = BASE.copy().src(6, 0, 6, 6);
   private static final Blitter FRAME_D_SIDE_6_6 = BASE.copy().src(6, 250, 6, 6);
   private static final Blitter BG_GROUND_6_6 = BASE.copy().src(6, 6, 6, 6);
   private static final Blitter SCROLLBAR_TOP = BASE.copy().src(188, 51, 14, 18);
   private static final Blitter SCROLLBAR_MIDDLE = BASE.copy().src(188, 87, 14, 18);
   private static final Blitter SCROLLBAR_BOTTOM = BASE.copy().src(188, 141, 14, 18);
   private static final Blitter GroupHeaderRow_LU = BASE.copy().src(21, 51, 18, 18);
   private static final Blitter GroupHeaderRow_MU = BASE.copy().src(39, 51, 18, 18);
   private static final Blitter GroupHeaderRow_RU = BASE.copy().src(165, 51, 18, 18);
   private static final Blitter GroupHeaderRow_LM = BASE.copy().src(21, 87, 18, 18);
   private static final Blitter GroupHeaderRow_MM = BASE.copy().src(39, 87, 18, 18);
   private static final Blitter GroupHeaderRow_RM = BASE.copy().src(165, 87, 18, 18);
   private static final Blitter GroupHeaderRow_LD = BASE.copy().src(21, 123, 18, 18);
   private static final Blitter GroupHeaderRow_MD = BASE.copy().src(39, 123, 18, 18);
   private static final Blitter GroupHeaderRow_RD = BASE.copy().src(165, 123, 18, 18);
   private static final Blitter SlotsRow_L = BASE.copy().src(21, 69, 18, 18);
   private static final Blitter SlotsRow_M = BASE.copy().src(39, 69, 18, 18);
   private static final Blitter SlotsRow_R = BASE.copy().src(165, 69, 18, 18);
   private static final Blitter TEXT_FIELD_ICON = BASE.copy().src(21, 25, 10, 25);
   private static final Blitter[] FRAME_3X3 = new Blitter[]{
      FRAME_LU_CORNER_6_6,
      FRAME_U_SIDE_6_6,
      FRAME_RU_CORNER_6_6,
      FRAME_L_SIDE_6_6,
      BG_GROUND_6_6,
      FRAME_R_SIDE_6_6,
      FRAME_LD_CORNER_6_6,
      FRAME_D_SIDE_6_6,
      FRAME_RD_CORNER_6_6
   };
   private static final Blitter[] ROW_BG_3X3 = new Blitter[]{
      GroupHeaderRow_LU,
      GroupHeaderRow_MU,
      GroupHeaderRow_RU,
      GroupHeaderRow_LM,
      GroupHeaderRow_MM,
      GroupHeaderRow_RM,
      GroupHeaderRow_LD,
      GroupHeaderRow_MD,
      GroupHeaderRow_RD
   };

   public ExPatternTerminalPanel(Me2in1Screen<?> screen) {
      super(screen, 0, 10, "exPatternTerminalPanel");
      screen.getWidgets().add("exPatternTerminalPanelScrollbar", this.scrollbar);
      PanelSizeMap.PanelSize storedSize = screen.getMenu().getPanelSizeMap() == null
         ? null
         : screen.getMenu().getPanelSizeMap().map().getPanelSize(this.getName());
      if (storedSize != null) {
         this.setPanelSize(storedSize.rows, 1);
      }

      this.enableSizeNotifications();
   }

   @Override
   public void updateBeforeRender() {
      super.updateBeforeRender();
      boolean visible = !this.isCollapsed() && this.columns > 3;
      boolean providerVisible = this.columns > 6;
      AETextField providerField = this.screen.gto$getSearchProviderField();
      AETextField searchInField = this.screen.gto$searchInField();
      AETextField searchOutField = this.screen.gto$searchOutField();
      if (providerField != null) {
         providerField.setVisible(visible && providerVisible);
         searchInField.setVisible(visible);
         searchOutField.setVisible(visible);
         providerField.active = visible && providerVisible;
         searchInField.active = visible;
         searchOutField.active = visible;
         if (searchInField.getWidth() <= 0) {
            providerField.setWidth(141);
            searchInField.setWidth(141);
            searchOutField.setWidth(141);
         }

         if (searchInField.getHeight() <= 0) {
            providerField.setHeight(9 + 2);
            searchInField.setHeight(9 + 2);
            searchOutField.setHeight(9 + 2);
         }

         if (visible) {
            searchInField.setPosition(this.getAbsoluteLeft() + 34, this.getAbsoluteTop() + 27);
            searchOutField.setPosition(this.getAbsoluteLeft() + 34, this.getAbsoluteTop() + 27 + providerField.getHeight() + 7);
         } else if (searchInField.isFocused() || searchOutField.isFocused()) {
            searchInField.setFocused(false);
            searchOutField.setFocused(false);
         }

         if (visible && providerVisible) {
            providerField.setPosition(this.getAbsoluteLeft() + this.getPanelWidth() - 64 - 10, this.getAbsoluteTop() + 27 + providerField.getHeight() + 7);
         } else if (providerField.isFocused()) {
            providerField.setFocused(false);
         }
      }

      this.scrollbar.setVisible(!this.isCollapsed());
      this.scrollbar.setPosition(new Point(this.x + 47 + 18 * this.columns - SCROLLBAR_TOP.getSrcWidth() - 6, this.y + 51 + 1));
      this.scrollbar.setHeight(this.rows * 18 - 2);
      this.scrollbar.setCaptureMouseWheel(false);
      this.screen.gto$resetExPatternTerminalScrollbar();
      this.screen.getVerticalToolbar().buttons.forEach(btn -> {
         btn.visible = !this.isCollapsed();
         btn.active = !this.isCollapsed();
      });
      this.screen.getVerticalToolbar().setPosition(new Point(this.x, this.y + 4));
      this.screen.craftingStatusBtn.visible = !this.isCollapsed();
      this.screen.craftingStatusBtn.setPosition(this.getAbsoluteLeft() + this.getPanelWidth() - 25, this.getAbsoluteTop() - 4);
      this.screen.craftingStatusBtn.setStyle(Style.CORNER);
   }

   @Override
   public boolean onMouseWheel(Point mousePos, double delta) {
      return this.isCollapsed() ? false : this.scrollbar.onMouseWheel(mousePos, delta);
   }

   @Override
   public void drawBackgroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
      if (this.isCollapsed()) {
         super.drawBackgroundLayer(guiGraphics, bounds, mouse);
      } else {
         this.updateResizeCursor(mouse);
         int offsetX = this.getAbsoluteLeft();
         int offsetY = this.getAbsoluteTop();
         BlitterHelper blitterHelper = BlitterHelper.of(guiGraphics);
         int headerWidth = 47 + 18 * this.columns;
         blitterHelper.bgBlit(FRAME_3X3, offsetX, offsetY, headerWidth, this.getPanelHeight());
         TEXT_FIELD_ICON.dest(offsetX + 21, offsetY + 25).blit(guiGraphics);
         int scrollLevel = this.scrollbar.getCurrentScroll();
         int currentBodyY = offsetY + 51;
         int scrollbarWidth = SCROLLBAR_TOP.getSrcWidth();
         int scrollbarX = offsetX + headerWidth - 6 - 1 - scrollbarWidth;
         blitterHelper.vBlit(SCROLLBAR_TOP, SCROLLBAR_MIDDLE, SCROLLBAR_BOTTOM, scrollbarX, currentBodyY, scrollbarWidth, 18 * this.rows);
         int slotsX = 15 + offsetX + 6;
         blitterHelper.gridBlit(ROW_BG_3X3, slotsX, currentBodyY, this.columns, this.rows);

         for (int r = 0; r < this.rows; r++) {
            for (int c = 0; c < this.columns; c++) {
               if (scrollLevel + r < this.screen.getExPatternTerminalRowCount()
                  && this.screen.getExPatternTerminalRow(scrollLevel + r) instanceof Me2in1Screen.SlotsRow slotsRow
                  && c < slotsRow.slots()) {
                  boolean firstColumn = c == 0;
                  boolean lastColumn = c == this.columns - 1;
                  Blitter blitter = this.selectRowBackgroundBlitter(firstColumn, lastColumn);
                  blitter.dest(slotsX + c * 18, currentBodyY).blit(guiGraphics);
               }
            }

            currentBodyY += 18;
         }
      }
   }

   @Override
   public void drawForegroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
      this.screen.getMenu().slots.removeIf(slot -> slot instanceof PatternSlot);
      this.screen.gto$getHighlisghtsButtons().forEach((key, value) -> value.setVisibility(false));
      if (this.isCollapsed()) {
         super.drawForegroundLayer(guiGraphics, bounds, mouse);
      } else {
         int markX = this.x + this.getDragMarkShiftX() + 21;
         int markY = this.y + this.getDragMarkShiftY() + 8;
         guiGraphics.drawString(Minecraft.getInstance().font, this.screen.getDialogTitle(), markX, markY, ChatFormatting.DARK_GRAY.getColor(), false);
         this.screen.gto$renderExPaT_FG(guiGraphics, this.getAbsoluteLeft(), this.getAbsoluteTop(), mouse.getX(), mouse.getY());
         super.drawForegroundLayer(guiGraphics, bounds, mouse);
      }
   }

   @Override
   int getDragMarkShiftX() {
      return 3;
   }

   @Override
   int getDragMarkShiftY() {
      return 3;
   }

   @Override
   public Rect2i getBounds() {
      return this.isCollapsed()
         ? this.getCollapsedBounds()
         : new Rect2i(this.getPanelOriginX(), this.getPanelOriginY(), this.getPanelWidth(), this.getPanelHeight());
   }

   @Override
   protected int getDefaultRows() {
      return 2;
   }

   @Override
   protected int getDefaultColumns() {
      return 9;
   }

   @Override
   protected int getMinRows() {
      return 2;
   }

   @Override
   protected int getMinColumns() {
      return 2;
   }

   @Override
   protected int getBasePanelWidth() {
      return 209;
   }

   @Override
   protected int getBasePanelHeight() {
      return 63;
   }

   @Override
   protected int getPanelHeight() {
      return this.getBasePanelHeight() + this.getRows() * 18;
   }

   @Override
   protected int getResizeGripOffsetX() {
      return -2;
   }

   @Override
   protected int getResizeGripOffsetY() {
      return -2;
   }

   public void resetScrollbar() {
      this.scrollbar.setHeight(this.rows * 18 - 2);
      this.scrollbar.setRange(0, this.screen.getExPatternTerminalRowCount() - this.rows, 2);
   }

   private Blitter selectRowBackgroundBlitter(boolean firstColumn, boolean lastColumn) {
      if (firstColumn) {
         return SlotsRow_L;
      } else {
         return lastColumn ? SlotsRow_R : SlotsRow_M;
      }
   }

   @Generated
   public Scrollbar getScrollbar() {
      return this.scrollbar;
   }
}
