package com.gtolib.api.machine.trait;

import com.gregtechceu.gtceu.api.blockentity.ITickSubscription;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.utils.TaskHandler;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.gtolib.api.capability.IExtendWirelessEnergyContainerHolder;
import com.gtolib.api.wireless.ExtendTransferData;
import com.gtolib.api.wireless.ExtendWirelessEnergyContainer;
import com.gtolib.utils.MathUtil;
import com.hepdd.gtmthings.api.misc.BasicTransferData;
import com.hepdd.gtmthings.api.misc.WirelessEnergyContainer;
import java.util.UUID;
import lombok.Generated;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class WirelessEnergyContainerTrait extends NotifiableRecipeHandlerTrait implements IEnergyContainer, IExtendWirelessEnergyContainerHolder {
   private WirelessEnergyContainer wirelessEnergyContainerCache;
   private int tier;
   @SaveToDisk(defaultValue = "0")
   private long energyStored;
   private final long energyCapacity;
   private final long inputVoltage;
   private final long inputAmperage;
   private final long outputVoltage;
   private final long outputAmperage;
   private final IO handlerIO;
   @Nullable
   private TickableSubscription updateSubs;

   private WirelessEnergyContainerTrait(
      MetaMachine machine, long maxCapacity, long maxInputVoltage, long maxInputAmperage, long maxOutputVoltage, long maxOutputAmperage
   ) {
      super(machine);
      this.energyCapacity = maxCapacity;
      this.inputVoltage = maxInputVoltage;
      this.inputAmperage = maxInputAmperage;
      this.outputVoltage = maxOutputVoltage;
      this.outputAmperage = maxOutputAmperage;
      this.handlerIO = maxInputVoltage > 0L ? IO.IN : IO.OUT;
   }

   public static WirelessEnergyContainerTrait emitterContainer(MetaMachine machine, long maxCapacity, long maxOutputVoltage, long maxOutputAmperage) {
      return new WirelessEnergyContainerTrait(machine, maxCapacity, 0L, 0L, maxOutputVoltage, maxOutputAmperage);
   }

   public static WirelessEnergyContainerTrait receiverContainer(MetaMachine machine, long maxCapacity, long maxInputVoltage, long maxInputAmperage) {
      return new WirelessEnergyContainerTrait(machine, maxCapacity, maxInputVoltage, maxInputAmperage, 0L, 0L);
   }

   @Override
   public void onMachineLoad() {
      super.onMachineLoad();
      if (this.machine.getLevel() instanceof ServerLevel serverLevel) {
         this.updateSubs = this.getMachine().subscribeServerTick(this.updateSubs, this::updateTick, 20);
         TaskHandler.enqueueTask(serverLevel, this::updateTick);
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

   @Override
   public long changeEnergy(long energyToAdd) {
      if (energyToAdd == 0L) {
         return 0L;
      }

      long change;
      if (energyToAdd > 0L) {
         change = Math.min(energyToAdd, this.energyCapacity - this.energyStored);
         if (WirelessEnergyContainer.observed && change > 0L) {
            ExtendWirelessEnergyContainer container = this.getWirelessEnergyContainer();
            if (container == null) {
               return 0L;
            }

            long loss = change / 1000L * container.getLoss();
            WirelessEnergyContainer.TRANSFER_DATA.put(this.machine, new ExtendTransferData(container.getUuid(), change - loss, loss, this.getMachine()));
         }
      } else {
         change = Math.max(energyToAdd, -this.energyStored);
         if (WirelessEnergyContainer.observed && change < 0L) {
            ExtendWirelessEnergyContainer container = this.getWirelessEnergyContainer();
            if (container == null) {
               return 0L;
            }

            WirelessEnergyContainer.TRANSFER_DATA.put(this.machine, new BasicTransferData(container.getUuid(), change, this.getMachine()));
         }
      }

      if (change != 0L) {
         this.energyStored += change;
         if (this.machine != null) {
            this.updateSubs = this.machine.subscribeServerTick(this.updateSubs, this::updateTick, 20);
         }
      }

      return change;
   }

   private void updateTick() {
      if (((IControllable)this.machine).isWorkingEnabled()) {
         Level level = this.machine.getLevel();
         ExtendWirelessEnergyContainer container = this.getWirelessEnergyContainer();
         if (container != null && level != null) {
            if (this.tier < ((ITieredMachine)this.machine).getTier()) {
               this.tier = container.getDimension().getInt(level.dimension().location());
               return;
            }

            long rate = MathUtil.saturatedCast(container.getRate() * 30.0);
            if (rate == 0L) {
               return;
            }

            if (this.handlerIO == IO.IN) {
               long canInput = this.energyCapacity - this.energyStored;
               if (canInput > 0L) {
                  long change = container.unrestrictedRemoveEnergy(Math.min(rate, canInput));
                  if (change > 0L) {
                     this.energyStored += change;
                     this.notifyListeners();
                  }
               } else {
                  this.updateSubs = ITickSubscription.unsubscribe(this.updateSubs);
               }
            } else if (this.energyStored > 0L) {
               long change = container.unrestrictedAddEnergy(Math.min(rate, this.energyStored));
               if (change > 0L) {
                  this.energyStored -= change;
                  this.notifyListeners();
               }
            } else {
               this.updateSubs = ITickSubscription.unsubscribe(this.updateSubs);
            }
         }
      }
   }

   @Override
   public long acceptEnergyFromNetwork(Object o, Direction side, long voltage, long amperage) {
      return 0L;
   }

   @Override
   public boolean inputsEnergy(Direction side) {
      return false;
   }

   @Nullable
   public UUID getUUID() {
      return this.getMachine().getOwnerUUID();
   }

   @Override
   public long getEnergyStored() {
      return this.energyStored;
   }

   @Override
   public long getEnergyCapacity() {
      return this.energyCapacity;
   }

   @Override
   public long getInputVoltage() {
      return this.inputVoltage;
   }

   @Override
   public long getInputAmperage() {
      return this.inputAmperage;
   }

   @Override
   public long getOutputVoltage() {
      return this.outputVoltage;
   }

   @Override
   public long getOutputAmperage() {
      return this.outputAmperage;
   }

   @Override
   public IO getHandlerIO() {
      return this.handlerIO;
   }

   @Generated
   public WirelessEnergyContainer getWirelessEnergyContainerCache() {
      return this.wirelessEnergyContainerCache;
   }

   @Generated
   public void setWirelessEnergyContainerCache(WirelessEnergyContainer wirelessEnergyContainerCache) {
      this.wirelessEnergyContainerCache = wirelessEnergyContainerCache;
   }
}
