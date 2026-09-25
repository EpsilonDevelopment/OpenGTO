package com.gtolib.api.ae2.me2in1.encoding;

import appeng.client.Point;
import appeng.client.gui.Icon;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.widgets.ToggleButton;
import appeng.core.localization.ButtonToolTips;
import appeng.core.localization.GuiText;
import appeng.menu.SlotSemantics;
import appeng.menu.slot.FakeSlot;
import com.gtolib.ae2.me2in1.panel.ModePanel;
import com.gtolib.api.ae2.me2in1.ME2in1Helper;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CraftingEncodingPanel extends EncodingSubPanel {
   private static final Blitter BG = Blitter.texture("guis/ex_encoding_modes.png").src(0, 0, 126, 68);
   private final ToggleButton substitutionsBtn = this.createSubstitutionButton();
   private final ToggleButton fluidSubstitutionsBtn;
   private static final int BG_X_OFFSET = 0;
   private static final int BG_Y_OFFSET = 0;
   private static final int CRAFTING_INPUT_SLOT_X_OFFSET = 9;
   private static final int CRAFTING_INPUT_SLOT_Y_OFFSET = 8;
   private static final int CRAFTING_RESULT_SLOT_X_OFFSET = 101;
   private static final int CRAFTING_RESULT_SLOT_Y_OFFSET = 26;
   private static final int CLR_BTN_X_OFFSET = 76;
   private static final int SUB_BTN_X_OFFSET = 86;
   private static final int F_SUB_BTN_X_OFFSET = 96;
   private static final int BTN_Y_OFFSET = 6;

   public CraftingEncodingPanel(ModePanel superPanel) {
      super(superPanel);
      this.screen.getSubWidgets().put("crafting_substitutions_btn", this.substitutionsBtn);
      this.fluidSubstitutionsBtn = this.createCraftingFluidSubstitutionButton();
      this.screen.getSubWidgets().put("crafting_fluid_substitutions_btn", this.fluidSubstitutionsBtn);
   }

   @Override
   public ItemStack getTabIconItem() {
      return Items.CRAFTING_TABLE.getDefaultInstance();
   }

   @Override
   public Component getTabTooltip() {
      return GuiText.CraftingPattern.text();
   }

   private ToggleButton createCraftingFluidSubstitutionButton() {
      ToggleButton button = new ToggleButton(Icon.FLUID_SUBSTITUTION_ENABLED, Icon.FLUID_SUBSTITUTION_DISABLED, this.menu::setSubstituteFluids);
      button.setHalfSize(true);
      button.setTooltipOn(List.of(ButtonToolTips.FluidSubstitutions.text(), ButtonToolTips.FluidSubstitutionsDescEnabled.text()));
      button.setTooltipOff(List.of(ButtonToolTips.FluidSubstitutions.text(), ButtonToolTips.FluidSubstitutionsDescDisabled.text()));
      return button;
   }

   @Override
   public void drawBackgroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
      super.drawBackgroundLayer(guiGraphics, bounds, mouse);
      BG.dest(this.x + bounds.getX() + 0, this.y + bounds.getY() + 0).blit(guiGraphics);
      int absMouseX = bounds.getX() + mouse.getX();
      int absMouseY = bounds.getY() + mouse.getY();
      if (this.menu.isSubstituteFluids() && this.fluidSubstitutionsBtn.isMouseOver(absMouseX, absMouseY)) {
         for (Integer slotIndex : this.encodingMenu.getSlotsSupportingFluidSubstitution()) {
            this.drawSlotGreenBG(bounds, guiGraphics, this.encodingMenu.getCraftingGridSlots()[slotIndex]);
         }
      }
   }

   private void drawSlotGreenBG(Rect2i bounds, GuiGraphics guiGraphics, Slot slot) {
      int x = bounds.getX() + slot.x;
      int y = bounds.getY() + slot.y;
      guiGraphics.fill(x, y, x + 16, y + 16, 2130771712);
   }

   @Override
   public void updateBeforeRender() {
      this.getScreen().setSlotsHidden(SlotSemantics.CRAFTING_GRID, !this.isVisible());
      this.getScreen().setSlotsHidden(SlotSemantics.CRAFTING_RESULT, !this.isVisible());
      if (this.isVisible()) {
         this.getScreen().setSlotsHidden(SlotSemantics.CRAFTING_GRID, !this.isVisible());
         this.getScreen().setSlotsHidden(SlotSemantics.CRAFTING_RESULT, !this.isVisible());

         for (FakeSlot slot : this.getScreen().getMenu().getEncoding().getCraftingGridSlots()) {
            slot.setActive(this.menu.mode == ExtendedEncodingMode.CRAFTING);
         }

         this.getScreen().getMenu().getEncoding().getCraftOutputSlot().setActive(this.menu.mode == ExtendedEncodingMode.CRAFTING);
         this.clearBtn.setPosition(this.getGuiLeft() + this.x + 76, this.y + 6 + this.getGuiTop());
         this.substitutionsBtn.setState(this.menu.isSubstitute());
         this.substitutionsBtn.setPosition(this.getGuiLeft() + this.x + 86, this.y + 6 + this.getGuiTop());
         this.fluidSubstitutionsBtn.setState(this.menu.isSubstituteFluids());
         this.fluidSubstitutionsBtn.setPosition(this.getGuiLeft() + this.x + 96, this.y + 6 + this.getGuiTop());

         for (int i = 0; i < this.encodingMenu.getCraftingGridSlots().length; i++) {
            Slot slot = this.encodingMenu.getCraftingGridSlots()[i];
            if (slot != null) {
               ME2in1Helper.setSlotPos(slot, i % 3 * 18 + this.x + 9, i / 3 * 18 + this.y + 8);
            }
         }

         Slot resultSlot = this.encodingMenu.getCraftOutputSlot();
         if (resultSlot != null) {
            ME2in1Helper.setSlotPos(resultSlot, this.x + 101, this.y + 26);
         }
      }
   }

   @Override
   public void setVisible(boolean visible) {
      super.setVisible(visible);
      this.substitutionsBtn.setVisibility(visible);
      this.fluidSubstitutionsBtn.setVisibility(visible);
      this.screen.setSlotsHidden(SlotSemantics.CRAFTING_GRID, !visible);
      this.screen.setSlotsHidden(SlotSemantics.CRAFTING_RESULT, !visible);
   }
}
