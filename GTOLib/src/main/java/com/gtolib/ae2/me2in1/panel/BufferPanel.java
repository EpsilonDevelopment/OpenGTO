package com.gtolib.ae2.me2in1.panel;

import appeng.api.config.ActionItems;
import appeng.api.stacks.AEItemKey;
import appeng.client.Point;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.widgets.ActionButton;
import appeng.client.gui.widgets.Scrollbar;
import appeng.core.definitions.AEItems;
import appeng.menu.SlotSemantics;
import appeng.menu.slot.FakeSlot;
import appeng.menu.slot.RestrictedInputSlot;
import com.gtolib.api.ae2.ModifyIcon;
import com.gtolib.api.ae2.ModifyIconButton;
import com.gtolib.api.ae2.gui.BlitterHelper;
import com.gtolib.api.ae2.me2in1.ExtendedEncodingMenu;
import com.gtolib.api.ae2.me2in1.ME2in1Helper;
import com.gtolib.api.ae2.me2in1.Me2in1Screen;
import com.gtolib.api.ae2.me2in1.Wireless;
import gto_ae.client.gui.slot.FixedRepoSlot;
import gto_ae.client.gui.widgets.AESlotWidget;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;

public class BufferPanel extends Panel implements GuiEventListener {
   private static final Blitter PATTERN_BUFFER = Blitter.texture("guis/pattern_buffer_area.png");
   private static final Blitter PATTERN_EXT = Blitter.texture("guis/pattern_buffer_area.png").src(112, 50, 40, 88);
   private static final Blitter PATTERN_BUFFER_HEADER_0_5_SLOT = PATTERN_BUFFER.copy().src(0, 0, 98, 67);
   private static final Blitter PATTERN_BUFFER_SLOT = PATTERN_BUFFER.copy().src(0, 67, 98, 18);
   private static final Blitter PATTERN_BUFFER_FOOTER_0_5_SLOT = PATTERN_BUFFER.copy().src(0, 121, 98, 17);
   public static final int COLUMNS = 4;
   public static final int NUM_ROWS = 9;
   private static final int ENCODING_BTN_X = 40;
   private static final int CLR_PATTERN_BTN_X = 58;
   private static final int ENCODE_TO_PATTERN_BTN_X = 22;
   private static final int BTN_Y = 3;
   private static final int PATTERN_SLOT_X = 40;
   private static final int PATTERN_SLOT_Y = -18;
   private static final int ENCODED_SLOTS_X = 13;
   private static final int ENCODED_SLOTS_Y = 23;
   private static final int ENCODING_BG_X_OFFSET = 2;
   private static final int ENCODING_BG_Y_OFFSET = -36;
   private static final int SCROLLBAR_OFFSET_X = 83;
   private static final int SCROLLBAR_OFFSET_Y = 59;
   private static final int EXT_AREA_OFFSET_X = -32;
   private static final int EXT_AREA_OFFSET_Y = 50;
   private static final int QUICK_MOVE_SLOT_X = -22;
   private static final int QUICK_MOVE_SLOT_Y = 77;
   private static final int QUICK_MOVE_BTN_X = -22;
   private static final int QUICK_MOVE_BTN_Y = 101;
   private final ActionButton encodeBtn = new ActionButton(ActionItems.ENCODE, act -> this.getEncodingMenu().encode(Screen.hasShiftDown()));
   private final ModifyIconButton encodeToPatternBtn;
   private final ActionButton clrPatternBtn;
   private final FixedRepoSlot blankPatternSlot;
   private final Scrollbar scrollbar;
   private final ModifyIconButton quickRemovePatternBtn;
   private final AESlotWidget quickRemoveSlotWidget;
   private final FakeSlot quickRemoveSlot;

