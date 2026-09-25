package com.gtolib.api.machine.mana.trait;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gtocore.common.machine.mana.part.ManaHatchPartMachine;
import com.gtolib.api.capability.IManaContainer;
import com.gtolib.api.machine.feature.multiblock.IMultiblockTraitHolder;
import com.gtolib.api.machine.mana.feature.IManaMultiblock;
import com.gtolib.api.machine.trait.MultiblockTrait;
import com.gtolib.api.misc.ManaContainerList;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class ManaTrait extends MultiblockTrait {
   @NotNull
   private ManaContainerList manaContainers = ManaContainerList.EMPTY;

   public ManaTrait(IManaMultiblock machine) {
      super((IMultiblockTraitHolder)machine);
   }

   @Override
   public void onStructureInvalid() {
      this.manaContainers = ManaContainerList.EMPTY;
   }

   @Override
   public void onStructureFormed() {
      List<IManaContainer> containers = new ArrayList<>();

      for (IMultiPart part : this.getMachine().getParts()) {
         if (part instanceof ManaHatchPartMachine manaHatchPartMachine) {
            NotifiableManaContainer container = manaHatchPartMachine.getManaContainer();
            if (((IManaMultiblock)this.machine).isGeneratorMana()) {
               if (container.getHandlerIO() == IO.OUT) {
                  containers.add(manaHatchPartMachine.getManaContainer());
               }
            } else if (container.getHandlerIO() == IO.IN) {
               containers.add(manaHatchPartMachine.getManaContainer());
            }
         }
      }

      if (containers.isEmpty()) {
         this.manaContainers = ManaContainerList.EMPTY;
      } else {
         this.manaContainers = new ManaContainerList(containers.toArray(new IManaContainer[0]));
      }
   }

   @Override
   public void customText(@NotNull List<Component> textList) {
      super.customText(textList);
      textList.add(
         Component.translatable(
            "gtocore.machine.mana_stored",
            FormattingUtil.formatNumbers(this.manaContainers.getCurrentMana()) + " / " + FormattingUtil.formatNumbers(this.manaContainers.getMaxMana())
         )
      );
      if (((IManaMultiblock)this.machine).isGeneratorMana()) {
         textList.add(Component.translatable("gtocore.machine.mana_production", FormattingUtil.formatNumbers(this.manaContainers.getMaxIORate()) + " /t"));
      } else {
         textList.add(Component.translatable("gtocore.machine.mana_consumption", FormattingUtil.formatNumbers(this.manaContainers.getMaxIORate()) + " /t"));
      }
   }

   @NotNull
   public ManaContainerList getManaContainers() {
      return this.manaContainers;
   }
}
