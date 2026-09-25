package com.gtolib.api.beam;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public interface IBeamOperator {
   BeamNode operate(BeamPassContext var1, BeamNode var2, Vec3 var3);

   default void onRayBeamPassRemoved(BeamPassContext context) {
   }

   default long getMaxIntensity() {
      return Long.MAX_VALUE;
   }

   default void requestRayBeamUpdate(Level level, BlockPos pos) {
      BeamManager.requestRebuildAt(level, pos);
   }

   default void requestRayBeamPathUpdate(Level level, BlockPos pos) {
      BeamManager.requestPathRebuildAt(level, pos);
   }
}
