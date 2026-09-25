package com.gtolib.ae2.me2in1.panel;

import appeng.client.Point;
import appeng.client.gui.Tooltip;
import appeng.client.gui.style.Blitter;
import appeng.menu.SlotSemantics;
import com.gtolib.api.ae2.me2in1.ME2in1Helper;
import com.gtolib.api.ae2.me2in1.Me2in1Screen;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;

public class PlayerInvPanel extends Panel {
   private static final Blitter INV = Blitter.texture("guis/tag_storage_bus.png");
   private static final Blitter INV_HEADER = INV.copy().src(0, 0, 176, 14);
   private static final Blitter INV_BODY = INV.copy().src(0, 121, 176, 86);
   private static final int PANEL_WIDTH = 176;
   private static final int PANEL_HEIGHT = 100;
   private static final int HEADER_HEIGHT = 14;
   private static final int PANEL_BG_OFFSET_X = 6;
   private static final int PLAYER_INV_X = 14;
   private static final int PLAYER_INV_Y = 18;
   private static final int HOTBAR_X = 14;
   private static final int HOTBAR_Y = 76;
   private static final int DRAG_MARK_X = 7;
   private static final int DRAG_MARK_Y = 1;

   public PlayerInvPanel(Me2in1Screen<?> screen) {
      super(screen, 15, 172, "PlayerInvPanel");
   }

   @Override
   public void updateBeforeRender() {
      super.updateBeforeRender();
      boolean collapsed = this.isCollapsed();
      this.screen.setSlotsHidden(SlotSemantics.PLAYER_INVENTORY, collapsed);
      this.screen.setSlotsHidden(SlotSemantics.PLAYER_HOTBAR, collapsed);
      if (!collapsed) {
         this.positionSlots(this.screen.getMenu().getSlots(SlotSemantics.PLAYER_INVENTORY), 14, 18);
         this.positionSlots(this.screen.getMenu().getSlots(SlotSemantics.PLAYER_HOTBAR), 14, 76);
      }
   }

   private void positionSlots(List<Slot> slots, int startX, int startY) {
      for (int i = 0; i < slots.size(); i++) {
         ME2in1Helper.setSlotPos(slots.get(i), this.x + startX + i % 9 * 18, this.y + startY + i / 9 * 18);
      }
   }

   @Override
   public void drawBackgroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
      super.drawBackgroundLayer(guiGraphics, bounds, mouse);
      if (!this.isCollapsed()) {
         INV_BODY.dest(this.getAbsoluteLeft(), this.getAbsoluteTop() + 14).blit(guiGraphics);
         INV_HEADER.dest(this.getAbsoluteLeft(), this.getAbsoluteTop()).blit(guiGraphics);
      }
   }

   @Override
   public void drawForegroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
      Font font = Minecraft.getInstance().font;
      int markX = this.x + this.getDragMarkShiftX();
      int markY = this.y + this.getDragMarkShiftY();
      if (this.isCollapsed()) {
         guiGraphics.drawString(font, getLabel(), markX + 20, markY + 4, ChatFormatting.WHITE.getColor(), true);
      } else {
         guiGraphics.drawString(font, getLabel(), markX + 18, markY + 4, ChatFormatting.DARK_GRAY.getColor(), false);
         super.drawForegroundLayer(guiGraphics, bounds, mouse);
      }
   }

   @Nullable
   @Override
   public Tooltip getTooltip(int mouseX, int mouseY) {
      return this.isCollapsed() ? new Tooltip(getLabel()) : super.getTooltip(mouseX, mouseY);
   }

   @Override
   public Rect2i getBounds() {
      return this.isCollapsed() ? this.getCollapsedBounds() : new Rect2i(this.x, this.getPanelOriginY(), this.getPanelWidth() + 6, this.getPanelHeight());
   }

   @Override
   protected Rect2i getCollapsedBounds() {
      Font font = Minecraft.getInstance().font;
      int markX = this.x + this.getDragMarkShiftX();
      int markY = this.y + this.getDragMarkShiftY();
      int labelWidth = font.width(getLabel());
      return new Rect2i(markX, markY, 20 + labelWidth, 16);
   }

   @Override
   int getDragMarkShiftX() {
      return 7;
   }

   @Override
   int getDragMarkShiftY() {
      return 1;
   }

   @Override
   protected int getMaxColumns() {
      return 0;
   }

   @Override
   protected int getMaxRows() {
      return 0;
   }

   @Override
   protected boolean isInResizeHandle(Point mousePos) {
      return false;
   }

   @Override
   protected int getPanelOriginX() {
      return this.x + 6;
   }

   @Override
   protected int getBasePanelWidth() {
      return 176;
   }

   @Override
   protected int getBasePanelHeight() {
      return 100;
   }

   private static Component getLabel() {
      return Component.translatable("container.inventory");
   }
}
