package com.gtolib.api.ae2.me2in1;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.inventories.InternalInventory;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.crafting.pattern.AECraftingPattern;
import appeng.crafting.pattern.AEProcessingPattern;
import appeng.crafting.pattern.AESmithingTablePattern;
import appeng.crafting.pattern.AEStonecuttingPattern;
import appeng.util.ConfigInventory;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import com.gtolib.ae2.me2in1.IMe2in1Host;
import com.gtolib.api.ae2.me2in1.encoding.ExtendedEncodingMode;
import com.gtolib.api.ae2.me2in1.panel.PanelPosMap;
import com.gtolib.api.ae2.me2in1.panel.PanelSizeMap;
import lombok.Generated;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ExtendedEncodingLogic implements InternalInventoryHost {
   private final IMe2in1Host host;
   private static final int MAX_INPUT_SLOTS = Math.max(9, 81);
   private static final int MAX_OUTPUT_SLOTS = 27;
   private final ConfigInventory encodedInputInv = ConfigInventory.configStacks(null, MAX_INPUT_SLOTS, this::onEncodedInputChanged, true);
   private final ConfigInventory encodedOutputInv = ConfigInventory.configStacks(null, 27, this::onEncodedOutputChanged, true);
   private final MaterialConfigInventory materialInv = MaterialConfigInventory.create(9, this::onEncodedInputChanged);
   private final AppEngInternalInventory encodedPatternInv = new AppEngInternalInventory(this, 36);
   private ExtendedEncodingMode mode = ExtendedEncodingMode.CRAFTING;
   private boolean substitute = false;
   private boolean substituteFluids = true;
   private boolean autoEncodeRenamePatterns = false;
   private boolean autoSearchProviders = false;
   private boolean isLoading = false;
   private boolean isEncoding = false;
   @Nullable
   private ResourceLocation stonecuttingRecipeId;
   private final PanelPosMap panelPosMap = new PanelPosMap();
   private final PanelSizeMap panelSizeMap = new PanelSizeMap();

   public ExtendedEncodingLogic(IMe2in1Host host) {
      this.host = host;
   }

   @Override
   public void onChangeInventory(InternalInventory inv, int slot) {
      if (inv == this.encodedPatternInv && !this.isLoading && !this.isEncoding) {
         this.loadEncodedPattern(this.encodedPatternInv.getStackInSlot(slot));
      }

      this.saveChanges();
   }

   @Override
   public void saveChanges() {
      if (!this.isLoading) {
         this.host.markForSave();
      }
   }

   @Override
   public boolean isClientSide() {
      return this.host.getLevel().isClientSide();
   }

   private void onEncodedInputChanged() {
      this.fixCraftingRecipes();
      this.saveChanges();
   }

   private void onEncodedOutputChanged() {
      this.saveChanges();
   }

   private void loadEncodedPattern(ItemStack pattern) {
      if (!pattern.isEmpty()) {
         IPatternDetails details = PatternDetailsHelper.decodePattern(pattern, this.host.getLevel());
         if (details instanceof AECraftingPattern craftingPattern) {
            this.loadCraftingPattern(craftingPattern);
         } else if (details instanceof AEProcessingPattern processingPattern) {
            this.loadProcessingPattern(processingPattern);
         } else if (details instanceof AESmithingTablePattern smithingTablePattern) {
            this.loadSmithingTablePattern(smithingTablePattern);
         } else if (details instanceof AEStonecuttingPattern stonecuttingPattern) {
            this.loadStonecuttingPattern(stonecuttingPattern);
         }

         this.saveChanges();
      }
   }

   private void loadCraftingPattern(AECraftingPattern pattern) {
      this.setMode(ExtendedEncodingMode.CRAFTING);
      this.substitute = pattern.canSubstitute();
      this.substituteFluids = pattern.canSubstituteFluids();
      fillInventoryFromSparseStacks(this.encodedInputInv, pattern.getSparseInputs());
      fillInventoryFromSparseStacks(this.encodedOutputInv, pattern.getSparseOutputs());
   }

   private void loadProcessingPattern(AEProcessingPattern pattern) {
      if (this.getMode() != ExtendedEncodingMode.BATCH) {
         this.setMode(ExtendedEncodingMode.PROCESSING);
      }

      fillInventoryFromSparseStacks(this.encodedInputInv, pattern.getSparseInputs());
      fillInventoryFromSparseStacks(this.encodedOutputInv, pattern.getSparseOutputs());
   }

   private void loadSmithingTablePattern(AESmithingTablePattern pattern) {
      this.setMode(ExtendedEncodingMode.SMITHING_TABLE);
      this.substitute = pattern.canSubstitute();
      this.encodedInputInv.clear();
      this.encodedInputInv.setStack(0, new GenericStack(pattern.getTemplate(), 1L));
      this.encodedInputInv.setStack(1, new GenericStack(pattern.getBase(), 1L));
      this.encodedInputInv.setStack(2, new GenericStack(pattern.getAddition(), 1L));
      this.encodedOutputInv.clear();
   }

   private void loadStonecuttingPattern(AEStonecuttingPattern pattern) {
      this.setMode(ExtendedEncodingMode.STONECUTTING);
      this.stonecuttingRecipeId = pattern.getRecipeId();
      this.substitute = pattern.canSubstitute;
      this.encodedInputInv.clear();
      this.encodedInputInv.setStack(0, new GenericStack(pattern.getInput(), 1L));
      this.encodedOutputInv.clear();
   }

   private static void fillInventoryFromSparseStacks(ConfigInventory inv, GenericStack[] stacks) {
      inv.beginBatch();

      try {
         for (int i = 0; i < inv.size(); i++) {
            inv.setStack(i, i < stacks.length ? stacks[i] : null);
         }
      } finally {
         inv.endBatch();
      }
   }

   public void setMode(ExtendedEncodingMode mode) {
      this.mode = mode;
      this.fixCraftingRecipes();
      this.saveChanges();
   }

   public boolean isSubstitution() {
      return this.substitute;
   }

   public void setSubstitution(boolean canSubstitute) {
      this.substitute = canSubstitute;
      this.saveChanges();
   }

   public boolean isFluidSubstitution() {
      return this.substituteFluids;
   }

   public void setFluidSubstitution(boolean canSubstitute) {
      this.substituteFluids = canSubstitute;
      this.saveChanges();
   }

   @Nullable
   public ResourceLocation getStonecuttingRecipeId() {
      return this.stonecuttingRecipeId;
   }

   public void setStonecuttingRecipeId(ResourceLocation stonecuttingRecipeId) {
      this.stonecuttingRecipeId = stonecuttingRecipeId;
      this.saveChanges();
   }

   public InternalInventory getEncodedPatternInv() {
      return this.encodedPatternInv;
   }

   public void readFromNBT(CompoundTag data) {
      this.isLoading = true;

      try {
         try {
            this.mode = ExtendedEncodingMode.valueOf(data.getString("mode"));
         } catch (IllegalArgumentException ignored) {
            this.mode = ExtendedEncodingMode.CRAFTING;
         }

         this.setSubstitution(data.getBoolean("substitute"));
         this.setFluidSubstitution(data.getBoolean("substituteFluids"));
         this.setAutoEncodeRenamePatterns(data.getBoolean("autoEncodeRenamePatterns"));
         this.setAutoSearchProviders(data.getBoolean("autoSearchProviders"));
         if (data.contains("stonecuttingRecipeId", 8)) {
            this.stonecuttingRecipeId = ResourceLocation.parse(data.getString("stonecuttingRecipeId"));
         } else {
            this.stonecuttingRecipeId = null;
         }

         this.encodedPatternInv.readFromNBT(data, "encodedPattern");
         this.encodedInputInv.readFromChildTag(data, "encodedInputs");
         this.encodedOutputInv.readFromChildTag(data, "encodedOutputs");
         this.materialInv.readFromChildTag(data, "materialInv");
         this.panelPosMap.readFromNbt(data, "panelPosMap");
         this.panelSizeMap.readFromNbt(data, "panelSizeMap");
      } finally {
         this.isLoading = false;
      }
   }

   public void writeToNBT(CompoundTag data) {
      data.putString("mode", this.mode.name());
      data.putBoolean("substitute", this.substitute);
      data.putBoolean("substituteFluids", this.substituteFluids);
      data.putBoolean("autoEncodeRenamePatterns", this.autoEncodeRenamePatterns);
      data.putBoolean("autoSearchProviders", this.autoSearchProviders);
      if (this.stonecuttingRecipeId != null) {
         data.putString("stonecuttingRecipeId", this.stonecuttingRecipeId.toString());
      }

      this.encodedPatternInv.writeToNBT(data, "encodedPattern");
      this.encodedInputInv.writeToChildTag(data, "encodedInputs");
      this.encodedOutputInv.writeToChildTag(data, "encodedOutputs");
      this.materialInv.writeToChildTag(data, "materialInv");
      this.panelPosMap.writeToNbt(data, "panelPosMap");
      this.panelSizeMap.writeToNbt(data, "panelSizeMap");
   }

   private void fixCraftingRecipes() {
      if (this.host.getLevel() != null && !this.host.getLevel().isClientSide()) {
         if (this.getMode().certernRecipe) {
            ConfigInventory craftingGrid = this.getEncodedInputInv();

            for (int slot = 0; slot < craftingGrid.size(); slot++) {
               GenericStack stack = craftingGrid.getStack(slot);
               if (stack != null) {
                  if (!AEItemKey.is(stack.what())) {
                     craftingGrid.setStack(slot, null);
                  } else if (stack.amount() != 1L) {
                     craftingGrid.setStack(slot, new GenericStack(stack.what(), 1L));
                  }
               }
            }
         }
      }
   }

   public void updatePanelPos(PanelPosMap.PanelPos pos) {
      this.panelPosMap.updatePanelPos(pos);
      this.saveChanges();
   }

   public void updatePanelSize(PanelSizeMap.PanelSize size) {
      PanelSizeMap.PanelSize current = this.panelSizeMap.getPanelSize(size.name);
      if (current == null || current.rows != size.rows || current.columns != size.columns) {
         this.panelSizeMap.updatePanelSize(size);
         this.saveChanges();
      }
   }

   public void setAutoSearchProviders(Boolean autoSearchProviders) {
      this.autoSearchProviders = autoSearchProviders;
   }

   @Generated
   public ConfigInventory getEncodedInputInv() {
      return this.encodedInputInv;
   }

   @Generated
   public ConfigInventory getEncodedOutputInv() {
      return this.encodedOutputInv;
   }

   @Generated
   public MaterialConfigInventory getMaterialInv() {
      return this.materialInv;
   }

   @Generated
   public ExtendedEncodingMode getMode() {
      return this.mode;
   }

   @Generated
   public boolean isAutoEncodeRenamePatterns() {
      return this.autoEncodeRenamePatterns;
   }

   @Generated
   public void setAutoEncodeRenamePatterns(boolean autoEncodeRenamePatterns) {
      this.autoEncodeRenamePatterns = autoEncodeRenamePatterns;
   }

   @Generated
   public boolean isAutoSearchProviders() {
      return this.autoSearchProviders;
   }

   @Generated
   public void setEncoding(boolean isEncoding) {
      this.isEncoding = isEncoding;
   }

   @Generated
   public PanelPosMap getPanelPosMap() {
      return this.panelPosMap;
   }

   @Generated
   public PanelSizeMap getPanelSizeMap() {
      return this.panelSizeMap;
   }
}
