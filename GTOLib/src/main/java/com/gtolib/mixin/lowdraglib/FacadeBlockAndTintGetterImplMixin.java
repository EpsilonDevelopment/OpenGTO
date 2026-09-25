package com.gtolib.mixin.lowdraglib;

import com.lowdragmc.lowdraglib.utils.forge.FacadeBlockAndTintGetterImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(FacadeBlockAndTintGetterImpl.class)
public class FacadeBlockAndTintGetterImplMixin {
   @Overwrite(remap = false)
   public static BlockState getAppearance(BlockState var0, BlockAndTintGetter var1, BlockPos var2, Direction var3, BlockState var4, BlockPos var5) {
      return var0.getBlock().getAppearance(var0, var1, var2, var3, var4, var5);
   }
}
