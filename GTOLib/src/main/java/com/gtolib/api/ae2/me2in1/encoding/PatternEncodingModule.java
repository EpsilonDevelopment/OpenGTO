package com.gtolib.api.ae2.me2in1.encoding;

import appeng.api.behaviors.ContainerItemStrategies;
import appeng.api.behaviors.EmptyingAction;
import appeng.api.stacks.GenericStack;
import appeng.client.Point;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.Tooltip;
import appeng.core.AEConfig;
import appeng.core.localization.ButtonToolTips;
import appeng.core.localization.Tooltips;
import com.gtolib.ae2.me2in1.panel.BufferPanel;
import com.gtolib.ae2.me2in1.panel.ModePanel;
import com.gtolib.api.ae2.me2in1.ExtendedEncodingMenu;
import com.gtolib.api.ae2.me2in1.Me2in1Screen;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.mixin.client.AbstractContainerScreenAccessor;

public class PatternEncodingModule implements ICompositeWidget {
   private final Me2in1Screen<?> screen;
   private final BufferPanel bufferPanel;
   private final ModePanel modePanel;

   public PatternEncodingModule(Me2in1Screen<?> screen) {
      this.screen = screen;
      this.modePanel = new ModePanel(screen);
      this.bufferPanel = new BufferPanel(screen);
      screen.getWidgets().add("PatternEncodingModule_encodePanel", this.modePanel);
      screen.getWidgets().add("PatternEncodingModule_bufferPanel", this.bufferPanel);
   }

   private ExtendedEncodingMenu getEncodingMenu() {
      return this.screen.getMenu().getEncoding();
   }

   public AEBaseScreen<?> getScreen() {
      return this.screen;
   }

   @Override
   public void setPosition(Point position) {
   }

   @Override
   public void setSize(int width, int height) {
   }

   @Override
   public Rect2i getBounds() {
      return new Rect2i(0, 0, 126, 68);
   }

   @Nullable
   @Override
   public Tooltip getTooltip(int mouseX, int mouseY) {
      Slot hoveredSlot = ((AbstractContainerScreenAccessor)this.screen).getHoveredSlot();
      if (this.screen.getMenu().getCarried().isEmpty() && this.getEncodingMenu().canModifyAmountForSlot(hoveredSlot)) {
         ArrayList<Component> itemTooltip = new ArrayList<>(this.screen.getTooltipFromContainerItem(hoveredSlot.getItem()));
         GenericStack unwrapped = GenericStack.fromItemStack(hoveredSlot.getItem());
         if (unwrapped != null) {
            itemTooltip.add(Tooltips.getAmountTooltip(ButtonToolTips.Amount, unwrapped));
         }

         itemTooltip.add(Tooltips.getSetAmountTooltip());
         return new Tooltip(itemTooltip);
      } else {
         return null;
      }
   }

   @Nullable
   public EmptyingAction getEmptyingAction(Slot slot, ItemStack carried) {
      return this.getEncodingMenu().isProcessingPatternSlot(slot) ? ContainerItemStrategies.getEmptyingAction(carried) : null;
   }

   public List<Component> getTooltipFromContainerItem(ItemStack stack) {
      ArrayList<Component> lines = new ArrayList<>();
      Slot hoveredSlot = ((AbstractContainerScreenAccessor)this.screen).getHoveredSlot();
      if (hoveredSlot != null && this.screen.shouldShowCraftableIndicatorForSlot(hoveredSlot)) {
         lines = new ArrayList<>(lines);
         lines.add(ButtonToolTips.Craftable.text().withStyle(ChatFormatting.DARK_GRAY));
      }

      return lines;
   }

   public void onClose() {
      if (AEConfig.instance().isClearGridOnClose()) {
         this.getEncodingMenu().clear();
         this.getEncodingMenu().clearFilter();
      }
   }

   public void syncPanelStateFromMenu() {
      this.screen.getMenu().receiveS2CPanelUpdate(this.bufferPanel);
      this.screen.getMenu().receiveS2CPanelUpdate(this.modePanel);
   }

   public void resetPanels() {
      this.bufferPanel.resetPosition();
      this.bufferPanel.resetSize();
      this.modePanel.resetPosition();
      this.modePanel.resetSize();
   }
}
