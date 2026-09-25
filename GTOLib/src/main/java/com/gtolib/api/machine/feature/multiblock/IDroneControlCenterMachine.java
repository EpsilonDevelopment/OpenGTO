package com.gtolib.api.machine.feature.multiblock;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gtolib.api.capability.IIWirelessInteractor;
import com.gtolib.api.machine.impl.part.DroneHatchPartMachine;
import com.gtolib.api.misc.Drone;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public interface IDroneControlCenterMachine extends IIWirelessInteractor.IWirelessProvider {
   BlockPos getPos();

   RecipeLogic getRecipeLogic();

   default Drone getFirstUsableDrone(BlockPos pos, Predicate<Drone> predicate) {
      if (this.getDroneHatchPartMachine().isEmpty()) {
         return null;
      }

      Drone drone = null;
      Iterator<DroneHatchPartMachine> it = this.getDroneHatchPartMachine().iterator();

      while (drone == null && it.hasNext()) {
         drone = it.next().getFirstUsableDrone(this.getPos(), pos, predicate);
      }

      return drone;
   }

   List<DroneHatchPartMachine> getDroneHatchPartMachine();

   default boolean isActiveState() {
      return this.isFormed() && this.getRecipeLogic().isWorking();
   }

   default void addCustomText(List<Component> textList) {
      if (!this.getDroneHatchPartMachine().isEmpty()) {
         textList.add(Component.translatable("tooltip.jade.state", ""));
         DroneHatchPartMachine first = this.getDroneHatchPartMachine().getFirst();

         for (int i = 0; i < first.getSize(); i++) {
            MutableComponent component = Component.translatable("side_config.ad_astra.slots").append(" " + i + ": ");
            Drone drone = first.getDrone(i);
            if (drone == null) {
               component.append(Component.translatable("tooltip.jade.empty"));
            } else {
               component.append(Component.translatable("gtocore.drone.times").append(String.valueOf(drone.times)).withStyle(ChatFormatting.GOLD));
               if (drone.isWork()) {
                  component.append(Component.translatable(drone.getWorkState()).withStyle(ChatFormatting.AQUA));
               } else {
                  component.append(Component.translatable("gtceu.multiblock.idling"));
               }
            }

            textList.add(component);
         }

         if (this.getDroneHatchPartMachine().size() > 1) {
            textList.add(
               Component.translatable("gtocore.machine.tooltips.items_are_hidden", this.getDroneHatchPartMachine().size() - 1).withStyle(ChatFormatting.GRAY)
            );
         }
      }
   }

   default boolean hasDrone(BlockPos pos, Predicate<Drone> predicate) {
      if (this.getDroneHatchPartMachine().isEmpty()) {
         return false;
      }

      for (DroneHatchPartMachine droneHatchPartMachine : this.getDroneHatchPartMachine()) {
         if (droneHatchPartMachine.hasDrone(this.getPos(), pos, predicate)) {
            return true;
         }
      }

      return false;
   }

   boolean isFormed();
}
