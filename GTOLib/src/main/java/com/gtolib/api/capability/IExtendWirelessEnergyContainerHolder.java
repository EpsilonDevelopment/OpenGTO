package com.gtolib.api.capability;

import com.gtolib.api.wireless.ExtendWirelessEnergyContainer;
import com.hepdd.gtmthings.api.machine.IWirelessEnergyContainerHolder;
import com.hepdd.gtmthings.api.misc.WirelessEnergyContainer;
import javax.annotation.Nullable;

public interface IExtendWirelessEnergyContainerHolder extends IWirelessEnergyContainerHolder {
   @Nullable
   default ExtendWirelessEnergyContainer getWirelessEnergyContainer() {
      WirelessEnergyContainer c = this.getWirelessEnergyContainerCache();
      if (c == null && this.getUUID() != null) {
         c = WirelessEnergyContainer.getOrCreateContainer(this.getUUID());
         this.setWirelessEnergyContainerCache(c);
      }

      return (ExtendWirelessEnergyContainer)c;
   }
}
