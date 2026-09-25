package com.gtolib.mixin.mc;

import com.gtolib.mc.ILevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.extensions.IForgeBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = Block.class, priority = 0)
public abstract class BlockMixin implements IForgeBlock {
   @Override
   public void onBlockExploded(BlockState var1, Level var2, BlockPos var3, Explosion var4) {
      ILevel.fastRemoveBlock(var2, var3, true, true);
      this.wasExploded(var2, var3, var4);
   }

   @Shadow
   public abstract void wasExploded(Level var1, BlockPos var2, Explosion var3);
}
