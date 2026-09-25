package com.gtolib.ae2.me2in1;

import appeng.client.gui.Icon;
import appeng.client.gui.widgets.ITooltip;
import appeng.client.gui.widgets.TabButton.Style;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Collections;
import java.util.List;
import lombok.Generated;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class NoShadowsTabButton extends Button implements ITooltip {
   private Style style = Style.BOX;
   private Icon icon = null;
   private ItemStack item;
   private boolean selected;

   public NoShadowsTabButton(Icon ico, Component message, OnPress onPress) {
      super(0, 0, 22, 22, message, onPress, Button.DEFAULT_NARRATION);
      this.icon = ico;
   }

   public NoShadowsTabButton(ItemStack ico, Component message, OnPress onPress) {
      super(0, 0, 22, 22, message, onPress, Button.DEFAULT_NARRATION);
      this.item = ico;
   }

   @Override
   public void renderWidget(GuiGraphics guiGraphics, int x, int y, float partial) {
      if (this.visible) {
         PoseStack pose = guiGraphics.pose();

         Icon backdrop = switch (this.style) {
            case CORNER -> this.isFocused() ? Icon.TAB_BUTTON_BACKGROUND_BORDERLESS_FOCUS : Icon.TAB_BUTTON_BACKGROUND_BORDERLESS;
            case BOX -> this.isFocused() ? Icon.TAB_BUTTON_BACKGROUND_FOCUS : Icon.TAB_BUTTON_BACKGROUND;
            case HORIZONTAL -> this.isFocused() ? Icon.HORIZONTAL_TAB_FOCUS : (this.selected ? Icon.HORIZONTAL_TAB_SELECTED : Icon.HORIZONTAL_TAB);
         };

         int baseIconX = switch (this.style) {
            case CORNER -> 4;
            case BOX -> 3;
            case HORIZONTAL -> 1;
         };
         int baseIconY = 3;
         pose.pushPose();

         try {
            if (this.style == Style.HORIZONTAL) {
               float centerX = this.getX() + this.width / 2.0F;
               float centerY = this.getY() + this.height / 2.0F;
               pose.translate(centerX, centerY, 0.0F);
               pose.mulPose(Axis.ZP.rotationDegrees(-90.0F));
               pose.translate(-centerX, -centerY, 0.0F);
            }

            backdrop.getBlitter().dest(this.getX(), this.getY()).blit(guiGraphics);
         } finally {
            pose.popPose();
         }

         int finalIconX;
         int finalIconY;
         if (this.style == Style.HORIZONTAL) {
            int iconSize = 16;
            float iconCenterX = baseIconX + 8.0F;
            float iconCenterY = baseIconY + 8.0F;
            float rotatedCenterX = iconCenterY;
            float rotatedCenterY = this.height - iconCenterX;
            finalIconX = this.getX() + (int)(rotatedCenterX - 8.0F);
            finalIconY = this.getY() + (int)(rotatedCenterY - 8.0F);
         } else {
            finalIconX = this.getX() + baseIconX;
            finalIconY = this.getY() + baseIconY;
         }

         if (this.icon != null) {
            this.icon.getBlitter().dest(finalIconX, finalIconY).blit(guiGraphics);
         }

         if (this.item != null) {
            pose.pushPose();
            pose.translate(0.0F, 0.0F, 100.0F);
            guiGraphics.renderItem(this.item, finalIconX, finalIconY);
            Font font = Minecraft.getInstance().font;
            guiGraphics.renderItemDecorations(font, this.item, finalIconX, finalIconY);
            pose.popPose();
         }
      }
   }

   @Override
   public List<Component> getTooltipMessage() {
      return Collections.singletonList(this.getMessage());
   }

   @Override
   public Rect2i getTooltipArea() {
      return new Rect2i(this.getX(), this.getY(), this.width, this.height);
   }

   @Override
   public boolean isTooltipAreaVisible() {
      return this.visible;
   }

   @Generated
   public void setStyle(Style style) {
      this.style = style;
   }

   @Generated
   public Style getStyle() {
      return this.style;
   }

   @Generated
   public void setSelected(boolean selected) {
      this.selected = selected;
   }

   @Generated
   public boolean isSelected() {
      return this.selected;
   }
}
