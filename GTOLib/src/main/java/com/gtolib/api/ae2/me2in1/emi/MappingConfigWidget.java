package com.gtolib.api.ae2.me2in1.emi;

import appeng.client.Point;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.widgets.ConfirmableTextField;
import appeng.client.gui.widgets.IconButton;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import gto_ae.client.gui.widgets.AEListBox.ListItem;
import java.util.function.Consumer;
import lombok.Generated;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;

public class MappingConfigWidget implements ListItem {
   private boolean visible = true;
   private int x;
   private int y;
   private int w = 100;
   private int h = 25;
   private final EmiRecipeCategory category;
   private String value;
   private final ConfirmableTextField textField;
   private final IconButton deleteButton;
   private final CategoryMappingSubScreen screen;

   public MappingConfigWidget(EmiRecipeCategory category, String initialValue, final CategoryMappingSubScreen screen, Consumer<AbstractWidget> addToScreen) {
      this.category = category;
      this.value = initialValue;
      this.screen = screen;
      RecipeCatecoryMapping.addMapping(category.id, initialValue);
      this.textField = new ConfirmableTextField(screen.getStyle(), Minecraft.getInstance().font, 0, 0, 40, 10) {
         @Override
         public void onClick(double mouseX, double mouseY) {
            super.onClick(mouseX, mouseY);
            screen.setFocused(MappingConfigWidget.this.textField);
         }
      };
      this.textField.setBordered(false);
      this.textField.setTextColor(16777215);
      this.textField.setSelectionColor(-16777088);
      this.textField.setValue(this.value);
      this.textField.setOnConfirm(() -> this.onConfirm(this.textField));
      this.textField.setResponder(s -> {
         this.value = s;
         RecipeCatecoryMapping.addMapping(category.id, s);
      });
      this.deleteButton = new IconButton(b -> {
         RecipeCatecoryMapping.removeMapping(category.id);
         screen.refreshList();
      }) {
         @Override
         protected Icon getIcon() {
            return Icon.CLEAR;
         }
      };
      this.deleteButton.setTooltip(Tooltip.create(Component.translatable("gui.tooltips.ae2.Clear")));
   }

   @Override
   public void setVisible(boolean visible) {
      this.visible = visible;
      this.textField.setVisible(visible);
      this.deleteButton.setVisibility(visible);
   }

   @Override
   public void populateScreen(Consumer<AbstractWidget> addWidget, Rect2i bounds, AEBaseScreen<?> screen) {
      addWidget.accept(this.textField);
      addWidget.accept(this.deleteButton);
   }

   @Override
   public boolean isVisible() {
      return this.visible;
   }

   @Override
   public void setPosition(Point position) {
      this.x = position.getX();
      this.y = position.getY();
   }

   @Override
   public Rect2i getBounds() {
      return new Rect2i(this.x, this.y, this.w, this.h);
   }

   public void onConfirm(ConfirmableTextField textField) {
      RecipeCatecoryMapping.addMapping(this.category.id, textField.getValue());
      this.value = textField.getValue();
   }

   @Override
   public void onRemove() {
      if (this.textField.isHoveredOrFocused()) {
         this.textField.setFocused(false);
      }

      this.screen.removeWidget(this.textField);
      this.screen.removeWidget(this.deleteButton);
   }

   @Override
   public void drawForegroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
      if (this.visible) {
         guiGraphics.drawString(Minecraft.getInstance().font, this.category.getName(), bounds.getX(), bounds.getY(), 16777215);
         guiGraphics.pose().pushPose();
         guiGraphics.pose().translate(-this.screen.getGuiLeft(), -this.screen.getGuiTop(), 0.0F);
         this.textField.render(guiGraphics, mouse.getX(), mouse.getY(), Minecraft.getInstance().getDeltaFrameTime());
         this.deleteButton.render(guiGraphics, mouse.getX(), mouse.getY(), Minecraft.getInstance().getDeltaFrameTime());
         guiGraphics.pose().popPose();
      }
   }

   @Override
   public void updateBeforeRender() {
      this.textField.setX(this.x + this.screen.getGuiLeft());
      this.textField.setY(this.y + this.screen.getGuiTop() + 10);
      this.textField.setWidth(this.w - 24);
      this.textField.setHeight(this.h / 2);
      this.deleteButton.setY(this.y + this.screen.getGuiTop() + 8);
      this.deleteButton.setX(this.x + this.w - 20 + this.screen.getGuiLeft());
   }

   @Override
   public boolean onMouseDown(Point mousePos, int button) {
      int x = this.screen.getGuiLeft() + mousePos.getX();
      int y = this.screen.getGuiTop() + mousePos.getY();
      return this.textField.mouseClicked(x, y, button) || this.deleteButton.mouseClicked(x, y, button);
   }

   @Override
   public boolean onMouseUp(Point mousePos, int button) {
      int x = this.screen.getGuiLeft() + mousePos.getX();
      int y = this.screen.getGuiTop() + mousePos.getY();
      return this.textField.mouseReleased(x, y, button) || this.deleteButton.mouseReleased(x, y, button);
   }

   @Generated
   public int getX() {
      return this.x;
   }

   @Generated
   public int getY() {
      return this.y;
   }

   @Generated
   public int getW() {
      return this.w;
   }

   @Generated
   public int getH() {
      return this.h;
   }

   @Generated
   public void setX(int x) {
      this.x = x;
   }

   @Generated
   public void setY(int y) {
      this.y = y;
   }

   @Generated
   public void setW(int w) {
      this.w = w;
   }

   @Generated
   public void setH(int h) {
      this.h = h;
   }

   @Generated
   public ConfirmableTextField getTextField() {
      return this.textField;
   }

   @Generated
   public IconButton getDeleteButton() {
      return this.deleteButton;
   }
}
