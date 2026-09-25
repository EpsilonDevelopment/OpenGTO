package com.gtolib.api.ae2.me2in1;

import appeng.api.config.Actionable;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.inventories.InternalInventory;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.client.gui.Icon;
import appeng.client.gui.me.common.Repo;
import appeng.core.definitions.AEItems;
import appeng.crafting.pattern.AECraftingPattern;
import appeng.helpers.IMenuCraftingPacket;
import appeng.helpers.InventoryAction;
import appeng.menu.SlotSemantics;
import appeng.menu.me.common.MEStorageMenu;
import appeng.menu.slot.FakeSlot;
import appeng.menu.slot.PatternTermSlot;
import appeng.menu.slot.RestrictedInputSlot;
import appeng.menu.slot.RestrictedInputSlot.PlacableItemType;
import appeng.util.ConfigInventory;
import appeng.util.ConfigMenuInventory;
import com.google.common.base.Preconditions;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gtocore.config.GTOConfig;
import com.gtolib.GTOCore;
import com.gtolib.api.ae2.me2in1.encoding.ExtendedEncodingMode;
import com.gtolib.api.ae2.pattern.GridPatternsRemover;
import com.gtolib.api.ae2.pattern.PatternUtils;
import com.gtolib.api.recipe.RecipeBuilder;
import com.gtolib.utils.AEChemicalHelper;
import com.gtolib.utils.ClientUtil;
import com.gtolib.utils.RLUtils;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Stream;
import lombok.Generated;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Unique;

public class ExtendedEncodingMenu extends MEStorageMenu implements IMenuCraftingPacket {
   private static final int CRAFTING_GRID_WIDTH = 3;
   private static final int CRAFTING_GRID_HEIGHT = 3;
   private static final int CRAFTING_GRID_SLOTS = 9;
   private static final String TITLE_ENABLED = "gtocore.pattern.recipeInfoButton.title.enabled";
   private static final String TITLE_DISABLED = "gtocore.pattern.recipeInfoButton.title.disabled";
   private static final String CLICK_TO_ENABLE = "gtocore.pattern.recipeInfoButton.clickToEnable";
   private static final String CLICK_TO_DISABLE = "gtocore.pattern.recipeInfoButton.clickToDisable";
   private static final String CLICK_TO_CLEAR = "gtocore.pattern.recipeInfoButton.clickToClear";
   private static final String ACTION_SET_MODE = "setMode";
   private static final String ACTION_ENCODE = "encode";
   private static final String ACTION_ENCODING_TO_PROVIDER = "encodingToProvider";
   private static final String ACTION_QUICK_MOVE_TO_PROVIDER = "quickMoveToProvider";
   private static final String ACTION_CLEAR = "clear";
   private static final String ACTION_MODIFY_PATTER = "modifyPatter";
   private static final String ACTION_CLEAR_FILTER = "clearFilter";
   private static final String ACTION_CLEAR_PATTERN = "clearPattern";
   private static final String ACTION_SET_SUBSTITUTION = "setSubstitution";
   private static final String ACTION_SET_FLUID_SUBSTITUTION = "setFluidSubstitution";
   private static final String ACTION_SET_AUTO_RENAMING = "setAutoRenaming";
   private static final String ACTION_SET_AUTO_SEARCH_PROVIDERS = "setAutoSearchProviders";
   private static final String ACTION_SET_STONECUTTING_RECIPE_ID = "setStonecuttingRecipeId";
   private static final String ACTION_CYCLE_PROCESSING_OUTPUT = "cycleProcessingOutput";
   private static final String ACTION_CYCLE_PROCESSING_INPUT = "cycleProcessingInput";
   private static final String ACTION_QUICK_REMOVE_PATTERN = "quickRemovePattern";
   public static final String ACTION_CLEAR_USED_VISIBLE_SLOT = "clearUsedVisibleSlot";
   public static final String ACTION_CLEAR_SEC_OUTPUT = "clearSecOutput";
   public static final String ACTION_ADD_RECIPE = "addRecipe";
   public static final String ACTION_ADD_UUID = "addUUID";
   public static final String ACTION_CLICK_RECIPE_INFO = "clickRecipeInfo";
   private final ExtendedEncodingLogic encodingLogic;
   private final Me2in1Menu menu;
   private final FakeSlot[] craftingGridSlots = new FakeSlot[9];
   private final PatternTermSlot craftOutputSlot;
   private final FakeSlot[] processingInputSlots = new FakeSlot[81];
   private final FakeSlot[] processingOutputSlots = new FakeSlot[27];
   private final FakeSlot stonecuttingInputSlot;
   private final FakeSlot smithingTableTemplateSlot;
   private final FakeSlot smithingTableBaseSlot;
   private final FakeSlot smithingTableAdditionSlot;
   private final FakeSlot[] materialSlots;
   private final FakeSlot quickRemovePatternFilterSlot;
   private final RestrictedInputSlot[] encodedPatternSlots;
   private final ConfigInventory encodedInputsInv;
   private final ConfigInventory encodedOutputsInv;
   private final MaterialConfigInventory materialsInv;
   private final ConfigInventory quickRemovePatternFilterInv;
   private CraftingRecipe currentRecipe;
   private ExtendedEncodingMode currentMode;
   private final List<StonecutterRecipe> stonecuttingRecipes = new ArrayList<>();
   private final IntSet usedVisibleSlotIds = new IntOpenHashSet();
   private String recipe;
   private UUID uuid;
   public IntSet slotsSupportingFluidSubstitution = new IntArraySet();

