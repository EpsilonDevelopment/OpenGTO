package com.gtolib.api.machine.mana.trait;

import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gtolib.api.machine.mana.feature.IWirelessManaContainerHolder;
import com.gtolib.api.wireless.WirelessManaContainer;
import java.math.BigInteger;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.Generated;

public final class NotifiableWirelessManaContainer extends NotifiableManaContainer implements IWirelessManaContainerHolder {
   private WirelessManaContainer WirelessManaContainerCache;

   public NotifiableWirelessManaContainer(MetaMachine machine, IO io, long maxMana, long maxIORate) {
      super(machine, io, maxMana, maxIORate);
   }

   @Override
   protected void updateTick() {
      if (!(this.machine instanceof IControllable iControllable && !iControllable.isWorkingEnabled())) {
         WirelessManaContainer container = this.getWirelessManaContainer();
         if (container != null) {
            long stored = this.getCurrentMana();
            if (this.getHandlerIO() == IO.IN) {
               long canInput = Math.min(this.getMaxMana() - stored, container.getStorage().longValue());
               if (canInput > 0L) {
                  container.setStorage(container.getStorage().subtract(BigInteger.valueOf(canInput)));
                  this.setCurrentMana(stored + canInput);
                  this.notifyListeners();
               }
            } else if (stored > 0L) {
               container.setStorage(container.getStorage().add(BigInteger.valueOf(stored)));
               this.setCurrentMana(0L);
               this.notifyListeners();
            }
         }
      }
   }

   @Nullable
   public UUID getUUID() {
      return this.getMachine().getOwnerUUID();
   }

   @Override
   public boolean acceptDistributor() {
      return false;
   }

   @Generated
   @Override
   public void setWirelessManaContainerCache(WirelessManaContainer WirelessManaContainerCache) {
      this.WirelessManaContainerCache = WirelessManaContainerCache;
   }

   @Generated
   @Override
   public WirelessManaContainer getWirelessManaContainerCache() {
      return this.WirelessManaContainerCache;
   }
}
