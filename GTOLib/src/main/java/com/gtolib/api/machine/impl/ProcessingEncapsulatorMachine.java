package com.gtolib.api.machine.impl;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.handler.ICustomRecipeLogicHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gto.datasynclib.annotations.SyncToClient;
import com.gtocore.common.data.GTORecipeDataKeys;
import com.gtocore.common.data.GTORecipeTypes;
import com.gtolib.api.machine.feature.multiblock.IHighlightMachine;
import com.gtolib.api.machine.impl.part.MachineAccessTerminalPartMachine;
import com.gtolib.api.machine.multiblock.TierCasingMultiblockMachine;
import com.gtolib.utils.MachineUtils;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public final class ProcessingEncapsulatorMachine
   extends TierCasingMultiblockMachine
   implements IHighlightMachine,
   IDataStickInteractable,
   ICustomRecipeLogicHolder {
   @SyncToClient
   private final List<BlockPos> highlightPos = new ArrayList<>();
   private int moduleCount;
   int processingAmount;
   private final List<MachineAccessTerminalPartMachine> terminalPartMachine = new ArrayList<>(4);
   final Reference2IntOpenHashMap<GTRecipeType> typeMap = new Reference2IntOpenHashMap<>();
   public final ReferenceOpenHashSet<GTRecipeType> types = new ReferenceOpenHashSet<>();

   public ProcessingEncapsulatorMachine(MetaMachineBlockEntity holder) {
      super(holder, GTORecipeDataKeys.INTEGRAL_FRAMEWORK_TIER, GTORecipeDataKeys.GLASS_TIER);
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
   public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
      super.attachConfigurators(configuratorPanel);
      this.attachHighlightConfigurators(configuratorPanel);
   }

   @Override
   public void onPartScan(@NotNull IMultiPart part) {
      super.onPartScan(part);
      if (part instanceof MachineAccessTerminalPartMachine accessTerminalPartMachine) {
         this.terminalPartMachine.add(accessTerminalPartMachine);
         this.traitSubscriptions.add(accessTerminalPartMachine.getInventory().addChangedListener(this::updateMap));
      }
   }

   @Override
   protected void onStructureFormedAfter() {
      super.onStructureFormedAfter();
      this.update(true);
   }

   @Override
   public void onStructureFormed() {
      this.terminalPartMachine.clear();
      super.onStructureFormed();
      this.highlightPos.clear();
      BlockPos centerPosition = MachineUtils.getOffsetPos(26, 0, this.getFrontFacing(), this.getPos());

      for (int i = 3; i < 34; i += 6) {
         for (int j = -1; j < 2; j += 2) {
            int y = i * j;
            this.highlightPos.add(centerPosition.offset(22, y, 0));
            this.highlightPos.add(centerPosition.offset(-22, y, 0));
            this.highlightPos.add(centerPosition.offset(0, y, 22));
            this.highlightPos.add(centerPosition.offset(0, y, -22));
         }
      }

      for (int j = -1; j < 2; j += 2) {
         int y = 40 * j;
         this.highlightPos.add(centerPosition.offset(34, y, 5));
         this.highlightPos.add(centerPosition.offset(34, y, -5));
         this.highlightPos.add(centerPosition.offset(-34, y, 5));
         this.highlightPos.add(centerPosition.offset(-34, y, -5));
      }

      int glassTier = this.getCasingTier(GTORecipeDataKeys.GLASS_TIER);
      if (glassTier < 11) {
         this.processingAmount = 0;
      } else {
         this.processingAmount = 32 * (1 << this.getCasingTier(GTORecipeDataKeys.GLASS_TIER) - 11);
      }

      this.updateMap();
   }

   @Override
   public void onStructureInvalid() {
      super.onStructureInvalid();
      this.processingAmount = 0;
      this.highlightPos.clear();
      this.terminalPartMachine.clear();
   }

   @Override
   public void onWorking() {
      super.onWorking();
      this.update(false);
   }

   public void updateMap() {
      this.typeMap.clear();
      int ifTier = this.getCasingTier(GTORecipeDataKeys.INTEGRAL_FRAMEWORK_TIER);

      for (MachineAccessTerminalPartMachine part : this.terminalPartMachine) {
         NotifiableItemStackHandler inv = part.getInventory();

         for (int i = 0; i < inv.getSlots(); i++) {
            if (inv.getStackInSlot(i).getItem() instanceof MetaMachineItem metaMachineItem) {
               MachineDefinition definition = metaMachineItem.getDefinition();
               if (definition.getTier() <= ifTier && definition.getRecipeTypes().length > 0 && definition.getRecipeTypes()[0] != GTORecipeTypes.DUMMY_RECIPES) {
                  for (GTRecipeType type : definition.getRecipeTypes()) {
                     this.typeMap.put(type, definition.getTier());
                  }
               }
            }
         }
      }

      this.types.forEach(typex -> this.typeMap.put(typex, 14));
   }

   private void update(boolean promptly) {
      if (promptly || this.getOffsetTimer() % 40 == 0) {
         this.moduleCount = 0;
         Level level = this.getLevel();
         if (level == null) {
            return;
         }

         for (BlockPos blockPoss : this.highlightPos) {
            if (getMachine(level, blockPoss) instanceof EncapsulatorExecutionModuleMachine executionModuleMachine && executionModuleMachine.isFormed()) {
               if (executionModuleMachine.encapsulatorMachine != this) {
                  executionModuleMachine.getRecipeLogic().updateTickSubscription();
               }

               executionModuleMachine.encapsulatorMachine = this;
               this.moduleCount++;
            }
         }
      }
   }

   @Override
   public void customText(@NotNull List<Component> textList) {
      super.customText(textList);
      this.update(false);
      textList.add(
         Component.translatable("cover.advanced_energy_detector.max").append(Component.translatable("gtocore.tooltip.item.craft_step", this.processingAmount))
      );
      textList.add(Component.translatable("gtocore.machine.module", this.moduleCount));
   }

   @Override
   public void addDisplayText(@NotNull List<Component> textList) {
      super.addDisplayText(textList);
      textList.add(Component.translatable("gtceu.machine.available_recipe_map_1.tooltip", ""));
      this.typeMap.keySet().forEach(t -> textList.add(Component.translatable(t.registryName.toLanguageKey())));
   }

   @Override
   public List<BlockPos> getHighlightPos() {
      return this.highlightPos;
   }

   @Override
   public InteractionResult onDataStickUse(Player player, ItemStack dataStick) {
      if (this.isRemote()) {
         return InteractionResult.sidedSuccess(true);
      }

      CompoundTag tag = dataStick.getOrCreateTag();
      tag.putLong("pos", this.getPos().asLong());
      dataStick.setHoverName(
         Component.translatable("gtceu.machine.me.import_part.data_stick.name", Component.translatable(this.getDefinition().getDescriptionId()))
      );
      return InteractionResult.SUCCESS;
   }

   @Override
   public GTRecipeDefinition createCustomRecipe(RecipeHandlerUnit unit) {
      return this.getTier() > 11 ? this.getRecipeBuilder().duration(400).EUt(GTValues.VA[this.getTier()]).build() : null;
   }
}
