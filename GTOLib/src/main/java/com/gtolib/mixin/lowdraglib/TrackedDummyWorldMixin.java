package com.gtolib.mixin.lowdraglib;

import com.lowdragmc.lowdraglib.utils.BlockInfo;
import com.lowdragmc.lowdraglib.utils.TrackedDummyWorld;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TrackedDummyWorld.class)
public class TrackedDummyWorldMixin {
   @Mutable
   @Shadow(remap = false)
   @Final
   public Map<BlockPos, BlockInfo> renderedBlocks;
   @Mutable
   @Shadow(remap = false)
   @Final
   public Map<BlockPos, BlockEntity> blockEntities;
   @Mutable
   @Shadow(remap = false)
   @Final
   public Map<Integer, Entity> entities;

   @Inject(method = "<init>()V", at = @At("TAIL"), remap = false)
   private void init(CallbackInfo var1) {
      this.renderedBlocks = new ConcurrentHashMap<>();
      this.blockEntities = new ConcurrentHashMap<>();
      this.entities = new ConcurrentHashMap<>();
   }
}
