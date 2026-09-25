package com.gtolib.api.ae2.me2in1.encoding;

import appeng.client.Point;
import appeng.client.gui.Tooltip;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.widgets.Scrollbar;
import appeng.core.localization.GuiText;
import appeng.menu.SlotSemantics;
import appeng.menu.slot.FakeSlot;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.gtolib.ae2.me2in1.panel.ModePanel;
import com.gtolib.api.ae2.me2in1.ME2in1Helper;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import org.jetbrains.annotations.Nullable;

public final class StonecuttingEncodingPanel extends EncodingSubPanel {
   private static final Blitter BG = Blitter.texture("guis/ex_encoding_modes.png").src(0, 141, 126, 68);
   private static final Blitter BG_SLOT = BG.copy().src(126, 141, 16, 18);
   private static final Blitter BG_SLOT_SELECTED = BG.copy().src(126, 159, 16, 18);
   private static final Blitter BG_SLOT_HOVER = BG.copy().src(126, 177, 16, 18);
   private static final int COLS = 4;
   private static final int ROWS = 3;
   private final Scrollbar scrollbar = new Scrollbar(Scrollbar.SMALL);
   private static final int BG_X_OFFSET = 0;
   private static final int BG_Y_OFFSET = 0;
   private static final int SCROLLBAR_X = 110;
   private static final int SCROLLBAR_Y = 8;
   private static final int CLR_BTN_X_OFFSET = 31;
   private static final int CLR_BTN_Y_OFFSET = 24;
   private static final int STONECUTTER_SLOT_X = 12;
   private static final int STONECUTTER_SLOT_Y = 25;
   private static final int STONECUTTER_OUT_SLOT_X = 44;
   private static final int STONECUTTER_OUT_SLOT_Y = 8;

   public StonecuttingEncodingPanel(ModePanel p) {
      super(p);
      this.scrollbar.setRange(0, 0, 4);
      this.scrollbar.setCaptureMouseWheel(false);
   }

   @Override
   public void updateBeforeRender() {
      this.getScreen().setSlotsHidden(SlotSemantics.STONECUTTING_INPUT, !this.isVisible());
      if (this.isVisible()) {
         this.clearBtn.setPosition(31 + this.x + this.getGuiLeft(), this.y + this.getGuiTop() + 24);
         int totalRows = (this.encodingMenu.getStonecuttingRecipes().size() + 4 - 1) / 4;
         this.scrollbar.setRange(0, totalRows - 3, 3);
         this.scrollbar.setPosition(new Point(this.x + 110, this.y + 8));
         this.scrollbar.setSize(18, 52);
         FakeSlot inputSlot = this.encodingMenu.getStonecuttingInputSlot();
         ME2in1Helper.setSlotPos(inputSlot, this.x + 12, this.y + 25);
      }
   }

   @Override
   public void drawBackgroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
      super.drawBackgroundLayer(guiGraphics, bounds, mouse);
      BG.dest(this.x + bounds.getX() + 0, this.y + bounds.getY() + 0).blit(guiGraphics);
      this.scrollbar.drawBackgroundLayer(guiGraphics, bounds, mouse);
      this.drawRecipes(guiGraphics, bounds, mouse);
   }

   @Override
   public void drawForegroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
      super.drawForegroundLayer(guiGraphics, bounds, mouse);
      this.scrollbar.drawForegroundLayer(guiGraphics, bounds, mouse);
   }

   private RegistryAccess getRegistryAccess() {
      return Objects.requireNonNull(GTUtil.getClientLevel()).registryAccess();
   }

   private void drawRecipes(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
      List<StonecutterRecipe> recipes = this.encodingMenu.getStonecuttingRecipes();
      int startIndex = this.scrollbar.getCurrentScroll() * 4;
      int endIndex = startIndex + 12;
      ResourceLocation selectedRecipe = this.menu.getStonecuttingRecipeId();

      for (int i = startIndex; i < endIndex && i < recipes.size(); i++) {
         Rect2i slotBounds = this.getRecipeBounds(i - startIndex);
         StonecutterRecipe recipe = recipes.get(i);
         boolean selected = selectedRecipe != null && selectedRecipe.equals(recipe.getId());
         Blitter blitter = BG_SLOT;
         if (selected) {
            blitter = BG_SLOT_SELECTED;
         } else if (mouse.isIn(slotBounds)) {
            blitter = BG_SLOT_HOVER;
         }

         int renderX = bounds.getX() + slotBounds.getX();
         int renderY = bounds.getY() + slotBounds.getY();
         blitter.dest(renderX, renderY - 1).blit(guiGraphics);
         ItemStack resultItem = recipe.getResultItem(this.getRegistryAccess());
         guiGraphics.renderItem(resultItem, renderX, renderY);
         guiGraphics.renderItemDecorations(Minecraft.getInstance().font, resultItem, renderX, renderY);
      }
   }

   @Override
   public boolean onMouseDown(Point mousePos, int button) {
      StonecutterRecipe recipe = this.getRecipeAt(mousePos);
      if (recipe != null) {
         this.encodingMenu.setStonecuttingRecipeId(recipe.getId());
         Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
         return true;
      } else {
         return false;
      }
   }

   @Nullable
   @Override
   public Tooltip getTooltip(int mouseX, int mouseY) {
      StonecutterRecipe recipe = this.getRecipeAt(new Point(mouseX, mouseY));
      if (recipe != null) {
         List<Component> lines = this.screen.getTooltipFromContainerItem(recipe.getResultItem(this.getRegistryAccess()));
         return new Tooltip(lines);
      } else {
         return null;
      }
   }

   @Nullable
   private StonecutterRecipe getRecipeAt(Point point) {
      List<StonecutterRecipe> recipes = this.encodingMenu.getStonecuttingRecipes();
      if (!recipes.isEmpty()) {
         int startIndex = this.scrollbar.getCurrentScroll() * 4;
         int endIndex = startIndex + 12;

         for (int i = startIndex; i < endIndex && i < recipes.size(); i++) {
            Rect2i slotBounds = this.getRecipeBounds(i - startIndex);
            if (point.isIn(slotBounds)) {
               return recipes.get(i);
            }
         }
      }

      return null;
   }

   private Rect2i getRecipeBounds(int index) {
      int col = index % 4;
      int row = index / 4;
      int slotX = this.x + 44 + col * BG_SLOT.getSrcWidth();
      int slotY = this.y + 8 + row * BG_SLOT.getSrcHeight();
      return new Rect2i(slotX, slotY, BG_SLOT.getSrcWidth(), BG_SLOT.getSrcHeight());
   }

   @Override
   public boolean onMouseWheel(Point mousePos, double delta) {
      return this.scrollbar.onMouseWheel(mousePos, delta);
   }

   @Override
   public ItemStack getTabIconItem() {
      return new ItemStack(Items.STONECUTTER);
   }

   @Override
   public Component getTabTooltip() {
      return GuiText.StonecuttingPattern.text();
   }

   @Override
   public void setVisible(boolean visible) {
      super.setVisible(visible);
      this.scrollbar.setVisible(visible);
      this.screen.setSlotsHidden(SlotSemantics.STONECUTTING_INPUT, !visible);
   }
}
