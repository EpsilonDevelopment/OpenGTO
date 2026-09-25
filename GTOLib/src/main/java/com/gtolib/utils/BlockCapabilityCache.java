package com.gtolib.utils;

import com.gregtechceu.gtceu.utils.cache.BlockEntityDirectionCache;
import com.gtolib.api.blockentity.IDirectionCacheBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;

public class BlockCapabilityCache<C> {
   private final BlockEntity blockEntity;
   private final Capability<C> capability;

   private BlockCapabilityCache(Capability<C> capability, BlockEntity blockEntity) {
      this.blockEntity = blockEntity;
      this.capability = capability;
   }

   public static <C> BlockCapabilityCache<C> create(Capability<C> capability, BlockEntity blockEntity) {
      return new BlockCapabilityCache<>(capability, blockEntity);
   }

   public C find(BlockPos pos, Direction side, Direction oppositeSide) {
      BlockEntityDirectionCache cache = IDirectionCacheBlockEntity.getBlockEntityDirectionCache(this.blockEntity);
      if (cache != null) {
         BlockEntity be = cache.getAdjacentBlockEntity(this.blockEntity.getLevel(), this.blockEntity.getBlockPos(), side);
         return be != null ? be.getCapability(this.capability, oppositeSide).orElse(null) : null;
      } else {
         BlockEntity be = this.blockEntity.getLevel().getBlockEntity(pos);
         return be != null ? be.getCapability(this.capability, oppositeSide).orElse(null) : null;
      }
   }
}
