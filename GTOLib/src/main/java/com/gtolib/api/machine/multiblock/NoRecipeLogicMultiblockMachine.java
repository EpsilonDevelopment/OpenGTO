package com.gtolib.api.machine.multiblock;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.gui.fancy.TooltipsPanel;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton.Toggle;
import com.gregtechceu.gtceu.api.gui.widget.CustomComponentPanelWidget;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.gtolib.api.machine.feature.multiblock.IMultiblockTraitHolder;
import com.gtolib.api.machine.trait.MultiblockTrait;
import com.gtolib.utils.MachineUtils;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.Generated;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.HoverEvent.Action;
import net.minecraft.world.entity.player.Player;

public class NoRecipeLogicMultiblockMachine
   extends MultiblockControllerMachine
   implements IFancyUIMachine,
   IDisplayUIMachine,
   IMultiblockTraitHolder,
   IControllable {
   private final List<MultiblockTrait> multiblockTraits = new ArrayList<>();
   @SaveToDisk(defaultValue = "true")
   private boolean enabled = true;

   public NoRecipeLogicMultiblockMachine(MetaMachineBlockEntity holder) {
      super(holder);
   }

   @Override
   public boolean hasCheckButton() {
      return true;
   }

   @Override
   public void onStructureFormed() {
      super.onStructureFormed();

      for (IMultiPart part : this.getParts()) {
         this.multiblockTraits.forEach(trait -> trait.onPartScan(part));
         this.onPartScan(part);
      }

      this.multiblockTraits.forEach(MultiblockTrait::onStructureFormed);
   }

   @Override
   public void onStructureInvalid() {
      super.onStructureInvalid();
      this.multiblockTraits.forEach(MultiblockTrait::onStructureInvalid);
   }

   @Override
   public void addDisplayText(List<Component> textList) {
      if (!this.isRemote()) {
         if (this.isFormed()) {
            this.customText(textList);
         } else {
            MutableComponent base = Component.translatable("gtceu.multiblock.invalid_structure").withStyle(ChatFormatting.RED);
            Component hover = Component.translatable("gtceu.multiblock.invalid_structure.tooltip").withStyle(ChatFormatting.GRAY);
            textList.add(base.withStyle(style -> style.withHoverEvent(new HoverEvent(Action.SHOW_TEXT, hover))));
         }

         IDisplayUIMachine.super.addDisplayText(textList);
      }
   }

   @Override
   public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
      configuratorPanel.attachConfigurators(
         new Toggle(
               GuiTextures.BUTTON_POWER.getSubTexture(0.0, 0.0, 1.0, 0.5),
               GuiTextures.BUTTON_POWER.getSubTexture(0.0, 0.5, 1.0, 0.5),
               this::isWorkingEnabled,
               (clickData, pressed) -> this.setWorkingEnabled(pressed)
            )
            .setTooltipsSupplier(pressed -> List.of(Component.translatable(pressed ? "behaviour.soft_hammer.enabled" : "behaviour.soft_hammer.disabled")))
      );
      MachineUtils.attachStructureCheckConfigurators(configuratorPanel, this);
   }

   @Override
   public Widget createUIWidget() {
      WidgetGroup group = new WidgetGroup(0, 0, 190, 125);
      group.addWidget(
         new DraggableScrollableWidgetGroup(4, 4, 182, 117)
            .setBackground(this.getScreenTexture())
            .addWidget(new LabelWidget(4, 5, this.self().getBlockState().getBlock().getDescriptionId()))
            .addWidget(
               new CustomComponentPanelWidget(4, 17)
                  .setTextDataReader(this::readClientTextData)
                  .setTextDataWriter(this::writeClientTextData)
                  .textSupplier(Objects.requireNonNull(this.getLevel()).isClientSide ? null : this::addDisplayText)
                  .setMaxWidthLimit(200)
                  .clickHandler(this::handleDisplayClick)
            )
      );
      group.setBackground(GuiTextures.BACKGROUND_INVERSE);
      return group;
   }

   @Override
   public ModularUI createUI(Player entityPlayer) {
      return new ModularUI(198, 208, this, entityPlayer).widget(new FancyMachineUIWidget(this, 198, 208));
   }

   @Override
   public List<IFancyUIProvider> getSubTabs() {
      return Arrays.stream(this.getParts()).filter(Objects::nonNull).map(IFancyUIProvider.class::cast).toList();
   }

   @Override
   public void attachTooltips(TooltipsPanel tooltipsPanel) {
      for (IMultiPart part : this.getParts()) {
         part.attachFancyTooltipsToController(this, tooltipsPanel);
      }
   }

   public void onPartScan(IMultiPart part) {
   }

   @Override
   public boolean isWorkingEnabled() {
      return this.enabled;
   }

   @Override
   public void setWorkingEnabled(boolean isWorkingAllowed) {
      this.enabled = isWorkingAllowed;
   }

   @Generated
   @Override
   public List<MultiblockTrait> getMultiblockTraits() {
      return this.multiblockTraits;
   }

   @Generated
   public boolean isEnabled() {
      return this.enabled;
   }
}
