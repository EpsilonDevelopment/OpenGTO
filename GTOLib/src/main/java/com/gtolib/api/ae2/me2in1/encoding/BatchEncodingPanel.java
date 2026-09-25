package com.gtolib.api.ae2.me2in1.encoding;

import appeng.api.config.ActionItems;
import appeng.client.Point;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.widgets.ActionButton;
import appeng.menu.SlotSemantics;
import appeng.menu.slot.FakeSlot;
import com.gtocore.common.data.GTOMachines;
import com.gtolib.ae2.me2in1.panel.ModePanel;
import com.gtolib.api.ae2.ModifyIconButton;
import com.gtolib.api.ae2.me2in1.GTOSlotSemantics;
import com.gtolib.api.ae2.me2in1.ME2in1Helper;
import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

@DataGeneratorScanned
public class BatchEncodingPanel extends ProcessingEncodingPanel {
   private static final int HIDDEN_SLOT_POS = -9999;
   private static final Blitter FILTER_SLOTS = Blitter.texture("guis/encoding_module.png").src(0, 112, 180, 50);
   @RegisterLanguage(cn = "批量编码", en = "Batch Encoding")
   private static final String BATCH_ENCODING_TAB_TOOLTIP = "gtocore.machine.batch_encoding_tab_tooltip";
   private final ActionButton clearFilterBtn = new ActionButton(ActionItems.CLOSE, act -> this.encodingMenu.clearFilter());
   private static final int BG_X_OFFSET = -49;
   private static final int BG_Y_OFFSET = 72;
   private static final int FILTER_SLOT_X = -39;
   private static final int FLITER_SLOT_Y = 87;
   private static final int CLEAR_FILTER_BTN_X = 110;
   private static final int CLEAR_FILTER_BTN_Y = 106;

   public BatchEncodingPanel(ModePanel p) {
      super(p);
      this.clearFilterBtn.setHalfSize(true);
      this.screen.getSubWidgets().put("clearFilterBtn", this.clearFilterBtn);
   }

   @Override
   public void drawBackgroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
      super.drawBackgroundLayer(guiGraphics, bounds, mouse);
      FILTER_SLOTS.dest(this.x + bounds.getX() + -49 + this.offsetColumns(), this.y + bounds.getY() + 72 + this.offsetRows()).blit(guiGraphics);
   }

   @Override
   public void updateBeforeRender() {
      super.updateBeforeRender();
      this.screen.setSlotsHidden(GTOSlotSemantics.BATCH_FILTER, !this.isVisible());
      if (!this.isVisible()) {
         for (FakeSlot slot : this.encodingMenu.getMaterialSlots()) {
            slot.setActive(false);
            ME2in1Helper.setSlotPos(slot, -9999, -9999);
         }
      } else {
         for (int i = 0; i < this.encodingMenu.getMaterialSlots().length; i++) {
            FakeSlot slot = this.encodingMenu.getMaterialSlots()[i];
            ME2in1Helper.setSlotPos(slot, this.x + i * 18 + -39 + this.parent.getColumns() * 18, this.y + 87 + this.parent.getRows() * 18);
            slot.setActive(true);
         }

         this.clearFilterBtn
            .setPosition(this.x + this.getGuiLeft() + 110 + this.parent.getColumns() * 18, this.y + this.getGuiTop() + 106 + this.parent.getRows() * 18);
      }
   }

   @Override
   public void setVisible(boolean visible) {
      this.visible = visible;
      this.scrollbar.setVisible(visible);
      this.clearBtn.setVisibility(visible);
      this.clearSecOutput.setVisibility(visible && this.encodingMenu.canCycleProcessingOutputs());
      this.cycleOutputBtn.setVisibility(visible && this.encodingMenu.canCycleProcessingOutputs());
      this.cycleInputBtn.setVisibility(visible && this.encodingMenu.canCycleProcessingInputs());

      for (ModifyIconButton button : this.modifyButtons) {
         button.setVisibility(visible);
      }

      this.clearFilterBtn.setVisibility(visible);
      this.recordRecipeBtn.setVisibility(visible);
      this.screen.setSlotsHidden(SlotSemantics.PROCESSING_OUTPUTS, !visible);
      this.screen.setSlotsHidden(SlotSemantics.PROCESSING_INPUTS, !visible);
      this.screen.setSlotsHidden(GTOSlotSemantics.BATCH_FILTER, !visible);
   }

   @Override
   public ItemStack getTabIconItem() {
      return GTOMachines.THREAD_HATCH[8].asStack();
   }

   @Override
   public Component getTabTooltip() {
      return Component.translatable("gtocore.machine.batch_encoding_tab_tooltip");
   }
}
