package com.gtolib.api.ae2.me2in1.encoding;

import appeng.api.config.ActionItems;
import appeng.client.Point;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.widgets.ActionButton;
import appeng.client.gui.widgets.Scrollbar;
import appeng.core.localization.GuiText;
import appeng.menu.SlotSemantics;
import appeng.menu.slot.FakeSlot;
import com.gtolib.ae2.me2in1.panel.ModePanel;
import com.gtolib.api.ae2.ModifyIcon;
import com.gtolib.api.ae2.ModifyIconButton;
import com.gtolib.api.ae2.gui.BlitterHelper;
import com.gtolib.api.ae2.me2in1.ME2in1Helper;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

public class ProcessingEncodingPanel extends EncodingSubPanel {
   private static final Blitter[] BG_3x3 = new Blitter[]{
      bg(0, 0, 34, 25),
      bg(34, 0, 18, 25),
      bg(52, 0, 74, 25),
      bg(0, 25, 34, 18),
      bg(34, 25, 18, 18),
      bg(52, 25, 74, 18),
      bg(0, 43, 34, 25),
      bg(34, 43, 18, 25),
      bg(52, 43, 74, 25)
   };
   private static final Blitter[] FRAME_3x3 = new Blitter[]{
      frame(0, 0, 34, 25),
      frame(34, 0, 18, 25),
      frame(52, 0, 88, 25),
      frame(0, 25, 34, 18),
      frame(34, 25, 18, 18),
      frame(52, 25, 88, 18),
      frame(0, 43, 34, 43),
      frame(34, 43, 18, 43),
      frame(52, 43, 88, 43)
   };
   protected final ActionButton cycleOutputBtn;
   protected final Scrollbar scrollbar;
   protected final ActionButton cycleInputBtn;
   protected final ModifyIconButton[] modifyButtons = new ModifyIconButton[6];
   protected final ModifyIconButton clearSecOutput;
   protected final ModifyIconButton recordRecipeBtn;
   private boolean[] activeSlots;
   private static final int BG_X_OFFSET = 0;
   private static final int BG_Y_OFFSET = 0;
   private static final int CLR_BTN_X_OFFSET = 71;
   private static final int CLR_BTN_Y_OFFSET = 6;
   private static final int cycleI_X_OFFSET = 71;
   private static final int cycleI_Y_OFFSET = 17;
   private static final int cycleO_X_OFFSET = 119;
   private static final int cycleO_Y_OFFSET = 6;
   private static final int clearSecOutput_X_OFFSET = 119;
   private static final int clearSecOutput_Y_OFFSET = 17;
   private static final int SCROLLBAR_X = 8;
   private static final int SCROLLBAR_Y = 8;
   private static final int PROC_INPUT_X_OFFSET = 17;
   private static final int PROC_INPUT_Y_OFFSET = 8;
   private static final int PROC_OUTPUT_X_OFFSET = 101;
   private static final int PROC_OUTPUT_Y_OFFSET = 8;
   private static final int MUL_BTN_X_OFFSET = 91;
   private static final int DIV_BTN_X_OFFSET = 28;
   private static final int MUL_BTN_Y_OFFSET = 33;
   private static final int FRAME_X_OFFSET = -9;
   private static final int FRAME_Y_OFFSET = -12;

   private static Blitter bg(int x, int y, int width, int height) {
      return Blitter.texture("guis/ex_encoding_modes.png").src(x, y + 70, width, height);
   }

   private static Blitter frame(int x, int y, int width, int height) {
      return Blitter.texture("guis/encoding_module.png").src(x, y, width, height);
   }

