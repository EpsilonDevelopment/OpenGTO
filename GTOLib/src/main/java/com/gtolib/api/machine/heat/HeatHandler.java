package com.gtolib.api.machine.heat;

import com.gregtechceu.gtceu.api.blockentity.GTBlockEntity;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IExplosionMachine;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.gregtechceu.gtceu.utils.TaskHandler;
import com.gto.datasynclib.FieldDataManager;
import com.gto.datasynclib.IFieldDataHolder;
import com.gto.datasynclib.LazyFieldDataManager;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.gtolib.api.capability.IHeatContainer;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import lombok.Generated;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

public class HeatHandler implements IFieldDataHolder, IHeatContainer {
   public static final BooleanSupplier TRUE = () -> true;
   private final LazyFieldDataManager fieldDataManager = new LazyFieldDataManager(this);
   protected final GTBlockEntity blockEntity;
   public final long maxTemperature;
   public final double heatCapacity;
   public final double baseTransferRate;
   public final double cooldownRate;
   public long maxHeat;
   @SyncToClient
   public double ambientTemperature;
   @SaveToDisk(defaultValue = "0")
   @SyncToClient(notifyUpdate = true)
   public long currentHeat;
   public Predicate<Direction> sideIOCondition = GTUtil.NEGATIVE;
   @SaveToDisk(defaultValue = "0")
   public double coolDownRemaining;
   protected int timer;
   @Nullable
   protected TickableSubscription transferSubs;
   protected final List<Runnable> listeners = new ArrayList<>();
   protected boolean isDirty = true;

   public HeatHandler(GTBlockEntity blockEntity, long maxTemperature, double heatCapacity, double baseTransferRate, double cooldownRate) {
      this.blockEntity = blockEntity;
      this.maxTemperature = maxTemperature;
      this.heatCapacity = heatCapacity;
      this.baseTransferRate = baseTransferRate;
      this.cooldownRate = cooldownRate;
      this.ambientTemperature = 293.15;
      this.currentHeat = 0L;
      this.maxHeat = (long)((maxTemperature - 293.15) * heatCapacity);
   }

   public void onLoad() {
      this.isDirty = true;
      if (this.blockEntity.getLevel() instanceof ServerLevel serverLevel) {
         this.ambientTemperature = AmbientTemperature.calculate(serverLevel, this.blockEntity.getBlockPos());
         if (this.ambientTemperature >= this.maxTemperature) {
            this.doExplosion();
            return;
         }

         this.maxHeat = (long)((this.maxTemperature - this.ambientTemperature) * this.heatCapacity);
         if (this.currentHeat < 0L) {
            this.currentHeat = 0L;
         }

         if (this.currentHeat > 0L) {
            this.subscribeTransfer();
         }
      }
   }

   public void onUnLoad() {
      this.unsubscribeTransfer();
      this.isDirty = true;
   }

   protected void doExplosion() {
      if (this.blockEntity instanceof MetaMachineBlockEntity machineBlock
         && machineBlock.metaMachine instanceof IExplosionMachine machine
         && this.blockEntity.getLevel() instanceof ServerLevel level) {
         TaskHandler.enqueueTask(level, () -> machine.doExplosion(3.0F));
      }
   }

   protected void runNotify() {
      this.isDirty = true;
      if (!this.blockEntity.remove) {
         this.listeners.forEach(Runnable::run);
         this.blockEntity.setChanged();
         if (this.currentHeat > 0L) {
            this.subscribeTransfer();
         }
      }
   }

   public final void subscribeTransfer() {
      this.transferSubs = this.blockEntity.subscribeServerTick(this.transferSubs, () -> {
         double transferred = this.transferHeatToAdjacent(20);
         if (transferred == 0.0) {
            this.unsubscribeTransfer();
         }
      }, 20);
   }

   public final void unsubscribeTransfer() {
      if (this.transferSubs != null) {
         this.transferSubs.unsubscribe();
         this.transferSubs = null;
      }
   }

   public final ISubscription addChangedListener(Runnable listener) {
      this.listeners.add(listener);
      return () -> this.listeners.remove(listener);
   }

   public final void notifyListeners() {
      if (this.isDirty && !this.blockEntity.remove && this.blockEntity.getLevel() instanceof ServerLevel serverLevel) {
         this.isDirty = false;
         TaskHandler.enqueueTask(serverLevel, this::runNotify, 0);
      }
   }

   @Override
   public final boolean heatIO(Direction side) {
      return this.sideIOCondition.test(side);
   }

   @Override
   public final void setCurrentHeat(long heat) {
      if (heat < 0L) {
         heat = 0L;
      }

      if (heat != this.currentHeat) {
         this.currentHeat = heat;
         this.notifyListeners();
      }
   }

   @Override
   public final FieldDataManager getFieldDataManager() {
      return this.fieldDataManager.get();
   }

   @Override
   public final double getTemperature() {
      return this.ambientTemperature + this.currentHeat / this.heatCapacity;
   }

   @Override
   public final long addHeatUnrestricted(long amount, boolean simulate) {
      if (amount <= 0L) {
         return 0L;
      }

      long current = this.currentHeat;
      long space = this.maxHeat - current;
      if (space < amount) {
         if (!simulate) {
            this.doExplosion();
         }

         if (space == 0L) {
            return 0L;
         }

         amount = space;
      }

      if (!simulate) {
         this.setCurrentHeat(current + amount);
      }

      return amount;
   }

