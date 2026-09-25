package com.gtolib.api.machine.mana.trait;

import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.gtolib.api.capability.IManaContainer;
import com.gtolib.api.machine.ManaDistributorMachine;
import lombok.Generated;
import org.jetbrains.annotations.Nullable;

public class NotifiableManaContainer extends NotifiableRecipeHandlerTrait implements IManaContainer {
   private ManaDistributorMachine NetMachineCache;
   @Nullable
   private TickableSubscription updateSubs;
   @SaveToDisk(defaultValue = "0")
   private long currentMana;
   private boolean acceptDistributor;
   private final IO handlerIO;
   private final long maxMana;
   private final long maxIORate;

   public NotifiableManaContainer(MetaMachine machine, IO io, long maxMana) {
      super(machine);
      this.handlerIO = io;
      this.maxMana = maxMana;
      this.maxIORate = io != IO.NONE ? maxMana : 0L;
   }

   public NotifiableManaContainer(MetaMachine machine, IO io, long maxMana, long maxIORate) {
      super(machine);
      this.handlerIO = io;
      this.maxMana = maxMana;
      this.maxIORate = maxIORate;
   }

   @Override
   public void onMachineLoad() {
      super.onMachineLoad();
      this.updateSubs = this.getMachine().subscribeServerTick(this.updateSubs, this::updateTick, 20);
   }

   @Override
   public void onMachineUnLoad() {
      super.onMachineUnLoad();
      this.removeNetMachineCache();
      if (this.updateSubs != null) {
         this.updateSubs.unsubscribe();
         this.updateSubs = null;
      }
   }

   void updateTick() {
      if (!(this.machine instanceof IControllable iControllable && !iControllable.isWorkingEnabled())) {
         ManaDistributorMachine distributor = this.getNetMachine();
         if (distributor != null) {
            long mana = this.extractionRate();
            if (mana > 0L) {
               long change = distributor.removeMana(mana, 20, false);
               if (change > 0L) {
                  this.currentMana += change;
                  this.notifyListeners();
               }
            }
         }
      }
   }

   protected long extractionRate() {
      return this.maxMana - this.currentMana;
   }

   @Override
   public boolean acceptDistributor() {
      return this.acceptDistributor;
   }

   @Override
   public void setCurrentMana(long mana) {
      if (this.currentMana != mana) {
         this.currentMana = mana;
         this.notifyListeners();
      }
   }

   @Generated
   public void setNetMachineCache(ManaDistributorMachine NetMachineCache) {
      this.NetMachineCache = NetMachineCache;
   }

   @Generated
   public ManaDistributorMachine getNetMachineCache() {
      return this.NetMachineCache;
   }

   @Generated
   @Override
   public long getCurrentMana() {
      return this.currentMana;
   }

   @Generated
   public void setAcceptDistributor(boolean acceptDistributor) {
      this.acceptDistributor = acceptDistributor;
   }

   @Generated
   @Override
   public IO getHandlerIO() {
      return this.handlerIO;
   }

   @Generated
   @Override
   public long getMaxMana() {
      return this.maxMana;
   }

   @Generated
   @Override
   public long getMaxIORate() {
      return this.maxIORate;
   }
}
