package com.gtolib.api.machine.impl.part;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IWorkableMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.WorkableTieredPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.gtolib.api.misc.Drone;
import com.gtolib.utils.GTOUtils;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import java.util.function.Predicate;
import lombok.Generated;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DroneHatchPartMachine extends WorkableTieredPartMachine implements IMachineLife {
   private final DroneHatchPartMachine.DroneItemStackHandler inventory;
   @SaveToDisk
   private final NotifiableItemStackHandler handler;
   private final int size;

   public DroneHatchPartMachine(MetaMachineBlockEntity holder, int tier) {
      super(holder, tier);
      int sizeRoot = tier - 1;
      this.size = sizeRoot * sizeRoot;
      this.handler = new NotifiableItemStackHandler(this, this.size, IO.NONE, IO.BOTH, DroneHatchPartMachine.DroneItemStackHandler::new);
      this.inventory = (DroneHatchPartMachine.DroneItemStackHandler)this.handler.storage;
   }

   @Override
   public void onWorking(IWorkableMultiController controller) {
      super.onWorking(controller);
      if (this.getOffsetTimer() % 20 == 0) {
         for (int i = 0; i < this.inventory.getSlots(); i++) {
            Drone drone = this.getDrone(i);
            if (drone != null) {
               drone.work();
            }
         }
      }
   }

   @Override
   public boolean hasOnWorkingMethod() {
      return true;
   }

   public boolean hasDrone(BlockPos pos1, BlockPos pos2, Predicate<Drone> predicate) {
      for (int i = 0; i < this.size; i++) {
         Drone drone = this.getDrone(i);
         if (drone != null && GTOUtils.calculateDistance(pos1, pos2) < drone.getRange()) {
            return true;
         }
      }

      return false;
   }

   @Nullable
   public Drone getFirstUsableDrone(BlockPos pos1, BlockPos pos2, Predicate<Drone> predicate) {
      for (int i = 0; i < this.size; i++) {
         Drone drone = this.getDrone(i);
         if (drone != null && !drone.isWork() && GTOUtils.calculateDistance(pos1, pos2) < drone.getRange() && predicate.test(drone)) {
            return drone;
         }
      }

      return null;
   }

   @Nullable
   public Drone getDrone(int slot) {
      return this.inventory.drones[slot];
   }

   @Override
   public void onMachineRemoved() {
      if (this.getLevel() != null) {
         for (int i = 0; i < this.inventory.getSlots(); i++) {
            Drone drone = this.getDrone(i);
            if (drone != null && drone.isWork()) {
               return;
            }

            ItemStack stackInSlot = this.inventory.getStackInSlot(i);
            if (!stackInSlot.isEmpty()) {
               this.inventory.setStackInSlot(i, ItemStack.EMPTY);
               Block.popResource(this.getLevel(), this.getPos(), stackInSlot);
            }
         }

         this.inventory.clean();
      }
   }

   @Override
   public void onLoad() {
      super.onLoad();
      if (!this.isRemote()) {
         this.inventory.load();
      }
   }

   @Override
   public void onUnload() {
      super.onUnload();
      this.inventory.clean();
   }

   @Override
   public Widget createUIWidget() {
      int rowSize = (int)Math.sqrt(this.size);
      WidgetGroup group = new WidgetGroup(0, 0, 18 * rowSize + 16, 18 * rowSize + 16);
      WidgetGroup container = new WidgetGroup(4, 4, 18 * rowSize + 8, 18 * rowSize + 8);
      int index = 0;

      for (int y = 0; y < rowSize; y++) {
         for (int x = 0; x < rowSize; x++) {
            container.addWidget(
               new SlotWidget(this.inventory, index++, 4 + x * 18, 4 + y * 18, true, true)
                  .setBackgroundTexture(GuiTextures.SLOT)
                  .setIngredientIO(IngredientIO.INPUT)
            );
         }
      }

      container.setBackground(GuiTextures.BACKGROUND_INVERSE);
      group.addWidget(container);
      return group;
   }

   @Override
   public boolean canShared() {
      return false;
   }

   @Generated
   public int getSize() {
      return this.size;
   }

   private static final class DroneItemStackHandler extends CustomItemStackHandler {
      private final Drone[] drones = new Drone[this.getSlots()];

      private DroneItemStackHandler(int size) {
         super(size);
      }

      private void load() {
         for (int i = 0; i < this.getSlots(); i++) {
            Drone drone = Drone.create(this.getStackInSlot(i));
            if (drone != null) {
               this.drones[i] = drone;
            }
         }
      }

      private void clean() {
         for (int i = 0; i < this.getSlots(); i++) {
            this.drones[i] = null;
         }
      }

      @Override
      public void onContentsChanged(int index) {
         this.drones[index] = null;
         Drone drone = Drone.create(this.stacks[index]);
         if (drone != null) {
            this.drones[index] = drone;
         }

         super.onContentsChanged(index);
      }

      @Override
      public int insert(int slot, @NotNull ItemStack stack, int amount, boolean simulate) {
         this.drones[slot] = null;
         Drone drone = Drone.create(stack);
         if (drone != null) {
            if (!simulate) {
               this.drones[slot] = drone;
               this.stacks[slot] = stack.copy();
               super.onContentsChanged(slot);
            }

            return amount;
         } else {
            return 0;
         }
      }

      @Override
      public int extract(int slot, ItemStack s, int amount, boolean simulate) {
         this.drones[slot] = null;
         ItemStack stack = this.getStackInSlot(slot);
         if (!simulate) {
            this.stacks[slot] = ItemStack.EMPTY;
            super.onContentsChanged(slot);
            if (stack.hasTag()) {
               CompoundTag tag = stack.getTag();
               tag.remove("workState");
               tag.remove("work");
               tag.remove("times");
               if (tag.isEmpty()) {
                  stack.setTag(null);
               }
            }

            return stack.getCount();
         } else {
            return stack.getCount();
         }
      }
   }
}
