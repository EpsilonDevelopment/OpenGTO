package com.gtolib.data;

import com.gtolib.api.wireless.ExtendWirelessEnergyContainer;
import com.gtolib.utils.GTOUtils;
import com.hepdd.gtmthings.api.misc.WirelessEnergyContainer;
import com.hepdd.gtmthings.data.WirelessEnergySavaedData;
import java.math.BigInteger;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;

public final class ExtendWirelessEnergySavaedData extends WirelessEnergySavaedData {
   public ExtendWirelessEnergySavaedData() {
   }

   public ExtendWirelessEnergySavaedData(CompoundTag tag) {
      super(tag);
   }

   protected WirelessEnergyContainer readTag(CompoundTag engTag) {
      UUID uuid = engTag.getUUID("u");
      String en = engTag.getString("s");
      String ca = engTag.getString("c");
      BigInteger energy = new BigInteger(en.isEmpty() ? "0" : en);
      BigInteger capacity = new BigInteger(ca.isEmpty() ? "0" : ca);
      long rate = engTag.getLong("r");
      int loss = engTag.getInt("l");
      GlobalPos bindPos = GTOUtils.readGlobalPos(engTag.getString("d"), engTag.getLong("p"));
      return new ExtendWirelessEnergyContainer(uuid, energy, rate, bindPos, capacity, loss);
   }

   protected CompoundTag toTag(WirelessEnergyContainer c) {
      CompoundTag engTag = new CompoundTag();
      if (c instanceof ExtendWirelessEnergyContainer container) {
         BigInteger storage = container.getStorage();
         if (!Objects.equals(storage, BigInteger.ZERO)) {
            engTag.putString("s", storage.toString());
         }

         BigInteger capacity = container.getCapacity();
         if (!Objects.equals(capacity, BigInteger.ZERO)) {
            engTag.putString("c", capacity.toString());
         }

         long rate = container.getRate();
         if (rate != 0L) {
            engTag.putLong("r", rate);
         }

         int loss = container.getLoss();
         if (loss != 0) {
            engTag.putInt("l", loss);
         }

         GlobalPos bindPos = container.getBindPos();
         if (bindPos != null) {
            engTag.putString("d", bindPos.dimension().location().toString());
            engTag.putLong("p", bindPos.pos().asLong());
         }

         if (!engTag.isEmpty()) {
            engTag.putUUID("u", container.getUuid());
         }
      }

      return engTag;
   }
}