   public ExtendedEncodingMenu(Me2in1Menu menu, int id, Inventory ip, IExtendedPatternMenuHost host, boolean bindInventory) {
      super(menu.getType(), id, ip, host, bindInventory);
      this.menu = menu;
      this.encodingLogic = host.getLogic();
      this.encodedInputsInv = this.encodingLogic.getEncodedInputInv();
      this.encodedOutputsInv = this.encodingLogic.getEncodedOutputInv();
      this.materialsInv = this.encodingLogic.getMaterialInv();
      ConfigMenuInventory encodedInputs = this.encodedInputsInv.createMenuWrapper();
      ConfigMenuInventory encodedOutputs = this.encodedOutputsInv.createMenuWrapper();
      ConfigMenuInventory matSlots = this.materialsInv.createMenuWrapper();

      for (int i = 0; i < 9; i++) {
         FakeSlot slot = new FakeSlot(encodedInputs, i);
         slot.setHideAmount(true);
         menu.addSlot(this.craftingGridSlots[i] = slot, SlotSemantics.CRAFTING_GRID);
      }

      menu.addSlot(
         this.craftOutputSlot = new PatternTermSlot(ip.player, this.getActionSource(), this.powerSource, host.getInventory(), encodedInputs, this),
         SlotSemantics.CRAFTING_RESULT
      );
      this.craftOutputSlot.setIcon((Icon)null);

      for (int i = 0; i < this.processingInputSlots.length; i++) {
         menu.addSlot(this.processingInputSlots[i] = new FakeSlot(encodedInputs, i), SlotSemantics.PROCESSING_INPUTS);
      }

      for (int i = 0; i < this.processingOutputSlots.length; i++) {
         menu.addSlot(this.processingOutputSlots[i] = new FakeSlot(encodedOutputs, i), SlotSemantics.PROCESSING_OUTPUTS);
      }

      this.processingOutputSlots[0].setIcon(Icon.BACKGROUND_PRIMARY_OUTPUT);

      for (int i = 0; i < this.processingOutputSlots.length; i++) {
         menu.addSlot(this.processingOutputSlots[i] = new FakeSlot(encodedOutputs, i), SlotSemantics.PROCESSING_OUTPUTS);
      }

      this.materialSlots = new FakeSlot[this.materialsInv.size()];

      for (int i = 0; i < this.materialsInv.size(); i++) {
         menu.addSlot(this.materialSlots[i] = new FakeSlot(matSlots, i), GTOSlotSemantics.BATCH_FILTER);
         this.materialSlots[i]
            .setEmptyTooltip(
               () -> List.of(
                  Component.translatable("gtocore.ae.appeng.me2in1.material_slot"),
                  Component.translatable("gtocore.ae.appeng.me2in1.material_slot.1").withStyle(ChatFormatting.GRAY),
                  Component.translatable("gtocore.ae.appeng.me2in1.material_slot.2").withStyle(ChatFormatting.GRAY)
               )
            );
         this.materialSlots[i].setHideAmount(true);
      }

      InternalInventory inv = this.encodingLogic.getEncodedPatternInv();
      this.encodedPatternSlots = new RestrictedInputSlot[inv.size()];

      for (int i = 0; i < inv.size(); i++) {
         menu.addSlot(
            this.encodedPatternSlots[i] = new RestrictedInputSlot(PlacableItemType.ENCODED_PATTERN, this.encodingLogic.getEncodedPatternInv(), i),
            SlotSemantics.ENCODED_PATTERN
         );
         this.encodedPatternSlots[i].setStackLimit(1);
      }

      menu.addSlot(this.stonecuttingInputSlot = new FakeSlot(encodedInputs, 0), SlotSemantics.STONECUTTING_INPUT);
      this.stonecuttingInputSlot.setHideAmount(true);
      menu.addSlot(this.smithingTableTemplateSlot = new FakeSlot(encodedInputs, 0), SlotSemantics.SMITHING_TABLE_TEMPLATE);
      this.smithingTableTemplateSlot.setHideAmount(true);
      menu.addSlot(this.smithingTableBaseSlot = new FakeSlot(encodedInputs, 1), SlotSemantics.SMITHING_TABLE_BASE);
      this.smithingTableBaseSlot.setHideAmount(true);
      menu.addSlot(this.smithingTableAdditionSlot = new FakeSlot(encodedInputs, 2), SlotSemantics.SMITHING_TABLE_ADDITION);
      this.smithingTableAdditionSlot.setHideAmount(true);
      this.quickRemovePatternFilterInv = ConfigInventory.configTypes(1, null);
      menu.addSlot(this.quickRemovePatternFilterSlot = new FakeSlot(this.quickRemovePatternFilterInv.createMenuWrapper(), 0), SlotSemantics.CONFIG);
      this.quickRemovePatternFilterSlot.setHideAmount(true);
      menu.registerSubmenuAction("encode", Boolean.class, this::encode);
      menu.registerSubmenuAction("encodingToProvider", SeenProviderSlots.class, this::encodeAndTransferToProvider);
      menu.registerSubmenuAction("quickMoveToProvider", ExtendedEncodingMenu.SeenProviderSlotsWithSlotId.class, this::quickMoveToProvider);
      menu.registerSubmenuAction("setStonecuttingRecipeId", ResourceLocation.class, this.encodingLogic::setStonecuttingRecipeId);
      menu.registerSubmenuAction("clear", this::clear);
      menu.registerSubmenuAction("clearFilter", this::clearFilter);
      menu.registerSubmenuAction("clearPattern", this::clearPattern);
      menu.registerSubmenuAction("setMode", ExtendedEncodingMode.class, this.encodingLogic::setMode);
      menu.registerSubmenuAction("setSubstitution", Boolean.class, this.encodingLogic::setSubstitution);
      menu.registerSubmenuAction("setFluidSubstitution", Boolean.class, this.encodingLogic::setFluidSubstitution);
      menu.registerSubmenuAction("setAutoRenaming", Boolean.class, this.encodingLogic::setAutoEncodeRenamePatterns);
      menu.registerSubmenuAction("setAutoSearchProviders", Boolean.class, this.encodingLogic::setAutoSearchProviders);
      menu.registerSubmenuAction("cycleProcessingInput", this::cycleProcessingInput);
      menu.registerSubmenuAction("cycleProcessingOutput", this::cycleProcessingOutput);
      menu.registerSubmenuAction("modifyPatter", Integer.class, this::gtolib$modifyPatter);
      menu.registerSubmenuAction("quickRemovePattern", Boolean.class, this::quickRemovePattern);
      menu.registerSubmenuAction("clearUsedVisibleSlot", this.usedVisibleSlotIds::clear);
      menu.registerSubmenuAction("clearSecOutput", this::gtolib$clearSecOutput);
      menu.registerSubmenuAction("addRecipe", String.class, this::gtolib$addRecipe);
      menu.registerSubmenuAction("addUUID", UUID.class, this::gtolib$addUUID);
      menu.registerSubmenuAction("clickRecipeInfo", this::gtolib$clickRecipeInfo);
      this.updateStonecuttingRecipes();
   }

