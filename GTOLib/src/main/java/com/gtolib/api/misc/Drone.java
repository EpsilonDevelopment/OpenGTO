package com.gtolib.api.misc;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.capability.ElectricItem;
import com.gtocore.common.item.DroneBehavior;
import lombok.Generated;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class Drone {
   private String workState;
   private int work;
   private final int range;
   private final ElectricItem electricItem;
   private final ItemStack itemStack;
   public int times;

   private Drone(int range, ElectricItem electricItem, ItemStack itemStack) {
      this.range = range;
      this.electricItem = electricItem;
      this.itemStack = itemStack;
   }

   @Nullable
   public static Drone create(ItemStack itemStack) {
      DroneBehavior behavior = DroneBehavior.getDroneBehavior(itemStack);
      if (behavior != null) {
         Drone drone = new Drone(behavior.getRange(), (ElectricItem)GTCapabilityHelper.getElectricItem(itemStack), itemStack);
         drone.work = itemStack.getOrCreateTag().getInt("work");
         drone.workState = itemStack.getOrCreateTag().getString("workState");
         drone.times = itemStack.getOrCreateTag().getInt("times");
         return drone;
      } else {
         return null;
      }
   }

   public boolean isWork() {
      return this.work > 0;
   }

   public void work() {
      if (this.isWork()) {
         this.work--;
         this.itemStack.getOrCreateTag().putInt("work", this.work);
      } else if (!this.workState.isEmpty()) {
         this.workState = "";
         this.itemStack.getOrCreateTag().putString("workState", this.workState);
      }
   }

   public boolean start(int time, int eu, String state) {
      int var4 = eu * (1 << this.electricItem.getTier());
      if (var4 > this.electricItem.getCharge()) {
         return false;
      }

      this.electricItem.setCharge(this.electricItem.getCharge() - var4);
      this.work = time / (this.electricItem.getTier() - 2);
      this.workState = state;
      this.times++;
      this.itemStack.getOrCreateTag().putString("workState", this.workState);
      this.itemStack.getOrCreateTag().putInt("work", this.work);
      this.itemStack.getOrCreateTag().putInt("times", this.times);
      return true;
   }

   public long getCharge() {
      return this.electricItem.getCharge() / (1L << this.electricItem.getTier());
   }

   @Generated
   public String getWorkState() {
      return this.workState;
   }

   @Generated
   public int getRange() {
      return this.range;
   }
}
