package com.gtolib.api.machine.multiblock;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.data.lang.LangHandler;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.gtolib.api.machine.MultiblockDefinition;
import com.gtolib.api.machine.feature.IOverclockConfigMachine;
import com.gtolib.api.machine.feature.IPowerAmplifierMachine;
import com.gtolib.api.machine.feature.IUpgradeMachine;
import com.gtolib.api.machine.feature.multiblock.IEnhancedMultiblockMachine;
import com.gtolib.api.machine.trait.MultiblockTrait;
import com.gtolib.utils.MachineUtils;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ElectricMultiblockMachine
   extends WorkableElectricMultiblockMachine
   implements IEnhancedMultiblockMachine,
   IOverclockConfigMachine,
   IUpgradeMachine,
   IPowerAmplifierMachine {
   private final List<MultiblockTrait> multiblockTraits = new ArrayList<>();
   protected MultiblockTrait[] mtraits;
   @SaveToDisk(defaultValue = "0")
   protected long energyBuffer;
   protected long maxEnergyBuffer;

   public ElectricMultiblockMachine(MetaMachineBlockEntity holder) {
      super(holder);
   }

   @Override
   public boolean useEnergy(long eu, boolean simulate) {
      if (eu < 0L) {
         return this.generateEnergy(-eu, simulate);
      }

      if (simulate) {
         return this.energyBuffer + this.energyContainer.getEnergyStored() >= eu;
      }

      if (this.energyBuffer < eu) {
         this.energyBuffer = this.energyBuffer - this.energyContainer.changeEnergy(this.maxEnergyBuffer);
         if (this.energyBuffer < eu) {
            return false;
         }
      }

      this.energyBuffer -= eu;
      return true;
   }

   @Override
   public void onLoad() {
      super.onLoad();
      this.mtraits = this.multiblockTraits.toArray(new MultiblockTrait[0]);
   }

   @Nullable
   @Override
   public GTRecipe fullModifyRecipe(@NotNull RecipeHandlerUnit unit, @NotNull GTRecipeDefinition definition) {
      GTRecipe recipe = this.getModifyRecipe(unit, definition);
      if (recipe == null) {
         return null;
      }

      for (MultiblockTrait trait : this.mtraits) {
         recipe = trait.modifyRecipe(unit, recipe);
         if (recipe == null) {
            return null;
         }
      }

      return super.doModifyRecipe(unit, recipe);
   }

   @Override
   public void afterWorking() {
      for (MultiblockTrait trait : this.mtraits) {
         trait.afterWorking();
      }

      super.afterWorking();
   }

   @MustBeInvokedByOverriders
   @Override
   public void onPartScan(@NotNull IMultiPart part) {
      for (MultiblockTrait trait : this.mtraits) {
         trait.onPartScan(part);
      }
   }

   @MustBeInvokedByOverriders
   @Override
   public void onStructureFormed() {
      this.mtraits = this.multiblockTraits.toArray(new MultiblockTrait[0]);
      super.onStructureFormed();

      for (MultiblockTrait trait : this.mtraits) {
         trait.onStructureFormed();
      }

      this.maxEnergyBuffer = this.getOverclockVoltage() * 10L;
      if (this.maxEnergyBuffer < 0L) {
         this.maxEnergyBuffer = Long.MIN_VALUE;
      } else {
         this.maxEnergyBuffer = -this.maxEnergyBuffer;
      }
   }

   @MustBeInvokedByOverriders
   @Override
   public void onStructureInvalid() {
      super.onStructureInvalid();
      this.maxEnergyBuffer = 0L;

      for (MultiblockTrait trait : this.mtraits) {
         trait.onStructureInvalid();
      }
   }

   @Override
   public void addDisplayText(@NotNull List<Component> textList) {
      MachineUtils.addMachineText(textList, this, this::customText);

      for (IMultiPart part : this.getParts()) {
         part.addMultiText(textList);
      }
   }

   @Override
   public void customText(@NotNull List<Component> textList) {
      textList.add(
         Component.translatable("gtceu.gui.multiblock_no_voiding.0")
            .append(": ")
            .append(ComponentPanelWidget.withButton(LangHandler.getFromMultiLang(this.getVoidingMode().getSerializedName(), 1), "voidingMode"))
      );
      IEnhancedMultiblockMachine.super.customText(textList);
   }

   @Override
   public boolean gtolib$canUpgraded() {
      return ((MultiblockDefinition)this.definition).upgradable;
   }

   @Generated
   @Override
   public List<MultiblockTrait> getMultiblockTraits() {
      return this.multiblockTraits;
   }

   @Generated
   public MultiblockTrait[] getMtraits() {
      return this.mtraits;
   }

   @Generated
   public long getEnergyBuffer() {
      return this.energyBuffer;
   }

   @Generated
   public long getMaxEnergyBuffer() {
      return this.maxEnergyBuffer;
   }
}
