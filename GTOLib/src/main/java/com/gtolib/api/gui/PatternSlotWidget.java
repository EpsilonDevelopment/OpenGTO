package com.gtolib.api.gui;

import com.gregtechceu.gtceu.api.gui.ISlotWidget;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.transfer.item.ICustomItemStackHandler;
import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class PatternSlotWidget extends SlotWidget {
   private final Supplier<EmiIngredient> ingredient;

   public PatternSlotWidget(ICustomItemStackHandler itemHandler, int slotIndex, int xPosition, int yPosition) {
      super(itemHandler, slotIndex, xPosition, yPosition, false, false);
      this.setClientSideWidget();
      this.setBackgroundTexture(ColorPattern.T_GRAY.rectTexture());
      this.ingredient = () -> EmiStack.of(itemHandler.getStackInSlot(slotIndex));
      this.appendHoverTooltips(Component.translatable("gui.tooltips.ae2.Amount", itemHandler.getStackInSlot(0).getCount()).withStyle(ChatFormatting.GRAY));
      ((ISlotWidget)this).gtm$setAEStylePredicate(stack -> true);
   }

   @Override
   public Object getXEIIngredientOverMouse(double mouseX, double mouseY) {
      return this.isMouseOverElement(mouseX, mouseY) ? this.ingredient.get() : null;
   }

   @Override
   public List<Object> getXEIIngredients() {
      return List.of(this.ingredient.get());
   }

   @Override
   public Object getXEICurrentIngredient() {
      return this.ingredient.get();
   }
}
