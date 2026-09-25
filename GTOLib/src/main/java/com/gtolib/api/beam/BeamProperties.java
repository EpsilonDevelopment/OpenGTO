package com.gtolib.api.beam;

import net.minecraft.world.phys.Vec3;

public class BeamProperties {
   public static final BeamProperties NO_INTENSITY = new BeamProperties();
   public double vx;
   public double vy;
   public double vz;
   public long intensity;
   public int waveLength;
   public float polarization;

   public BeamProperties copy() {
      BeamProperties p = new BeamProperties();
      p.vx = this.vx;
      p.vy = this.vy;
      p.vz = this.vz;
      p.intensity = this.intensity;
      p.waveLength = this.waveLength;
      p.polarization = this.polarization;
      return p;
   }

   public Vec3 getVector() {
      return new Vec3(this.vx, this.vy, this.vz);
   }
}