   public void gtolib$addRecipe(String id) {
      if (this.isClientSide()) {
         this.menu.sendSubmenuAction("addRecipe", id);
      } else {
         this.recipe = id;
      }
   }

   public void gtolib$addUUID(UUID id) {
      if (this.isClientSide()) {
         this.menu.sendSubmenuAction("addUUID", id);
      } else {
         this.uuid = id;
      }
   }

   public void gtolib$clickRecipeInfo() {
      if (this.isClientSide()) {
         this.menu.sendSubmenuAction("clickRecipeInfo");
      } else if (this.menu.isExtraInfoEnabled() && this.recipe != null && !this.recipe.isEmpty()) {
         this.recipe = null;
      } else {
         this.recipe = null;
         this.menu.setExtraInfoEnabled(!this.menu.isExtraInfoEnabled());
      }
   }

   public Component gtolib$getRecipeInfoTooltip() {
      MutableComponent title = Component.empty();
      title.append(
         this.menu.isExtraInfoEnabled()
            ? Component.translatable("gtocore.pattern.recipeInfoButton.title.enabled")
            : Component.translatable("gtocore.pattern.recipeInfoButton.title.disabled")
      );
      title.append("\n");
      if (!this.menu.isExtraInfoEnabled()) {
         return title.append(Component.translatable("gtocore.pattern.recipeInfoButton.clickToEnable"));
      } else if (this.recipe != null && !this.recipe.isEmpty()) {
         MutableComponent tooltip = Component.empty();
         tooltip.append(Component.translatable("gtocore.pattern.recipe")).append("\n");
         String key = RLUtils.parse(this.recipe.split("/")[0]).toLanguageKey();
         tooltip.append(Component.translatable("gtocore.pattern.type", Component.translatable(key))).append("\n");
         return title.append(tooltip.append(Component.translatable("gtocore.pattern.recipeInfoButton.clickToClear")));
      } else {
         return title.append(Component.translatable("gtocore.pattern.recipeInfoButton.clickToDisable"));
      }
   }

   @Unique
   public void gtolib$clearSecOutput() {
      if (this.isClientSide()) {
         this.menu.sendSubmenuAction("clearSecOutput");
      } else {
         for (int i = 1; i <= 8; i++) {
            this.encodedOutputsInv.setStack(i, null);
         }
      }
   }

   public void quickRemovePattern(boolean recursive) {
      if (this.isClientSide()) {
         this.menu.sendSubmenuAction("quickRemovePattern", recursive);
      } else if (this.quickRemovePatternFilterInv.getStack(0) != null && this.menu.getNetworkNode() != null) {
         List<GridPatternsRemover.PatternLocator> patternsToRemove = GridPatternsRemover.collectPatternToRemove(
            this.menu.getNetworkNode().getGrid(), this.getPlayerInventory().player.level(), this.quickRemovePatternFilterInv.getStack(0).what(), recursive
         );
         int size = patternsToRemove.size();
         if (size != 0) {
            AtomicInteger atomicWhich = new AtomicInteger(0);
            this.findEncodedPatternSlots(size, pattern -> pattern.isEmpty() || AEItems.BLANK_PATTERN.isSameAs(pattern)).forEach(slotId -> {
               GridPatternsRemover.PatternLocator encodedPatternWhere = patternsToRemove.get(atomicWhich.getAndIncrement());
               ItemStack encodedPatternWhat = encodedPatternWhere.container().getTerminalPatternInventory().getStackInSlot(encodedPatternWhere.slot());
               this.encodedPatternSlots[slotId].set(encodedPatternWhat);
               encodedPatternWhere.container().getTerminalPatternInventory().setItemDirect(encodedPatternWhere.slot(), ItemStack.EMPTY);
            });
            this.menu.markPatternChange();
         }
      }
   }

   public void quickMoveToProvider(ExtendedEncodingMenu.SeenProviderSlotsWithSlotId seenProviderSlotsWithSlotId) {
      if (this.isClientSide()) {
         this.menu.sendSubmenuAction("quickMoveToProvider", seenProviderSlotsWithSlotId);
      } else {
         int slotId = seenProviderSlotsWithSlotId.getSlotId();
         if (slotId >= 0 && slotId < this.menu.slots.size()) {
            ItemStack stackInSlot = this.menu.getSlot(slotId).getItem();
            if (PatternDetailsHelper.isEncodedPattern(stackInSlot)) {
               SeenProviderSlots seenProviderSlots = seenProviderSlotsWithSlotId.getSeenProviderSlots();
               List<Integer> visibleSlotsIds = seenProviderSlots.getMachineSlotIndex();
               if (visibleSlotsIds.isEmpty()) {
                  return;
               }

               List<Long> containerIds = seenProviderSlots.getMachineSlotServerId();
               Preconditions.checkArgument(visibleSlotsIds.size() == containerIds.size());

               for (int i = 0; i < visibleSlotsIds.size(); i++) {
                  if (this.usedVisibleSlotIds.add(visibleSlotsIds.get(i).intValue())) {
                     ItemStack carriedBefore = this.getCarried();
                     this.setCarried(stackInSlot);
                     this.menu.doPatternAction((ServerPlayer)this.getPlayer(), InventoryAction.PICKUP_OR_SET_DOWN, visibleSlotsIds.get(i), containerIds.get(i));
                     this.setCarried(carriedBefore);
                     this.menu.getSlot(slotId).set(ItemStack.EMPTY);
                     return;
                  }
               }
            }
         } else {
            if (GTOConfig.INSTANCE.devMode.aeLog) {
               GTOCore.LOGGER.warn("Invalid slot ID: {} while trying to quick move to pattern accessor", slotId);
            }
         }
      }
   }