   public ProcessingEncodingPanel(ModePanel superPanel) {
      super(superPanel);
      int[] multipliers = new int[]{-2, 2, -3, 3, -5, 5};

      for (int i = 0; i < this.modifyButtons.length; i++) {
         int multiplier = multipliers[i];
         this.modifyButtons[i] = this.getMultiplyButton(multiplier);
         this.screen.getSubWidgets().put("modifyButton" + multiplier + this.getClass().getSimpleName(), this.modifyButtons[i]);
      }

      this.clearSecOutput = new ModifyIconButton(
         b -> this.menu.gtolib$clearSecOutput(),
         ModifyIcon.CLEAR_SEC_OUTPUT,
         Component.translatable("gtocore.pattern.clearSecOutput"),
         Component.translatable("gtocore.pattern.tooltip.clearSecOutput")
      );
      this.screen.getSubWidgets().put("clearSecOutput" + this.getClass().getSimpleName(), this.clearSecOutput);
      this.recordRecipeBtn = new ModifyIconButton(
         b -> this.menu.getEncoding().gtolib$clickRecipeInfo(), ModifyIcon.RECORD_RECIPE_INFO, Component.empty(), Component.empty()
      ) {
         @NotNull
         @Override
         public List<Component> getTooltipMessage() {
            return Collections.singletonList(ProcessingEncodingPanel.this.menu.getEncoding().gtolib$getRecipeInfoTooltip());
         }
      };
      this.screen.getSubWidgets().put("recordRecipeBtn" + this.getClass().getSimpleName(), this.recordRecipeBtn);
      this.cycleOutputBtn = new ActionButton(ActionItems.CYCLE_PROCESSING_OUTPUT, act -> this.encodingMenu.cycleProcessingOutput());
      this.cycleOutputBtn.setHalfSize(true);
      this.screen.getSubWidgets().put("cycleOutputBtn" + this.getClass().getSimpleName(), this.cycleOutputBtn);
      this.cycleInputBtn = new ActionButton(ActionItems.CYCLE_PROCESSING_OUTPUT, act -> this.encodingMenu.cycleProcessingInput());
      this.cycleInputBtn.setHalfSize(true);
      this.screen.getSubWidgets().put("cycleInputBtn" + this.getClass().getSimpleName(), this.cycleInputBtn);
      this.scrollbar = new Scrollbar(Scrollbar.SMALL);
      this.scrollbar.setRange(0, this.encodingMenu.getProcessingInputSlots().length / 3 - 3, 1);
      this.scrollbar.setCaptureMouseWheel(false);
      this.screen.getWidgets().add("processingEncodingModeScrollbar" + this.getClass().getSimpleName(), this.scrollbar);
   }

   @Override
   public void updateBeforeRender() {
      this.screen.setSlotsHidden(SlotSemantics.PROCESSING_INPUTS, !this.isVisible());
      this.screen.setSlotsHidden(SlotSemantics.PROCESSING_OUTPUTS, !this.isVisible());
      if (!this.isVisible()) {
         for (FakeSlot slot : this.encodingMenu.getProcessingInputSlots()) {
            slot.setActive(false);
         }

         for (FakeSlot slot : this.encodingMenu.getProcessingOutputSlots()) {
            slot.setActive(false);
         }
      } else {
         int absX = this.getGuiLeft() + this.x;
         int absY = this.getGuiTop() + this.y;
         this.clearBtn.setPosition(absX + this.offsetColumns() + 71, absY + 6);
         this.recordRecipeBtn.setPosition(absX + 71 + 9 + this.offsetColumns(), absY + 6);
         AtomicInteger atomicY = new AtomicInteger(0);

         for (ModifyIconButton button : this.modifyButtons) {
            button.setPosition(
               absX + 91 + (atomicY.get() % 2 == 0 ? 0 : 28) + this.offsetColumns(), absY + 33 + atomicY.getAndIncrement() / 2 * 11 + this.offsetRows()
            );
         }

         this.clearSecOutput.setPosition(absX + 119 + this.offsetColumns(), absY + 17);
         this.cycleOutputBtn.setPosition(absX + 119 + this.offsetColumns(), absY + 6);
         this.cycleInputBtn.setPosition(absX + 71 + this.offsetColumns(), absY + 17);
         this.scrollbar.setPosition(new Point(this.x + 8, this.y + 8));
         this.scrollbar.setSize(18, 52 + this.offsetRows());
         int effectiveSlotsPerRow = this.parent.getColumns() + 3;
         int effectiveRows = this.parent.getRows() + 3;
         int totalSlots = this.encodingMenu.getProcessingInputSlots().length;
         int inputsTotalRows = (totalSlots + effectiveSlotsPerRow - 1) / effectiveSlotsPerRow;
         int outputTotalRows = this.encodingMenu.getProcessingOutputSlots().length;
         int maxRows = Math.max(inputsTotalRows, outputTotalRows);
         this.scrollbar.setRange(0, Math.max(0, maxRows - effectiveRows), 1);
         this.activeSlots = new boolean[effectiveSlotsPerRow * effectiveRows];

         for (int i = 0; i < totalSlots; i++) {
            FakeSlot slot = this.encodingMenu.getProcessingInputSlots()[i];
            int effectiveRow = i / effectiveSlotsPerRow - this.scrollbar.getCurrentScroll();
            boolean isActive = effectiveRow >= 0 && effectiveRow < effectiveRows;
            slot.setActive(isActive);
            if (isActive) {
               this.activeSlots[effectiveRow * effectiveSlotsPerRow + i % effectiveSlotsPerRow] = true;
            }

            ME2in1Helper.setSlotPos(
               slot, this.x + i % effectiveSlotsPerRow * 18 + 17, -this.scrollbar.getCurrentScroll() * 18 + i / effectiveSlotsPerRow * 18 + this.y + 8
            );
         }

         for (int i = 0; i < this.encodingMenu.getProcessingOutputSlots().length; i++) {
            FakeSlot slot = this.encodingMenu.getProcessingOutputSlots()[i];
            int effectiveRow = i - this.scrollbar.getCurrentScroll();
            slot.setActive(effectiveRow >= 0 && effectiveRow < effectiveRows);
            ME2in1Helper.setSlotPos(slot, this.x + 101 + this.offsetColumns(), -this.scrollbar.getCurrentScroll() * 18 + i * 18 + this.y + 8);
         }
      }
   }

