package com.gtolib.ae2.me2in1.panel;

import appeng.client.Point;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.Icon;
import appeng.client.gui.Rects;
import appeng.client.gui.Tooltip;
import appeng.client.gui.style.Blitter;
import com.gtolib.GTOCore;
import com.gtolib.api.ae2.gui.BlitterHelper;
import com.gtolib.api.ae2.me2in1.Me2in1Screen;
import java.util.List;
import java.util.function.BiConsumer;
import lombok.Generated;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public abstract class Panel implements ICompositeWidget, PanelBackgroundRenderer {
   private static final Blitter collapsedBgBlitterLeft = Blitter.texture("guis/panel_collapsed.png", 64, 64).src(0, 0, 16, 24);
   private static final Blitter collapsedBgBlitterMiddle = Blitter.texture("guis/panel_collapsed.png", 64, 64).src(17, 0, 1, 24);
   private static final Blitter collapsedBgBlitterRight = Blitter.texture("guis/panel_collapsed.png", 64, 64).src(39, 0, 9, 24);
   private static final Blitter resizeGripBlitter = Blitter.texture("guis/me2in1_resize_grip.png", 8, 8).src(0, 0, 8, 8);
   private static final int COLLAPSED_MARK_Y = 4;
   private static final int COLLAPSE_TRIGGER_MOUSE_Y = 8;
   private static final int EXPAND_TRIGGER_MOUSE_Y = 18;
   private static final int RESIZE_GRIP_SIZE = 8;
   private static final int RESIZE_GRIP_INSET = 2;
   protected static final int HIDDEN_SLOT_POS = -9999;
   protected static final int SLOT_SPACING = 18;
   protected static final int RESIZE_HANDLE_SIZE = 12;
   protected final Me2in1Screen<?> screen;
   protected final Panel.DraggableMark draggableMark;
   private final String name;
   protected int x;
   protected int y;
   protected int rows = this.getDefaultRows();
   protected int columns = this.getDefaultColumns();
   protected final int initX;
   protected final int initY;
   private boolean collapsed = false;
   private boolean initialized = false;
   private boolean resizing = false;
   private boolean resizeChangedGrid = false;
   private int resizeStartMouseX = 0;
   private int resizeStartMouseY = 0;
   private int resizeStartRows = this.getDefaultRows();
   private int resizeStartColumns = this.getDefaultColumns();
   private boolean resizeCursorActive = false;
   private boolean sizeNotificationsEnabled = false;
   private static long resizeCursor = 0L;

   @Override
   public void setPosition(Point position) {
      this.x = position.getX();
      this.y = position.getY();
      if (this.initialized) {
         this.collapsed = this.getDragMarkShiftY() + this.y == 4;
      }
   }

   @Override
   public void setSize(int width, int height) {
   }

   public void resetPosition() {
      this.collapsed = false;
      this.x = this.initX;
      this.y = this.initY;
      this.syncPanelPosition();
   }

   public void resetSize() {
      this.setPanelSize(this.getDefaultRows(), this.getDefaultColumns());
      this.syncPanelSize();
   }

   public Panel(Me2in1Screen<?> screen, int x, int y, String name) {
      this.name = name;
      this.screen = screen;
      this.initX = x;
      this.initY = y;
      this.setPosition(new Point(x, y));
      this.draggableMark = new Panel.DraggableMark((x0, y0) -> {
         this.x = x0 - this.getDragMarkShiftX();
         this.y = y0 - this.getDragMarkShiftY();
      });
      this.rows = this.getDefaultRows();
      this.columns = this.getDefaultColumns();
      this.initialized = true;
      screen.getWidgets().add(name + "draggableMark", this.draggableMark);
   }

   public int getAbsoluteLeft() {
      return this.getGuiLeft() + this.getPanelOriginX();
   }

   public int getAbsoluteTop() {
      return this.getGuiTop() + this.getPanelOriginY();
   }

   @Override
   public void updateBeforeRender() {
      if (this.collapsed) {
         this.snapToCollapsedTop();
      }

      this.draggableMark.setPosition(new Point(this.getDragMarkShiftX() + this.x, this.getDragMarkShiftY() + this.y));
   }

   protected int getGuiLeft() {
      return this.screen.getGuiLeft();
   }

   protected int getGuiTop() {
      return this.screen.getGuiTop();
   }

   abstract int getDragMarkShiftX();

   abstract int getDragMarkShiftY();

   protected int getDefaultRows() {
      return 0;
   }

   protected int getDefaultColumns() {
      return 0;
   }

   protected int getMinRows() {
      return 0;
   }

   protected int getMinColumns() {
      return 0;
   }

   protected int getMaxRows() {
      return Integer.MAX_VALUE;
   }

   protected int getMaxColumns() {
      return Integer.MAX_VALUE;
   }

   protected int getBasePanelWidth() {
      return 0;
   }

   protected int getBasePanelHeight() {
      return 0;
   }

   protected int getPanelOriginX() {
      return this.x;
   }

   protected int getPanelOriginY() {
      return this.y;
   }

   protected int getPanelWidth() {
      return this.getBasePanelWidth() + (this.columns - this.getDefaultColumns()) * 18;
   }

   protected int getPanelHeight() {
      return this.getBasePanelHeight() + (this.rows - this.getDefaultRows()) * 18;
   }

   protected void onPanelSizeChanged(int rows, int columns) {
   }

   protected final void syncPanelPosition() {
      this.screen.getMenu().updateC2SPanelPos(this);
   }

   protected final void syncPanelSize() {
      this.screen.getMenu().updateC2SPanelSize(this);
   }

   protected boolean isInResizeHandle(Point mousePos) {
      if (!this.isCollapsed() && this.hasResizeHandle()) {
         int handleX = this.getPanelOriginX() + this.getPanelWidth();
         int handleY = this.getPanelOriginY() + this.getPanelHeight();
         return mousePos.getX() >= handleX - 12 && mousePos.getX() <= handleX && mousePos.getY() >= handleY - 12 && mousePos.getY() <= handleY;
      } else {
         return false;
      }
   }

   protected boolean hasResizeHandle() {
      return this.getMinRows() < this.getMaxRows() || this.getMinColumns() < this.getMaxColumns();
   }

   protected int getResizeGripOffsetX() {
      return 0;
   }

   protected int getResizeGripOffsetY() {
      return 0;
   }

   protected void updateResizeCursor(Point mouse) {
      this.setResizeCursor(this.resizing || this.isInResizeHandle(mouse));
   }

   public void resetResizeCursor() {
      this.setResizeCursor(false);
   }

   private void setResizeCursor(boolean resize) {
      if (resize != this.resizeCursorActive) {
         long window = Minecraft.getInstance().getWindow().getWindow();
         if (resize) {
            long cursor = getResizeCursor();
            if (cursor == 0L) {
               return;
            }

            GLFW.glfwSetCursor(window, cursor);
         } else {
            GLFW.glfwSetCursor(window, 0L);
         }

         this.resizeCursorActive = resize;
      }
   }

   private static long getResizeCursor() {
      if (resizeCursor == 0L) {
         resizeCursor = GLFW.glfwCreateStandardCursor(getResizeCursorShape());
         if (resizeCursor == 0L) {
            resizeCursor = GLFW.glfwCreateStandardCursor(221189);
         }
      }

      return resizeCursor;
   }

   private static int getResizeCursorShape() {
      try {
         return GLFW.class.getField("GLFW_RESIZE_NWSE_CURSOR").getInt(null);
      } catch (ReflectiveOperationException ignored) {
         return 221189;
      }
   }

   public boolean setPanelSize(int rows, int columns) {
      int clampedRows = Mth.clamp(rows, this.getMinRows(), this.getMaxRows());
      int clampedColumns = Mth.clamp(columns, this.getMinColumns(), this.getMaxColumns());
      if (this.rows == clampedRows && this.columns == clampedColumns) {
         return false;
      }

      this.rows = clampedRows;
      this.columns = clampedColumns;
      if (this.initialized && this.sizeNotificationsEnabled) {
         this.onPanelSizeChanged(this.rows, this.columns);
      }

      return true;
   }

   protected void enableSizeNotifications() {
      this.sizeNotificationsEnabled = true;
   }

   protected Rect2i getCollapsedBounds() {
      Font font = Minecraft.getInstance().font;
      int markX = this.x + this.getDragMarkShiftX();
      int markY = this.y + this.getDragMarkShiftY();
      int labelWidth = font.width(this.getLocalizedName());
      return new Rect2i(markX, markY, 20 + labelWidth, 16);
   }

   private Component getLocalizedName() {
      return Component.translatable(GTOCore.id(this.name).withPrefix("ae.appeng.me2in1.panel.").toLanguageKey());
   }

   @Override
   public void drawForegroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
      if (!this.collapsed) {
         this.drawResizeGrip(guiGraphics);
      } else {
         Font font = Minecraft.getInstance().font;
         int markX = this.x + this.getDragMarkShiftX();
         int markY = this.y + this.getDragMarkShiftY();
         guiGraphics.drawString(font, this.getLocalizedName(), markX + 20, markY + 4, 16777215, true);
      }
   }

   private void drawResizeGrip(GuiGraphics guiGraphics) {
      if (this.hasResizeHandle()) {
         int gripX = this.getPanelOriginX() + this.getPanelWidth() - 8 - 2 + this.getResizeGripOffsetX();
         int gripY = this.getPanelOriginY() + this.getPanelHeight() - 8 - 2 + this.getResizeGripOffsetY();
         resizeGripBlitter.copy().dest(gripX, gripY).blit(guiGraphics);
      }
   }

   @Override
   public void drawBackgroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
      if (this.collapsed) {
         this.resetResizeCursor();
         Rect2i collapsedBounds = this.getCollapsedBounds();
         BlitterHelper.of(guiGraphics)
            .hBlit(
               collapsedBgBlitterLeft,
               collapsedBgBlitterMiddle,
               collapsedBgBlitterRight,
               collapsedBounds.getX() + this.screen.getGuiLeft() - 4,
               collapsedBounds.getY() + this.screen.getGuiTop() - 4,
               collapsedBounds.getWidth() + 10,
               collapsedBgBlitterLeft.getSrcHeight()
            );
      } else {
         this.updateResizeCursor(mouse);
      }
   }

   @Override
   public void addExclusionZones(List<Rect2i> exclusionZones, Rect2i screenBounds) {
      Rect2i bounds = this.getBounds();
      if (bounds.getWidth() > 0 && bounds.getHeight() > 0) {
         if (bounds.getX() < 0
            || bounds.getY() < 0
            || bounds.getX() + bounds.getWidth() > screenBounds.getWidth()
            || bounds.getY() + bounds.getHeight() > screenBounds.getHeight()) {
            exclusionZones.add(
               Rects.expand(new Rect2i(screenBounds.getX() + bounds.getX(), screenBounds.getY() + bounds.getY(), bounds.getWidth(), bounds.getHeight()), 4)
            );
         }
      }
   }

   private void snapToCollapsedTop() {
      this.y = 4 - this.getDragMarkShiftY();
   }

   private void updateCollapsedStateForDrag(Point mousePos) {
      if (this.collapsed) {
         if (mousePos.getY() > 18) {
            this.collapsed = false;
         } else {
            this.snapToCollapsedTop();
         }
      } else {
         if (mousePos.getY() <= 8) {
            this.collapsed = true;
            this.snapToCollapsedTop();
         }
      }
   }

   @Override
   public boolean wantsAllMouseUpEvents() {
      return this.resizing;
   }

   @Override
   public boolean onMouseDown(Point mousePos, int button) {
      if (button == 0 && !this.isCollapsed() && this.isInResizeHandle(mousePos)) {
         this.resizing = true;
         this.resizeChangedGrid = false;
         this.resizeStartMouseX = mousePos.getX();
         this.resizeStartMouseY = mousePos.getY();
         this.resizeStartRows = this.rows;
         this.resizeStartColumns = this.columns;
         this.setResizeCursor(true);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean onMouseDrag(Point mousePos, int button) {
      if (this.resizing && button == 0) {
         int newColumns = this.resizeStartColumns + (mousePos.getX() - this.resizeStartMouseX) / 18;
         int newRows = this.resizeStartRows + (mousePos.getY() - this.resizeStartMouseY) / 18;
         if (this.setPanelSize(newRows, newColumns)) {
            this.resizeChangedGrid = true;
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean onMouseUp(Point mousePos, int button) {
      if (this.resizing && button == 0) {
         this.resizing = false;
         this.updateResizeCursor(mousePos);
         if (this.resizeChangedGrid) {
            this.syncPanelSize();
         }

         this.resizeChangedGrid = false;
         return true;
      } else {
         return false;
      }
   }

   @Generated
   public Me2in1Screen<?> getScreen() {
      return this.screen;
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public int getX() {
      return this.x;
   }

   @Generated
   public int getY() {
      return this.y;
   }

   @Generated
   public int getRows() {
      return this.rows;
   }

   @Generated
   public int getColumns() {
      return this.columns;
   }

   @Generated
   public boolean isCollapsed() {
      return this.collapsed;
   }

   public class DraggableMark implements ICompositeWidget {
      private static final Blitter markBlitter = Icon.WRENCH_DISABLED.getBlitter();
      private static final Blitter markBlitterSelected = Icon.WRENCH.getBlitter();
      private final BiConsumer<Integer, Integer> positionSetter;
      private boolean isDragging = false;
      private boolean hasDragged = false;
      private int x = 0;
      private int y = 0;

      public DraggableMark(BiConsumer<Integer, Integer> positionSetter) {
         this.positionSetter = positionSetter;
      }

      @Override
      public boolean wantsAllMouseUpEvents() {
         return true;
      }

      @Override
      public boolean onMouseDown(Point mousePos, int button) {
         if (mousePos.getX() >= this.x && mousePos.getX() <= this.x + 16 && mousePos.getY() >= this.y && mousePos.getY() <= this.y + 16) {
            this.isDragging = true;
            this.hasDragged = false;
            return true;
         } else {
            return false;
         }
      }

      @Override
      public boolean onMouseUp(Point mousePos, int button) {
         boolean wasDragging = this.isDragging;
         this.isDragging = false;
         if (wasDragging) {
            if (this.hasDragged) {
               Panel.this.updateCollapsedStateForDrag(mousePos);
            }

            Panel.this.syncPanelPosition();
         }

         return false;
      }

      @Override
      public boolean onMouseDrag(Point mousePos, int button) {
         if (this.isDragging) {
            int newX = mousePos.getX() - 8;
            int newY = mousePos.getY() - 8;
            this.positionSetter.accept(newX, newY);
            this.hasDragged = true;
            Panel.this.updateCollapsedStateForDrag(mousePos);
            return true;
         } else {
            return false;
         }
      }

      @Override
      public void setPosition(Point position) {
         this.x = position.getX();
         this.y = position.getY();
      }

      @Override
      public void setSize(int width, int height) {
      }

      @Override
      public Rect2i getBounds() {
         return new Rect2i(this.x, this.y, 16, 16);
      }

      @Override
      public void drawForegroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
         (this.isDragging ? markBlitterSelected : markBlitter).copy().dest(this.x + bounds.getX(), this.y + bounds.getY()).blit(guiGraphics);
      }

      public AEBaseScreen<?> getScreen() {
         return Panel.this.screen;
      }

      @Nullable
      @Override
      public Tooltip getTooltip(int mouseX, int mouseY) {
         return new Tooltip(
            Component.translatable("gtocore.ae.appeng.me2in1.draggable_mark.tooltip"),
            Component.translatable("gtocore.ae.appeng.me2in1.draggable_mark.tooltip.1")
         );
      }
   }
}