   @Override
   public final long removeHeatUnrestricted(long amount, boolean simulate) {
      if (amount <= 0L) {
         return 0L;
      }

      long current = this.currentHeat;
      if (current <= 0L) {
         return 0L;
      }

      long actual = Math.min(amount, current);
      if (!simulate) {
         this.setCurrentHeat(current - actual);
      }

      return actual;
   }

   @Override
   public final long addHeat(long amount, int rateMultiplier, boolean simulate) {
      long maxRate = (long)(this.baseTransferRate * rateMultiplier);
      long actual = Math.min(amount, maxRate);
      if (actual <= 0L) {
         return 0L;
      }

      long current = this.currentHeat;
      long space = this.maxHeat - current;
      if (space < actual) {
         if (!simulate) {
            this.doExplosion();
         }

         if (space == 0L) {
            return 0L;
         }

         actual = space;
      }

      if (!simulate) {
         this.setCurrentHeat(current + actual);
      }

      return actual;
   }

   @Override
   public final long removeHeat(long amount, int rateMultiplier, boolean simulate) {
      long current = this.currentHeat;
      if (current <= 0L) {
         return 0L;
      }

      long maxRate = (long)(this.baseTransferRate * rateMultiplier);
      long actual = Math.min(current, Math.min(amount, maxRate));
      if (actual <= 0L) {
         return 0L;
      }

      if (!simulate) {
         this.setCurrentHeat(current - actual);
      }

      return actual;
   }

   @Override
   public final double transferHeatToAdjacent(int rateMultiplier) {
      long available = this.currentHeat;
      if (available <= 0L) {
         return 0.0;
      }

      double temperature = this.getTemperature();
      double deltaT = temperature - this.ambientTemperature;
      double totalTransferred = 0.0;
      if (deltaT > 0.0) {
         double coolAmount = this.coolDownRemaining + this.cooldownRate * Math.sqrt(deltaT) / this.heatCapacity * rateMultiplier;
         if (coolAmount < 1.0) {
            this.coolDownRemaining = coolAmount;
            totalTransferred = coolAmount;
         } else {
            this.coolDownRemaining = 0.0;
            long transfer = Math.min(available, (long)coolAmount);
            available -= transfer;
            totalTransferred += transfer;
         }
      }

      int start = this.timer++;

      for (int i = 0; i < 6; i++) {
         Direction side = GTUtil.DIRECTIONS[(i + start) % 6];
         if (this.sideIOCondition.test(side)) {
            IHeatContainer receiver = GTCapabilityHelper.getBlockEntityGTCapability(
               IHeatContainer.class, this.blockEntity.getNeighborBlockEntity(side), side.getOpposite()
            );
            if (receiver != null) {
               long transfer = receiver.acceptHeatFromNetwork(
                  this, side.getOpposite(), available, temperature, this.heatCapacity, this.baseTransferRate, rateMultiplier
               );
               if (transfer != 0L) {
                  totalTransferred += transfer;
                  available -= transfer;
                  if (available <= 0L) {
                     break;
                  }

                  temperature = this.getTemperature();
               }
            }
         }
      }

      this.setCurrentHeat(available);
      return totalTransferred;
   }

   @Override
   public long acceptHeatFromNetwork(
      Object sender, Direction side, long heat, double temperature, double heatCapacity, double baseTransferRate, int rateMultiplier
   ) {
      double deltaT = temperature - this.getTemperature();
      if (deltaT <= 0.0) {
         return 0L;
      }

      if (!this.sideIOCondition.test(side)) {
         return 0L;
      }

      long actual = Math.min(
         heat,
         Math.min(
            getMaxBalancedTransfer(this.heatCapacity, heatCapacity, deltaT),
            (long)(Math.min(this.baseTransferRate, baseTransferRate) * deltaT * rateMultiplier)
         )
      );
      if (actual <= 0L) {
         return 0L;
      }

      long current = this.currentHeat;
      long space = this.maxHeat - current;
      if (space < actual) {
         this.doExplosion();
         if (space == 0L) {
            return 0L;
         }

         actual = space;
      }

      this.setCurrentHeat(current + actual);
      return actual;
   }

   private static long getMaxBalancedTransfer(double cap1, double cap2, double deltaT) {
      return (long)(deltaT * cap1 * cap2 / (cap1 + cap2));
   }

   @Override
   public final int getSignal() {
      return this.maxTemperature <= this.ambientTemperature ? 0 : (int)(15.0 * this.currentHeat / this.maxHeat);
   }

   @Generated
   @Override
   public long getMaxTemperature() {
      return this.maxTemperature;
   }

   @Generated
   @Override
   public double getHeatCapacity() {
      return this.heatCapacity;
   }

   @Generated
   @Override
   public double getBaseTransferRate() {
      return this.baseTransferRate;
   }

   @Generated
   @Override
   public double getCooldownRate() {
      return this.cooldownRate;
   }

   @Generated
   @Override
   public long getMaxHeat() {
      return this.maxHeat;
   }

   @Generated
   @Override
   public double getAmbientTemperature() {
      return this.ambientTemperature;
   }

   @Generated
   @Override
   public long getCurrentHeat() {
      return this.currentHeat;
   }

   @Generated
   public void setSideIOCondition(Predicate<Direction> sideIOCondition) {
      this.sideIOCondition = sideIOCondition;
   }
}
