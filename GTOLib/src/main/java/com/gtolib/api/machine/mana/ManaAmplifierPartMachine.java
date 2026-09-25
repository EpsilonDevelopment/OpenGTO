package com.gtolib.api.machine.mana;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.machine.feature.IOverclockMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IWorkableMultiController;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.mana.feature.IManaMachine;
import com.gtolib.api.machine.mana.trait.NotifiableManaContainer;
import com.gtolib.api.machine.part.WorkableAmountConfigurationPartMachine;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import lombok.Generated;
import org.jetbrains.annotations.NotNull;

@DataGeneratorScanned
public class ManaAmplifierPartMachine extends WorkableAmountConfigurationPartMachine implements IManaMachine, IControllable {
   @RegisterLanguage(cn = "最大魔力", en = "Max Mana")
   private static final String MAX = "gtocore.machine.mana_amplifier_part.max_mana";
   @SaveToDisk
   protected final NotifiableManaContainer manaContainer;
   @SaveToDisk(defaultValue = "true")
   @SyncToClient
   protected boolean workingEnabled = true;

   public ManaAmplifierPartMachine(MetaMachineBlockEntity holder) {
      super(holder, 2, 1L, Long.MAX_VALUE);
      this.manaContainer = new ManaAmplifierPartMachine.ManaContainer(this);
      this.manaContainer.setAcceptDistributor(true);
   }

   @Override
   public Widget createUIWidget() {
      return ((WidgetGroup)super.createUIWidget()).addWidget(new LabelWidget(24, -16, () -> "gtocore.machine.mana_amplifier_part.max_mana"));
   }

   @Override
   public boolean hasModifyRecipeMethod() {
      return true;
   }

   @Override
   public GTRecipe modifyRecipe(IWorkableMultiController controller, RecipeHandlerUnit unit, @NotNull GTRecipe recipe) {
      if (this.workingEnabled && controller instanceof IOverclockMachine overclockMachine) {
         long voltage = overclockMachine.getOverclockVoltage();
         if (this.manaContainer.removeMana(voltage, 1, true) >= voltage) {
            recipe.perfect = true;
            this.manaContainer.removeMana(voltage, 1, false);
            return recipe;
         }
      }

      return null;
   }

   @Override
   public boolean canReceiveManaFromBursts() {
      return true;
   }

   @Override
   public boolean isWorkingEnabled() {
      return this.workingEnabled;
   }

   @Override
   public void setWorkingEnabled(boolean workingEnabled) {
      this.workingEnabled = workingEnabled;
   }

   @Generated
   public NotifiableManaContainer getManaContainer() {
      return this.manaContainer;
   }

   protected static final class ManaContainer extends NotifiableManaContainer {
      private ManaContainer(ManaAmplifierPartMachine machine) {
         super(machine, IO.IN, Long.MAX_VALUE);
      }

      @Override
      protected long extractionRate() {
         return this.getMaxMana() - this.getCurrentMana();
      }

      @Override
      public long getMaxMana() {
         return ((ManaAmplifierPartMachine)this.machine).getCurrent();
      }
   }
}
