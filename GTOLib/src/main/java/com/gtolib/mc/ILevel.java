package com.gtolib.mc;

import earth.terrarium.adastra.api.planets.Planet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.lighting.LightEngine;

public interface ILevel {
   default Reference2ReferenceOpenHashMap<Class, ReferenceSet> gtolib$getMachineNet() {
      return null;
   }

   default LongSet gtolib$getPreventUpdate() {
      return null;
   }

   default Planet gtolib$getPlanet() {
      return null;
   }

   default boolean gtolib$isVoid() {
      return false;
   }

   static void fastRemoveBlock(Level level, BlockPos pos, boolean breakBedrock, boolean updateLight) {
      int i = pos.getY();
      if (!level.isOutsideBuildHeight(i)) {
         LevelChunk levelchunk = level.getChunkAt(pos);
         LevelChunkSection levelchunksection = levelchunk.getSection(levelchunk.getSectionIndex(i));
         if (!levelchunksection.hasOnlyAir()) {
            if (!breakBedrock && levelchunk.getBlockState(pos).is(Blocks.BEDROCK)) {
               return;
            }

            BlockState state = Blocks.AIR.defaultBlockState();
            int j = pos.getX() & 15;
            int k = i & 15;
            int l = pos.getZ() & 15;
            BlockState old = levelchunksection.setBlockState(j, k, l, state);
            if (!old.isAir()) {
               levelchunk.getOrCreateHeightmapUnprimed(Types.MOTION_BLOCKING).update(j, i, l, state);
               levelchunk.getOrCreateHeightmapUnprimed(Types.MOTION_BLOCKING_NO_LEAVES).update(j, i, l, state);
               levelchunk.getOrCreateHeightmapUnprimed(Types.OCEAN_FLOOR).update(j, i, l, state);
               levelchunk.getOrCreateHeightmapUnprimed(Types.WORLD_SURFACE).update(j, i, l, state);
               if (updateLight && LightEngine.hasDifferentLightProperties(level, pos, old, state)) {
                  levelchunk.getSkyLightSources().update(level, j, i, l);
                  level.getChunkSource().getLightEngine().checkBlock(pos);
               }

               if (levelchunksection.hasOnlyAir()) {
                  level.getChunkSource().getLightEngine().updateSectionStatus(pos, true);
               }

               old.onRemove(level, pos, Blocks.AIR.defaultBlockState(), false);
               if (old.hasBlockEntity()) {
                  level.removeBlockEntity(pos);
               }

               FullChunkStatus fullStatus = levelchunk.getFullStatus();
               if (fullStatus != null && fullStatus.isOrAfter(FullChunkStatus.BLOCK_TICKING) && level instanceof ServerLevel serverLevel) {
                  serverLevel.getChunkSource().blockChanged(pos);
               }

               levelchunk.setUnsaved(true);
            }
         }
      }
   }
}
