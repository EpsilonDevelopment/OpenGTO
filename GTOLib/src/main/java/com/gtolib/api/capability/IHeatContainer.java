package com.gtolib.api.capability;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface IHeatContainer {
   static IHeatContainer getCapability(BlockEntity blockEntity) {
      return GTCapabilityHelper.getBlockEntityGTCapability(IHeatContainer.class, blockEntity, null);
   }

   long getMaxTemperature();

   double getTemperature();

   double getHeatCapacity();

   double getBaseTransferRate();

   double getCooldownRate();

   double getAmbientTemperature();

   long getCurrentHeat();

   void setCurrentHeat(long var1);

   long getMaxHeat();

   boolean heatIO(Direction var1);

   long addHeat(long var1, int var3, boolean var4);

   long removeHeat(long var1, int var3, boolean var4);

   long addHeatUnrestricted(long var1, boolean var3);

   long removeHeatUnrestricted(long var1, boolean var3);

   double transferHeatToAdjacent(int var1);

   long acceptHeatFromNetwork(Object var1, Direction var2, long var3, double var5, double var7, double var9, int var11);

   int getSignal();
}