   protected int offsetColumns() {
      return this.parent.getColumns() * 18;
   }

   protected int offsetRows() {
      return this.parent.getRows() * 18;
   }

   @Override
   public void drawBackgroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
      BlitterHelper blitterHelper = BlitterHelper.of(guiGraphics);
      blitterHelper.gridBlit(FRAME_3x3, this.x + bounds.getX() + -9, this.y + bounds.getY() + -12, this.parent.getColumns() + 3, this.parent.getRows() + 3);
      blitterHelper.gridBlit(BG_3x3, this.x + bounds.getX() + 0, this.y + bounds.getY() + 0, this.parent.getColumns() + 3, this.parent.getRows() + 3);
      if (this.activeSlots != null) {
         for (int i = 0; i < this.activeSlots.length; i++) {
            if (!this.activeSlots[i]) {
               int row = i / (this.parent.getColumns() + 3);
               int col = i % (this.parent.getColumns() + 3);
               guiGraphics.fill(
                  this.x + col * 18 + bounds.getX() + 17,
                  this.y + row * 18 + bounds.getY() + 8,
                  this.x + col * 18 + bounds.getX() + 17 + 16,
                  this.y + row * 18 + bounds.getY() + 8 + 16,
                  2130706432
               );
            }
         }
      }

      this.scrollbar.drawBackgroundLayer(guiGraphics, bounds, mouse);
   }

   @Override
   public void drawForegroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
      super.drawForegroundLayer(guiGraphics, bounds, mouse);
      this.scrollbar.drawForegroundLayer(guiGraphics, bounds, mouse);
   }

   @Override
   public boolean onMouseWheel(Point mousePos, double delta) {
      return this.scrollbar.onMouseWheel(mousePos, delta);
   }

   @Override
   public ItemStack getTabIconItem() {
      return Items.FURNACE.getDefaultInstance();
   }

   @Override
   public Component getTabTooltip() {
      return GuiText.ProcessingPattern.text();
   }

   @Override
   public Rect2i getBounds() {
      return new Rect2i(this.x, this.y, 126 + this.offsetColumns(), 68 + this.offsetRows());
   }

   @Override
   public void setVisible(boolean visible) {
      super.setVisible(visible);
      this.scrollbar.setVisible(visible);
      this.cycleOutputBtn.setVisibility(visible && this.encodingMenu.canCycleProcessingOutputs());
      this.cycleInputBtn.setVisibility(visible && this.encodingMenu.canCycleProcessingInputs());

      for (ModifyIconButton button : this.modifyButtons) {
         button.setVisibility(visible);
      }

      this.clearSecOutput.setVisibility(visible && this.encodingMenu.canCycleProcessingOutputs());
      this.recordRecipeBtn.setVisibility(visible);
      this.screen.setSlotsHidden(SlotSemantics.PROCESSING_INPUTS, !visible);
      this.screen.setSlotsHidden(SlotSemantics.PROCESSING_OUTPUTS, !visible);
   }

   private ModifyIconButton getMultiplyButton(int multiplier) {
      String isDiv = multiplier < 0 ? "divide" : "multiply";
      int abs = Math.abs(multiplier);
      return new ModifyIconButton(
         b -> this.menu.gtolib$modifyPatter(multiplier),
         ModifyIcon.getBy(multiplier),
         Component.translatable("gtocore.pattern." + isDiv, abs),
         Component.translatable("gtocore.pattern.tooltip." + isDiv, abs)
      );
   }
}
