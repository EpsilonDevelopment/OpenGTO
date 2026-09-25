package com.gtolib.api.machine;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.editor.EditableMachineUI;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfigurator;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton.Toggle;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.TieredMachine;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.CircuitFancyConfigurator;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputBoth;
import com.gregtechceu.gtceu.api.machine.feature.ICleanroomProvider;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.feature.IMufflableMachine;
import com.gregtechceu.gtceu.api.machine.feature.IVoidable;
import com.gregtechceu.gtceu.api.machine.feature.IVoidable.VoidingMode;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IInputLimitableMachine;
import com.gregtechceu.gtceu.api.machine.trait.IRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.IRecipeHandler;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.info.FluidRecipeInfo;
import com.gregtechceu.gtceu.api.recipe.info.ItemRecipeInfo;
import com.gregtechceu.gtceu.api.recipe.info.RecipeInfo;
import com.gregtechceu.gtceu.api.recipe.ui.GTRecipeTypeUI.RecipeHolder;
import com.gregtechceu.gtceu.utils.TaskHandler;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.gto.datasynclib.datastream.DataComponentMap;
import com.gtocore.common.machine.multiblock.part.ProgrammableHatchPartMachine;
import com.gtocore.common.machine.multiblock.part.ProgrammableHatchPartMachine.ProgrammableCircuitHandler;
import com.gtolib.api.machine.feature.IEnhancedRecipeLogicMachine;
import com.hepdd.gtmthings.api.machine.IProgrammableMachine;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.utils.Position;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import javax.annotation.ParametersAreNonnullByDefault;
import lombok.Generated;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SimpleNoEnergyMachine
   extends TieredMachine
   implements IEnhancedRecipeLogicMachine,
   IMachineLife,
   IMufflableMachine,
   IAutoOutputBoth,
   IFancyUIMachine,
   IProgrammableMachine,
   IInputLimitableMachine {
   @SaveToDisk
   @SyncToClient(notifyUpdate = true)
   protected Direction outputFacingItems;
   @SaveToDisk
   @SyncToClient(notifyUpdate = true)
   protected Direction outputFacingFluids;
   @SaveToDisk(defaultValue = "false")
   @SyncToClient(notifyUpdate = true)
   protected boolean autoOutputItems;
   @SaveToDisk(defaultValue = "false")
   @SyncToClient(notifyUpdate = true)
   protected boolean autoOutputFluids;
   @SaveToDisk(defaultValue = "false")
   protected boolean allowInputFromOutputSideItems;
   @SaveToDisk(defaultValue = "false")
   protected boolean allowInputFromOutputSideFluids;
   @SaveToDisk
   protected final NotifiableItemStackHandler circuitInventory;
   @Nullable
   protected TickableSubscription autoOutputSubs;
   @Nullable
   protected ISubscription exportItemSubs;
   @Nullable
   protected ISubscription exportFluidSubs;
   @SaveToDisk
   @SyncToClient
   protected final RecipeLogic recipeLogic;
   protected GTRecipeType[] availableRecipeTypesCache;
   @SaveToDisk(defaultValue = "0")
   protected int activeRecipeType;
   protected final Int2IntFunction tankScalingFunction;
   @Nullable
   protected ICleanroomProvider cleanroom;
   @SaveToDisk
   protected final NotifiableItemStackHandler importItems;
   @SaveToDisk
   protected final NotifiableItemStackHandler exportItems;
   @SaveToDisk
   protected final NotifiableFluidTank importFluids;
   @SaveToDisk
   protected final NotifiableFluidTank exportFluids;
   protected final Map<IO, List<RecipeHandlerUnit>> capabilitiesProxy;
   protected final Map<IO, List<IRecipeHandler>> capabilitiesFlat;
   protected final List<ISubscription> traitSubscriptions;
   @SaveToDisk(defaultValue = "false")
   @SyncToClient
   protected boolean isMuffled;
   @SaveToDisk(defaultValue = "VOID_NONE")
   @SyncToClient
   protected VoidingMode voidingMode = VoidingMode.VOID_NONE;
   protected boolean isProgrammable;
   public static final BiFunction<ResourceLocation, GTRecipeType, EditableMachineUI> EDITABLE_UI_CREATOR = Util.memoize(
      (path, recipeType) -> new EditableMachineUI(
         "simple",
         path,
         () -> {
            WidgetGroup template = recipeType.getRecipeUI().createEditableUITemplate(false, false).createDefault();
            WidgetGroup group = new WidgetGroup(0, 0, template.getSize().width, Math.max(template.getSize().height, 78));
            template.setSelfPosition(new Position(0, (group.getSize().height - template.getSize().height) / 2));
            group.addWidget(template);
            return group;
         },
         (template, machine) -> {
            if (machine instanceof SimpleNoEnergyMachine magicMachine) {
               Table<IO, RecipeInfo, Object> storages = Tables.newCustomTable(new EnumMap<>(IO.class), LinkedHashMap::new);
               storages.put(IO.IN, ItemRecipeInfo.INSTANCE, magicMachine.importItems.storage);
               storages.put(IO.OUT, ItemRecipeInfo.INSTANCE, magicMachine.exportItems.storage);
               storages.put(IO.IN, FluidRecipeInfo.INSTANCE, magicMachine.importFluids);
               storages.put(IO.OUT, FluidRecipeInfo.INSTANCE, magicMachine.exportFluids);
               magicMachine.getRecipeType()
                  .getRecipeUI()
                  .createEditableUITemplate(false, false)
                  .setupUI(
                     template,
                     new RecipeHolder(magicMachine.recipeLogic::getProgressPercent, storages, new DataComponentMap(), Collections.emptyList(), false, false)
                  );
            }
         }
      )
   );

   public SimpleNoEnergyMachine(MetaMachineBlockEntity holder, int tier, Int2IntFunction tankScalingFunction, Object... args) {
      super(holder, tier);
      this.tankScalingFunction = tankScalingFunction;
      this.capabilitiesProxy = new EnumMap<>(IO.class);
      this.capabilitiesFlat = new EnumMap<>(IO.class);
      this.traitSubscriptions = new ArrayList<>();
      this.recipeLogic = this.createRecipeLogic();
      this.importItems = this.createImportItemHandler();
      this.exportItems = this.createExportItemHandler();
      this.importFluids = this.createImportFluidHandler();
      this.exportFluids = this.createExportFluidHandler();
      this.outputFacingItems = this.hasFrontFacing() ? this.getFrontFacing().getOpposite() : Direction.UP;
      this.outputFacingFluids = this.outputFacingItems;
      this.circuitInventory = this.createCircuitItemHandler();
   }

   private NotifiableItemStackHandler createCircuitItemHandler() {
      return new ProgrammableCircuitHandler(this);
   }

   private NotifiableItemStackHandler createImportItemHandler() {
      NotifiableItemStackHandler handler = new NotifiableItemStackHandler(this, this.getRecipeType().getMaxInputs(ItemRecipeInfo.INSTANCE), IO.IN)
         .setFilter(i -> !ProgrammableHatchPartMachine.isConfiguredVirtualProvider(i));
      if (handler.storage.size == 0) {
         handler.setAvailable(false);
      }

      return handler;
   }

   private NotifiableItemStackHandler createExportItemHandler() {
      NotifiableItemStackHandler handler = new NotifiableItemStackHandler(this, this.getRecipeType().getMaxOutputs(ItemRecipeInfo.INSTANCE), IO.OUT);
      if (handler.storage.size == 0) {
         handler.setAvailable(false);
      }

      return handler;
   }

   private NotifiableFluidTank createImportFluidHandler() {
      NotifiableFluidTank handler = new NotifiableFluidTank(
         this, this.getRecipeType().getMaxInputs(FluidRecipeInfo.INSTANCE), this.tankScalingFunction.apply(this.getTier()), IO.IN
      );
      if (handler.getStorages().length == 0) {
         handler.setAvailable(false);
      }

      return handler;
   }

   private NotifiableFluidTank createExportFluidHandler() {
      NotifiableFluidTank handler = new NotifiableFluidTank(
         this, this.getRecipeType().getMaxOutputs(FluidRecipeInfo.INSTANCE), this.tankScalingFunction.apply(this.getTier()), IO.OUT
      );
      if (handler.getStorages().length == 0) {
         handler.setAvailable(false);
      }

      return handler;
   }

   @Override
   public void onLoad() {
      super.onLoad();
      Map<IO, List<IRecipeHandler>> ioTraits = new EnumMap<>(IO.class);

      for (MachineTrait trait : this.getTraits()) {
         if (trait instanceof IRecipeHandlerTrait handlerTrait && handlerTrait.isAvailable() && handlerTrait.getHandlerIO() != IO.NONE) {
            ioTraits.computeIfAbsent(handlerTrait.getHandlerIO(), i -> new ArrayList<>()).add(handlerTrait);
         }
      }

      for (Entry<IO, List<IRecipeHandler>> entry : ioTraits.entrySet()) {
         RecipeHandlerUnit handlerList = RecipeHandlerUnit.of(entry.getKey(), entry.getValue());
         this.addHandlerList(handlerList);
         this.traitSubscriptions.add(handlerList.subscribe(this.recipeLogic::updateTickSubscription));
      }

      if (!this.isRemote()) {
         if (this.getLevel() instanceof ServerLevel serverLevel) {
            TaskHandler.enqueueTask(serverLevel, this::updateAutoOutputSubscription);
         }

         this.exportItemSubs = this.exportItems.addChangedListener(this::updateAutoOutputSubscription);
         this.exportFluidSubs = this.exportFluids.addChangedListener(this::updateAutoOutputSubscription);
      }
   }

   @Override
   public void onUnload() {
      super.onUnload();
      this.traitSubscriptions.forEach(ISubscription::unsubscribe);
      this.traitSubscriptions.clear();
      this.capabilitiesProxy.clear();
      this.capabilitiesFlat.clear();
      if (this.exportItemSubs != null) {
         this.exportItemSubs.unsubscribe();
         this.exportItemSubs = null;
      }

      if (this.exportFluidSubs != null) {
         this.exportFluidSubs.unsubscribe();
         this.exportFluidSubs = null;
      }
   }

   @Override
   public void onMachineRemoved() {
      this.clearInventory(this.importItems.storage);
      this.clearInventory(this.exportItems.storage);
   }

   @Nullable
   @Override
   public ICleanroomProvider getCleanroom() {
      return this.cleanroom;
   }

   @Override
   public void setCleanroom(@Nullable ICleanroomProvider cleanroom) {
      this.cleanroom = cleanroom;
   }

   @Override
   public boolean isMuffled() {
      return this.isMuffled;
   }

   @Override
   public void setMuffled(boolean isMuffled) {
      this.isMuffled = isMuffled;
   }

   @Override
   public boolean hasAutoOutputFluid() {
      return this.exportFluids.getTanks() > 0;
   }

   @Override
   public boolean hasAutoOutputItem() {
      return this.exportItems.getSlots() > 0;
   }

   @Nullable
   @Override
   public Direction getOutputFacingFluids() {
      return this.hasAutoOutputFluid() ? this.outputFacingFluids : null;
   }

   @Nullable
   @Override
   public Direction getOutputFacingItems() {
      return this.hasAutoOutputItem() ? this.outputFacingItems : null;
   }

   @Override
   public void setAutoOutputItems(boolean allow) {
      if (this.hasAutoOutputItem()) {
         this.autoOutputItems = allow;
         this.updateAutoOutputSubscription();
      }
   }

   @Override
   public void setAutoOutputFluids(boolean allow) {
      if (this.hasAutoOutputFluid()) {
         this.autoOutputFluids = allow;
         this.updateAutoOutputSubscription();
      }
   }

   @Override
   public void setOutputFacingFluids(@Nullable Direction outputFacing) {
      if (this.hasAutoOutputFluid()) {
         this.clearDirectionCache();
         this.outputFacingFluids = outputFacing;
         this.updateAutoOutputSubscription();
      }
   }

   @Override
   public void setOutputFacingItems(@Nullable Direction outputFacing) {
      if (this.hasAutoOutputItem()) {
         this.clearDirectionCache();
         this.outputFacingItems = outputFacing;
         this.updateAutoOutputSubscription();
      }
   }

   @Override
   public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
      super.onNeighborChanged(block, fromPos, isMoving);
      this.updateAutoOutputSubscription();
   }

   private void updateAutoOutputSubscription() {
      if (this.getLevel() != null) {
         Direction outputFacingItems = this.getOutputFacingItems();
         Direction outputFacingFluids = this.getOutputFacingFluids();
         if ((
               !this.autoOutputItems
                  || this.exportItems.isEmpty()
                  || outputFacingItems == null
                  || !this.holder.blockEntityDirectionCache.hasAdjacentItemHandler(this.getLevel(), this.getPos(), outputFacingItems)
            )
            && (
               !this.autoOutputFluids
                  || this.exportFluids.isEmpty()
                  || outputFacingFluids == null
                  || !this.holder.blockEntityDirectionCache.hasAdjacentFluidHandler(this.getLevel(), this.getPos(), outputFacingFluids)
            )) {
            if (this.autoOutputSubs != null) {
               this.autoOutputSubs.unsubscribe();
               this.autoOutputSubs = null;
            }
         } else {
            this.autoOutputSubs = this.subscribeServerTick(this.autoOutputSubs, this::autoOutput, 20);
         }
      }
   }

   private void autoOutput() {
      if (this.autoOutputFluids && this.getOutputFacingFluids() != null) {
         this.exportFluids.exportToNearby(this.getOutputFacingFluids());
      }

      if (this.autoOutputItems && this.getOutputFacingItems() != null) {
         this.exportItems.exportToNearby(this.getOutputFacingItems());
      }

      this.updateAutoOutputSubscription();
   }

   @Override
   public boolean isFacingValid(Direction facing) {
      return facing != this.getOutputFacingItems() && facing != this.getOutputFacingFluids() ? super.isFacingValid(facing) : false;
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
      IVoidable.attachConfigurators(configuratorPanel, this);
      IInputLimitableMachine.super.attachConfigurators(configuratorPanel);
      configuratorPanel.attachConfigurators(new CircuitFancyConfigurator(this.circuitInventory.storage));

      for (Direction direction : Direction.values()) {
         if (this.self().getCoverContainer().hasCover(direction)) {
            IFancyConfigurator configurator = this.self().getCoverContainer().getCoverAtSide(direction).getConfigurator();
            if (configurator != null) {
               configuratorPanel.attachConfigurators(configurator);
            }
         }
      }
   }

   @Override
   public ResourceTexture sideTips(Player player, BlockPos pos, BlockState state, Set<GTToolType> toolTypes, Direction side) {
      if (!toolTypes.contains(GTToolType.WRENCH) || player.isShiftKeyDown() || this.hasFrontFacing() && side == this.getFrontFacing()) {
         return !toolTypes.contains(GTToolType.SCREWDRIVER) || side != this.getOutputFacingItems() && side != this.getOutputFacingFluids()
            ? super.sideTips(player, pos, state, toolTypes, side)
            : GuiTextures.TOOL_ALLOW_INPUT;
      } else {
         return GuiTextures.TOOL_IO_FACING_ROTATION;
      }
   }

   @Override
   public void setAllowInputFromOutputSideItems(boolean allowInputFromOutputSideItems) {
      this.clearDirectionCache();
      this.allowInputFromOutputSideItems = allowInputFromOutputSideItems;
   }

   @Override
   public void setAllowInputFromOutputSideFluids(boolean allowInputFromOutputSideFluids) {
      this.clearDirectionCache();
      this.allowInputFromOutputSideFluids = allowInputFromOutputSideFluids;
   }

   @Override
   public void setVoidingMode(VoidingMode mode) {
      this.voidingMode = mode;
   }

   @Override
   public VoidingMode getVoidingMode() {
      return this.voidingMode;
   }

   public boolean isProgrammable() {
      return this.isProgrammable;
   }

   public void setProgrammable(boolean programmable) {
      this.isProgrammable = programmable;
   }

   @Override
   public boolean isInputLimit() {
      return this.importItems.storage.isInputLimited;
   }

   @Override
   public void setInputLimit(boolean inputLimit) {
      this.importItems.storage.isInputLimited = inputLimit;
   }

   @Generated
   @Override
   public boolean isAutoOutputItems() {
      return this.autoOutputItems;
   }

   @Generated
   @Override
   public boolean isAutoOutputFluids() {
      return this.autoOutputFluids;
   }

   @Generated
   @Override
   public boolean isAllowInputFromOutputSideItems() {
      return this.allowInputFromOutputSideItems;
   }

   @Generated
   @Override
   public boolean isAllowInputFromOutputSideFluids() {
      return this.allowInputFromOutputSideFluids;
   }

   @Generated
   public NotifiableItemStackHandler getCircuitInventory() {
      return this.circuitInventory;
   }

   @Generated
   @Override
   public RecipeLogic getRecipeLogic() {
      return this.recipeLogic;
   }

   @Generated
   @Override
   public void setAvailableRecipeTypesCache(GTRecipeType[] availableRecipeTypesCache) {
      this.availableRecipeTypesCache = availableRecipeTypesCache;
   }

   @Generated
   @Override
   public GTRecipeType[] getAvailableRecipeTypesCache() {
      return this.availableRecipeTypesCache;
   }

   @Generated
   @Override
   public void setActiveRecipeType(int activeRecipeType) {
      this.activeRecipeType = activeRecipeType;
   }

   @Generated
   @Override
   public int getActiveRecipeType() {
      return this.activeRecipeType;
   }

   @Generated
   public Int2IntFunction getTankScalingFunction() {
      return this.tankScalingFunction;
   }

   @Generated
   @Override
   public Map<IO, List<RecipeHandlerUnit>> getCapabilitiesProxy() {
      return this.capabilitiesProxy;
   }

   @Generated
   @Override
   public Map<IO, List<IRecipeHandler>> getCapabilitiesFlat() {
      return this.capabilitiesFlat;
   }
}
