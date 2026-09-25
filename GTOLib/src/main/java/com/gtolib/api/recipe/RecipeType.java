package com.gtolib.api.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.gui.SteamTexture;
import com.gregtechceu.gtceu.api.recipe.GTRecipeBuilder;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType.ICustomRecipeLogic;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.info.EURecipeInfo;
import com.gregtechceu.gtceu.api.recipe.info.FluidRecipeInfo;
import com.gregtechceu.gtceu.api.recipe.info.ItemRecipeInfo;
import com.gregtechceu.gtceu.api.recipe.info.RecipeInfo;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.ItemIngredient;
import com.gregtechceu.gtceu.api.recipe.ui.GTRecipeTypeUI;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.sound.SoundEntry;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gto.fastcollection.fastutil.OpenCacheHashSet;
import com.gto.recipesearch.IntLongMap;
import com.gtolib.api.fluid.IFluid;
import com.gtolib.api.recipe.lookup.IngredientConverter;
import com.gtolib.api.recipe.lookup.MapIngredient;
import com.gtolib.gtm.RecipeScript;
import com.gtolib.utils.GTOUtils;
import com.gtolib.utils.RLUtils;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture.FillDirection;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RecipeType extends GTRecipeType {
   Map<ResourceLocation, Boolean> filter;
   public final IntSet itemsCanBeProduced = new IntOpenHashSet();
   public final IntSet fluidsCanBeProduced = new IntOpenHashSet();
   public boolean specialConverter = false;
   private IngredientConverter<Ingredient> ingredientConverter = MapIngredient.INGREDIENT_CONVERTER;
   private IngredientConverter<ItemStack> itemConverter = MapIngredient.ITEM_CONVERTER;
   private static boolean initSearch;

   public RecipeType(ResourceLocation registryName, String group, net.minecraft.world.item.crafting.RecipeType<?>... proxyRecipes) {
      super(registryName, group, proxyRecipes);
      this.setRecipeBuilder(new RecipeBuilder(registryName, this));
   }

   public void convertIngredient(Ingredient ingredient, long amount, IntLongMap map) {
      this.ingredientConverter.convert(ingredient, amount, map);
   }

   @Override
   public void convertItem(ItemIngredient ingredient, IntLongMap map) {
      MapIngredient.convert(this, map, ingredient);
   }

   @Override
   public void convertFluid(FluidIngredient ingredient, IntLongMap map) {
      MapIngredient.convert(this, map, ingredient);
   }

   @Override
   public void convertItem(ItemStack stack, long amount, IntLongMap map) {
      this.itemConverter.convert(stack, amount, map);
   }

   @Override
   public void convertFluid(FluidStack stack, long amount, IntLongMap map) {
      map.add(((IFluid)stack.getFluid()).gtolib$getMapFluid(), amount);
   }

   @Override
   public boolean search(RecipeHandlerUnit unit, IntLongMap map, BiPredicate<RecipeHandlerUnit, GTRecipeDefinition> canHandle) {
      return this.db == null ? false : this.db.search(unit, map, canHandle);
   }

   public static void initSearch() {
      if (!initSearch) {
         initSearch = true;
         GTOUtils.asyncExecute(
            () -> {
               GTRegistries.RECIPE_TYPES.forEach(t -> t.recipes.clear());
               RecipeScript.values().forEach(r -> {
                  if (r.recipeType instanceof RecipeType recipeType) {
                     recipeType.recipes.put(r.id, r);
                  }
               });
               GTRegistries.RECIPE_TYPES.forEach(t -> {
                  if (t instanceof RecipeType recipeType) {
                     if (recipeType.noSearch) {
                        recipeType.db = null;
                     } else {
                        recipeType.initDB();
                     }
                  }
               });
               MapIngredient.INGREDIENT_BUILDER
                  .reference2ObjectEntrySet()
                  .fastForEach(
                     entry -> entry.getKey().gtolib$setMapItem(entry.getValue().stream().mapToInt(obj -> MapIngredient.COUNT_MAP.getInt(obj)).toArray())
                  );
               MapIngredient.COUNT_MAP = null;
               MapIngredient.INGREDIENT_BUILDER = null;
            }
         );
      }
   }

   public RecipeBuilder recipeBuilder(ResourceLocation id) {
      RecipeBuilder builder = this.getRecipeBuilder().copy(id);
      if (this.filter != null && this.filter.containsKey(id)) {
         builder.deleted = true;
         this.filter.put(id, true);
      }

      return builder;
   }

   public RecipeBuilder recipeBuilder(ResourceLocation id, Object... append) {
      if (append.length > 0) {
         String toAppend = Arrays.stream(append).map(Object::toString).map(FormattingUtil::toLowerCaseUnderscore).reduce("", (a, b) -> a + "_" + b);
         id = id.withSuffix(toAppend);
      }

      return this.recipeBuilder(id);
   }

   public RecipeBuilder recipeBuilder(String id) {
      return this.recipeBuilder(GTCEu.id(id));
   }

   public RecipeBuilder recipeBuilder(String id, Object... append) {
      return this.recipeBuilder(GTCEu.id(id), append);
   }

   public RecipeBuilder copyFrom(GTRecipeBuilder builder) {
      return this.getRecipeBuilder().copyFrom(builder);
   }

   public RecipeType onRecipeBuild(Consumer<GTRecipeBuilder> onBuild) {
      this.getRecipeBuilder().onSave(onBuild);
      return this;
   }

   public RecipeType setMaxIOSize(int maxInputs, int maxOutputs, int maxFluidInputs, int maxFluidOutputs) {
      return this.setMaxSize(IO.IN, ItemRecipeInfo.INSTANCE, maxInputs)
         .setMaxSize(IO.IN, FluidRecipeInfo.INSTANCE, maxFluidInputs)
         .setMaxSize(IO.OUT, ItemRecipeInfo.INSTANCE, maxOutputs)
         .setMaxSize(IO.OUT, FluidRecipeInfo.INSTANCE, maxFluidOutputs);
   }

   public RecipeType setEUIO(IO io) {
      if (io.support(IO.IN)) {
         this.setMaxSize(IO.IN, EURecipeInfo.INSTANCE, 1);
      }

      if (io.support(IO.OUT)) {
         this.setMaxSize(IO.OUT, EURecipeInfo.INSTANCE, 1);
      }

      return this.setMaxTooltips(3);
   }

   public RecipeType setMaxSize(IO io, RecipeInfo cap, int max) {
      if (io == IO.IN || io == IO.BOTH) {
         this.maxInputs.put(cap, max);
      }

      if (io == IO.OUT || io == IO.BOTH) {
         this.maxOutputs.put(cap, max);
      }

      return this;
   }

   public RecipeType setSlotOverlay(boolean isOutput, boolean isFluid, IGuiTexture slotOverlay) {
      return (RecipeType)super.setSlotOverlay(isOutput, isFluid, slotOverlay);
   }

   public RecipeType setSlotOverlay(boolean isOutput, boolean isFluid, boolean isLast, IGuiTexture slotOverlay) {
      return (RecipeType)super.setSlotOverlay(isOutput, isFluid, isLast, slotOverlay);
   }

   public RecipeType setProgressBar(ResourceTexture progressBar, FillDirection moveType) {
      return (RecipeType)super.setProgressBar(progressBar, moveType);
   }

   public RecipeType setSteamProgressBar(SteamTexture progressBar, FillDirection moveType) {
      return (RecipeType)super.setSteamProgressBar(progressBar, moveType);
   }

   public RecipeType setUiBuilder(BiConsumer<GTRecipeDefinition, WidgetGroup> uiBuilder) {
      return (RecipeType)super.setUiBuilder(uiBuilder);
   }

   public RecipeType setMaxTooltips(int maxTooltips) {
      return (RecipeType)super.setMaxTooltips(maxTooltips);
   }

   public RecipeType setXEIVisible(boolean XEIVisible) {
      return (RecipeType)super.setXEIVisible(XEIVisible);
   }

   public RecipeType addDataInfo(Function<GTRecipeDefinition, String> dataInfo) {
      if (GTRegistries.RECIPE_TYPES.isFrozen()) {
         throw new IllegalStateException("[register] registry %s has been frozen".formatted(this.registryName));
      }

      this.dataInfos.add(dataInfo);
      return this;
   }

   public RecipeType addCustomRecipeLogic(ICustomRecipeLogic recipeLogic) {
      if (GTRegistries.RECIPE_TYPES.isFrozen()) {
         throw new IllegalStateException("[register] registry %s has been frozen".formatted(this.registryName));
      } else {
         return (RecipeType)super.addCustomRecipeLogic(recipeLogic);
      }
   }

   public RecipeType prepareBuilder(Consumer<GTRecipeBuilder> onPrepare) {
      if (GTRegistries.RECIPE_TYPES.isFrozen()) {
         throw new IllegalStateException("[register] registry %s has been frozen".formatted(this.registryName));
      } else {
         return (RecipeType)super.prepareBuilder(onPrepare);
      }
   }

   public RecipeType setRecipeBuilder(GTRecipeBuilder recipeBuilder) {
      if (GTRegistries.RECIPE_TYPES.isFrozen()) {
         throw new IllegalStateException("[register] registry %s has been frozen".formatted(this.registryName));
      } else {
         return (RecipeType)super.setRecipeBuilder(recipeBuilder);
      }
   }

   public RecipeType setRecipeUI(GTRecipeTypeUI recipeUI) {
      return (RecipeType)super.setRecipeUI(recipeUI);
   }

   public RecipeType setSmallRecipeMap(GTRecipeType smallRecipeMap) {
      return (RecipeType)super.setSmallRecipeMap(smallRecipeMap);
   }

   public RecipeType getSmallRecipeMap() {
      return (RecipeType)super.getSmallRecipeMap();
   }

   public RecipeType setIconSupplier(@Nullable Supplier<ItemStack> iconSupplier) {
      return (RecipeType)super.setIconSupplier(iconSupplier);
   }

   public RecipeType setSound(@Nullable SoundEntry sound) {
      this.sound = sound;
      return this;
   }

   @Nullable
   @Override
   public Collection<GTRecipeDefinition> getDataStickEntry(@NotNull String researchId) {
      Collection<GTRecipeDefinition> r = super.getDataStickEntry(researchId);
      if (r == null) {
         GTRecipeDefinition r1 = this.recipes.get(RLUtils.parse(researchId));
         if (r1 != null) {
            return Collections.singleton(r1);
         }
      }

      return r;
   }

   public RecipeType setScanner(boolean isScanner) {
      this.isScanner = isScanner;
      return this;
   }

   public RecipeType setHasResearchSlot(boolean hasResearchSlot) {
      this.hasResearchSlot = hasResearchSlot;
      return this;
   }

   public RecipeType setOffsetVoltageText(boolean offsetVoltageText) {
      return (RecipeType)super.setOffsetVoltageText(offsetVoltageText);
   }

   public RecipeType setVoltageTextOffset(int voltageTextOffset) {
      return (RecipeType)super.setVoltageTextOffset(voltageTextOffset);
   }

   @Override
   public Set<GTRecipeCategory> getCategories() {
      Set<GTRecipeCategory> categories = new OpenCacheHashSet<>(this.categoryMap.keySet());
      this.getProxyRecipes().forEach(t -> {
         if (t instanceof RecipeType recipeType) {
            categories.addAll(recipeType.getCategories());
         }
      });
      return categories;
   }

   public RecipeBuilder builder(String id, Object... append) {
      return this.recipeBuilder(id, append);
   }

   public void addFilter(String id) {
      this.filter.put(GTCEu.id(id), false);
   }

   public RecipeType noSearch(boolean noSearch) {
      this.noSearch = noSearch;
      return this;
   }

   public boolean noSearch() {
      return this.noSearch;
   }

   public RecipeType ingredientConverter(IngredientConverter<Ingredient> ingredientConverter) {
      this.ingredientConverter = ingredientConverter;
      return this;
   }

   public RecipeType itemConverter(IngredientConverter<ItemStack> itemConverter) {
      this.itemConverter = itemConverter;
      this.specialConverter = itemConverter != MapIngredient.ITEM_CONVERTER;
      return this;
   }

   private RecipeBuilder getRecipeBuilder() {
      return (RecipeBuilder)this.recipeBuilder;
   }
}
