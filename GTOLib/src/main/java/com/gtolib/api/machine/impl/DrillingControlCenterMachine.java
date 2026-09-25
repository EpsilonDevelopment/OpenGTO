package com.gtolib.api.machine.impl;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.handler.ICustomRecipeLogicHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gtolib.api.capability.IIWirelessInteractor;
import com.gtolib.api.machine.multiblock.ElectricMultiblockMachine;
import com.gtolib.utils.ClientUtil;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import java.util.List;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public final class DrillingControlCenterMachine extends ElectricMultiblockMachine implements ICustomRecipeLogicHolder {
   public DrillingControlCenterMachine(MetaMachineBlockEntity holder) {
      super(holder);
   }

   public double getMultiplier() {
      return Math.pow(1.5, this.getTier() - 5);
   }

   @Override
   public boolean hasOverclockConfig() {
      return false;
   }

   @Override
   public boolean hasBatchConfig() {
      return false;
   }

   @Override
   public void onStructureFormed() {
      super.onStructureFormed();
      IIWirelessInteractor.addToNet(this);
   }

   @Override
   public void onStructureInvalid() {
      super.onStructureInvalid();
      IIWirelessInteractor.removeFromNet(this);
   }

   @Override
   public void onUnload() {
      super.onUnload();
      IIWirelessInteractor.removeFromNet(this);
   }

   @Override
   public void customText(@NotNull List<Component> textList) {
      super.customText(textList);
      textList.add(ComponentPanelWidget.withButton(Component.translatable("gtocore.digital_miner.show_range"), "show"));
   }

   @Override
   public void handleDisplayClick(String componentData, ClickData clickData) {
      if (clickData.isRemote && "show".equals(componentData)) {
         ClientUtil.highlighting(this.getPos(), 16);
      }
   }

   @Override
   public GTRecipeDefinition createCustomRecipe(RecipeHandlerUnit unit) {
      return this.getTier() < 5 ? null : this.getRecipeBuilder().duration(20).EUt(this.getOverclockVoltage()).build();
   }
}
