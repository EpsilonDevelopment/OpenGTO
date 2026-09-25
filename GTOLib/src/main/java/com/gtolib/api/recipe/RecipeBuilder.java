package com.gtolib.api.recipe;

import appeng.hooks.IUnique;
import com.google.common.collect.ImmutableList;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.GTCEu.Mods;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.ItemMaterialData;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.ItemMaterialInfo;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKey;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.api.recipe.GTRecipeBuilder;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.extension.RecipeExtension;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntCircuitIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.ItemIngredient;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.api.recipe.research.ScannerBuilder;
import com.gregtechceu.gtceu.api.recipe.research.StationBuilder;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTRecipeDataKeys;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.recipe.condition.AdjacentBlockCondition;
import com.gregtechceu.gtceu.common.recipe.condition.AdjacentFluidCondition;
import com.gregtechceu.gtceu.common.recipe.condition.BiomeCondition;
import com.gregtechceu.gtceu.common.recipe.condition.CleanroomCondition;
import com.gregtechceu.gtceu.common.recipe.condition.DaytimeCondition;
import com.gregtechceu.gtceu.common.recipe.condition.DimensionCondition;
import com.gregtechceu.gtceu.common.recipe.condition.FTBQuestCondition;
import com.gregtechceu.gtceu.common.recipe.condition.PositionYCondition;
import com.gregtechceu.gtceu.common.recipe.condition.RainingCondition;
import com.gregtechceu.gtceu.common.recipe.condition.ResearchCondition;
import com.gregtechceu.gtceu.common.recipe.condition.ThunderCondition;
import com.gregtechceu.gtceu.data.recipe.builder.ShapedRecipeBuilder;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.gregtechceu.gtceu.utils.ResearchManager;
import com.gto.datasynclib.datastream.DataComponentKey;
import com.gto.datasynclib.datastream.DataComponentMap;
import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import com.gtocore.api.research.ResearchPoints;
import com.gtocore.api.research.ResearchTag;
import com.gtocore.api.research.recipe.ResearchPointsRecipeExtion;
import com.gtocore.api.research.techtree.TechNode;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTORecipeDataKeys;
import com.gtocore.common.recipe.condition.GravityCondition;
import com.gtocore.common.recipe.condition.HeatCondition;
import com.gtocore.common.recipe.condition.ResearchRecipeCondition;
import com.gtocore.common.recipe.condition.RestrictedMachineCondition;
import com.gtocore.common.recipe.condition.RunLimitCondition;
import com.gtocore.common.recipe.condition.VacuumCondition;
import com.gtocore.config.GTOConfig;
import com.gtolib.GTOCore;
import com.gtolib.api.GTOValues;
import com.gtolib.api.annotation.component_builder.ComponentListSupplier;
import com.gtolib.api.data.GTODimensions;
import com.gtolib.api.data.tag.ITagPrefix;
import com.gtolib.api.item.IItem;
import com.gtolib.api.item.NBTItem;
import com.gtolib.api.recipe.extension.HURecipeExtension;
import com.gtolib.api.recipe.extension.HUTRecipeExtension;
import com.gtolib.api.recipe.extension.MANARecipeExtension;
import com.gtolib.api.recipe.extension.MANATRecipeExtension;
import com.gtolib.gtm.RecipeLogicExt;
import com.gtolib.gtm.RecipeScript;
import com.gtolib.mc.ITagKey;
import com.gtolib.utils.RLUtils;
import com.gtolib.utils.RegistriesUtils;
import com.gtolib.utils.TagUtils;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.crafting.StrictNBTIngredient;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class RecipeBuilder extends GTRecipeBuilder {
   private static Map<MaterialEntry, Ingredient> MATERIAL_INGREDIENT_MAP;
   private static Map<NBTItem, Ingredient> ITEM_INGREDIENT_MAP;
   boolean deleted;
   private TechNode researchNode;
   IntSet itemsCanBeProduced;
   IntSet fluidsCanBeProduced;
   ComponentListSupplier descriptionSupplier;

   RecipeBuilder(ResourceLocation id, GTRecipeType recipeType) {
      super(id, recipeType);
      this.itemsCanBeProduced = MATERIAL_INGREDIENT_MAP == null ? null : new IntOpenHashSet();
      this.fluidsCanBeProduced = MATERIAL_INGREDIENT_MAP == null ? null : new IntOpenHashSet();
      this.descriptionSupplier = null;
   }

   private RecipeBuilder(ResourceLocation id) {
      super(id);
      this.itemsCanBeProduced = MATERIAL_INGREDIENT_MAP == null ? null : new IntOpenHashSet();
      this.fluidsCanBeProduced = MATERIAL_INGREDIENT_MAP == null ? null : new IntOpenHashSet();
      this.descriptionSupplier = null;
   }

   @Nullable
   public static GTRecipeDefinition get(ResourceLocation id) {
      return RecipeScript.get(id);
   }

   public static GTRecipeDefinition remove(ResourceLocation id) {
      return RecipeScript.remove(id);
   }

   public static Collection<GTRecipeDefinition> values() {
      return RecipeScript.values();
   }

   public static void initialization() {
      MATERIAL_INGREDIENT_MAP = new O2OOpenCacheHashMap<>(1024, 0.25F);
      ITEM_INGREDIENT_MAP = new O2OOpenCacheHashMap<>(1024, 0.25F);
      GTRegistries.RECIPE_TYPES.forEach(t -> ((RecipeType)t).filter = new Object2BooleanOpenHashMap<>());
      if (GTCEu.isDev() && GTOConfig.INSTANCE.devMode.enableCustomRecipes) {
         RecipeScript.loadRecipes(true);
      }
   }

   public static void finish() {
      MATERIAL_INGREDIENT_MAP = null;
      ITEM_INGREDIENT_MAP = null;
      GTRegistries.RECIPE_TYPES.forEach(t -> {
         t.recipes.clear();
         ((RecipeType)t).filter.forEach((k, v) -> {
            if (!v) {
               GTOCore.LOGGER.error("Recipe filter [{}] not in use", k);
            }
         });
         ((RecipeType)t).filter = null;
      });
      RecipeScript.values().forEach(r -> {
         if (r.recipeType instanceof RecipeType recipeType) {
            recipeType.recipes.put(r.id, r);
         }
      });
   }

   private static ItemIngredient createSizedIngredient(Item item, int amount) {
      return ItemIngredient.of(item, amount);
   }

   private static ItemIngredient createSizedIngredient(MaterialEntry MaterialEntry, int amount) {
      if (MATERIAL_INGREDIENT_MAP == null) {
         return ItemIngredient.of(ChemicalHelper.get(MaterialEntry, amount));
      }

      Ingredient ingredient = MATERIAL_INGREDIENT_MAP.get(MaterialEntry);
      if (ingredient == null) {
         Item item = ChemicalHelper.getItem(MaterialEntry);
         if (item == Items.AIR) {
            GTOCore.LOGGER
               .error("Tried to set output item stack that doesn't exist, TagPrefix: {}, Material: {}", MaterialEntry.tagPrefix(), MaterialEntry.material());
         }

         ingredient = ((IItem)item).gtolib$getIngredient();
         MATERIAL_INGREDIENT_MAP.put(MaterialEntry, ingredient);
      }

      return ItemIngredient.of(ingredient, amount);
   }

   private static ItemIngredient createSizedIngredient(ItemStack stack) {
      if (ITEM_INGREDIENT_MAP != null && stack.hasTag()) {
         NBTItem nbtItem = NBTItem.of(stack);
         Ingredient ingredient = ITEM_INGREDIENT_MAP.get(nbtItem);
         if (ingredient == null) {
            ingredient = StrictNBTIngredient.of(stack);
            ITEM_INGREDIENT_MAP.put(nbtItem, ingredient);
         }

         return ItemIngredient.of(ingredient, stack.getCount());
      } else {
         return ItemIngredient.of(stack);
      }
   }

   public static RecipeBuilder ofRaw() {
      return (RecipeBuilder)RAW.copy();
   }

   public RecipeBuilder reset() {
      if (this.itemInputs != null) {
         this.itemInputs.clear();
      }

      if (this.itemOutputs != null) {
         this.itemOutputs.clear();
      }

      if (this.fluidInputs != null) {
         this.fluidInputs.clear();
      }

      if (this.fluidOutputs != null) {
         this.fluidOutputs.clear();
      }

      if (this.conditions != null) {
         this.conditions.clear();
      }

      if (this.recipeExtensions != null) {
         this.recipeExtensions.clear();
      }

      if (this.tickRecipeExtensions != null) {
         this.tickRecipeExtensions.clear();
      }

      if (this.data != null) {
         this.data.clear();
      }

      this.duration = 100;
      this.tier = 0;
      this.eut = 0L;
      return this;
   }

   public RecipeBuilder copy() {
      return this.copy(this.id);
   }

   public RecipeBuilder copy(String id) {
      return this.copy(GTCEu.id(id));
   }

   public RecipeBuilder copy(ResourceLocation id) {
      RecipeBuilder copy = new RecipeBuilder(id);
      copy.recipeType = this.recipeType;
      copy.recipeCategory = this.recipeCategory;
      if (this.itemInputs != null) {
         copy.itemInputs = new ArrayList<>(this.itemInputs);
      }

      if (this.itemOutputs != null) {
         copy.itemOutputs = new ArrayList<>(this.itemOutputs);
      }

      if (this.fluidInputs != null) {
         copy.fluidInputs = new ArrayList<>(this.fluidInputs);
      }

      if (this.fluidOutputs != null) {
         copy.fluidOutputs = new ArrayList<>(this.fluidOutputs);
      }

      if (this.conditions != null) {
         copy.conditions = new ReferenceOpenHashSet<>(this.conditions);
      }

      if (this.recipeExtensions != null) {
         copy.recipeExtensions = new ReferenceOpenHashSet<>(this.recipeExtensions);
      }

      if (this.tickRecipeExtensions != null) {
         copy.tickRecipeExtensions = new ReferenceOpenHashSet<>(this.tickRecipeExtensions);
      }

      if (this.data != null) {
         copy.data = this.data.clone();
      }

      copy.duration = this.duration;
      copy.tier = this.tier;
      copy.eut = this.eut;
      copy.chance = this.chance;
      copy.onSave = this.onSave;
      return copy;
   }

   public RecipeBuilder copyFrom(GTRecipeBuilder builder) {
      RecipeBuilder b = ((RecipeBuilder)builder).copy(((RecipeBuilder)builder).id);
      b.onSave = null;
      return b.recipeType(this.recipeType).category(this.recipeCategory);
   }

   public RecipeBuilder addCondition(RecipeCondition condition) {
      if (this.conditions == null) {
         this.conditions = new ReferenceOpenHashSet<>();
      }

      this.conditions.add(condition);
      return this;
   }

   public RecipeBuilder addCondition(Collection<RecipeCondition> conditions) {
      if (this.conditions == null) {
         this.conditions = new ReferenceOpenHashSet<>();
      }

      this.conditions.addAll(conditions);
      return this;
   }

   public RecipeBuilder addExtension(RecipeExtension extension) {
      if (this.recipeExtensions == null) {
         this.recipeExtensions = new ReferenceOpenHashSet<>();
      }

      this.recipeExtensions.add(extension);
      return this;
   }

   public RecipeBuilder addTickExtension(RecipeExtension extension) {
      if (this.tickRecipeExtensions == null) {
         this.tickRecipeExtensions = new ReferenceOpenHashSet<>();
      }

      this.tickRecipeExtensions.add(extension);
      return this;
   }

   public RecipeBuilder EUt(long eu) {
      if (this.deleted) {
         return this;
      }

      if (eu == 0L) {
         GTCEu.LOGGER.error("EUt can't be explicitly set to 0, id: {}", this.id);
      }

      this.eut = eu;
      if (this.recipeType != GTRecipeTypes.DUMMY_RECIPES) {
         this.tier = GTUtil.getTierByVoltage(Math.abs(eu));
      }

      return this;
   }

   public RecipeBuilder EUtVC(int tier) {
      return this.EUt(GTValues.VC[tier]);
   }

   public RecipeBuilder CWUt(long cwu) {
      if (this.deleted) {
         return this;
      }

      super.CWUt(cwu);
      return this;
   }

   public RecipeBuilder totalCWU(int cwu) {
      this.durationIsTotalCWU(true);
      this.hideDuration(true);
      this.duration(cwu);
      return this;
   }

   public RecipeBuilder inputItems(Object input) {
      if (this.deleted) {
         return this;
      }

      Object var2 = input;
      byte var3 = 0;

      while (true) {
         RecipeBuilder var10000;
         switch (var3 /* $VF: typeSwitch(var2, var3) */) {
            case 0:
               String string = (String)var2;
               var10000 = this.inputItems(string);
               break;
            case 1: {
               ItemLike item = (ItemLike)var2;
               var10000 = this.inputItems(item.asItem());
               break;
            }
            case 2: {
               Supplier<?> supplier = (Supplier<?>)var2;
               if (!(supplier.get() instanceof ItemLike item)) {
                  var3 = 3;
                  continue;
               }

               var10000 = this.inputItems(item.asItem());
               break;
            }
            case 3:
               ItemStack stack = (ItemStack)var2;
               var10000 = this.inputItems(stack);
               break;
            case 4: {
               Ingredient ingredient = (Ingredient)var2;
               var10000 = this.inputItems(ingredient);
               break;
            }
            case 5: {
               ItemIngredient ingredient = (ItemIngredient)var2;
               var10000 = this.inputItems(ingredient);
               break;
            }
            case 6:
               MaterialEntry entry = (MaterialEntry)var2;
               var10000 = this.inputItems(entry);
               break;
            case 7:
               TagKey<?> tag = (TagKey<?>)var2;
               var10000 = this.inputItems((TagKey<Item>)tag);
               break;
            default:
               throw new IllegalArgumentException(
                  "Input: "
                     + input
                     + " is not one of: Item, Supplier<Item>, ItemStack, Ingredient, MaterialEntry, TagKey<Item>, MachineDefinition, id: "
                     + this.id
               );
         }

         return var10000;
      }
   }

   public RecipeBuilder inputItems(Object input, int count) {
      if (this.deleted) {
         return this;
      }

      Object var3 = input;
      byte var4 = 0;

      while (true) {
         RecipeBuilder var10000;
         switch (var4 /* $VF: typeSwitch(var3, var4) */) {
            case 0:
               String string = (String)var3;
               var10000 = this.inputItems(string, count);
               break;
            case 1: {
               ItemLike item = (ItemLike)var3;
               var10000 = this.inputItems(item.asItem(), count);
               break;
            }
            case 2: {
               Supplier<?> supplier = (Supplier<?>)var3;
               if (!(supplier.get() instanceof ItemLike item)) {
                  var4 = 3;
                  continue;
               }

               var10000 = this.inputItems(item.asItem(), count);
               break;
            }
            case 3:
               ItemStack stack = (ItemStack)var3;
               var10000 = this.inputItems(stack.copyWithCount(count));
               break;
            case 4: {
               Ingredient ingredient = (Ingredient)var3;
               var10000 = this.inputItems(ingredient, count);
               break;
            }
            case 5: {
               ItemIngredient ingredient = (ItemIngredient)var3;
               var10000 = this.inputItems(ingredient, count);
               break;
            }
            case 6:
               MaterialEntry entry = (MaterialEntry)var3;
               var10000 = this.inputItems(entry, count);
               break;
            case 7:
               TagKey<?> tag = (TagKey<?>)var3;
               var10000 = this.inputItems((TagKey<Item>)tag, count);
               break;
            default:
               throw new IllegalArgumentException(
                  "Input: "
                     + input
                     + " is not one of: Item, Supplier<Item>, ItemStack, Ingredient, MaterialEntry, TagKey<Item>, MachineDefinition, id: "
                     + this.id
               );
         }

         return var10000;
      }
   }

   public RecipeBuilder inputItems(Block input, int count) {
      return this.inputItems(input.asItem(), count);
   }

   public RecipeBuilder inputItems(ItemIngredient inputs, int count) {
      return this.inputItems(inputs.copy(count));
   }

   public RecipeBuilder inputItems(Ingredient inputs, int count) {
      return this.inputItems(ItemIngredient.of(inputs, count));
   }

   public RecipeBuilder inputIngredient(Ingredient inputs) {
      return this.inputItems(ItemIngredient.of(inputs));
   }

   public RecipeBuilder inputItems(Content<ItemIngredient> input) {
      if (this.itemInputs == null) {
         this.itemInputs = new ArrayList<>();
      }

      this.itemInputs.add(input);
      return this;
   }

   public RecipeBuilder inputItems(ItemIngredient inputs) {
      return this.inputItems(new Content<>(inputs, this.chance, this.tierChanceBoost));
   }

   public RecipeBuilder inputItems(Ingredient input) {
      return this.inputItems(ItemIngredient.of(input));
   }

   public RecipeBuilder inputItems(ItemStack input) {
      if (this.deleted) {
         return this;
      }

      if (input.isEmpty()) {
         throw new IllegalArgumentException("Input " + input + " is empty, id: " + this.id);
      }

      if (this.chance == 10000 && this.itemMaterialInfo) {
         ItemMaterialInfo matStack = ItemMaterialData.getMaterialInfo(input.getItem());
         if (matStack != null) {
            for (MaterialStack mat : matStack.getMaterials()) {
               if (this.tempItemMaterialStacks == null) {
                  this.tempItemMaterialStacks = new ArrayList<>();
               }

               this.tempItemMaterialStacks.add(new MaterialStack(mat.material(), mat.amount() * input.getCount()));
            }
         }
      }

      return this.inputItems(createSizedIngredient(input));
   }

   public RecipeBuilder inputItems(TagKey<Item> tag, int amount) {
      if (this.deleted) {
         return this;
      } else if (amount == 0) {
         throw new IllegalArgumentException("Input " + tag + " is empty, id: " + this.id);
      } else {
         return this.inputItems(ItemIngredient.of(tag, amount));
      }
   }

   public RecipeBuilder inputItems(TagKey<Item> tag) {
      return this.inputItems(tag, 1);
   }

   public RecipeBuilder inputItems(Item input, int amount) {
      if (this.deleted) {
         return this;
      }

      if (this.chance == 10000 && this.itemMaterialInfo) {
         ItemMaterialInfo matStack = ItemMaterialData.getMaterialInfo(input);
         if (matStack != null) {
            for (MaterialStack mat : matStack.getMaterials()) {
               if (this.tempItemMaterialStacks == null) {
                  this.tempItemMaterialStacks = new ArrayList<>();
               }

               this.tempItemMaterialStacks.add(new MaterialStack(mat.material(), mat.amount() * amount));
            }
         }
      }

      return this.inputItems(createSizedIngredient(input, amount));
   }

   public RecipeBuilder inputItems(Item input) {
      return this.deleted ? this : this.inputItems(input, 1);
   }

   public RecipeBuilder inputItems(Supplier<? extends Item> input) {
      return this.deleted ? this : this.inputItems(input.get());
   }

   public RecipeBuilder inputItems(Supplier<? extends Item> input, int amount) {
      return this.deleted ? this : this.inputItems(input.get(), amount);
   }

   public RecipeBuilder inputItems(TagPrefix orePrefix, Material material) {
      return this.inputItems(orePrefix, material, 1);
   }

   public RecipeBuilder inputItems(MaterialEntry input) {
      return this.inputItems(input, 1);
   }

   public RecipeBuilder inputItems(MaterialEntry input, int count) {
      if (this.deleted) {
         return this;
      }

      if (input.material().isNull()) {
         throw new IllegalArgumentException("Unification Entry material is null, id: " + this.id + ", TagPrefix: " + input.tagPrefix());
      }

      if (((ITagPrefix)input.tagPrefix()).gtolib$isTagInput()) {
         TagKey<Item> tag = ChemicalHelper.getTag(input.tagPrefix(), input.material());
         if (tag != null) {
            return this.inputItems(tag, count);
         }
      }

      if (this.chance == 10000 && this.itemMaterialInfo) {
         if (this.tempItemMaterialStacks == null) {
            this.tempItemMaterialStacks = new ArrayList<>();
         }

         this.tempItemMaterialStacks.add(new MaterialStack(input.material(), input.tagPrefix().materialAmount() * count));
      }

      return this.inputItems(createSizedIngredient(input, count));
   }

   public RecipeBuilder inputItems(TagPrefix orePrefix, Material material, int count) {
      return this.deleted ? this : this.inputItems(new MaterialEntry(orePrefix, material), count);
   }

   public RecipeBuilder inputItems(MachineDefinition machine) {
      return this.inputItems(machine, 1);
   }

   public RecipeBuilder inputItems(MachineDefinition machine, int count) {
      return this.inputItems(machine.asStack(count));
   }

   public RecipeBuilder outputItems(Object output) {
      if (this.deleted) {
         return this;
      }

      Object var2 = output;
      byte var3 = 0;

      while (true) {
         RecipeBuilder var10000;
         switch (var3 /* $VF: typeSwitch(var2, var3) */) {
            case 0:
               String string = (String)var2;
               var10000 = this.outputItems(string);
               break;
            case 1: {
               ItemLike item = (ItemLike)var2;
               var10000 = this.outputItems(item.asItem());
               break;
            }
            case 2: {
               Supplier<?> supplier = (Supplier<?>)var2;
               if (!(supplier.get() instanceof ItemLike item)) {
                  var3 = 3;
                  continue;
               }

               var10000 = this.outputItems(item.asItem());
               break;
            }
            case 3:
               ItemStack stack = (ItemStack)var2;
               var10000 = this.outputItems(stack);
               break;
            case 4:
               MaterialEntry entry = (MaterialEntry)var2;
               var10000 = this.outputItems(entry);
               break;
            default:
               throw new IllegalArgumentException(
                  "Output: "
                     + output
                     + " is not one of: Item, Supplier<Item>, ItemStack, Ingredient, MaterialEntry, TagKey<Item>, MachineDefinition, id: "
                     + this.id
               );
         }

         return var10000;
      }
   }

   public RecipeBuilder outputItems(Object output, int count) {
      if (this.deleted) {
         return this;
      }

      Object var3 = output;
      byte var4 = 0;

      while (true) {
         RecipeBuilder var10000;
         switch (var4 /* $VF: typeSwitch(var3, var4) */) {
            case 0:
               String string = (String)var3;
               var10000 = this.outputItems(string, count);
               break;
            case 1: {
               ItemLike item = (ItemLike)var3;
               var10000 = this.outputItems(item.asItem(), count);
               break;
            }
            case 2: {
               Supplier<?> supplier = (Supplier<?>)var3;
               if (!(supplier.get() instanceof ItemLike item)) {
                  var4 = 3;
                  continue;
               }

               var10000 = this.outputItems(item.asItem(), count);
               break;
            }
            case 3:
               ItemStack stack = (ItemStack)var3;
               var10000 = this.outputItems(stack.copyWithCount(count));
               break;
            case 4:
               MaterialEntry entry = (MaterialEntry)var3;
               var10000 = this.outputItems(entry, count);
               break;
            default:
               throw new IllegalArgumentException(
                  "Output: "
                     + output
                     + " is not one of: Item, Supplier<Item>, ItemStack, Ingredient, MaterialEntry, TagKey<Item>, MachineDefinition, id: "
                     + this.id
               );
         }

         return var10000;
      }
   }

   public RecipeBuilder outputItems(Block output, int count) {
      return this.outputItems(output.asItem(), count);
   }

   public RecipeBuilder outputItems(Ingredient output, int count) {
      return this.outputItems(ItemIngredient.of(output, count));
   }

   public RecipeBuilder outputItems(ItemIngredient output) {
      return this.outputItems(new Content<>(output, this.chance, this.tierChanceBoost));
   }

   public RecipeBuilder outputItems(Content<ItemIngredient> content) {
      if (this.itemOutputs == null) {
         this.itemOutputs = new ArrayList<>();
      }

      this.itemOutputs.add(content);
      return this;
   }

   public RecipeBuilder outputItems(Ingredient output) {
      return this.outputItems(ItemIngredient.of(output));
   }

   public RecipeBuilder outputItems(ItemStack output) {
      if (this.deleted) {
         return this;
      }

      if (output.isEmpty()) {
         throw new IllegalArgumentException("Output " + output + " is empty, id: " + this.id);
      }

      if (this.itemsCanBeProduced != null) {
         this.itemsCanBeProduced.add(((IUnique)output.getItem()).ae2$getUid());
      }

      return this.outputItems(createSizedIngredient(output));
   }

   public RecipeBuilder outputItems(ItemStack... outputs) {
      for (ItemStack output : outputs) {
         this.outputItems(output);
      }

      return this;
   }

   public RecipeBuilder outputItems(Item output, int amount) {
      if (this.deleted) {
         return this;
      }

      if (this.itemsCanBeProduced != null) {
         this.itemsCanBeProduced.add(((IUnique)output).ae2$getUid());
      }

      return this.outputItems(createSizedIngredient(output, amount));
   }

   public RecipeBuilder outputItems(Item output) {
      return this.deleted ? this : this.outputItems(output, 1);
   }

   public RecipeBuilder outputItems(Supplier<? extends ItemLike> input) {
      return this.deleted ? this : this.outputItems(input.get().asItem());
   }

   public RecipeBuilder outputItems(Supplier<? extends ItemLike> input, int amount) {
      return this.deleted ? this : this.outputItems(input.get().asItem(), amount);
   }

   public RecipeBuilder outputItems(TagPrefix orePrefix, Material material) {
      return this.outputItems(orePrefix, material, 1);
   }

   public RecipeBuilder outputItems(TagPrefix orePrefix, Material material, int count) {
      return this.deleted ? this : this.outputItems(new MaterialEntry(orePrefix, material), count);
   }

   public RecipeBuilder outputItems(MaterialEntry entry) {
      return this.outputItems(entry, 1);
   }

   public RecipeBuilder outputItems(MaterialEntry output, int count) {
      if (this.deleted) {
         return this;
      }

      if (output.material().isNull()) {
         throw new IllegalArgumentException("Unification Entry material is null, id: " + this.id + ", TagPrefix: " + output.tagPrefix());
      }

      if (this.itemsCanBeProduced != null) {
         this.itemsCanBeProduced.add(((IUnique)ChemicalHelper.getItem(output)).ae2$getUid());
      }

      return this.outputItems(createSizedIngredient(output, count));
   }

   public RecipeBuilder outputItems(MachineDefinition machine) {
      return this.outputItems(machine, 1);
   }

   public RecipeBuilder outputItems(MachineDefinition machine, int count) {
      return this.outputItems(machine.asStack(count));
   }

   public RecipeBuilder notConsumable(ItemStack itemStack) {
      if (this.deleted) {
         return this;
      }

      int lastChance = this.chance;
      this.chance = 0;
      this.inputItems(itemStack);
      this.chance = lastChance;
      return this;
   }

   public RecipeBuilder notConsumable(Ingredient ingredient) {
      if (this.deleted) {
         return this;
      }

      int lastChance = this.chance;
      this.chance = 0;
      this.inputItems(ingredient);
      this.chance = lastChance;
      return this;
   }

   public RecipeBuilder notConsumable(Item item, int count) {
      if (this.deleted) {
         return this;
      }

      int lastChance = this.chance;
      this.chance = 0;
      this.inputItems(item, count);
      this.chance = lastChance;
      return this;
   }

   public RecipeBuilder notConsumable(Item item) {
      if (this.deleted) {
         return this;
      }

      int lastChance = this.chance;
      this.chance = 0;
      this.inputItems(item);
      this.chance = lastChance;
      return this;
   }

   public RecipeBuilder notConsumable(Supplier<? extends Item> item) {
      if (this.deleted) {
         return this;
      }

      int lastChance = this.chance;
      this.chance = 0;
      this.inputItems(item);
      this.chance = lastChance;
      return this;
   }

   public RecipeBuilder notConsumable(TagPrefix orePrefix, Material material) {
      if (this.deleted) {
         return this;
      }

      int lastChance = this.chance;
      this.chance = 0;
      this.inputItems(orePrefix, material);
      this.chance = lastChance;
      return this;
   }

   public RecipeBuilder notConsumable(TagPrefix orePrefix, Material material, int count) {
      if (this.deleted) {
         return this;
      }

      int lastChance = this.chance;
      this.chance = 0;
      this.inputItems(orePrefix, material, count);
      this.chance = lastChance;
      return this;
   }

   public RecipeBuilder notConsumableFluid(Material material, long amount) {
      return this.deleted ? this : this.notConsumableFluid(FluidIngredient.of(material.getFluid(), amount));
   }

   public RecipeBuilder notConsumableFluid(FluidStack fluid) {
      return this.deleted ? this : this.notConsumableFluid(FluidIngredient.of(fluid));
   }

   public RecipeBuilder notConsumableFluid(FluidIngredient ingredient) {
      if (this.deleted) {
         return this;
      }

      int lastChance = this.chance;
      this.chance = 0;
      this.inputFluids(ingredient);
      this.chance = lastChance;
      return this;
   }

   public RecipeBuilder circuitMeta(int configuration) {
      if (this.deleted) {
         return this;
      }

      if (configuration < 0 || configuration > 32) {
         GTOCore.LOGGER.error("Circuit configuration must be in the bounds 0 - 32");
      }

      return (RecipeBuilder)this.notConsumable(IntCircuitIngredient.of(configuration));
   }

   public RecipeBuilder chancedInput(Item item, int chance, int tierChanceBoost) {
      return this.chancedInput(item, 1, chance, tierChanceBoost);
   }

   public RecipeBuilder chancedInput(Item item, int amount, int chance, int tierChanceBoost) {
      if (this.deleted) {
         return this;
      } else if (0 < chance && chance <= 10000) {
         int lastChance = this.chance;
         int lastTierChanceBoost = this.tierChanceBoost;
         this.chance = chance;
         this.tierChanceBoost = tierChanceBoost;
         this.inputItems(item, amount);
         this.chance = lastChance;
         this.tierChanceBoost = lastTierChanceBoost;
         return this;
      } else {
         GTOCore.LOGGER.error("Chance cannot be less or equal to 0 or more than {}. Actual: {}.", 10000, chance, new Throwable());
         return this;
      }
   }

   public RecipeBuilder chancedInput(Fluid fluid, int amount, int chance, int tierChanceBoost) {
      if (this.deleted) {
         return this;
      } else if (0 < chance && chance <= 10000) {
         int lastChance = this.chance;
         int lastTierChanceBoost = this.tierChanceBoost;
         this.chance = chance;
         this.tierChanceBoost = tierChanceBoost;
         this.inputFluids(fluid, amount);
         this.chance = lastChance;
         this.tierChanceBoost = lastTierChanceBoost;
         return this;
      } else {
         GTOCore.LOGGER.error("Chance cannot be less or equal to 0 or more than {}. Actual: {}.", 10000, chance, new Throwable());
         return this;
      }
   }

   public RecipeBuilder chancedOutput(Item item, int chance, int tierChanceBoost) {
      return this.chancedOutput(item, 1, chance, tierChanceBoost);
   }

   public RecipeBuilder chancedOutput(Item item, int amount, int chance, int tierChanceBoost) {
      if (this.deleted) {
         return this;
      } else if (0 < chance && chance <= 10000) {
         int lastChance = this.chance;
         int lastTierChanceBoost = this.tierChanceBoost;
         this.chance = chance;
         this.tierChanceBoost = tierChanceBoost;
         this.outputItems(item, amount);
         this.chance = lastChance;
         this.tierChanceBoost = lastTierChanceBoost;
         return this;
      } else {
         GTOCore.LOGGER.error("Chance cannot be less or equal to 0 or more than {}. Actual: {}.", 10000, chance, new Throwable());
         return this;
      }
   }

   public RecipeBuilder chancedOutput(Fluid fluid, int amount, int chance, int tierChanceBoost) {
      if (this.deleted) {
         return this;
      } else if (0 < chance && chance <= 10000) {
         int lastChance = this.chance;
         int lastTierChanceBoost = this.tierChanceBoost;
         this.chance = chance;
         this.tierChanceBoost = tierChanceBoost;
         this.outputFluids(fluid, amount);
         this.chance = lastChance;
         this.tierChanceBoost = lastTierChanceBoost;
         return this;
      } else {
         GTOCore.LOGGER.error("Chance cannot be less or equal to 0 or more than {}. Actual: {}.", 10000, chance, new Throwable());
         return this;
      }
   }

   public RecipeBuilder chancedInput(ItemStack stack, int chance, int tierChanceBoost) {
      if (this.deleted) {
         return this;
      } else if (0 < chance && chance <= 10000) {
         int lastChance = this.chance;
         int lastTierChanceBoost = this.tierChanceBoost;
         this.chance = chance;
         this.tierChanceBoost = tierChanceBoost;
         this.inputItems(stack);
         this.chance = lastChance;
         this.tierChanceBoost = lastTierChanceBoost;
         return this;
      } else {
         GTOCore.LOGGER.error("Chance cannot be less or equal to 0 or more than {}. Actual: {}.", 10000, chance, new Throwable());
         return this;
      }
   }

   public RecipeBuilder chancedInput(FluidStack stack, int chance, int tierChanceBoost) {
      if (this.deleted) {
         return this;
      } else if (0 < chance && chance <= 10000) {
         int lastChance = this.chance;
         int lastTierChanceBoost = this.tierChanceBoost;
         this.chance = chance;
         this.tierChanceBoost = tierChanceBoost;
         this.inputFluids(stack);
         this.chance = lastChance;
         this.tierChanceBoost = lastTierChanceBoost;
         return this;
      } else {
         GTOCore.LOGGER.error("Chance cannot be less or equal to 0 or more than {}. Actual: {}.", 10000, chance, new Throwable());
         return this;
      }
   }

   public RecipeBuilder chancedOutput(ItemStack stack, int chance, int tierChanceBoost) {
      if (this.deleted) {
         return this;
      } else if (0 < chance && chance <= 10000) {
         int lastChance = this.chance;
         int lastTierChanceBoost = this.tierChanceBoost;
         this.chance = chance;
         this.tierChanceBoost = tierChanceBoost;
         this.outputItems(stack);
         this.chance = lastChance;
         this.tierChanceBoost = lastTierChanceBoost;
         return this;
      } else {
         GTOCore.LOGGER.error("Chance cannot be less or equal to 0 or more than {}. Actual: {}.", 10000, chance, new Throwable());
         return this;
      }
   }

   public RecipeBuilder chancedOutput(FluidStack stack, int chance, int tierChanceBoost) {
      if (this.deleted) {
         return this;
      } else if (0 < chance && chance <= 10000) {
         int lastChance = this.chance;
         int lastTierChanceBoost = this.tierChanceBoost;
         this.chance = chance;
         this.tierChanceBoost = tierChanceBoost;
         this.outputFluids(stack);
         this.chance = lastChance;
         this.tierChanceBoost = lastTierChanceBoost;
         return this;
      } else {
         GTOCore.LOGGER.error("Chance cannot be less or equal to 0 or more than {}. Actual: {}.", 10000, chance, new Throwable());
         return this;
      }
   }

   public RecipeBuilder chancedOutput(TagPrefix tag, Material mat, int chance, int tierChanceBoost) {
      return this.deleted ? this : this.chancedOutput(ChemicalHelper.get(tag, mat), chance, tierChanceBoost);
   }

   public RecipeBuilder chancedOutput(TagPrefix tag, Material mat, int count, int chance, int tierChanceBoost) {
      return this.deleted ? this : this.chancedOutput(ChemicalHelper.get(tag, mat, count), chance, tierChanceBoost);
   }

   public RecipeBuilder inputFluids(Fluid fluid, long amount) {
      return this.deleted ? this : this.inputFluids(FluidIngredient.of(fluid, amount));
   }

   public RecipeBuilder inputFluids(Material material, int amount) {
      if (this.chance == 10000 && this.fluidMaterialInfo) {
         if (this.tempFluidStacks == null) {
            this.tempFluidStacks = new ArrayList<>();
         }

         this.tempFluidStacks.add(new MaterialStack(material, amount * 3628800L / 144L));
      }

      return this.inputFluids(material.getFluid(), amount);
   }

   public RecipeBuilder inputFluids(Material material, FluidStorageKey key, long amount) {
      return this.inputFluids(material.getFluid(key), amount);
   }

   public RecipeBuilder inputFluids(FluidStack input) {
      return this.deleted ? this : this.inputFluids(FluidIngredient.of(input));
   }

   public RecipeBuilder inputFluids(FluidIngredient inputs) {
      return this.deleted ? this : this.inputFluids(new Content<>(inputs, this.chance, this.tierChanceBoost));
   }

   public RecipeBuilder inputFluids(Content<FluidIngredient> inputs) {
      if (this.fluidInputs == null) {
         this.fluidInputs = new ArrayList<>();
      }

      this.fluidInputs.add(inputs);
      return this;
   }

   public RecipeBuilder outputFluids(Material material, int amount) {
      return this.outputFluids(material.getFluid(), amount);
   }

   public RecipeBuilder outputFluids(Material material, FluidStorageKey key, int amount) {
      return this.outputFluids(material.getFluid(key), amount);
   }

   public RecipeBuilder outputFluids(Fluid fluid, long amount) {
      if (this.deleted) {
         return this;
      }

      if (this.fluidsCanBeProduced != null) {
         this.fluidsCanBeProduced.add(((IUnique)fluid).ae2$getUid());
      }

      return this.outputFluids(FluidIngredient.of(fluid, amount));
   }

   public RecipeBuilder outputFluids(FluidStack output) {
      if (this.deleted) {
         return this;
      }

      if (this.fluidsCanBeProduced != null) {
         this.fluidsCanBeProduced.add(((IUnique)output.getFluid()).ae2$getUid());
      }

      return this.outputFluids(FluidIngredient.of(output));
   }

   public RecipeBuilder outputFluids(FluidStack... outputs) {
      if (this.deleted) {
         return this;
      }

      for (FluidStack output : outputs) {
         this.outputFluids(output);
      }

      return this;
   }

   public RecipeBuilder outputFluids(FluidIngredient outputs) {
      return this.deleted ? this : this.outputFluids(new Content<>(outputs, this.chance, this.tierChanceBoost));
   }

   public RecipeBuilder outputFluids(Content<FluidIngredient> inputs) {
      if (this.fluidOutputs == null) {
         this.fluidOutputs = new ArrayList<>();
      }

      this.fluidOutputs.add(inputs);
      return this;
   }

   public <T> RecipeBuilder addData(DataComponentKey<T> key, T data) {
      if (this.deleted) {
         return this;
      }

      if (this.data == null) {
         this.data = new DataComponentMap();
      }

      this.data.put(key, data);
      return this;
   }

   public RecipeBuilder blastFurnaceTemp(int blastTemp) {
      return this.deleted ? this : this.addData(GTRecipeDataKeys.EBF_TEMP, blastTemp);
   }

   public RecipeBuilder explosivesAmount(int explosivesAmount) {
      return this.deleted ? this : this.inputItems(new ItemStack(Blocks.TNT, explosivesAmount));
   }

   public RecipeBuilder explosivesType(ItemStack explosivesType) {
      return this.deleted ? this : this.inputItems(explosivesType);
   }

   public RecipeBuilder solderMultiplier(int multiplier) {
      return this.deleted ? this : this.addData(GTRecipeDataKeys.SOLDER_MULTIPLIER, multiplier);
   }

   public RecipeBuilder disableDistilleryRecipes(boolean flag) {
      return this.deleted ? this : this.addData(GTRecipeDataKeys.DISABLE_DISTILLERY, flag);
   }

   public RecipeBuilder fusionStartEU(long eu) {
      return this.deleted ? this : this.addData(GTRecipeDataKeys.EU_TO_START, eu);
   }

   public RecipeBuilder researchScan(boolean isScan) {
      return this.deleted ? this : this.addData(GTRecipeDataKeys.SCAN_FOR_RESEARCH, isScan);
   }

   public RecipeBuilder durationIsTotalCWU(boolean durationIsTotalCWU) {
      return this.deleted ? this : this.addData(GTRecipeDataKeys.DURATION_IS_TOTAL_CWU, durationIsTotalCWU);
   }

   public RecipeBuilder hideDuration(boolean hideDuration) {
      return this.deleted ? this : this.addData(GTRecipeDataKeys.HIDE_DURATION, hideDuration);
   }

   public RecipeBuilder cleanroom(CleanroomType cleanroomType) {
      return this.deleted ? this : this.addCondition(CleanroomCondition.get(cleanroomType));
   }

   public RecipeBuilder dimension(ResourceKey<Level> dimension, boolean reverse) {
      return this.deleted ? this : this.addCondition(new DimensionCondition(reverse, dimension));
   }

   public RecipeBuilder dimension(ResourceLocation dimension, boolean reverse) {
      return this.deleted ? this : this.dimension(GTODimensions.getDimensionKey(dimension), false);
   }

   public RecipeBuilder dimension(ResourceKey<Level> dimension) {
      return this.deleted ? this : this.dimension(dimension, false);
   }

   public RecipeBuilder dimension(ResourceLocation dimension) {
      return this.deleted ? this : this.dimension(dimension, false);
   }

   public RecipeBuilder biome(ResourceLocation biome, boolean reverse) {
      return this.biome(ResourceKey.create(Registries.BIOME, biome), reverse);
   }

   public RecipeBuilder biome(ResourceLocation biome) {
      return this.biome(biome, false);
   }

   public RecipeBuilder biome(ResourceKey<Biome> biome, boolean reverse) {
      return this.addCondition(new BiomeCondition(reverse, biome));
   }

   public RecipeBuilder biome(ResourceKey<Biome> biome) {
      return this.biome(biome, false);
   }

   public RecipeBuilder rain(float level, boolean reverse) {
      return this.deleted ? this : this.addCondition(new RainingCondition(reverse, level));
   }

   public RecipeBuilder rain(float level) {
      return this.deleted ? this : this.rain(level, false);
   }

   public RecipeBuilder thunder(float level, boolean reverse) {
      return this.deleted ? this : this.addCondition(new ThunderCondition(reverse, level));
   }

   public RecipeBuilder thunder(float level) {
      return this.deleted ? this : this.thunder(level, false);
   }

   public RecipeBuilder posY(int min, int max, boolean reverse) {
      return this.deleted ? this : this.addCondition(new PositionYCondition(reverse, min, max));
   }

   public RecipeBuilder posY(int min, int max) {
      return this.deleted ? this : this.posY(min, max, false);
   }

   public RecipeBuilder daytime(boolean isNight) {
      return this.deleted ? this : this.addCondition(isNight ? DaytimeCondition.NIGHT : DaytimeCondition.DAY);
   }

   public RecipeBuilder daytime() {
      return this.deleted ? this : this.daytime(false);
   }

   public RecipeBuilder addModifier(RecipeModifier modifier) {
      return (RecipeBuilder)super.addModifier(modifier);
   }

   public RecipeBuilder adjacentBlock(Block A, Block B) {
      return this.addCondition(new AdjacentBlockCondition(false, A, B));
   }

   public RecipeBuilder adjacentBlock(Block A, Block B, boolean reverse) {
      return this.addCondition(new AdjacentBlockCondition(reverse, A, B));
   }

   public RecipeBuilder adjacentFluid(Fluid A, Fluid B) {
      return this.addCondition(new AdjacentFluidCondition(false, A, B));
   }

   public RecipeBuilder adjacentFluid(Fluid A, Fluid B, boolean reverse) {
      return this.addCondition(new AdjacentFluidCondition(reverse, A, B));
   }

   public RecipeBuilder ftbQuest(String questId, boolean isReverse) {
      if (!Mods.isFTBQuestsLoaded()) {
         GTCEu.LOGGER.error("FTBQuests is not loaded!");
         return this;
      } else if (questId.isEmpty()) {
         GTCEu.LOGGER.error("Quest ID cannot be empty for recipe {}", this.id);
         return this;
      } else {
         long qID = QuestObjectBase.parseCodeString(questId);
         if (qID == 0L) {
            GTCEu.LOGGER.error("Quest {} not found for recipe {}", questId, this.id);
            return this;
         } else {
            return this.addCondition(new FTBQuestCondition(isReverse, qID));
         }
      }
   }

   public RecipeBuilder ftbQuest(String questId) {
      return this.ftbQuest(questId, false);
   }

   public RecipeBuilder category(GTRecipeCategory category) {
      this.recipeCategory = category;
      return this;
   }

   public RecipeBuilder id(ResourceLocation id) {
      this.id = id;
      return this;
   }

   public RecipeBuilder recipeType(GTRecipeType recipeType) {
      this.recipeType = recipeType;
      return this;
   }

   public RecipeBuilder duration(int duration) {
      this.duration = duration;
      return this;
   }

   public RecipeBuilder chance(int chance) {
      this.chance = chance;
      return this;
   }

   public RecipeBuilder tierChanceBoost(int tierChanceBoost) {
      this.tierChanceBoost = tierChanceBoost;
      return this;
   }

   public RecipeBuilder onSave(@Nullable Consumer<GTRecipeBuilder> onSave) {
      if (GTRegistries.RECIPE_TYPES.isFrozen()) {
         throw new IllegalStateException("[register] registry %s has been frozen".formatted(this.recipeType));
      }

      this.onSave = onSave;
      return this;
   }

   public RecipeBuilder addMaterialInfo() {
      return (RecipeBuilder)super.addMaterialInfo(true, true);
   }

   public RecipeBuilder addMaterialInfo(boolean item) {
      return (RecipeBuilder)super.addMaterialInfo(item);
   }

   public RecipeBuilder addMaterialInfo(boolean item, boolean fluid) {
      return (RecipeBuilder)super.addMaterialInfo(item, fluid);
   }

   public RecipeBuilder researchStation(UnaryOperator<StationBuilder> research) {
      return (RecipeBuilder)super.researchStation(research);
   }

   public RecipeBuilder researchNode(TechNode research) {
      this.researchNode = research;
      this.addCondition(new ResearchRecipeCondition(research, getTypeID(this.id, this.recipeType), this.recipeType));
      return this;
   }

   public RecipeBuilder scanner(UnaryOperator<ScannerBuilder> research) {
      return (RecipeBuilder)super.scanner(research);
   }

   public RecipeBuilder scanner(ItemStack researchStack) {
      return this.scanner(b -> b.researchStack(researchStack));
   }

   public RecipeBuilder scanner(ItemLike researchStack) {
      return this.scanner(b -> b.researchStack(researchStack));
   }

   @Override
   public GTRecipeDefinition build() {
      return new GTRecipeDefinition(
         false,
         this.recipeType,
         this.recipeCategory,
         this.id.withPrefix(this.recipeType.registryName.getPath() + "/"),
         this.getItemInputs(),
         this.getItemOutputs(),
         this.getFluidInputs(),
         this.getFluidOutputs(),
         ImmutableList.copyOf(this.getModifiers()),
         ImmutableList.copyOf(this.getConditions()),
         ImmutableList.copyOf(this.getRecipeExtensions()),
         ImmutableList.copyOf(this.getTickRecipeExtensions()),
         this.getData(),
         this.chanceFunction,
         this.eut,
         this.tier,
         this.duration,
         this.priority
      );
   }

   @Override
   public GTRecipeDefinition build(boolean registered) {
      return this.build();
   }

   @Override
   public GTRecipeDefinition save() {
      if (!this.deleted && MATERIAL_INGREDIENT_MAP != null) {
         ResourceLocation typeid = getTypeID(this.id, this.recipeType);
         if (this.recipeType != GTRecipeTypes.RESEARCH_STATION_RECIPES
            && this.recipeType != GTRecipeTypes.MACERATOR_RECIPES
            && RecipeScript.get(typeid) != null) {
            throw new IllegalStateException("Recipe with id " + typeid + " already exists!");
         }

         if (!RecipeLogicExt.isCallerTrusted()) {
            return GTRecipeTypes.DUMMY_RECIPES.defaultDefinition;
         }

         if (this.onSave != null) {
            this.onSave.accept(this);
         }

         RecipeType recipeTypeExt = (RecipeType)this.recipeType;
         if (this.itemsCanBeProduced != null) {
            recipeTypeExt.itemsCanBeProduced.addAll(this.itemsCanBeProduced);
         }

         if (this.fluidsCanBeProduced != null) {
            recipeTypeExt.fluidsCanBeProduced.addAll(this.fluidsCanBeProduced);
         }

         this.itemsCanBeProduced = null;
         this.fluidsCanBeProduced = null;
         if (this.itemMaterialInfo) {
            this.addOutputMaterialInfo();
         }

         GTRecipeDefinition recipe = new GTRecipeDefinition(
            true,
            this.recipeType,
            this.recipeCategory,
            typeid,
            ImmutableList.copyOf(this.getItemInputs()),
            ImmutableList.copyOf(this.getItemOutputs()),
            ImmutableList.copyOf(this.getFluidInputs()),
            ImmutableList.copyOf(this.getFluidOutputs()),
            ImmutableList.copyOf(this.getModifiers()),
            ImmutableList.copyOf(this.getConditions()),
            ImmutableList.copyOf(this.getRecipeExtensions()),
            ImmutableList.copyOf(this.getTickRecipeExtensions()),
            this.getData(),
            this.chanceFunction,
            this.eut,
            this.tier,
            this.duration,
            this.priority
         );
         this.getConditions()
            .stream()
            .filter(ResearchCondition.class::isInstance)
            .findAny()
            .map(ResearchCondition.class::cast)
            .ifPresent(condition -> this.recipeType.addDataStickEntry(condition.researchId, recipe));
         RecipeScript.add(recipe);
         if (this.researchNode != null) {
            this.researchNode.addRecipeToNode(recipe);
         }

         return recipe;
      } else {
         return GTRecipeTypes.DUMMY_RECIPES.defaultDefinition;
      }
   }

   public static ResourceLocation getTypeID(ResourceLocation id, GTRecipeType recipeType) {
      return RLUtils.fromNamespaceAndPath(id.getNamespace(), recipeType.registryName.getPath() + "/" + id.getPath());
   }

   public RecipeBuilder notConsumableFluid(Material material, int amount) {
      return this.notConsumableFluid(material.getFluid(amount));
   }

   public RecipeBuilder notConsumable(TagKey<Item> tag) {
      int lastChance = this.chance;
      this.chance = 0;
      this.inputItems(tag);
      this.chance = lastChance;
      return this;
   }

   public RecipeBuilder notConsumable(String id) {
      return this.notConsumable(RegistriesUtils.getItem(id));
   }

   public RecipeBuilder notConsumable(String id, int count) {
      return this.notConsumable(RegistriesUtils.getItemStack(id, count));
   }

   public RecipeBuilder inputItemTags(ResourceLocation id) {
      return this.inputItems(TagUtils.createItemTag(id));
   }

   public RecipeBuilder inputItemTags(String id) {
      return this.inputItems(TagUtils.createItemTag(id));
   }

   public RecipeBuilder inputItems(String id) {
      return this.inputItems(RegistriesUtils.getItem(id));
   }

   public RecipeBuilder inputItems(String id, int count) {
      return this.inputItems(RegistriesUtils.getItem(id), count);
   }

   public RecipeBuilder chancedInput(String id, int chance, int tierChanceBoost) {
      return this.chancedInput(RegistriesUtils.getItemStack(id), chance, tierChanceBoost);
   }

   public RecipeBuilder outputItems(String id) {
      return this.outputItems(RegistriesUtils.getItem(id));
   }

   public RecipeBuilder outputItems(String id, int count) {
      return this.outputItems(RegistriesUtils.getItem(id), count);
   }

   public RecipeBuilder vacuum(int tier) {
      return this.addCondition(new VacuumCondition(tier));
   }

   public RecipeBuilder gravity(boolean noGravity) {
      return this.addCondition(new GravityCondition(noGravity));
   }

   public RecipeBuilder heat(int temperature) {
      return this.addCondition(new HeatCondition(temperature));
   }

   public RecipeBuilder euVATier(int tier) {
      return this.EUt(GTOValues.VAEX[tier]);
   }

   public RecipeBuilder MANAt(long mana) {
      if (mana == 0L) {
         return this;
      }

      this.addData(MANATRecipeExtension.INSTANCE, mana);
      this.addTickExtension(MANATRecipeExtension.INSTANCE);
      if (this.recipeType != GTRecipeTypes.DUMMY_RECIPES) {
         this.tier = GTUtil.getTierByVoltage(Math.abs(mana)) + 1;
      }

      return this;
   }

   public RecipeBuilder MANA(long mana) {
      if (mana == 0L) {
         return this;
      }

      this.addData(MANARecipeExtension.INSTANCE, mana);
      this.addExtension(MANARecipeExtension.INSTANCE);
      return this;
   }

   public RecipeBuilder setDescriptionSupplier(ComponentListSupplier descriptionSupplier) {
      this.descriptionSupplier = descriptionSupplier;
      return this;
   }

   public RecipeBuilder HUt(long hu) {
      if (hu == 0L) {
         return this;
      }

      this.addData(HUTRecipeExtension.INSTANCE, hu);
      this.addTickExtension(HUTRecipeExtension.INSTANCE);
      return this;
   }

   public RecipeBuilder HU(long hu) {
      if (hu == 0L) {
         return this;
      }

      this.addData(HURecipeExtension.INSTANCE, hu);
      this.addExtension(HURecipeExtension.INSTANCE);
      return this;
   }

   public RecipeBuilder researchPoints(ResearchTag tag, int points) {
      if (this.data != null && this.data.containsKey(ResearchPointsRecipeExtion.INSTANCE)) {
         ResearchPoints researchPoints = this.data.getData(ResearchPointsRecipeExtion.INSTANCE);
         researchPoints.addTo(tag, points);
      } else {
         this.addData(ResearchPointsRecipeExtion.INSTANCE, ResearchPoints.of(tag, points));
      }

      this.addExtension(ResearchPointsRecipeExtion.INSTANCE);
      return this;
   }

   public RecipeBuilder temperature(int temperature) {
      return this.addData(GTORecipeDataKeys.TEMPERATURE, temperature);
   }

   public RecipeBuilder runLimit(int count) {
      return this.addCondition(new RunLimitCondition(count));
   }

   public RecipeBuilder restrictedMachine(ResourceLocation id) {
      return this.addCondition(new RestrictedMachineCondition(id));
   }

   private void addOutputMaterialInfo() {
      List<Content<ItemIngredient>> itemOutputs = this.getItemOutputs();
      List<Content<ItemIngredient>> itemInputs = this.getItemInputs();
      if (itemOutputs.size() == 1 && (!itemInputs.isEmpty() || this.tempFluidStacks != null && !this.tempFluidStacks.isEmpty())) {
         ItemIngredient currOutput = (ItemIngredient)itemOutputs.getFirst().inner;
         Item out = null;
         int outputCount = 0;
         if (!currOutput.isEmpty()) {
            ItemStack items = currOutput.getInnerItemStack();
            if (!items.isEmpty()) {
               out = items.getItem();
               outputCount = currOutput.getAmount();
            }
         }

         if (out == null || out == Items.AIR) {
            return;
         }

         Reference2LongOpenHashMap<Material> matStacks = new Reference2LongOpenHashMap<>();
         if (this.itemMaterialInfo && this.tempItemMaterialStacks != null) {
            for (MaterialStack input : this.tempItemMaterialStacks) {
               long am = input.amount() / outputCount;
               matStacks.addTo(input.material(), am);
            }
         }

         if (this.fluidMaterialInfo && this.tempFluidStacks != null) {
            for (MaterialStack input : this.tempFluidStacks) {
               long am = input.amount() / outputCount;
               matStacks.addTo(input.material(), am);
            }
         }

         if (!matStacks.isEmpty()) {
            ItemMaterialData.registerMaterialInfo(out, new ItemMaterialInfo(matStacks));
         }
      }
   }

   public RecipeBuilder priority(int priority) {
      this.priority = priority;
      return this;
   }

   public RecipeBuilder tier(int tier) {
      this.tier = tier;
      return this;
   }

   static {
      ShapedRecipeBuilder.INGREDIENT_ITEM_FUNCTION = i -> ((IItem)i).gtolib$getIngredient();
      ShapedRecipeBuilder.INGREDIENT_TAG_FUNCTION = t -> ((ITagKey)(Object)t).gtolib$getIngredient();
      ResearchManager.DATA_ITEM_PROVIDER = cwut -> {
         if (cwut > 16384) {
            return (Item)GTOItems.MICROCOSM.get();
         } else if (cwut > 4096) {
            return (Item)GTOItems.OBSIDIAN_MATRIX.get();
         } else if (cwut > 1024) {
            return (Item)GTOItems.ATOMIC_ARCHIVES.get();
         } else if (cwut > 256) {
            return (Item)GTOItems.NEURAL_MATRIX.get();
         } else {
            return cwut > 32 ? GTItems.TOOL_DATA_MODULE.get() : GTItems.TOOL_DATA_ORB.get();
         }
      };
   }
}
