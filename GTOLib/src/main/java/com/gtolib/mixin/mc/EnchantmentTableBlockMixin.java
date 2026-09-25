package com.gtolib.mixin.mc;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantmentTableBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(EnchantmentTableBlock.class)
public class EnchantmentTableBlockMixin {
   @Overwrite
   public static boolean isValidBookShelf(Level var0, BlockPos var1, BlockPos var2) {
      return var0.getBlockState(var1.offset(var2)).getEnchantPowerBonus(var0, var1.offset(var2)) != 0.0F
         && var0.getBlockState(var1.offset(var2.getX() / 2, var2.getY(), var2.getZ() / 2)).isAir();
   }
}
