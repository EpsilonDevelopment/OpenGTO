package com.gtolib.api.blockentity;

import com.gregtechceu.gtceu.utils.cache.BlockEntityDirectionCache;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface IDirectionCacheBlockEntity {
   BlockEntityDirectionCache gtolib$getDirectionCache();

   static BlockEntityDirectionCache getBlockEntityDirectionCache(BlockEntity blockEntity) {
      return blockEntity instanceof IDirectionCacheBlockEntity aeBaseBlockEntity ? aeBaseBlockEntity.gtolib$getDirectionCache() : null;
   }
}
