package com.gtolib.api.ae2.gui.hooks;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.client.Point;
import appeng.client.gui.Tooltip;
import appeng.menu.slot.FakeSlot;
import com.glodblock.github.extendedae.client.gui.widget.MultilineTextFieldWidget;
import com.lowdragmc.lowdraglib.core.mixins.accessor.SlotAccessor;
import gto_ae.client.gui.widgets.AEListBox;
import gto_ae.client.gui.widgets.AEListBox.ListItem;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Unique;

public interface ITagSelectableScreen {
   @Unique
   void gtolib$onChangedCallback(boolean var1, @Nullable AEKey var2);

   static void gtolib$onChangedCallback(
      boolean isWhitelist,
      @Nullable AEKey key,
      AEListBox gtolib$listBox,
      MultilineTextFieldWidget filterInputs,
      MultilineTextFieldWidget filterInputs2,
      FakeSlot gtolib$whitelistSlot,
      FakeSlot gtolib$blacklistSlot
   ) {
      if (key == null) {
         gtolib$listBox.setVisible(false);
      } else {
         gtolib$listBox.setVisible(true);
         List<String> tags;
         if (key.getType() == AEKeyType.items()) {
            tags = ((AEItemKey)key).getItem().getDefaultInstance().getTags().map(tagx -> tagx.location().toString()).toList();
         } else {
            if (key.getType() != AEKeyType.fluids()) {
               return;
            }

            tags = ((AEFluidKey)key).getFluid().defaultFluidState().getTags().map(tagx -> tagx.location().toString()).toList();
         }

         gtolib$listBox.clearItems();
         final MultilineTextFieldWidget fieldsInput = isWhitelist ? filterInputs : filterInputs2;
         SlotAccessor slot = (SlotAccessor)(isWhitelist ? gtolib$whitelistSlot : gtolib$blacklistSlot);
         gtolib$listBox.setPosition(new Point(slot.getX() + 20, slot.getY()));
         final Font font = Minecraft.getInstance().font;

         for (final String tag : tags) {
            final Component tagName = Component.literal(tag);
            gtolib$listBox.addItem(new ListItem() {
               boolean visible = true;
               Point position = new Point(0, 0);

               @Override
               public void setVisible(boolean visible) {
                  this.visible = visible;
               }

               @Override
               public void setPosition(Point position) {
                  this.position = position;
               }

               @Override
               public Rect2i getBounds() {
                  return new Rect2i(this.position.getX(), this.position.getY(), font.width(tagName), 9);
               }

               @Override
               public void drawForegroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
                  if (this.visible) {
                     guiGraphics.drawString(font, tagName, bounds.getX(), bounds.getY(), 16777215);
                  }
               }

               @Override
               public boolean onMouseUp(Point mousePos, int button) {
                  if (this.visible && this.getBounds().contains(mousePos.getX(), mousePos.getY())) {
                     switch (button) {
                        case 0:
                           fieldsInput.setValue(tag);
                           break;
                        case 1:
                           Minecraft.getInstance().keyboardHandler.setClipboard(tag);
                           break;
                        default:
                           return false;
                     }

                     return true;
                  } else {
                     return false;
                  }
               }

               @Override
               public Tooltip getTooltip(int mouseX, int mouseY) {
                  return new Tooltip(Component.translatable("cover.tag_filter.tag_entry.tooltip"));
               }
            });
         }
      }
   }
}
