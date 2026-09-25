package com.gtolib.mixin.farmersdelight;

import com.gtolib.gtm.IContinuousCraftingItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeItem;
import org.spongepowered.asm.mixin.Mixin;
import vectorwing.farmersdelight.common.item.KnifeItem;

@Mixin(KnifeItem.class)
public abstract class KnifeItemMixin implements IContinuousCraftingItem, IForgeItem {
   @Override
   public ItemStack getCraftingRemainingItem(ItemStack var1) {
      return this.definition$getCraftingRemainingItem(var1);
   }

   @Override
   public boolean hasCraftingRemainingItem(ItemStack var1) {
      return true;
   }
}
