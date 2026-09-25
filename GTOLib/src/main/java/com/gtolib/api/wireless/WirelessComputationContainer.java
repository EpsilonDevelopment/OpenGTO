package com.gtolib.api.wireless;

import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import com.hepdd.gtmthings.utils.TeamUtil;
import java.util.Map;
import java.util.UUID;
import lombok.Generated;

public final class WirelessComputationContainer {
   private static final Map<UUID, WirelessComputationContainer> MAP = new O2OOpenCacheHashMap<>();
   private long capacity = 102400L;
   private long storage;
   private final UUID uuid;

   public static WirelessComputationContainer getOrCreateContainer(UUID uuid) {
      return MAP.computeIfAbsent(TeamUtil.getTeamUUID(uuid), WirelessComputationContainer::new);
   }

   private WirelessComputationContainer(UUID uuid) {
      this.uuid = uuid;
   }

   public long free() {
      return this.capacity - this.storage;
   }

   public long getCache() {
      return this.storage;
   }

   public void shrink(long change) {
      this.storage -= change;
   }

   @Generated
   public long getCapacity() {
      return this.capacity;
   }

   @Generated
   public long getStorage() {
      return this.storage;
   }

   @Generated
   public UUID getUuid() {
      return this.uuid;
   }

   @Generated
   public void setCapacity(long capacity) {
      this.capacity = capacity;
   }

   @Generated
   public void setStorage(long storage) {
      this.storage = storage;
   }
}