   public BufferPanel(Me2in1Screen<?> screen) {
      super(screen, 240, 136, "bufferPanel");
      screen.getSubWidgets().put("encodeBtn", this.encodeBtn);
      this.encodeToPatternBtn = new ModifyIconButton(
         act -> this.getEncodingMenu().encodeAndTransferToProvider(),
         ModifyIcon.DIRECTLY_ENCODE_TO_GRID,
         Component.translatable("gtocore.ae.appeng.me2in1.encode_to.accessor.title"),
         Component.translatable("gtocore.ae.appeng.me2in1.encode_to.accessor")
      );
      this.encodeToPatternBtn.setHalfScale(false);
      screen.getSubWidgets().put("encodeToPatternBtn", this.encodeToPatternBtn);
      this.clrPatternBtn = new ActionButton(ActionItems.CLOSE, act -> this.getEncodingMenu().clearPattern());
      screen.getSubWidgets().put("clrPatternBtn", this.clrPatternBtn);
      this.getScreen()
         .getMenu()
         .addClientSideSlot(
            this.blankPatternSlot = new FixedRepoSlot(this.getEncodingMenu().getRepo(), AEItemKey.of(AEItems.BLANK_PATTERN.asItem()), 0, 0),
            SlotSemantics.BLANK_PATTERN
         );
      this.scrollbar = new Scrollbar(Scrollbar.SMALL);
      this.scrollbar.setCaptureMouseWheel(false);
      this.scrollbar.setHeight(this.rows * 18 - 2);
      this.scrollbar.setRange(0, 9 - this.rows, 1);
      this.quickRemovePatternBtn = new ModifyIconButton(
         b -> screen.getMenu().getEncoding().quickRemovePattern(Wireless.Screen.hasShiftDown()),
         ModifyIcon.QUICK_REMOVE,
         Component.translatable("gtocore.ae.appeng.me2in1.quick_remove_pattern"),
         Component.translatable("gtocore.ae.appeng.me2in1.quick_remove_pattern.1")
      );
      this.quickRemovePatternBtn.setHalfScale(false);
      screen.getSubWidgets().put("quickRemovePatternBtn", this.quickRemovePatternBtn);
      this.quickRemoveSlot = screen.getMenu().getEncoding().getQuickRemovePatternFilterSlot();
      this.quickRemoveSlotWidget = new AESlotWidget(this.quickRemoveSlot, screen);
      screen.getWidgets().add("quickRemoveSlot", this.quickRemoveSlotWidget);
      this.quickRemoveSlot.setEmptyTooltip(() -> List.of(Component.translatable("gtocore.machine.monitor.ae.set_filter")));
      screen.getWidgets().add("bufferPanelWidget", this.scrollbar);
   }

   @Override
   protected int getMaxColumns() {
      return 4;
   }

   @Override
   protected int getMinColumns() {
      return 4;
   }

   @Override
   protected int getDefaultColumns() {
      return 4;
   }

   @Override
   protected int getMaxRows() {
      return 9;
   }

   @Override
   protected int getMinRows() {
      return 1;
   }

   @Override
   protected int getDefaultRows() {
      return 4;
   }

   private ExtendedEncodingMenu getEncodingMenu() {
      return this.screen.getMenu().getEncoding();
   }

   @Override
   public boolean onMouseWheel(Point mousePos, double delta) {
      return this.isCollapsed() ? false : !Screen.hasShiftDown() && this.scrollbar.onMouseWheel(mousePos, delta);
   }

   @Override
   int getDragMarkShiftX() {
      return 4;
   }

   @Override
   int getDragMarkShiftY() {
      return -34;
   }

   @Override
   public Rect2i getBounds() {
      return this.isCollapsed()
         ? this.getCollapsedBounds()
         : new Rect2i(this.getPanelOriginX(), this.getPanelOriginY(), this.getPanelWidth(), this.getPanelHeight());
   }

   @Override
   protected int getBasePanelWidth() {
      return 134;
   }

   @Override
   protected int getBasePanelHeight() {
      return 138;
   }

   @Override
   protected int getPanelOriginX() {
      return this.x + 2 - PATTERN_EXT.getSrcWidth();
   }

   @Override
   protected int getPanelOriginY() {
      return this.y + -36;
   }

   @Override
   protected int getResizeGripOffsetX() {
      return 2;
   }

   @Override
   protected int getResizeGripOffsetY() {
      return -2;
   }

