package com.gtolib.api.beam;

import net.minecraft.world.phys.Vec3;

public class BeamNode {
   public final double x;
   public final double y;
   public final double z;
   private final BeamProperties propertiesSnapshot;

   public BeamNode(double x, double y, double z, BeamProperties propertiesSnapshot) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.propertiesSnapshot = propertiesSnapshot;
   }

   public BeamProperties propertiesSnapshot() {
      return this.propertiesSnapshot;
   }

   public Vec3 position() {
      return new Vec3(this.x, this.y, this.z);
   }
}
