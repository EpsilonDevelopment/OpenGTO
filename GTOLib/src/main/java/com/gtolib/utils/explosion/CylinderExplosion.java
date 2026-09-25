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

public final class CylinderExplosion {
   private final BlockPos center;
   private final ServerLevel level;
   private final boolean breakBedrock;
   private final int radius;
   private int time = 0;
   private ISubscription subscription;

   private CylinderExplosion(BlockPos center, ServerLevel level, int radius, boolean breakBedrock, boolean spawnParticles, boolean affectEntities) {
      this.center = center;
      this.level = level;
      this.breakBedrock = breakBedrock;
      this.radius = radius;
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
         int minX = x - radius;
         int maxX = x + radius;
         int minY = level.getMinBuildHeight();
         int maxY = level.getMaxBuildHeight();
         int minZ = z - radius;
         int maxZ = z + radius;

         for (Entity entity : this.level.getEntities(null, new AABB(minX, minY, minZ, maxX, maxY, maxZ))) {
            if (!entity.ignoreExplosion()) {
               double dx = entity.getX() - x;
               double dz = entity.getZ() - z;
               double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
               if (horizontalDistance <= radius) {
                  double knockbackFactor = 1.0 - horizontalDistance / radius;
                  int damage = (int)((knockbackFactor * knockbackFactor + knockbackFactor) / 2.0 * 7.0 * radius + 1.0);
                  entity.hurt(entity.damageSources().genericKill(), damage);
                  Vec3 horizontalDirection = new Vec3(dx, 0.0, dz).normalize().scale(knockbackFactor);
                  Vec3 verticalDirection = new Vec3(0.0, knockbackFactor * 0.5, 0.0);
                  entity.setDeltaMovement(entity.getDeltaMovement().add(horizontalDirection).add(verticalDirection));
               }
            }
         }
      }

      this.subscription = TaskHandler.enqueueTick(level, this::breakBlocksInCylinder, 0, 0);
   }

   private void breakBlocksInCylinder() {
      int height = this.level.getHeight();
      int speed = height < 600 ? 8 : (height < 1200 ? 4 : (height < 2400 ? 2 : 1));
      int stepsPerChunk = 16 / speed;
      int radiusSquared = this.radius * this.radius;
      int centerX = this.center.getX();
      int centerZ = this.center.getZ();
      int sideLength = this.radius / 8 + 2;
      if (sideLength % 2 == 0) {
         sideLength++;
      }

      int totalTime = sideLength * sideLength * stepsPerChunk;
      if (this.time >= totalTime) {
         this.subscription.unsubscribe();
      } else {
         int chunkIndex = this.time / stepsPerChunk;
         int stepInChunk = this.time % stepsPerChunk;
         int[] spiralPos = this.getSpiralOffset(chunkIndex);
         int dxChunk = spiralPos[0];
         int dzChunk = spiralPos[1];
         int centerChunkX = centerX >> 4;
         int centerChunkZ = centerZ >> 4;
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
               int dx = x - centerX;
               int dz = z - centerZ;
               if (dx * dx + dz * dz <= radiusSquared) {
                  for (int y = minY; y <= maxY; y++) {
                     ILevel.fastRemoveBlock(this.level, new BlockPos(x, y, z), this.breakBedrock, false);
                  }
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

   public static void explosion(BlockPos center, Level level, int radius, boolean breakBedrock, boolean spawnParticles, boolean affectEntities) {
      if (level instanceof ServerLevel serverLevel) {
         new CylinderExplosion(center, serverLevel, radius, breakBedrock, spawnParticles, affectEntities);
      }
   }
}
