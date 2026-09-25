package com.gtolib.api.machine.impl.part;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.gtolib.api.machine.impl.ProcessingEncapsulatorMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class MachineAccessLinkPartMachine extends MultiblockPartMachine implements IDataStickInteractable {
   @SaveToDisk(defaultValue = "0")
   private long pos;

   public MachineAccessLinkPartMachine(MetaMachineBlockEntity holder) {
      super(holder);
   }

   @Override
   public void addedToController(@NotNull IMultiController controller) {
      super.addedToController(controller);
      this.bound();
   }

   @Override
   public void onUnload() {
      this.remove();
      super.onUnload();
   }

   @Override
   public void removedFromController(@NotNull IMultiController controller) {
      super.removedFromController(controller);
      this.remove();
   }

   private void remove() {
      if (this.pos != 0L && this.getLevel() != null) {
         if (MetaMachine.getMachine(this.getLevel(), BlockPos.of(this.pos)) instanceof ProcessingEncapsulatorMachine encapsulatorMachine) {
            for (IMultiController c : this.getControllers()) {
               if (c instanceof IRecipeLogicMachine recipeLogicMachine) {
                  encapsulatorMachine.types.remove(recipeLogicMachine.getRecipeType());
               }
            }

            encapsulatorMachine.updateMap();
         }
      }
   }

   private boolean bound() {
      if (this.pos != 0L && this.getLevel() != null) {
         if (MetaMachine.getMachine(this.getLevel(), BlockPos.of(this.pos)) instanceof ProcessingEncapsulatorMachine encapsulatorMachine) {
            for (IMultiController c : this.getControllers()) {
               if (c instanceof IRecipeLogicMachine recipeLogicMachine) {
                  encapsulatorMachine.types.add(recipeLogicMachine.getRecipeType());
               }
            }

            encapsulatorMachine.updateMap();
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public InteractionResult onDataStickUse(Player player, ItemStack dataStick) {
      if (this.isRemote()) {
         return InteractionResult.sidedSuccess(true);
      }

      CompoundTag tag = dataStick.getTag();
      if (tag != null && tag.contains("pos")) {
         this.pos = tag.getLong("pos");
         if (this.bound()) {
            player.sendSystemMessage(Component.translatable("ars_nouveau.scryer_scroll.bound", dataStick.getHoverName()));
            return InteractionResult.SUCCESS;
         } else {
            return InteractionResult.FAIL;
         }
      } else {
         return InteractionResult.FAIL;
      }
   }
}
