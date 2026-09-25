package com.gtolib.data;

import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import com.gtolib.api.wireless.WirelessManaContainer;
import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

public final class WirelessManaSavaedData extends SavedData {
   public static WirelessManaSavaedData INSTANCE = new WirelessManaSavaedData();
   public final Map<UUID, WirelessManaContainer> containerMap = new O2OOpenCacheHashMap<>();

   public WirelessManaSavaedData(CompoundTag tag) {
      ListTag all = tag.getList("all", 10);

      for (int i = 0; i < all.size(); i++) {
         WirelessManaContainer container = readTag(all.getCompound(i));
         this.containerMap.put(container.getUuid(), container);
      }
   }

   @NotNull
   @Override
   public CompoundTag save(@NotNull CompoundTag compoundTag) {
      ListTag all = new ListTag();

      for (WirelessManaContainer container : this.containerMap.values()) {
         CompoundTag tag = toTag(container);
         if (!tag.isEmpty()) {
            all.add(tag);
         }
      }

      compoundTag.put("all", all);
      return compoundTag;
   }

   private static WirelessManaContainer readTag(CompoundTag engTag) {
      String mana = engTag.getString("mana");
      return new WirelessManaContainer(engTag.getUUID("uuid"), new BigInteger(mana.isEmpty() ? "0" : mana));
   }

   private static CompoundTag toTag(WirelessManaContainer container) {
      CompoundTag engTag = new CompoundTag();
      BigInteger storage = container.getStorage();
      if (!Objects.equals(storage, BigInteger.ZERO)) {
         engTag.putString("mana", storage.toString());
      }

      if (!engTag.isEmpty()) {
         engTag.putUUID("uuid", container.getUuid());
      }

      return engTag;
   }

   public WirelessManaSavaedData() {
   }
}
