package com.gtolib.mixin.mc.world;

import com.gregtechceu.gtceu.api.blockentity.GTBlockEntity;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.pattern.MultiblockState;
import com.gregtechceu.gtceu.api.pattern.MultiblockWorldData;
import com.gregtechceu.gtceu.core.ILevel;
import java.util.ArrayList;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.LevelChunk.RebindableTickingBlockEntityWrapper;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = LevelChunk.class, priority = 0)
public abstract class LevelChunkMixin extends ChunkAccess {
   @Mutable
   @Shadow
   @Final
   private Map<BlockPos, RebindableTickingBlockEntityWrapper> tickersInLevel;
   @Shadow
   @Final
   private static TickingBlockEntity NULL_TICKER;

   public LevelChunkMixin(
      ChunkPos var1, UpgradeData var2, LevelHeightAccessor var3, Registry<Biome> var4, long var5, LevelChunkSection[] var7, BlendingData var8
   ) {
      super(var1, var2, var3, var4, var5, var7, var8);
   }

   @Overwrite
   public void clearAllBlockEntities() {
      ArrayList<BlockEntity> var1 = new ArrayList<>(this.blockEntities.values());
      var1.forEach(BlockEntity::onChunkUnloaded);
      var1.forEach(BlockEntity::setRemoved);
      this.blockEntities.clear();
      ArrayList<RebindableTickingBlockEntityWrapper> var2 = new ArrayList<>(this.tickersInLevel.values());
      var2.forEach(var0 -> var0.rebind(NULL_TICKER));
      this.tickersInLevel.clear();
   }

   @Unique
   private static boolean gtolib$isPosInCache(Level var0, ChunkPos var1, BlockEntity var2) {
      if (var2 instanceof GTBlockEntity) {
         return false;
      }

      if (var0 instanceof ServerLevel var3) {
         MultiblockWorldData var4 = ((ILevel)var3).gtceu$getMultiblockWorldSavedData();
         if (var4 != null) {
            MultiblockState[] var5 = var4.getControllersInChunk(var1.toLong());
            if (var5 != null) {
               long var6 = var2.getBlockPos().asLong();

               for (MultiblockState var11 : var5) {
                  if (var11.blockEntityCache.contains(var6)) {
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   @Redirect(method = "getBlockEntityNbtForSaving", at = @At(value = "INVOKE", target = "Ljava/lang/Class;getName()Ljava/lang/String;", remap = false))
   public String getBlockEntityClass(Class<?> var1, BlockEntity var2) {
      return var2 instanceof MetaMachineBlockEntity var3 ? var3.metaMachine.getDefinition().toString() : var1.getName();
   }

   @Redirect(
      method = "updateBlockEntityTicker",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/level/block/state/BlockState;getTicker(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/entity/BlockEntityType;)Lnet/minecraft/world/level/block/entity/BlockEntityTicker;"
      )
   )
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(BlockState var1, Level var2, BlockEntityType<T> var3, T var4) {
      return gtolib$isPosInCache(var2, this.getPos(), var4) ? null : var1.getTicker(var2, var3);
   }
}
