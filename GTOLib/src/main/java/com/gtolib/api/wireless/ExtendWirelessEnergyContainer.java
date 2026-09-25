package com.gtolib.api.wireless;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gto.fastcollection.fastutil.O2IOpenCacheHashMap;
import com.gtolib.utils.MathUtil;
import com.hepdd.gtmthings.api.misc.WirelessEnergyContainer;
import com.hepdd.gtmthings.data.WirelessEnergySavaedData;
import com.hepdd.gtmthings.utils.BigIntegerUtils;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.math.BigInteger;
import java.util.UUID;
import lombok.Generated;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ExtendWirelessEnergyContainer extends WirelessEnergyContainer implements IWirelessContainer {
   private BigInteger capacity;
   private int loss;
   private final Object2IntOpenHashMap<ResourceLocation> dimension = new O2IOpenCacheHashMap<>();
   private ResourceLocation defaultDimension;

   public ExtendWirelessEnergyContainer(UUID uuid, BigInteger storage, long rate, GlobalPos bindPos, BigInteger capacity, int loss) {
      super(uuid, storage, rate, bindPos);
      this.capacity = capacity;
      this.loss = loss;
   }

   public ExtendWirelessEnergyContainer(UUID uuid) {
      super(uuid, BigInteger.ZERO, 0L, null);
      this.capacity = BigInteger.ZERO;
   }

   public long addEnergy(long energy, @Nullable MetaMachine machine) {
      long change = Math.min(BigIntegerUtils.getLongValue(this.capacity.subtract(this.getStorage())), Math.min(this.getRate(), energy));
      if (change <= 0L) {
         return 0L;
      }

      long loss = change * this.loss / 1000L;
      long actualChange = change - loss;
      this.setStorage(this.getStorage().add(BigInteger.valueOf(actualChange)));
      this.getEnergyStat().update(BigInteger.valueOf(change), server.getTickCount());
      if (observed && machine != null) {
         TRANSFER_DATA.put(machine, new ExtendTransferData(this.getUuid(), actualChange, loss, machine));
      }

      return change;
   }

   public long removeEnergy(long energy, @Nullable MetaMachine machine) {
      if (machine instanceof ITieredMachine tieredMachine) {
         Level level = machine.getLevel();
         if (level != null) {
            int tier = this.dimension.getInt(level.dimension().location());
            if (tier < tieredMachine.getTier()) {
               return 0L;
            }
         }
      }

      return super.removeEnergy(energy, machine);
   }

   public long unrestrictedAddEnergy(long energy) {
      long change = Math.min(BigIntegerUtils.getLongValue(this.capacity.subtract(this.getStorage())), energy);
      if (change <= 0L) {
         return 0L;
      }

      this.setStorage(this.getStorage().add(BigInteger.valueOf(change - change * this.loss / 1000L)));
      this.getEnergyStat().update(BigInteger.valueOf(change), server.getTickCount());
      return change;
   }

   public long unrestrictedRemoveEnergy(long energy) {
      long change = Math.min(BigIntegerUtils.getLongValue(this.getStorage()), energy);
      if (change <= 0L) {
         return 0L;
      }

      this.setStorage(this.getStorage().subtract(BigInteger.valueOf(change)));
      this.getEnergyStat().update(BigInteger.valueOf(change).negate(), server.getTickCount());
      return change;
   }

   public BigInteger unrestrictedAddEnergy(BigInteger energy) {
      BigInteger change = MathUtil.min(this.capacity.subtract(this.getStorage()), energy);
      if (change.compareTo(BigInteger.ZERO) <= 0) {
         return BigInteger.ZERO;
      }

      this.setStorage(this.getStorage().add(change));
      this.getEnergyStat().update(change, server.getTickCount());
      return change;
   }

   public BigInteger unrestrictedRemoveEnergy(BigInteger energy) {
      BigInteger change = MathUtil.min(this.getStorage(), energy);
      if (change.compareTo(BigInteger.ZERO) <= 0) {
         return BigInteger.ZERO;
      }

      this.setStorage(this.getStorage().subtract(change));
      this.getEnergyStat().update(change.negate(), server.getTickCount());
      return change;
   }

   public void setCapacity(BigInteger capacity) {
      this.capacity = capacity;
      WirelessEnergySavaedData.INSTANCE.setDirty(true);
   }

   public void setLoss(int loss) {
      this.loss = loss;
      WirelessEnergySavaedData.INSTANCE.setDirty(true);
   }

   public void setDimension(ResourceLocation dimension, boolean add) {
      if (add) {
         if (this.defaultDimension != null) {
            this.dimension.removeInt(this.defaultDimension);
         }

         this.defaultDimension = dimension;
         this.dimension.put(dimension, 15);
      } else {
         this.dimension.removeInt(dimension);
         if (this.defaultDimension == dimension) {
            this.defaultDimension = null;
         }
      }
   }

   @Override
   public String getUnit() {
      return "EU";
   }

   @Override
   public BigInteger unrestrictedRemoveStorage(BigInteger storage) {
      return this.unrestrictedRemoveEnergy(storage);
   }

   @Generated
   public BigInteger getCapacity() {
      return this.capacity;
   }

   @Generated
   public int getLoss() {
      return this.loss;
   }

   @Generated
   public Object2IntOpenHashMap<ResourceLocation> getDimension() {
      return this.dimension;
   }
}
