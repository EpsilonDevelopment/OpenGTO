package com.gtolib.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.IOpticalComputationProvider;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gtocore.common.machine.multiblock.part.WirelessNetworkComputationHatchMachine;
import com.gtolib.api.capability.IWirelessComputationContainerHolder;
import com.gtolib.api.wireless.WirelessComputationContainer;
import java.util.UUID;
import lombok.Generated;
import org.jetbrains.annotations.Nullable;

public final class WirelessComputationContainerTrait
   extends NotifiableRecipeHandlerTrait
   implements IOpticalComputationProvider,
   IWirelessComputationContainerHolder {
   private WirelessComputationContainer wirelessComputationContainerCache;
   private final boolean isTransmitter;
   @Nullable
   private TickableSubscription updateSubs;

   public WirelessComputationContainerTrait(MetaMachine machine, boolean isTransmitter) {
      super(machine);
      this.isTransmitter = isTransmitter;
   }

   @Override
   public void onMachineLoad() {
      super.onMachineLoad();
      if (this.isTransmitter) {
         this.updateSubs = this.getMachine().subscribeServerTick(this.updateSubs, this::updateTick);
      }
   }

   @Override
   public void onMachineUnLoad() {
      super.onMachineUnLoad();
      if (this.updateSubs != null) {
         this.updateSubs.unsubscribe();
         this.updateSubs = null;
      }
   }

   private void updateTick() {
      if (this.machine instanceof WirelessNetworkComputationHatchMachine hatchMachine) {
         if (!hatchMachine.isFormed()) {
            return;
         }

         if (hatchMachine.getController() instanceof IOpticalComputationProvider provider) {
            WirelessComputationContainer container = this.getWirelessComputationContainer();
            if (container != null && container.free() > 0L) {
               container.shrink(-provider.requestCWU(container.free(), false));
            }
         }
      } else if (this.updateSubs != null) {
         this.updateSubs.unsubscribe();
         this.updateSubs = null;
      }
   }

   @Override
   public long requestCWU(long cwu, boolean simulate) {
      WirelessComputationContainer container = this.getWirelessComputationContainer();
      if (container != null) {
         long cache = container.getCache();
         long change = Math.min(cwu, cache);
         if (!simulate) {
            container.shrink(change);
         }

         return change;
      } else {
         return 0L;
      }
   }

   @Override
   public IO getHandlerIO() {
      return this.isTransmitter ? IO.NONE : IO.IN;
   }

   @Nullable
   public UUID getUUID() {
      return this.machine.getOwnerUUID();
   }

   @Generated
   @Override
   public void setWirelessComputationContainerCache(WirelessComputationContainer wirelessComputationContainerCache) {
      this.wirelessComputationContainerCache = wirelessComputationContainerCache;
   }

   @Generated
   @Override
   public WirelessComputationContainer getWirelessComputationContainerCache() {
      return this.wirelessComputationContainerCache;
   }
}