   @Override
   public void updateBeforeRender() {
      super.updateBeforeRender();
      boolean collapsed = this.isCollapsed();
      this.encodeBtn.setVisibility(!collapsed);
      this.encodeToPatternBtn.setVisibility(!collapsed);
      this.clrPatternBtn.setVisibility(!collapsed);
      this.quickRemovePatternBtn.setVisibility(!collapsed);
      this.quickRemoveSlot.setActive(!collapsed);
      this.scrollbar.setVisible(!collapsed);
      if (collapsed) {
         this.screen.setSlotsHidden(SlotSemantics.BLANK_PATTERN, true);
         this.screen.setSlotsHidden(SlotSemantics.ENCODED_PATTERN, true);
         ME2in1Helper.setSlotPos(this.blankPatternSlot, -9999, -9999);

         for (RestrictedInputSlot encodedSlot : this.getEncodingMenu().getEncodedPatternSlots()) {
            encodedSlot.setActive(false);
            ME2in1Helper.setSlotPos(encodedSlot, -9999, -9999);
         }

         ME2in1Helper.setSlotPos(this.quickRemoveSlot, -9999, -9999);
      } else {
         this.screen.setSlotsHidden(SlotSemantics.BLANK_PATTERN, false);
         this.screen.setSlotsHidden(SlotSemantics.ENCODED_PATTERN, false);
         this.encodeBtn.setPosition(this.x + this.getGuiLeft() + 40, this.y + 3 + this.getGuiTop());
         this.encodeToPatternBtn.setPosition(this.x + this.getGuiLeft() + 22, this.y + 3 + this.getGuiTop());
         this.clrPatternBtn.setPosition(this.x + this.getGuiLeft() + 58, this.y + 3 + this.getGuiTop());
         ME2in1Helper.setSlotPos(this.blankPatternSlot, this.x + 40, this.y + -18);
         ME2in1Helper.setSlotPos(this.blankPatternSlot, this.x + 40, this.y + -18);
         AtomicInteger atomicIndex = new AtomicInteger(0);

         for (RestrictedInputSlot encodedSlot : this.getEncodingMenu().getEncodedPatternSlots()) {
            int currentRow = atomicIndex.get() / 4;
            encodedSlot.setActive(currentRow - this.scrollbar.getCurrentScroll() >= 0 && currentRow - this.scrollbar.getCurrentScroll() < this.rows);
            ME2in1Helper.setSlotPos(
               encodedSlot, this.x + atomicIndex.getAndIncrement() % 4 * 18 + 13, this.y + 23 + currentRow * 18 - this.scrollbar.getCurrentScroll() * 18
            );
         }

         this.quickRemovePatternBtn.setPosition(this.x + this.getGuiLeft() + 2 + -22, this.y + 101 + -36 + this.getGuiTop() + (this.rows - 4) * 18);
         ME2in1Helper.setSlotPos(this.quickRemoveSlot, this.x + 2 + -22, this.y + -36 + 77 + (this.rows - 4) * 18);
         this.scrollbar.setPosition(new Point(this.x + 83 + 2, this.y + 59 + -36));
         this.scrollbar.setHeight(this.rows * 18 - 2);
         this.scrollbar.setRange(0, 9 - this.rows, 1);
      }
   }

   @Override
   public void drawBackgroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
      super.drawBackgroundLayer(guiGraphics, bounds, mouse);
      if (!this.isCollapsed()) {
         BlitterHelper.of(guiGraphics)
            .vBlit(
               PATTERN_BUFFER_HEADER_0_5_SLOT,
               PATTERN_BUFFER_SLOT,
               PATTERN_BUFFER_FOOTER_0_5_SLOT,
               this.getAbsoluteLeft() + PATTERN_EXT.getSrcWidth(),
               this.getAbsoluteTop(),
               PATTERN_BUFFER_HEADER_0_5_SLOT.getSrcWidth(),
               PATTERN_BUFFER_HEADER_0_5_SLOT.getSrcHeight()
                  + (this.rows - 1) * PATTERN_BUFFER_SLOT.getSrcHeight()
                  + PATTERN_BUFFER_FOOTER_0_5_SLOT.getSrcHeight()
            );
         BlitterHelper.of(guiGraphics).slotBlit(this.getAbsoluteLeft() + PATTERN_EXT.getSrcWidth() + 10, this.getAbsoluteTop() + 58, 4, this.rows);
         PATTERN_EXT.dest(this.x + this.getGuiLeft() + -32 + 2, this.y + this.getGuiTop() + 50 + -36 + (this.rows - 4) * 18).blit(guiGraphics);
      }
   }

   @Override
   public void setFocused(boolean b) {
      this.encodeBtn.setFocused(b);
   }

   @Override
   public boolean isFocused() {
      return this.encodeBtn.isFocused();
   }
}