   private IntSet findEncodedPatternSlots(int maxCount, Predicate<ItemStack> filter) {
      IntSet emptySlots = new IntArraySet();

      for (int i = 0; i < this.encodedPatternSlots.length; i++) {
         if (filter.test(this.encodedPatternSlots[i].getItem())) {
            emptySlots.add(i);
            if (emptySlots.size() >= maxCount) {
               break;
            }
         }
      }

      return emptySlots;
   }

   public void encode(boolean encodeToPlayerInv) {
      if (this.isClientSide()) {
         this.gtolib$addUUID(ClientUtil.getUUID());
         this.menu.sendSubmenuAction("encode", encodeToPlayerInv);
      } else {
         this.encodingLogic.setEncoding(true);
         ItemStack[] encodedPatterns = this.encodePattern();
         if (encodeToPlayerInv) {
            this.transferToPlayerInventory(encodedPatterns);
         } else {
            this.transferToBufferSlots(encodedPatterns);
         }

         this.encodingLogic.setEncoding(false);
      }
   }

   public void encodeAndTransferToProvider(SeenProviderSlots seenProviderSlots) {
      if (this.isClientSide()) {
         this.gtolib$addUUID(ClientUtil.getUUID());
         this.menu.sendSubmenuAction("encodingToProvider", seenProviderSlots);
      } else {
         this.encodingLogic.setEncoding(true);
         ItemStack[] encodedPatterns = this.encodePattern();
         List<Integer> visibleSlotsIds = seenProviderSlots.getMachineSlotIndex();
         List<Long> containerIds = seenProviderSlots.getMachineSlotServerId();
         Preconditions.checkArgument(visibleSlotsIds.size() == containerIds.size());
         ItemStack carriedBefore = this.getCarried();
         int availableSlots;
         if (encodedPatterns.length > visibleSlotsIds.size()) {
            this.transferToBufferSlots(Arrays.copyOfRange(encodedPatterns, visibleSlotsIds.size(), encodedPatterns.length));
            availableSlots = visibleSlotsIds.size();
         } else {
            availableSlots = encodedPatterns.length;
         }

         for (int i = 0; i < availableSlots; i++) {
            if (!this.drawBlankPattern()) {
               this.setCarried(carriedBefore);
               return;
            }

            this.setCarried(encodedPatterns[i]);
            this.menu.doPatternAction((ServerPlayer)this.getPlayer(), InventoryAction.PICKUP_OR_SET_DOWN, visibleSlotsIds.get(i), containerIds.get(i));
         }

         this.setCarried(carriedBefore);
         this.encodingLogic.setEncoding(false);
      }
   }

   public void encodeAndTransferToProvider() {
      SeenProviderSlots slots1 = this.menu.getVisibleEmptyPatternSlots();
      this.encodeAndTransferToProvider(slots1);
   }

   private void transferToPlayerInventory(ItemStack... encodedPatterns) {
      for (ItemStack encodedPattern : encodedPatterns) {
         if (this.drawBlankPattern() && !this.getPlayerInventory().add(encodedPattern)) {
            this.getPlayer().drop(encodedPattern, false);
         }
      }
   }

   private void transferToBufferSlots(ItemStack... encodedPatterns) {
      IntIterator emptySlots = this.findEncodedPatternSlots(encodedPatterns.length, output -> output.isEmpty() || AEItems.BLANK_PATTERN.isSameAs(output))
         .iterator();

      for (ItemStack encodedPattern : encodedPatterns) {
         if (encodedPattern != null) {
            int availableSlot = emptySlots.hasNext() ? emptySlots.nextInt() : 0;
            ItemStack encodeOutput = this.encodedPatternSlots[availableSlot].getItem();
            if (encodeOutput.isEmpty() && !this.drawBlankPattern()) {
               return;
            }

            this.encodedPatternSlots[availableSlot].set(encodedPattern);
         }
      }
   }

   private boolean drawBlankPattern() {
      return this.storage != null && this.storage.extract(AEItemKey.of(AEItems.BLANK_PATTERN.asItem()), 1L, Actionable.MODULATE, this.getActionSource()) == 1L;
   }

   @Nullable
   private ItemStack[] encodePattern() {
      ArrayList<ItemStack> encodedList = new ArrayList<>();

      ItemStack basePattern = switch (this.menu.mode) {
         case CRAFTING -> this.encodeCraftingPattern();
         case PROCESSING -> this.encodeProcessingPattern();
         case SMITHING_TABLE -> this.encodeSmithingTablePattern();
         case STONECUTTING -> this.encodeStonecuttingPattern();
         case BATCH -> null;
      };
      if (basePattern != null) {
         encodedList.add(basePattern);
      }

      ItemStack[] batchPattern = this.menu.mode == ExtendedEncodingMode.BATCH ? this.batchEncodingPattern() : null;
      if (batchPattern != null && batchPattern.length > 0) {
         encodedList.addAll(Arrays.asList(batchPattern));
      }

      if (!this.menu.mode.certernRecipe && this.menu.isAutoEncodeRenaming()) {
         for (int i = 0; i < this.encodedInputsInv.size(); i++) {
            ItemStack pattern = this.encodedInputsInv.getStack(i) == null
               ? null
               : PatternUtils.encodeProcessRenamedItemPattern(this.encodedInputsInv.getStack(i));
            if (pattern != null) {
               encodedList.add(pattern);
            }
         }

         for (int i = 0; i < this.encodedOutputsInv.size(); i++) {
            ItemStack pattern = this.encodedOutputsInv.getStack(i) == null
               ? null
               : PatternUtils.encodeProcessRenamedItemPattern(this.encodedOutputsInv.getStack(i));
            if (pattern != null) {
               encodedList.add(pattern);
            }
         }
      }

      return encodedList.isEmpty() ? null : encodedList.toArray(new ItemStack[0]);
   }

