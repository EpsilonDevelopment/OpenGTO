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

public final class AreaExplosion {
   private final ServerLevel level;
   private final boolean breakBedrock;
   private final int minX;
   private final int minY;
   private final int minZ;
   private final int maxX;
   private final int maxY;
   private final int maxZ;
   private int time = 0;
   private ISubscription subscription;

   private AreaExplosion(BlockPos center, BlockPos pos1, BlockPos pos2, ServerLevel level, boolean breakBedrock, boolean spawnParticles, boolean affectEntities) {
      this.level = level;
      this.breakBedrock = breakBedrock;
      this.minX = Math.min(pos1.getX(), pos2.getX());
      this.minY = Math.min(pos1.getY(), pos2.getY());
      this.minZ = Math.min(pos1.getZ(), pos2.getZ());
      this.maxX = Math.max(pos1.getX(), pos2.getX());
      this.maxY = Math.max(pos1.getY(), pos2.getY());
      this.maxZ = Math.max(pos1.getZ(), pos2.getZ());
      int X = center.getX();
      int Y = center.getY();
      int Z = center.getZ();
      if (this.level.isClientSide) {
         float soundPitch = (1.0F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F) * 0.7F;
         this.level.playLocalSound(X, Y, Z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 4.0F, soundPitch, false);
      }

      if (spawnParticles) {
         this.level.addParticle(ParticleTypes.EXPLOSION_EMITTER, X, Y, Z, 1.0, 0.0, 0.0);
      }

      this.level.gameEvent(null, GameEvent.EXPLODE, new Vec3(X, Y, Z));
      if (affectEntities) {
         AABB affectBox = new AABB(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);

         for (Entity entity : this.level.getEntities(null, affectBox)) {
            if (!entity.ignoreExplosion()) {
               entity.hurt(entity.damageSources().genericKill(), 40.0F);
            }
         }
      }

      this.subscription = TaskHandler.enqueueTick(level, this::breakBlocksInArea, 0, 0);
   }

   private void breakBlocksInArea() {
      int height = this.maxY - this.minY;
      int speedZ = height < 80 ? 64 : (height < 160 ? 32 : (height < 300 ? 16 : (height < 600 ? 8 : (height < 1200 ? 4 : (height < 2400 ? 2 : 1)))));
      int speedX = height < 20 ? 128 : (height < 40 ? 64 : (height < 80 ? 32 : 16));
      int timeX = (int)Math.ceil((double)(this.maxX - this.minX) / speedX);
      int timeZ = (int)Math.ceil((double)(this.maxZ - this.minZ) / speedZ);
      int totalTime = timeX * timeZ;
      if (this.time >= totalTime) {
         this.subscription.unsubscribe();
      } else {
         int indexX = this.time / timeZ;
         int indexZ = this.time % timeZ;
         int chunkRow = this.time / timeX;
         int startX = this.minX + indexX * speedX;
         int endX = Math.min(startX + speedX, this.maxX);
         int startZ = this.minZ + indexZ * speedZ;
         int endZ = Math.min(startZ + speedZ, this.maxZ);

         for (int x = startX - (chunkRow == 0 ? 0 : 1); x <= endX; x++) {
            for (int z = startZ; z <= endZ; z++) {
               for (int y = this.minY; y <= this.maxY; y++) {
                  ILevel.fastRemoveBlock(this.level, new BlockPos(x, y, z), this.breakBedrock, false);
               }
            }
         }

         this.time++;
      }
   }

   public static void explosion(
      BlockPos center, BlockPos pos1, BlockPos pos2, Level level, boolean breakBedrock, boolean spawnParticles, boolean affectEntities
   ) {
      if (level instanceof ServerLevel serverLevel) {
         new AreaExplosion(center, pos1, pos2, serverLevel, breakBedrock, spawnParticles, affectEntities);
      }
   }
}
