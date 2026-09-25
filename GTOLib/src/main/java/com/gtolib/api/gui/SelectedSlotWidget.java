package com.gtolib.api.gui;

import com.gregtechceu.gtceu.api.transfer.item.ICustomItemStackHandler;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ListEmiIngredient;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public class SelectedSlotWidget extends PatternSlotWidget {
   private final List<EmiStack> candidateStacks;

   public SelectedSlotWidget(List<ItemStack> candidateStacks, ICustomItemStackHandler itemHandler, int slotIndex, int xPosition, int yPosition) {
      super(itemHandler, slotIndex, xPosition, yPosition);
      this.candidateStacks = candidateStacks.stream().map(EmiStack::of).toList();
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (!this.isMouseOverElement(mouseX, mouseY) || button != 0 && button != 1) {
         return false;
      }

      EmiApi.displayRecipes(new ListEmiIngredient(this.candidateStacks, 1L));
      return true;
   }
}