   @Nullable
   private ItemStack encodeCraftingPattern() {
      ItemStack[] ingredients = new ItemStack[9];
      boolean valid = false;

      for (int x = 0; x < ingredients.length; x++) {
         ingredients[x] = this.getEncodedCraftingIngredient(x);
         if (ingredients[x] == null) {
            return null;
         }

         if (!ingredients[x].isEmpty()) {
            valid = true;
         }
      }

      if (!valid) {
         return null;
      }

      ItemStack result = this.getAndUpdateOutput();
      return !result.isEmpty() && this.currentRecipe != null
         ? PatternDetailsHelper.encodeCraftingPattern(this.currentRecipe, ingredients, result, this.menu.isSubstitute(), this.menu.isSubstituteFluids())
         : null;
   }

   @Nullable
   private ItemStack encodeProcessingPattern() {
      GenericStack[] inputs = new GenericStack[this.encodedInputsInv.size()];
      boolean valid = false;

      for (int slot = 0; slot < this.encodedInputsInv.size(); slot++) {
         inputs[slot] = this.encodedInputsInv.getStack(slot);
         if (inputs[slot] != null) {
            valid = true;
         }
      }

      if (!valid) {
         return null;
      }

      GenericStack[] outputs = new GenericStack[this.encodedOutputsInv.size()];

      for (int slot = 0; slot < this.encodedOutputsInv.size(); slot++) {
         outputs[slot] = this.encodedOutputsInv.getStack(slot);
      }

      if (outputs[0] == null) {
         return null;
      }

      ItemStack out = PatternDetailsHelper.encodeProcessingPattern(inputs, outputs);
      if (this.menu.isExtraInfoEnabled() && this.recipe != null && !this.recipe.isEmpty()) {
         out.getOrCreateTag().putString("recipe", this.recipe);
      }

      if (this.uuid != null) {
         out.getOrCreateTag().putUUID("uuid", this.uuid);
      }

      return out;
   }

