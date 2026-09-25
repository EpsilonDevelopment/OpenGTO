package com.gtolib.api.machine.multiblock;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfigurator;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.gui.fancy.TooltipsPanel;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton.Toggle;
import com.gregtechceu.gtceu.api.gui.widget.CustomComponentPanelWidget;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IVoidable.VoidingMode;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.data.lang.LangHandler;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.gtolib.api.machine.feature.multiblock.IEnhancedMultiblockMachine;
import com.gtolib.api.machine.trait.MultiblockTrait;
import com.gtolib.utils.MachineUtils;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import lombok.Generated;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class NoEnergyMultiblockMachine extends WorkableMultiblockMachine implements IFancyUIMachine, IDisplayUIMachine, IEnhancedMultiblockMachine {
   private final List<MultiblockTrait> multiblockTraits = new ArrayList<>();
   protected MultiblockTrait[] mtraits;
   @SaveToDisk(defaultValue = "VOID_NONE")
   protected VoidingMode voidingMode = VoidingMode.VOID_NONE;
   @SaveToDisk(defaultValue = "false")
   protected boolean batchEnabled;

   public NoEnergyMultiblockMachine(MetaMachineBlockEntity holder) {
      super(holder);
   }

   @Override
   public void onLoad() {
      super.onLoad();
      this.mtraits = this.multiblockTraits.toArray(new MultiblockTrait[0]);
   }

   @Override
   public boolean hasCheckButton() {
      return true;
   }

   @Nullable
   @Override
   public GTRecipe fullModifyRecipe(RecipeHandlerUnit unit, GTRecipeDefinition definition) {
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
   public void onPartScan(IMultiPart part) {
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
   }

   @MustBeInvokedByOverriders
   @Override
   public void onStructureInvalid() {
      super.onStructureInvalid();

      for (MultiblockTrait trait : this.mtraits) {
         trait.onStructureInvalid();
      }
   }

   @Override
   public void addDisplayText(List<Component> textList) {
      MachineUtils.addMachineText(textList, this, this::customText);
      IDisplayUIMachine.super.addDisplayText(textList);
   }

   @Override
   public void customText(List<Component> textList) {
      textList.add(
         Component.translatable("gtceu.gui.multiblock_no_voiding.0")
            .append(": ")
            .append(ComponentPanelWidget.withButton(LangHandler.getFromMultiLang(this.getVoidingMode().getSerializedName(), 1), "voidingMode"))
      );
      IEnhancedMultiblockMachine.super.customText(textList);
   }

   @Override
   public void handleDisplayClick(String componentData, ClickData clickData) {
      if (!clickData.isRemote && componentData.equals("voidingMode")) {
         if (this.voidingMode.ordinal() + 1 < VoidingMode.VALUES.length) {
            this.voidingMode = VoidingMode.VALUES[this.voidingMode.ordinal() + 1];
         } else {
            this.voidingMode = VoidingMode.VALUES[0];
         }
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
      if (this.hasBatchConfig()) {
         MachineUtils.attachBatchConfigurators(configuratorPanel, this::isBatchEnabled, (clickData, pressed) -> this.batchEnabled = pressed);
      }

      MachineUtils.attachStructureCheckConfigurators(configuratorPanel, this);

      for (Direction direction : Direction.values()) {
         if (this.getCoverContainer().hasCover(direction)) {
            IFancyConfigurator configurator = this.getCoverContainer().getCoverAtSide(direction).getConfigurator();
            if (configurator != null) {
               configuratorPanel.attachConfigurators(configurator);
            }
         }
      }
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

   @Override
   public List<MultiblockTrait> getMultiblockTraits() {
      return this.multiblockTraits;
   }

   @Override
   public void setVoidingMode(VoidingMode mode) {
      this.voidingMode = mode;
   }

   @Override
   public VoidingMode getVoidingMode() {
      return this.voidingMode;
   }

   @Generated
   @Override
   public boolean isBatchEnabled() {
      return this.batchEnabled;
   }
}
