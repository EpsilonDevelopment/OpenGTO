package com.gtolib.api.machine.impl.part;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gtolib.api.capability.IExtendWirelessEnergyContainerHolder;
import com.hepdd.gtmthings.api.misc.WirelessEnergyContainer;
import java.util.UUID;
import lombok.Generated;
import org.jetbrains.annotations.Nullable;

public final class WirelessEnergyInterfacePartMachine extends MultiblockPartMachine implements IExtendWirelessEnergyContainerHolder {
   private WirelessEnergyContainer wirelessEnergyContainerCache;

   public WirelessEnergyInterfacePartMachine(MetaMachineBlockEntity holder) {
      super(holder);
   }

   @Nullable
   public UUID getUUID() {
      return this.getOwnerUUID();
   }

   @Generated
   public WirelessEnergyContainer getWirelessEnergyContainerCache() {
      return this.wirelessEnergyContainerCache;
   }

   @Generated
   public void setWirelessEnergyContainerCache(WirelessEnergyContainer wirelessEnergyContainerCache) {
      this.wirelessEnergyContainerCache = wirelessEnergyContainerCache;
   }
}