   @Nullable
   private ItemStack encodeSmithingTablePattern() {
      if (this.encodedInputsInv.getKey(0) instanceof AEItemKey template
         && this.encodedInputsInv.getKey(1) instanceof AEItemKey base
         && this.encodedInputsInv.getKey(2) instanceof AEItemKey addition) {
         SimpleContainer container = new SimpleContainer(3);
         container.setItem(0, template.toStack());
         container.setItem(1, base.toStack());
         container.setItem(2, addition.toStack());
         Level level = this.getPlayer().level();
         SmithingRecipe recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMITHING, container, level).orElse(null);
         if (recipe == null) {
            return null;
         } else {
            AEItemKey output = AEItemKey.of(recipe.assemble(container, level.registryAccess()));
            if (output != null) {
               return PatternDetailsHelper.encodeSmithingTablePattern(recipe, template, base, addition, output, this.encodingLogic.isSubstitution());
            } else {
               throw new IllegalStateException("Smithing recipe returned null output: " + recipe.getId());
            }
         }
      } else {
         return null;
      }
   }

   @Nullable
   private ItemStack encodeStonecuttingPattern() {
      if (this.menu.stonecuttingRecipeId == null) {
         return null;
      }

      if (this.encodedInputsInv.getKey(0) instanceof AEItemKey input) {
         SimpleContainer container = new SimpleContainer(1);
         container.setItem(0, input.toStack());
         Level level = this.getPlayer().level();
         StonecutterRecipe recipe = level.getRecipeManager()
            .getRecipeFor(RecipeType.STONECUTTING, container, level, this.menu.stonecuttingRecipeId)
            .map(Pair::getSecond)
            .orElse(null);
         if (recipe == null) {
            return null;
         } else {
            AEItemKey output = AEItemKey.of(recipe.getResultItem(level.registryAccess()));
            if (output != null) {
               return PatternDetailsHelper.encodeStonecuttingPattern(recipe, input, output, this.encodingLogic.isSubstitution());
            } else {
               throw new IllegalStateException("Stonecutting recipe returned null output: " + recipe.getId());
            }
         }
      } else {
         return null;
      }
   }

   private ItemStack[] batchEncodingPattern() {
      if (!this.isEncodingAreaValid()) {
         return new ItemStack[0];
      }

      Set<Material> matSet = this.materialsInv.materialSet();
      if (matSet.isEmpty()) {
         return new ItemStack[]{this.encodeProcessingPattern()};
      }

      ArrayList<ItemStack> encodeds = new ArrayList<>();
      GenericStack[] inputs = new GenericStack[this.encodedInputsInv.size()];
      GenericStack[] outputs = new GenericStack[this.encodedOutputsInv.size()];

      for (Material mat : matSet) {
         Material matOrigin = GTMaterials.NULL;

         for (int i = 0; i < this.encodedInputsInv.size(); i++) {
            inputs[i] = this.encodedInputsInv.getStack(i) == null
               ? null
               : new GenericStack(AEChemicalHelper.getKey(mat, this.encodedInputsInv.getKey(i)), this.encodedInputsInv.getStack(i).amount());
            if (matOrigin == GTMaterials.NULL && inputs[i] != null) {
               matOrigin = AEChemicalHelper.getMaterial(inputs[i].what());
            }
         }

         for (int i = 0; i < this.encodedOutputsInv.size(); i++) {
            outputs[i] = this.encodedOutputsInv.getStack(i) == null
               ? null
               : new GenericStack(AEChemicalHelper.getKey(mat, this.encodedOutputsInv.getKey(i)), this.encodedOutputsInv.getStack(i).amount());
            if (matOrigin == GTMaterials.NULL && outputs[i] != null) {
               matOrigin = AEChemicalHelper.getMaterial(outputs[i].what());
            }
         }

         ItemStack out = PatternDetailsHelper.encodeProcessingPattern(inputs, outputs);
         if (this.menu.isExtraInfoEnabled()
            && this.recipe != null
            && !this.recipe.isEmpty()
            && matOrigin != GTMaterials.NULL
            && this.recipe.contains(matOrigin.getName())) {
            String replacedRecipe = this.recipe.replaceAll(matOrigin.getName(), mat.getName());
            if (RecipeBuilder.get(RLUtils.parse(replacedRecipe)) != null) {
               out.getOrCreateTag().putString("recipe", replacedRecipe);
            }
         }

         if (this.uuid != null) {
            out.getOrCreateTag().putUUID("uuid", this.uuid);
         }

         encodeds.add(out);
      }

      return encodeds.toArray(new ItemStack[0]);
   }

   private boolean isEncodingAreaValid() {
      boolean valid = false;

      for (int slot = 0; slot < this.encodedInputsInv.size(); slot++) {
         if (this.encodedInputsInv.getStack(slot) != null) {
            valid = true;
         }
      }

      return !valid ? false : this.encodedOutputsInv.getStack(0) != null;
   }

   public void clearPattern() {
      if (this.isClientSide()) {
         this.menu.sendSubmenuAction("clearPattern");
      } else {
         int count = 0;

         for (RestrictedInputSlot encodedPatternSlot : this.encodedPatternSlots) {
            ItemStack encodedPattern = encodedPatternSlot.getItem();
            if (PatternDetailsHelper.isEncodedPattern(encodedPattern)) {
               count++;
               encodedPatternSlot.set(ItemStack.EMPTY);
            }
         }

         if (count != 0) {
            ItemStack remainder = AEItems.BLANK_PATTERN.stack(count);
            if (this.storage != null) {
               remainder.shrink((int)this.storage.insert(AEItemKey.of(remainder), remainder.getCount(), Actionable.MODULATE, this.getActionSource()));
            }

            if (remainder.getCount() > 0 && !this.getPlayerInventory().add(remainder)) {
               this.getPlayerInventory().player.drop(remainder, false);
            }
         }
      }
   }

   public void clear() {
      if (this.isClientSide()) {
         this.menu.sendSubmenuAction("clear");
      } else {
         this.encodedInputsInv.clear();
         this.encodedOutputsInv.clear();
         this.broadcastChanges();
         this.getAndUpdateOutput();
      }
   }

   public void clearFilter() {
      if (this.isClientSide()) {
         this.menu.sendSubmenuAction("clearFilter");
      } else {
         this.materialsInv.clear();
         this.broadcastChanges();
         this.getAndUpdateOutput();
      }
   }

   private ItemStack getAndUpdateOutput() {
      Level level = this.getPlayerInventory().player.level();
      TransientCraftingContainer ic = new TransientCraftingContainer(this.menu, 3, 3);
      boolean invalidIngredients = false;

      for (int x = 0; x < ic.getContainerSize(); x++) {
         ItemStack stack = this.getEncodedCraftingIngredient(x);
         if (stack != null) {
            ic.setItem(x, stack);
         } else {
            invalidIngredients = true;
         }
      }

      if (this.currentRecipe == null || !this.currentRecipe.matches(ic, level)) {
         if (invalidIngredients) {
            this.currentRecipe = null;
         } else {
            this.currentRecipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, ic, level).orElse(null);
         }

         this.currentMode = this.menu.mode;
         this.checkFluidSubstitutionSupport();
      }

      ItemStack is;
      if (this.currentRecipe == null) {
         is = ItemStack.EMPTY;
      } else {
         is = this.currentRecipe.assemble(ic, level.registryAccess());
      }

      this.craftOutputSlot.setDisplayedCraftingOutput(is);
      return is;
   }

   private void checkFluidSubstitutionSupport() {
      this.slotsSupportingFluidSubstitution.clear();
      if (this.currentRecipe != null) {
         ItemStack[] encodedPattern = this.encodePattern();
         if (encodedPattern != null
            && encodedPattern.length > 0
            && PatternDetailsHelper.decodePattern(encodedPattern[0], this.getPlayerInventory().player.level()) instanceof AECraftingPattern craftingPattern) {
            for (int i = 0; i < craftingPattern.getSparseInputs().length; i++) {
               if (craftingPattern.getValidFluid(i) != null) {
                  this.slotsSupportingFluidSubstitution.add(i);
               }
            }
         }
      }
   }

   @Nullable
   private ItemStack getEncodedCraftingIngredient(int slot) {
      AEKey what = this.encodedInputsInv.getKey(slot);
      if (what == null) {
         return ItemStack.EMPTY;
      } else {
         return what instanceof AEItemKey itemKey ? itemKey.toStack(1) : null;
      }
   }

   public void setMode(ExtendedEncodingMode mode) {
      if (this.menu.mode != mode && mode == ExtendedEncodingMode.STONECUTTING) {
         this.updateStonecuttingRecipes();
      }

      if (this.isClientSide()) {
         this.menu.sendSubmenuAction("setMode", mode);
      } else {
         this.menu.mode = mode;
      }
   }

   public void setSubstitute(boolean substitute) {
      if (this.isClientSide()) {
         this.menu.sendSubmenuAction("setSubstitution", substitute);
      } else {
         this.menu.substitute = substitute;
      }
   }

   public void setSubstituteFluids(boolean substituteFluids) {
      if (this.isClientSide()) {
         this.menu.sendSubmenuAction("setFluidSubstitution", substituteFluids);
      } else {
         this.menu.substituteFluids = substituteFluids;
      }
   }

   public void setAutoEncodeRenaming(boolean autoEncodeRenaming) {
      if (this.isClientSide()) {
         this.menu.sendSubmenuAction("setAutoRenaming", autoEncodeRenaming);
      } else {
         this.menu.autoEncodeRenaming = autoEncodeRenaming;
      }
   }

   public void setAutoSearchProviders(boolean autoSearchProviders) {
      if (this.isClientSide()) {
         this.menu.sendSubmenuAction("setAutoSearchProviders", autoSearchProviders);
      } else {
         this.menu.autoSearchProviders = autoSearchProviders;
      }
   }

   public void setStonecuttingRecipeId(ResourceLocation id) {
      if (this.isClientSide()) {
         this.menu.sendSubmenuAction("setStonecuttingRecipeId", id);
      } else {
         this.encodingLogic.setStonecuttingRecipeId(id);
      }
   }

   private void updateStonecuttingRecipes() {
      this.stonecuttingRecipes.clear();
      if (this.encodedInputsInv.getKey(0) instanceof AEItemKey itemKey) {
         Level level = this.getPlayer().level();
         RecipeManager recipeManager = level.getRecipeManager();
         SimpleContainer inventory = new SimpleContainer(1);
         inventory.setItem(0, itemKey.toStack());
         this.stonecuttingRecipes.addAll(recipeManager.getRecipesFor(RecipeType.STONECUTTING, inventory, level));
      }

      if (this.menu.stonecuttingRecipeId != null && this.stonecuttingRecipes.stream().noneMatch(r -> r.getId().equals(this.menu.stonecuttingRecipeId))) {
         this.menu.stonecuttingRecipeId = null;
      }
   }

   public void cycleProcessingOutput() {
      if (this.isClientSide()) {
         this.menu.sendSubmenuAction("cycleProcessingOutput");
      } else {
         if (this.menu.mode.certernRecipe) {
            return;
         }

         PatternUtils.cyclePatternEncodingArea(this.encodedOutputsInv);
      }
   }

   public boolean canCycleProcessingOutputs() {
      return !this.menu.mode.certernRecipe && Arrays.stream(this.processingOutputSlots).filter(s -> !s.getItem().isEmpty()).count() > 1L;
   }

   public void cycleProcessingInput() {
      if (this.isClientSide()) {
         this.menu.sendSubmenuAction("cycleProcessingInput");
      } else {
         if (this.menu.mode.certernRecipe) {
            return;
         }

         PatternUtils.cyclePatternEncodingArea(this.encodedInputsInv);
      }
   }

   public boolean canCycleProcessingInputs() {
      return !this.menu.mode.certernRecipe && Arrays.stream(this.processingInputSlots).filter(s -> !s.getItem().isEmpty()).count() > 1L;
   }

   public void gtolib$modifyPatter(Integer value) {
      if (this.isClientSide()) {
         this.menu.sendSubmenuAction("modifyPatter", value);
      } else {
         PatternUtils.mulPatternEncodingArea(this.encodedInputsInv, this.encodedOutputsInv, value);
      }
   }

   @Override
   protected ItemStack transferStackToMenu(ItemStack input) {
      return PatternDetailsHelper.isEncodedPattern(input) ? this.transferPatternToBuffer(input) : super.transferStackToMenu(input);
   }

   public ItemStack transferPatternToBuffer(ItemStack input) {
      if (input.isEmpty()) {
         return ItemStack.EMPTY;
      }

      IntSet firstEmptySlot = this.findEncodedPatternSlots(1, ItemStack::isEmpty);
      if (firstEmptySlot.isEmpty()) {
         return input;
      }

      RestrictedInputSlot slot = this.encodedPatternSlots[firstEmptySlot.iterator().nextInt()];
      if (slot.mayPlace(input)) {
         input = slot.safeInsert(input);
         if (input.isEmpty()) {
            return ItemStack.EMPTY;
         }
      }

      return input;
   }

   @Override
   public void setItem(int slotID, int stateId, @NotNull ItemStack stack) {
      super.setItem(slotID, stateId, stack);
      this.getAndUpdateOutput();
   }

   @NotNull
   @Override
   public Slot getSlot(int slotId) {
      return this.menu.getSlot(slotId);
   }

   @Override
   public void onSlotChange(Slot s) {
      if (Stream.of(this.encodedPatternSlots).anyMatch(slot -> slot == s) && this.isServerSide()) {
         this.broadcastChanges();
      }

      if (this.isClientSide()) {
         this.getAndUpdateOutput();
      }

      if (s == this.stonecuttingInputSlot) {
         this.updateStonecuttingRecipes();
      }
   }

   @Override
   public void setCarried(@NotNull ItemStack stack) {
      this.menu.setCarried(stack);
   }

   @NotNull
   @Override
   public ItemStack getCarried() {
      return this.menu.getCarried();
   }

   @Override
   public void broadcastChanges() {
      super.broadcastChanges();
      if (this.isServerSide()) {
         if (this.menu.mode != this.encodingLogic.getMode()) {
            this.setMode(this.encodingLogic.getMode());
         }

         this.menu.substitute = this.encodingLogic.isSubstitution();
         this.menu.substituteFluids = this.encodingLogic.isFluidSubstitution();
         this.menu.stonecuttingRecipeId = this.encodingLogic.getStonecuttingRecipeId();
         this.menu.autoEncodeRenaming = this.encodingLogic.isAutoEncodeRenamePatterns();
         this.menu.autoSearchProviders = this.encodingLogic.isAutoSearchProviders();
         this.menu.hasPower = this.hasPower;
         this.menu.activeCraftingJobs = this.activeCraftingJobs;
      }
   }

   @Override
   public void onServerDataSync() {
      super.onServerDataSync();

      for (FakeSlot slot : this.craftingGridSlots) {
         slot.setActive(this.menu.mode == ExtendedEncodingMode.CRAFTING);
      }

      this.craftOutputSlot.setActive(this.menu.mode == ExtendedEncodingMode.CRAFTING);

      for (FakeSlot slot : this.processingInputSlots) {
         slot.setActive(this.menu.mode == ExtendedEncodingMode.PROCESSING);
      }

      for (FakeSlot slot : this.processingOutputSlots) {
         slot.setActive(this.menu.mode == ExtendedEncodingMode.PROCESSING);
      }

      if (this.currentMode != this.menu.mode) {
         this.encodingLogic.setMode(this.menu.mode);
         this.getAndUpdateOutput();
         this.updateStonecuttingRecipes();
      }
   }

   @Override
   public InternalInventory getCraftingMatrix() {
      return this.encodedInputsInv.createMenuWrapper().getSubInventory(0, 9);
   }

   @Override
   public boolean useRealItems() {
      return false;
   }

   @Contract("null -> false")
   public boolean canModifyAmountForSlot(@Nullable Slot slot) {
      return this.isProcessingPatternSlot(slot) && slot.hasItem();
   }

   @Contract("null -> false")
   public boolean isProcessingPatternSlot(@Nullable Slot slot) {
      if (slot != null && !this.menu.mode.certernRecipe) {
         for (FakeSlot processingOutputSlot : this.processingOutputSlots) {
            if (processingOutputSlot == slot) {
               return true;
            }
         }

         for (FakeSlot craftingSlot : this.processingInputSlots) {
            if (craftingSlot == slot) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public Repo getRepo() {
      return (Repo)this.getClientRepo();
   }

   @Generated
   public ExtendedEncodingLogic getEncodingLogic() {
      return this.encodingLogic;
   }

   @Generated
   public Me2in1Menu getMenu() {
      return this.menu;
   }

   @Generated
   public FakeSlot[] getCraftingGridSlots() {
      return this.craftingGridSlots;
   }

   @Generated
   public PatternTermSlot getCraftOutputSlot() {
      return this.craftOutputSlot;
   }

   @Generated
   public FakeSlot[] getProcessingInputSlots() {
      return this.processingInputSlots;
   }

   @Generated
   public FakeSlot[] getProcessingOutputSlots() {
      return this.processingOutputSlots;
   }

   @Generated
   public FakeSlot getStonecuttingInputSlot() {
      return this.stonecuttingInputSlot;
   }

   @Generated
   public FakeSlot getSmithingTableTemplateSlot() {
      return this.smithingTableTemplateSlot;
   }

   @Generated
   public FakeSlot getSmithingTableBaseSlot() {
      return this.smithingTableBaseSlot;
   }

   @Generated
   public FakeSlot getSmithingTableAdditionSlot() {
      return this.smithingTableAdditionSlot;
   }

   @Generated
   public FakeSlot[] getMaterialSlots() {
      return this.materialSlots;
   }

   @Generated
   public FakeSlot getQuickRemovePatternFilterSlot() {
      return this.quickRemovePatternFilterSlot;
   }

   @Generated
   public RestrictedInputSlot[] getEncodedPatternSlots() {
      return this.encodedPatternSlots;
   }

   @Generated
   public ConfigInventory getEncodedInputsInv() {
      return this.encodedInputsInv;
   }

   @Generated
   public ConfigInventory getEncodedOutputsInv() {
      return this.encodedOutputsInv;
   }

   @Generated
   public MaterialConfigInventory getMaterialsInv() {
      return this.materialsInv;
   }

   @Generated
   public ConfigInventory getQuickRemovePatternFilterInv() {
      return this.quickRemovePatternFilterInv;
   }

   @Generated
   public CraftingRecipe getCurrentRecipe() {
      return this.currentRecipe;
   }

   @Generated
   public ExtendedEncodingMode getCurrentMode() {
      return this.currentMode;
   }

   @Generated
   public List<StonecutterRecipe> getStonecuttingRecipes() {
      return this.stonecuttingRecipes;
   }

   @Generated
   public IntSet getUsedVisibleSlotIds() {
      return this.usedVisibleSlotIds;
   }

   @Generated
   public String getRecipe() {
      return this.recipe;
   }

   @Generated
   public UUID getUuid() {
      return this.uuid;
   }

   @Generated
   public IntSet getSlotsSupportingFluidSubstitution() {
      return this.slotsSupportingFluidSubstitution;
   }

   @Generated
   public void setCurrentRecipe(CraftingRecipe currentRecipe) {
      this.currentRecipe = currentRecipe;
   }

   @Generated
   public void setCurrentMode(ExtendedEncodingMode currentMode) {
      this.currentMode = currentMode;
   }

   @Generated
   public void setRecipe(String recipe) {
      this.recipe = recipe;
   }

   @Generated
   public void setUuid(UUID uuid) {
      this.uuid = uuid;
   }

   @Generated
   public void setSlotsSupportingFluidSubstitution(IntSet slotsSupportingFluidSubstitution) {
      this.slotsSupportingFluidSubstitution = slotsSupportingFluidSubstitution;
   }

   public static class SeenProviderSlotsWithSlotId {
      private SeenProviderSlots seenProviderSlots;
      private int slotId;

      public SeenProviderSlotsWithSlotId(SeenProviderSlots slots, int slotId) {
         this.seenProviderSlots = slots;
         this.slotId = slotId;
      }

      @Generated
      public void setSeenProviderSlots(SeenProviderSlots seenProviderSlots) {
         this.seenProviderSlots = seenProviderSlots;
      }

      @Generated
      public void setSlotId(int slotId) {
         this.slotId = slotId;
      }

      @Generated
      public SeenProviderSlots getSeenProviderSlots() {
         return this.seenProviderSlots;
      }

      @Generated
      public int getSlotId() {
         return this.slotId;
      }
   }
}
