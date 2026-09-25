package com.gtolib.api.beam;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public final class BeamPassContext {
   private final Level level;
   private final int beamId;
   private final int passIndex;
   private final BlockPos operatorPos;
   private final double decay;
   private final BeamPassKey key;

   public BeamPassContext(Level level, int beamId, int passIndex, BlockPos operatorPos, double decay) {
      this.level = level;
      this.beamId = beamId;
      this.passIndex = passIndex;
      this.operatorPos = operatorPos.immutable();
      this.decay = decay;
      this.key = new BeamPassKey(beamId, passIndex);
   }

   public Level level() {
      return this.level;
   }

   public int beamId() {
      return this.beamId;
   }

   public int passIndex() {
      return this.passIndex;
   }

   public BlockPos operatorPos() {
      return this.operatorPos;
   }

   public double decay() {
      return this.decay;
   }

   public BeamPassKey key() {
      return this.key;
   }

   public void requestRebuild() {
      BeamManager manager = BeamManager.getIfPresent(this.level);
      if (manager != null && !this.level.isClientSide()) {
         manager.requestRebuild(this.beamId);
      }
   }

   public void requestPathRebuild() {
      BeamManager manager = BeamManager.getIfPresent(this.level);
      if (manager != null && !this.level.isClientSide()) {
         manager.requestPathRebuild(this.beamId);
      }
   }
}
