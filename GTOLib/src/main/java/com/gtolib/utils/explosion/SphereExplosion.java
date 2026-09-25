package com.gtolib.utils.explosion;

import com.gregtechceu.gtceu.utils.TaskHandler;
import com.gtolib.mc.ILevel;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class SphereExplosion {
   private final BlockPos center;
   private final ServerLevel level;
   private final int radius;
   private final boolean breakBedrock;
   private ISubscription subscription;
   private int currentLayer = -1;
   private int currentY;
   private int currentX;
   private int currentZ;
   private boolean layerInitialized = false;
   private static final int MAX_BLOCKS_PER_TICK = 50000;

   private SphereExplosion(BlockPos center, ServerLevel level, int radius, boolean breakBedrock, boolean spawnParticles, boolean affectEntities) {
      this.center = center;
      this.level = level;
      this.radius = radius;
      this.breakBedrock = breakBedrock;
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
         float f2 = this.radius * 2.0F;
         List<Entity> entities = this.level
            .getEntities(
               null,
               new AABB(
                  Mth.floor(x - f2 - 1.0),
                  Mth.floor(y - f2 - 1.0),
                  Mth.floor(z - f2 - 1.0),
                  Mth.floor(x + f2 + 1.0),
                  Mth.floor(y + f2 + 1.0),
                  Mth.floor(z + f2 + 1.0)
               )
            );
         Vec3 explosionPosition = new Vec3(x, y, z);
         double f2Squared = f2 * f2;
         double f2Inverse = 1.0 / f2;

         for (Entity entity : entities) {
            double distanceToSqr = entity.distanceToSqr(explosionPosition);
            if (!entity.ignoreExplosion() && !(distanceToSqr > f2Squared)) {
               double knockbackFactor = 1.0 - distanceToSqr * f2Inverse;
               int damage = (int)((knockbackFactor * knockbackFactor + knockbackFactor) / 2.0 * 7.0 * f2 + 1.0);
               entity.hurt(entity.damageSources().genericKill(), damage);
               Vec3 direction = new Vec3(entity.getX() - x, entity.getY() - y, entity.getZ() - z).normalize().scale(knockbackFactor);
               entity.setDeltaMovement(entity.getDeltaMovement().add(direction));
            }
         }
      }

      this.subscription = TaskHandler.enqueueTick(level, this::breakBlocksInExplosionArea, 0, 0);
   }

   private void breakBlocksInExplosionArea() {
      if (this.currentLayer < 0) {
         this.currentLayer = Math.min(this.radius, 19);
      }

      if (this.currentLayer > this.radius) {
         this.subscription.unsubscribe();
      } else {
         int layerEnd = this.currentLayer + 1;
         int endSq = layerEnd * layerEnd;
         int startSq = this.currentLayer > 20 ? (this.currentLayer - 1) * (this.currentLayer - 1) : 0;
         int maxY = this.level.getMaxBuildHeight();
         int minY = this.level.getMinBuildHeight();
         int centerX = this.center.getX();
         int centerY = this.center.getY();
         int centerZ = this.center.getZ();
         int blocksProcessed = 0;
         if (this.currentLayer <= 50) {
            for (int y = layerEnd; y >= -layerEnd; y--) {
               int worldY = centerY + y;
               if (worldY >= minY && worldY <= maxY) {
                  int ySq = y * y;
                  if (ySq <= endSq) {
                     int remY = endSq - ySq;
                     int xMax = (int)Math.sqrt(remY);

                     for (int x = -xMax; x <= xMax; x++) {
                        int xSq = x * x;
                        int xySq = xSq + ySq;
                        if (xySq <= endSq) {
                           int remX = remY - xSq;
                           int zMax = (int)Math.sqrt(remX);

                           for (int z = -zMax; z <= zMax; z++) {
                              int distSq = xySq + z * z;
                              if (distSq >= startSq && distSq <= endSq) {
                                 ILevel.fastRemoveBlock(this.level, new BlockPos(centerX + x, centerY + y, centerZ + z), this.breakBedrock, false);
                              }
                           }
                        }
                     }
                  }
               }
            }

            this.currentLayer++;
         } else {
            if (!this.layerInitialized) {
               this.currentY = layerEnd;
               this.currentX = Integer.MIN_VALUE;
               this.currentZ = 0;
               this.layerInitialized = true;
            }

            for (int y = this.currentY; y >= -layerEnd; y--) {
               int worldY = centerY + y;
               if (worldY >= minY && worldY <= maxY) {
                  int ySq = y * y;
                  if (ySq <= endSq) {
                     int remY = endSq - ySq;
                     int xMax = (int)Math.sqrt(remY);
                     int startX = this.currentX == Integer.MIN_VALUE ? -xMax : this.currentX;

                     for (int x = startX; x <= xMax; x++) {
                        int xSq = x * x;
                        int xySq = xSq + ySq;
                        if (xySq <= endSq) {
                           int remX = remY - xSq;
                           int zMax = (int)Math.sqrt(remX);
                           int startZ = this.currentX == x ? this.currentZ : -zMax;

                           for (int z = startZ; z <= zMax; z++) {
                              int distSq = xySq + z * z;
                              if (distSq >= startSq && distSq <= endSq) {
                                 ILevel.fastRemoveBlock(this.level, new BlockPos(centerX + x, centerY + y, centerZ + z), this.breakBedrock, false);
                                 if (++blocksProcessed >= 50000) {
                                    this.currentY = y;
                                    this.currentX = x;
                                    this.currentZ = z + 1;
                                    return;
                                 }
                              }
                           }

                           this.currentZ = -zMax;
                        }
                     }

                     this.currentX = Integer.MIN_VALUE;
                  }
               }
            }

            this.currentLayer++;
            this.layerInitialized = false;
         }
      }
   }

   public static void explosion(BlockPos center, Level level, int radius, boolean breakBedrock, boolean spawnParticles) {
      explosion(center, level, radius, breakBedrock, spawnParticles, true);
   }

   public static void explosion(BlockPos center, Level level, int radius, boolean breakBedrock, boolean spawnParticles, boolean affectEntities) {
      if (level instanceof ServerLevel serverLevel) {
         new SphereExplosion(center, serverLevel, radius, breakBedrock, spawnParticles, affectEntities);
      }
   }
}
