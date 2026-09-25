package com.gtolib.api.machine.trait;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.machine.feature.IElectricMachine;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.IRecipeHandlerHolder;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.gtolib.api.machine.feature.multiblock.IMultiblockTraitHolder;
import java.util.List;
import lombok.Generated;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.HoverEvent.Action;
import org.jetbrains.annotations.NotNull;

public class ElectricTrait extends MultiblockTrait {
   @NotNull
   protected EnergyContainerList energyContainer = EnergyContainerList.EMPTY;

   public ElectricTrait(IElectricMachine machine) {
      super((IMultiblockTraitHolder)machine);
   }

   @Override
   public void onStructureInvalid() {
      this.energyContainer = EnergyContainerList.EMPTY;
   }

   @Override
   public void onStructureFormed() {
      List<IEnergyContainer> handlers = ((IRecipeHandlerHolder)this.machine).getCapabilitiesFlat(IO.IN, IEnergyContainer.class);
      if (handlers.isEmpty()) {
         handlers = ((IRecipeHandlerHolder)this.machine).getCapabilitiesFlat(IO.OUT, IEnergyContainer.class);
      }

      this.energyContainer = new EnergyContainerList(handlers);
   }

   @Override
   public void customText(@NotNull List<Component> textList) {
      super.customText(textList);
      if (this.energyContainer.getEnergyCapacity() > 0L) {
         long maxVoltage = Math.max(this.energyContainer.getInputVoltage(), this.energyContainer.getOutputVoltage());
         String energyFormatted = FormattingUtil.formatNumbers(maxVoltage);
         byte voltageTier = GTUtil.getFloorTierByVoltage(maxVoltage);
         Component voltageName = Component.literal(GTValues.VNF[voltageTier]);
         MutableComponent bodyText = Component.translatable("gtceu.multiblock.max_energy_per_tick", energyFormatted, voltageName)
            .withStyle(ChatFormatting.GRAY);
         Component hoverText = Component.translatable("gtceu.multiblock.max_energy_per_tick_hover").withStyle(ChatFormatting.GRAY);
         textList.add(bodyText.withStyle(style -> style.withHoverEvent(new HoverEvent(Action.SHOW_TEXT, hoverText))));
      }
   }

   @NotNull
   @Generated
   public EnergyContainerList getEnergyContainer() {
      return this.energyContainer;
   }
}
