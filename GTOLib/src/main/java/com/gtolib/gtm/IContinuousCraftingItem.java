package com.gtolib.gtm;

import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;

public interface IContinuousCraftingItem {
   default ItemStack definition$getCraftingRemainingItem(ItemStack stack) {
      stack = stack.copy();
      Player player = ForgeHooks.getCraftingPlayer();
      ToolHelper.damageItemWhenCrafting(stack, player);
      return stack.isEmpty() ? ItemStack.EMPTY : stack;
   }
}
