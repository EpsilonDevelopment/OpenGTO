package com.gtolib.api.capability;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.item.IElectricItem;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IElectricMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gtolib.api.machine.impl.WirelessChargerMachine;
import com.gtolib.utils.GTOUtils;
import com.gtolib.utils.MathUtil;
import com.hepdd.gtmthings.api.capability.IBindable;
import com.hepdd.gtmthings.utils.TeamUtil;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.energy.IEnergyStorage;

public interface IWirelessChargerInteraction extends IIWirelessInteractor<WirelessChargerMachine>, IBindable {
   BlockPos getPos();

   @Override
   default Class<WirelessChargerMachine> getProviderClass() {
      return WirelessChargerMachine.class;
   }

   default boolean firstTestMachine(WirelessChargerMachine machine) {
      Level level = machine.getLevel();
      if (level == null) {
         return false;
      }

      UUID uuid = this.getUUID();
      return uuid == null ? false : this.testMachine(machine) && TeamUtil.getTeamUUID(uuid).equals(TeamUtil.getTeamUUID(machine.getUUID()));
   }

   default boolean testMachine(WirelessChargerMachine machine) {
      return machine.isMachine()
         && machine.isFormed()
         && machine.isWorkingEnabled()
         && machine.getRate() > 0
         && GTOUtils.calculateDistance(machine.getPos(), this.getPos()) < machine.getRange();
   }

   default boolean display() {
      return false;
   }

   default void charge(TickableSubscription subscription) {
      if (subscription != null && this instanceof IElectricMachine electricMachine) {
         WirelessChargerMachine machine = this.getNetMachine();
         if (machine != null && !machine.isOnlyProvideToPlayer) {
            if (!machine.isProvideToMultiBlockHatch && this instanceof IMultiPart) {
               return;
            }

            long stored = machine.getEnergyStored();
            if (stored > 0L) {
               IEnergyContainer electricContainer = electricMachine.getEnergyContainer();
               if (electricContainer.getEnergyStored() < electricContainer.getEnergyCapacity()) {
                  subscription.cycle = 20;
                  machine.removeEnergy(electricContainer.addEnergy(Math.min(GTValues.V[electricMachine.getTier()] * 40L, stored)));
               }

               return;
            }
         }

         if (subscription.cycle < 200) {
            subscription.cycle++;
         }
      } else if (subscription != null) {
         subscription.unsubscribe();
      }
   }

   static void charge(WirelessChargerMachine machine, ItemStack stack) {
      if (machine != null) {
         long stored = machine.getEnergyStored();
         if (stored > 0L) {
            IElectricItem electricitem = GTCapabilityHelper.getElectricItem(stack);
            if (electricitem != null) {
               if (electricitem.chargeable() && electricitem.getCharge() < electricitem.getMaxCharge()) {
                  machine.removeEnergy(
                     electricitem.charge(Math.min(stored, GTValues.V[electricitem.getTier()] * machine.getRate()), electricitem.getTier(), true, false)
                  );
               }
            } else {
               IEnergyStorage energyItem = GTCapabilityHelper.getForgeEnergyItem(stack);
               if (energyItem != null && energyItem.canReceive() && energyItem.getEnergyStored() < energyItem.getMaxEnergyStored()) {
                  machine.removeEnergy(energyItem.receiveEnergy(MathUtil.saturatedCast(stored), false));
               }
            }
         }
      }
   }
}
