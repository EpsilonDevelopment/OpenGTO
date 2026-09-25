package com.gtolib.api.wireless;

import com.gtolib.data.WirelessManaSavaedData;
import com.gtolib.utils.MathUtil;
import com.hepdd.gtmthings.utils.TeamUtil;
import java.math.BigInteger;
import java.util.UUID;
import lombok.Generated;

public final class WirelessManaContainer implements IWirelessContainer {
   private BigInteger storage;
   private final UUID uuid;

   public static WirelessManaContainer getOrCreateContainer(UUID uuid) {
      return WirelessManaSavaedData.INSTANCE.containerMap.computeIfAbsent(TeamUtil.getTeamUUID(uuid), WirelessManaContainer::new);
   }

   private WirelessManaContainer(UUID uuid) {
      this.uuid = uuid;
      this.storage = BigInteger.ZERO;
   }

   public WirelessManaContainer(UUID uuid, BigInteger storage) {
      this(uuid);
      this.storage = storage;
   }

   public void setStorage(BigInteger storage) {
      this.storage = storage;
      WirelessManaSavaedData.INSTANCE.setDirty();
   }

   @Override
   public String getUnit() {
      return "MANA";
   }

   @Override
   public BigInteger unrestrictedRemoveStorage(BigInteger storage) {
      BigInteger change = MathUtil.min(this.getStorage(), storage);
      if (change.compareTo(BigInteger.ZERO) <= 0) {
         return BigInteger.ZERO;
      }

      this.setStorage(this.getStorage().subtract(change));
      return change;
   }

   @Generated
   @Override
   public BigInteger getStorage() {
      return this.storage;
   }

   @Generated
   public UUID getUuid() {
      return this.uuid;
   }
}
