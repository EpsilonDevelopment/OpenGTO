package com.gtolib.mixin.forge;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ItemHandlerHelper.class)
public class ItemHandlerHelperMixin {
   @Overwrite(remap = false)
   public static boolean canItemStacksStack(@NotNull ItemStack var0, @NotNull ItemStack var1) {
      Item var2 = var0.getItem();
      if (var2 == Items.AIR) {
         return false;
      } else {
         Item var3 = var1.getItem();
         if (var2 == var3) {
            CompoundTag var4 = var0.getTag();
            CompoundTag var5 = var1.getTag();
            return var4 != null && !var4.isEmpty() ? var4.equals(var5) : var5 == null || var5.isEmpty();
         } else {
            return false;
         }
      }
   }

   @Overwrite(remap = false)
   public static boolean canItemStacksStackRelaxed(@NotNull ItemStack var0, @NotNull ItemStack var1) {
      Item var2 = var0.getItem();
      if (var2 == Items.AIR) {
         return false;
      }

      Item var3 = var1.getItem();
      if (var2 == var3) {
         CompoundTag var4 = var0.getTag();
         CompoundTag var5 = var1.getTag();
         if (var4 != null && !var4.isEmpty()) {
            return !var0.isStackable() ? false : var4.equals(var5);
         } else {
            return var5 == null || var5.isEmpty();
         }
      } else {
         return false;
      }
   }
}
