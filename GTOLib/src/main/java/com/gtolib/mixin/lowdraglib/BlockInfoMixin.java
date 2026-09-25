package com.gtolib.mixin.lowdraglib;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gtolib.utils.MapValueCache;
import com.lowdragmc.lowdraglib.utils.BlockInfo;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockInfo.class)
public class BlockInfoMixin {
   @Unique
   private static final MapValueCache<BlockState, BlockInfo> BLOCK_INFO_CACHE = new MapValueCache<>(var0 -> new BlockInfo(var0, false, null, null));

   @Overwrite(remap = false)
   public static BlockInfo fromBlockState(BlockState var0) {
      return var0.getBlock() instanceof MetaMachineBlock ? new BlockInfo(var0, true, null, null) : BLOCK_INFO_CACHE.getCache(var0);
   }
}
