package com.gtolib.api.ae2.me2in1.encoding;

import appeng.client.Point;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.widgets.ToggleButton;
import appeng.core.localization.GuiText;
import appeng.menu.SlotSemantics;
import com.gtolib.ae2.me2in1.panel.ModePanel;
import com.gtolib.api.ae2.me2in1.ME2in1Helper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.level.Level;

public class SmithingTableEncodingPanel extends EncodingSubPanel {
   private static final Blitter BG = Blitter.texture("guis/ex_encoding_modes.png").src(128, 70, 126, 68);
   private final ToggleButton substitutionsBtn = this.createSubstitutionButton();
   private final Slot resultSlot = new Slot(new SimpleContainer(1), 0, 0, 0);
   private static final int BG_X_OFFSET = 0;
   private static final int BG_Y_OFFSET = 0;
   private static final int SLOT_INGOT_X_OFFSET = 48;
   private static final int SLOT_TOOL_X_OFFSET = 30;
   private static final int SLOT_TEMPLATE_X_OFFSET = 12;
   private static final int SLOT_RESULT_X_OFFSET = 102;
   private static final int SLOT_Y_OFFSET = 26;
   private static final int CLR_BTN_X_OFFSET = 68;
   private static final int CLR_BTN_Y_OFFSET = 20;

   public SmithingTableEncodingPanel(ModePanel p) {
      super(p);
      this.screen.getMenu().addClientSideSlot(this.resultSlot, SlotSemantics.SMITHING_TABLE_RESULT);
   }

   @Override
   public ItemStack getTabIconItem() {
      return Items.SMITHING_TABLE.getDefaultInstance();
   }

   @Override
   public Component getTabTooltip() {
      return GuiText.SmithingTablePattern.text();
   }

   @Override
   public void drawBackgroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
      super.drawBackgroundLayer(guiGraphics, bounds, mouse);
      BG.dest(this.x + bounds.getX() + 0, this.y + bounds.getY() + 0).blit(guiGraphics);
   }

   @Override
   public void updateBeforeRender() {
      this.screen.setSlotsHidden(SlotSemantics.SMITHING_TABLE_TEMPLATE, !this.isVisible());
      this.screen.setSlotsHidden(SlotSemantics.SMITHING_TABLE_BASE, !this.isVisible());
      this.screen.setSlotsHidden(SlotSemantics.SMITHING_TABLE_ADDITION, !this.isVisible());
      this.screen.setSlotsHidden(SlotSemantics.SMITHING_TABLE_RESULT, !this.isVisible());
      if (this.isVisible()) {
         this.clearBtn.setPosition(this.getGuiLeft() + this.x + 68, this.y + 20 + this.getGuiTop());
         this.substitutionsBtn.setState(this.menu.isSubstitute());
         this.substitutionsBtn.setPosition(100 + this.x, 80 + this.y);
         SimpleContainer container = new SimpleContainer(3);
         container.setItem(0, this.encodingMenu.getSmithingTableTemplateSlot().getItem());
         container.setItem(1, this.encodingMenu.getSmithingTableBaseSlot().getItem());
         container.setItem(2, this.encodingMenu.getSmithingTableAdditionSlot().getItem());
         ME2in1Helper.setSlotPos(this.encodingMenu.getSmithingTableAdditionSlot(), 48 + this.x, 26 + this.y);
         ME2in1Helper.setSlotPos(this.encodingMenu.getSmithingTableBaseSlot(), 30 + this.x, 26 + this.y);
         ME2in1Helper.setSlotPos(this.encodingMenu.getSmithingTableTemplateSlot(), 12 + this.x, 26 + this.y);
         ME2in1Helper.setSlotPos(this.resultSlot, 102 + this.x, 26 + this.y);
         Level level = this.encodingMenu.getPlayer().level();
         SmithingRecipe recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMITHING, container, level).orElse(null);
         if (recipe == null) {
            this.resultSlot.set(ItemStack.EMPTY);
         } else {
            this.resultSlot.set(recipe.assemble(container, level.registryAccess()));
         }
      }
   }

   @Override
   public void setVisible(boolean visible) {
      super.setVisible(visible);
      this.clearBtn.setVisibility(visible);
      this.substitutionsBtn.setVisibility(visible);
      this.screen.setSlotsHidden(SlotSemantics.SMITHING_TABLE_TEMPLATE, !visible);
      this.screen.setSlotsHidden(SlotSemantics.SMITHING_TABLE_BASE, !visible);
      this.screen.setSlotsHidden(SlotSemantics.SMITHING_TABLE_ADDITION, !visible);
      this.screen.setSlotsHidden(SlotSemantics.SMITHING_TABLE_RESULT, !visible);
   }
}
