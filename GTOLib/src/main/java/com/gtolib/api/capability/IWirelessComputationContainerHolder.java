package com.gtolib.api.capability;

import com.gtolib.api.wireless.WirelessComputationContainer;
import com.hepdd.gtmthings.api.capability.IBindable;
import javax.annotation.Nullable;

public interface IWirelessComputationContainerHolder extends IBindable {
   void setWirelessComputationContainerCache(WirelessComputationContainer var1);

   WirelessComputationContainer getWirelessComputationContainerCache();

   @Nullable
   default WirelessComputationContainer getWirelessComputationContainer() {
      if (this.getUUID() != null && this.getWirelessComputationContainerCache() == null) {
         WirelessComputationContainer container = WirelessComputationContainer.getOrCreateContainer(this.getUUID());
         this.setWirelessComputationContainerCache(container);
      }

      return this.getWirelessComputationContainerCache();
   }
}
