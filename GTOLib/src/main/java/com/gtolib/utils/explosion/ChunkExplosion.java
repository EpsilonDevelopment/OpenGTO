package com.gtolib.utils.explosion;

import com.gregtechceu.gtceu.utils.TaskHandler;
import com.gtolib.mc.ILevel;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class ChunkExplosion {
   private final BlockPos center;
   private final ServerLevel level;
   private final boolean breakBedrock;
   private final int sideLength;
   private int time = 0;
   private ISubscription subscription;

   private ChunkExplosion(BlockPos center, ServerLevel level, int sideLength, boolean breakBedrock, boolean spawnParticles, boolean affectEntities) {
      this.center = center;
      this.level = level;
      this.breakBedrock = breakBedrock;
      this.sideLength = sideLength;
      int x = center.getX();
      int y = center.getY();
      int z = center.getZ();
      if (this.level.isClientSide) {
         float soundPitch = (1.0F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F) * 0.7F;
         this.level.playLocalSound(x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 4.0F, soundPitch, false);
      }

      if (spawnParticles) {
         this.level.addParticle(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 1.0, 0.0, 0.0);
      }

      this.level.gameEvent(null, GameEvent.EXPLODE, new Vec3(x, y, z));
      if (affectEntities) {
         int chunkX = x >> 4;
         int chunkZ = z >> 4;
         int chunkRadius = (sideLength - 1) / 2;
         int minChunkX = chunkX - chunkRadius;
         int maxChunkX = chunkX + chunkRadius;
         int minChunkZ = chunkZ - chunkRadius;
         int maxChunkZ = chunkZ + chunkRadius;
         int minX = minChunkX << 4;
         int maxX = (maxChunkX << 4) + 15;
         int minZ = minChunkZ << 4;
         int maxZ = (maxChunkZ << 4) + 15;
         int minY = level.getMinBuildHeight();
         int maxY = level.getMaxBuildHeight();

         for (Entity entity : this.level.getEntities(null, new AABB(minX, minY, minZ, maxX, maxY, maxZ))) {
            if (!entity.ignoreExplosion()) {
               entity.hurt(entity.damageSources().genericKill(), 40.0F);
            }
         }
      }

      this.subscription = TaskHandler.enqueueTick(level, this::breakBlocksInChunk, 0, 0);
   }

   private void breakBlocksInChunk() {
      int height = this.level.getHeight();
      int speed = height < 600 ? 8 : (height < 1200 ? 4 : (height < 2400 ? 2 : 1));
      int stepsPerChunk = 16 / speed;
      int totalTime = this.sideLength * this.sideLength * stepsPerChunk;
      if (this.time >= totalTime) {
         this.subscription.unsubscribe();
      } else {
         int chunkIndex = this.time / stepsPerChunk;
         int stepInChunk = this.time % stepsPerChunk;
         int[] spiralPos = this.getSpiralOffset(chunkIndex);
         int dxChunk = spiralPos[0];
         int dzChunk = spiralPos[1];
         int centerChunkX = this.center.getX() >> 4;
         int centerChunkZ = this.center.getZ() >> 4;
         int chunkX = centerChunkX + dxChunk;
         int chunkZ = centerChunkZ + dzChunk;
         int minX = chunkX << 4;
         int minZ = chunkZ << 4;
         int maxY = this.level.getMaxBuildHeight();
         int minY = this.level.getMinBuildHeight();
         int xStart = dxChunk >= 0 ? minX : minX + 15;
         int xEnd = dxChunk >= 0 ? minX + 16 : minX - 1;
         int xStep = dxChunk >= 0 ? 1 : -1;
         int zBase = minZ + stepInChunk * speed;
         int zStart = dzChunk >= 0 ? zBase : zBase + speed - 1;
         int zEnd = dzChunk >= 0 ? zBase + speed : zBase - 1;
         int zStep = dzChunk >= 0 ? 1 : -1;
         int zBound = chunkZ + 1 << 4;
         if (dzChunk >= 0 && zEnd > zBound) {
            zEnd = zBound;
         }

         if (dzChunk < 0 && zEnd < minZ - 1) {
            zEnd = minZ - 1;
         }

         for (int x = xStart; x != xEnd; x += xStep) {
            for (int z = zStart; z != zEnd; z += zStep) {
               for (int y = minY; y <= maxY; y++) {
                  ILevel.fastRemoveBlock(this.level, new BlockPos(x, y, z), this.breakBedrock, false);
               }
            }
         }

         this.time++;
      }
   }

   private int[] getSpiralOffset(int index) {
      if (index == 0) {
         return new int[]{0, 0};
      }

      int layer = (int)Math.ceil((Math.sqrt(index + 1) - 1.0) / 2.0);
      int legLen = layer * 2;
      int minIndexInLayer = (2 * layer - 1) * (2 * layer - 1);
      int offset = index - minIndexInLayer;
      int x;
      int z;
      if (offset < legLen) {
         x = layer;
         z = -layer + offset;
      } else if (offset < 2 * legLen) {
         x = layer - (offset - legLen);
         z = layer;
      } else if (offset < 3 * legLen) {
         x = -layer;
         z = layer - (offset - 2 * legLen);
      } else {
         x = -layer + (offset - 3 * legLen);
         z = -layer;
      }

      return new int[]{x, z};
   }

   public static void explosion(BlockPos center, Level level, int sideLength, boolean breakBedrock, boolean spawnParticles, boolean affectEntities) {
      if (level instanceof ServerLevel serverLevel) {
         int var7 = sideLength / 8;
         if (var7 < 1) {
            var7 = 1;
         }

         if (var7 % 2 == 0) {
            var7--;
         }

         new ChunkExplosion(center, serverLevel, var7, breakBedrock, spawnParticles, affectEntities);
      }
   }
}
