package com.gtolib.api.machine.impl;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.handler.ICustomRecipeLogicHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.capability.IIWirelessInteractor;
import com.gtolib.api.machine.feature.multiblock.IDroneControlCenterMachine;
import com.gtolib.api.machine.impl.part.DroneHatchPartMachine;
import com.gtolib.api.machine.multiblock.NoEnergyMultiblockMachine;
import com.gtolib.api.misc.Drone;
import com.gtolib.utils.ClientUtil;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

@DataGeneratorScanned
public final class DroneControlCenterMachine extends NoEnergyMultiblockMachine implements IDroneControlCenterMachine, ICustomRecipeLogicHolder {
   @RegisterLanguage(cn = "工作次数: ", en = "Work Times: ")
   public static final String TIMES = "gtocore.drone.times";
   private final List<DroneHatchPartMachine> droneHatchPartMachine = new ArrayList<>();

   public DroneControlCenterMachine(MetaMachineBlockEntity holder) {
      super(holder);
   }

   @Override
   public boolean hasBatchConfig() {
      return false;
   }

   @Override
   public void onPartScan(@NotNull IMultiPart part) {
      super.onPartScan(part);
      if (this.getDroneHatchPartMachine().isEmpty() && part instanceof DroneHatchPartMachine machine) {
         this.getDroneHatchPartMachine().add(machine);
      }
   }

   @Override
   public void onUnload() {
      super.onUnload();
      IIWirelessInteractor.removeFromNet(this, IDroneControlCenterMachine.class);
   }

   @Override
   public void onStructureFormed() {
      this.droneHatchPartMachine.clear();
      super.onStructureFormed();
      IIWirelessInteractor.addToNet(this, IDroneControlCenterMachine.class);
   }

   @Override
   public void onStructureInvalid() {
      super.onStructureInvalid();
      this.droneHatchPartMachine.clear();
      IIWirelessInteractor.removeFromNet(this, IDroneControlCenterMachine.class);
   }

   @Override
   public List<DroneHatchPartMachine> getDroneHatchPartMachine() {
      return this.droneHatchPartMachine;
   }

   @Override
   public void customText(@NotNull List<Component> textList) {
      super.customText(textList);
      int range = 0;
      if (!this.droneHatchPartMachine.isEmpty()) {
         DroneHatchPartMachine hatch = this.droneHatchPartMachine.getFirst();

         for (int slot = 0; slot < hatch.getSize(); slot++) {
            Drone drone = hatch.getDrone(slot);
            if (drone != null) {
               range = Math.max(range, drone.getRange());
            }
         }
      }

      textList.add(ComponentPanelWidget.withButton(Component.translatable("gtocore.digital_miner.show_range").append("(" + range + ")"), "show:" + range));
      IDroneControlCenterMachine.super.addCustomText(textList);
   }

   @Override
   public void handleDisplayClick(@NotNull String componentData, ClickData clickData) {
      if (clickData.isRemote && componentData.startsWith("show:")) {
         int range = Integer.parseInt(componentData.substring(5));
         if (range > 0) {
            ClientUtil.highlighting(this.getPos(), range);
         }
      }
   }

   @Override
   public GTRecipeDefinition createCustomRecipe(RecipeHandlerUnit unit) {
      return this.droneHatchPartMachine.isEmpty() ? null : this.getRecipeBuilder().duration(20).build();
   }
}
