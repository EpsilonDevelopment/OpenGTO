package com.gtolib.api.machine.impl.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.WorkableTieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.SwitchWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import lombok.Generated;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;

public class VoidEnergyHatch extends WorkableTieredIOPartMachine {
   private final VoidEnergyHatch.VoidEnergyContainer energyContainer;
   @SaveToDisk(defaultValue = "0")
   private int simTier = 0;
   @SaveToDisk(defaultValue = "false")
   private boolean simLaser = false;

   public VoidEnergyHatch(MetaMachineBlockEntity holder) {
      super(holder, 14, IO.OUT);
      this.energyContainer = new VoidEnergyHatch.VoidEnergyContainer(this);
   }

   @Override
   public ModularUI createUI(Player entityPlayer) {
      return new ModularUI(140, 95, this, entityPlayer)
         .background(GuiTextures.BACKGROUND)
         .widget(new LabelWidget(7, 7, "tier"))
         .widget(new TextFieldWidget(9, 20, 122, 16, () -> String.valueOf(this.simTier), value -> {
            this.simTier = Integer.parseInt(value);
            this.getControllers().forEach(IMultiController::requestCheck);
         }).setNumbersOnly(0L, Long.MAX_VALUE))
         .widget(new LabelWidget(7, 42, "gtceu.creative.computation.average"))
         .widget(
            new SwitchWidget(9, 66, 122, 20, (clickData, value) -> {
                  this.simLaser = value;
                  this.getControllers().forEach(IMultiController::requestCheck);
               })
               .setSupplier(this::isSimLaser)
               .setTexture(
                  new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("gtocore.part_ability.output_energy")),
                  new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("gtocore.part_ability.output_laser"))
               )
         );
   }

   @Generated
   public VoidEnergyHatch.VoidEnergyContainer getEnergyContainer() {
      return this.energyContainer;
   }

   @Generated
   public int getSimTier() {
      return this.simTier;
   }

   @Generated
   public boolean isSimLaser() {
      return this.simLaser;
   }

   private static final class VoidEnergyContainer extends NotifiableRecipeHandlerTrait implements IEnergyContainer {
      public VoidEnergyContainer(MetaMachine machine) {
         super(machine);
      }

      @Override
      public long acceptEnergyFromNetwork(Object sender, Direction side, long voltage, long energyToAdd) {
         return 0L;
      }

      @Override
      public boolean inputsEnergy(Direction side) {
         return false;
      }

      @Override
      public long changeEnergy(long differenceAmount) {
         return differenceAmount;
      }

      @Override
      public long getEnergyStored() {
         return 325L;
      }

      @Override
      public long getEnergyCapacity() {
         return Long.MAX_VALUE;
      }

      @Override
      public long getInputAmperage() {
         return 0L;
      }

      @Override
      public long getInputVoltage() {
         return 0L;
      }

      @Override
      public long getOutputAmperage() {
         return 16777216L;
      }

      @Override
      public long getOutputVoltage() {
         return GTValues.V[14];
      }

      @Override
      public IO getHandlerIO() {
         return IO.OUT;
      }
   }
}
