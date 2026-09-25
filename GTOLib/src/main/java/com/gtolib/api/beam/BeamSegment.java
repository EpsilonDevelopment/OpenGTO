package com.gtolib.api.beam;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.ICleanroomProvider;
import com.gregtechceu.gtceu.api.pattern.MultiblockState;
import com.gregtechceu.gtceu.api.pattern.MultiblockWorldData;
import com.gregtechceu.gtceu.core.ILevel;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class BeamSegment {
   public final BeamNode start;
   @Nullable
   public final BeamNode end;
   public final Vec3 endPosition;
   @Nullable
   final IBeamOperator operator;
   @Nullable
   final BeamPassContext passContext;
   public final LongOpenHashSet passedBlocks = new LongOpenHashSet();

   public BeamSegment(BeamNode start, Level world) {
      this(start, world, -1, 0);
   }

   BeamSegment(BeamNode start, Level world, int beamId, int passIndex) {
      this.start = start;
      BeamSegment.RayPathEnd result = this.computeEndNode(start, world, beamId, passIndex);
      this.end = result.node();
      this.endPosition = result.position();
      this.operator = result.operator();
      this.passContext = result.passContext();
   }

   BeamSegment(BeamNode start, @Nullable BeamNode end, Vec3 endPosition) {
      this.start = start;
      this.end = end;
      this.endPosition = endPosition;
      this.operator = null;
      this.passContext = null;
   }

   private BeamSegment.RayPathEnd computeEndNode(BeamNode start, Level world, int beamId, int passIndex) {
      BeamProperties properties = start.propertiesSnapshot();
      double posX = start.x;
      double posY = start.y;
      double posZ = start.z;
      double velocityX = properties.vx;
      double velocityY = properties.vy;
      double velocityZ = properties.vz;
      if (velocityX * velocityX + velocityY * velocityY + velocityZ * velocityZ <= 1.0E-6) {
         return new BeamSegment.RayPathEnd(null, new Vec3(posX, posY, posZ), null, null);
      }

      int posBlockX = initialCell(posX, velocityX);
      int posBlockY = initialCell(posY, velocityY);
      int posBlockZ = initialCell(posZ, velocityZ);
      MutableBlockPos posBlock = new MutableBlockPos(posBlockX, posBlockY, posBlockZ);
      double decay = 1.0;
      long initialIntensity = properties.intensity;

      for (int i = 0; i < 256; i++) {
         if (posBlockY < world.getMinBuildHeight() || posBlockY >= world.getMaxBuildHeight()) {
            return new BeamSegment.RayPathEnd(null, new Vec3(posX, posY, posZ), null, null);
         }

         double nearestNextX = posBlockX + (velocityX > 0.0 ? 1.0 : -0.5);
         double nearestNextY = posBlockY + (velocityY > 0.0 ? 1.0 : -0.5);
         double nearestNextZ = posBlockZ + (velocityZ > 0.0 ? 1.0 : -0.5);
         double nearestNextDistanceX = velocityX != 0.0 ? (nearestNextX - posX) / velocityX : Double.MAX_VALUE;
         double nearestNextDistanceY = velocityY != 0.0 ? (nearestNextY - posY) / velocityY : Double.MAX_VALUE;
         double nearestNextDistanceZ = velocityZ != 0.0 ? (nearestNextZ - posZ) / velocityZ : Double.MAX_VALUE;
         double minDistance = Math.min(Math.min(nearestNextDistanceX, nearestNextDistanceY), nearestNextDistanceZ) + 1.0E-6;
         posX += velocityX * minDistance;
         posY += velocityY * minDistance;
         posZ += velocityZ * minDistance;
         posBlockX = Mth.floor(posX);
         posBlockY = Mth.floor(posY);
         posBlockZ = Mth.floor(posZ);
         posBlock.set(posBlockX, posBlockY, posBlockZ);
         this.passedBlocks.add(posBlock.asLong());
         if (!world.isLoaded(posBlock)) {
            return new BeamSegment.RayPathEnd(null, new Vec3(posX, posY, posZ), null, null);
         }

         double decayFactor = isPosInCleanroom(world, posBlock) ? 1.0 : 0.95;
         decay *= decayFactor;
         if (decay * initialIntensity < 1.0) {
            return new BeamSegment.RayPathEnd(null, new Vec3(posX, posY, posZ), null, null);
         }

         BlockState blockState = world.getBlockState(posBlock);
         if (!blockState.isAir()) {
            if (MetaMachine.getMachine(world, posBlock) instanceof IBeamOperator operator) {
               BeamPassContext context = new BeamPassContext(world, beamId, passIndex, posBlock, decay);
               Vec3 exactPosition = new Vec3(posX, posY, posZ);
               BeamNode node = operator.operate(context, start, exactPosition);
               if (node != null) {
                  return new BeamSegment.RayPathEnd(node, exactPosition, operator, context);
               }
            } else {
               BeamNode node = blockState.isSolidRender(world, posBlock) ? new BeamNode(posX, posY, posZ, BeamProperties.NO_INTENSITY) : null;
               if (node != null) {
                  return new BeamSegment.RayPathEnd(node, new Vec3(posX, posY, posZ), null, null);
               }
            }
         }
      }

      return new BeamSegment.RayPathEnd(null, new Vec3(posX, posY, posZ), null, null);
   }

   private static int initialCell(double coordinate, double velocity) {
      int cell = Mth.floor(coordinate);
      if (velocity != 0.0 && !(Math.abs(coordinate - Math.rint(coordinate)) > 1.0E-9)) {
         return velocity > 0.0 ? cell - 1 : cell;
      } else {
         return cell;
      }
   }

   private static boolean isPosInCleanroom(Level level, BlockPos blockPos) {
      if (level instanceof ServerLevel serverLevel) {
         MultiblockWorldData cache = ((ILevel)serverLevel).gtceu$getMultiblockWorldSavedData();
         if (cache != null) {
            MultiblockState[] states = cache.getControllersInChunk(ChunkPos.asLong(blockPos));
            if (states != null) {
               long p = blockPos.asLong();

               for (MultiblockState structure : states) {
                  if (structure.controller instanceof ICleanroomProvider cleanroomProvider && cleanroomProvider.isClean() && structure.cache.contains(p)) {
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   private record RayPathEnd(@Nullable BeamNode node, Vec3 position, @Nullable IBeamOperator operator, @Nullable BeamPassContext passContext) {
   }
}
