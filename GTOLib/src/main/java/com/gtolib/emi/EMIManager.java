package com.gtolib.emi;

import com.glodblock.github.extendedae.common.items.InfinityCell;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKey;
import com.gregtechceu.gtceu.api.item.LampBlockItem;
import com.gregtechceu.gtceu.common.item.GTTurbineItemCoated;
import com.gregtechceu.gtceu.integration.emi.recipe.GTRecipeEMICategory;
import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import com.gto.fastcollection.fastutil.O2OOpenCustomCacheHashMap;
import com.gto.fastcollection.fastutil.OpenCacheHashSet;
import com.gtocore.common.data.GTORecipes;
import com.gtocore.config.GTOConfig;
import com.gtocore.data.Data;
import com.gtocore.data.recipe.ae2.GTOInfCells;
import com.gtocore.integration.emi.GTEMIRecipe;
import com.gtolib.api.GTOApi;
import com.gtolib.api.emi.stack.EmiTagprefixStack;
import com.gtolib.gtm.RecipeScript;
import com.gtolib.utils.FluidUtils;
import com.gtolib.utils.ItemUtils;
import dev.emi.emi.EmiPort;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.EmiRecipeManager;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.registry.EmiRecipes;
import dev.emi.emi.registry.EmiStackList.ComparisonHashStrategy;
import dev.emi.emi.runtime.EmiLog;
import it.unimi.dsi.fastutil.Hash.Strategy;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public final class EMIManager implements EmiRecipeManager {
   public static volatile boolean searchBake;
   public static volatile boolean searchBaking;
   private static final Strategy<EmiStack> STACK_STRATEGY = new ComparisonHashStrategy();
   private static Map<String, List<EmiStack>> items = new LinkedHashMap<>();
   private static Map<String, List<EmiStack>> fluids = new LinkedHashMap<>();
   public static final List<EmiStack> stacks = new ArrayList<>();
   public static volatile Runnable JeiStarter = null;
   private final List<EmiRecipeCategory> categories;
   private final Map<EmiRecipeCategory, List<EmiIngredient>> workstations;
   private final List<EmiRecipe> recipes;
   private final Map<EmiStack, List<EmiRecipe>> byInputList = new O2OOpenCustomCacheHashMap<>(STACK_STRATEGY);
   private final Map<EmiStack, List<EmiRecipe>> byOutputList = new O2OOpenCustomCacheHashMap<>(STACK_STRATEGY);
   private final Map<EmiRecipeCategory, List<EmiRecipe>> byCategory;
   private final Map<ResourceLocation, EmiRecipe> byId;

   public static void startDeferredJei() {
      Runnable starter = JeiStarter;
      if (starter != null) {
         try {
            starter.run();
         } finally {
            JeiStarter = null;
         }
      }
   }

   public static void addStacks() {
      if (items != null) {
         Set<Item> disabledItems = new ReferenceOpenHashSet<>();
         GTOApi.EMI_HIDE_ITEM_EVENT.call(disabledItems);

         for (Item item : EmiPort.getItemRegistry()) {
            if (item != Items.AIR) {
               Item tagStack = item;
               switch (tagStack) {
                  case LampBlockItem lampBlockItem:
                     for (int i = 0; i < 8; i++) {
                        items.computeIfAbsent("gtceu", k -> new ArrayList<>()).add(EmiStack.of(lampBlockItem.getBlock().getStackFromIndex(i)));
                     }
                     break;
                  case EnchantedBookItem ignored:
                     for (Enchantment enchantment : BuiltInRegistries.ENCHANTMENT) {
                        items.computeIfAbsent("minecraft", k -> new ArrayList<>())
                           .add(EmiStack.of(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, enchantment.getMaxLevel()))));
                     }
                     break;
                  case PotionItem potionItem:
                     for (Potion potion : BuiltInRegistries.POTION) {
                        items.computeIfAbsent("minecraft", k -> new ArrayList<>()).add(EmiStack.of(PotionUtils.setPotion(new ItemStack(potionItem), potion)));
                     }
                     break;
                  case InfinityCell ignored:
                     if (GTOInfCells.AddedInfCells == null) {
                        break;
                     }

                     for (ItemStack cell : GTOInfCells.AddedInfCells) {
                        items.computeIfAbsent("gtceu", k -> new ArrayList<>()).add(EmiStack.of(cell));
                     }

                     GTOInfCells.AddedInfCells = null;
                     break;
                  case GTTurbineItemCoated var32:
                     GTTurbineItemCoated ignored = (GTTurbineItemCoated)tagStack;
                     break;
                  default:
                     if (!disabledItems.contains(item)) {
                        EmiStack stack = EmiStack.of(item);
                        items.computeIfAbsent(ItemUtils.getIdLocation(item).getNamespace(), a -> new ArrayList<>()).add(stack);
                     }
               }
            }
         }

         for (Fluid fluid : EmiPort.getFluidRegistry()) {
            if (fluid.isSource(fluid.defaultFluidState()) || fluid instanceof FlowingFluid ff && ff.getSource() == Fluids.EMPTY) {
               fluids.computeIfAbsent(FluidUtils.getIdLocation(fluid).getNamespace(), a -> new ArrayList<>()).add(EmiStack.of(fluid));
            }
         }

         for (List<EmiStack> set : items.values()) {
            stacks.addAll(set);
         }

         for (List<EmiStack> set : fluids.values()) {
            stacks.addAll(set);
         }

         for (TagPrefix tagPrefix : TagPrefix.values()) {
            if (!EmiTagprefixStack.filteredPrefixes.contains(tagPrefix)) {
               EmiTagprefixStack tagStack = new EmiTagprefixStack(tagPrefix);
               stacks.add(tagStack);
            }
         }

         for (FluidStorageKey storageKey : FluidStorageKey.allKeys()) {
            EmiTagprefixStack tagStack = new EmiTagprefixStack(storageKey);
            stacks.add(tagStack);
         }

         Set<EmiStack> stacksEx = new OpenCacheHashSet<>();
         GTOApi.EMI_ADD_STACK_EVENT.call(stacksEx);
         stacks.addAll(stacksEx);
         items = null;
         fluids = null;
      }
   }

   public EMIManager(List<EmiRecipeCategory> categories, Map<EmiRecipeCategory, List<EmiIngredient>> workstations, List<EmiRecipe> recipes) {
      if (Data.getThrowable() != null) {
         throw new RuntimeException("An error occurred during GTOCore data loading.", Data.getThrowable());
      }

      long start = System.currentTimeMillis();
      recipes.addAll(GTORecipes.EMI_RECIPES);
      if (GTCEu.isDev() && GTOConfig.INSTANCE.devMode.enableCustomRecipes) {
         recipes.addAll(
            RecipeScript.loadRecipes(false).stream().map(r -> new GTEMIRecipe(r, GTRecipeEMICategory.CATEGORIES.apply(r.recipeType.getCategory()))).toList()
         );
      }

      this.categories = categories;
      this.workstations = workstations;
      this.recipes = recipes;
      this.byCategory = new O2OOpenCacheHashMap<>(categories.size());
      this.byId = new O2OOpenCacheHashMap<>(recipes.size());
      O2OOpenCustomCacheHashMap<EmiStack, Set<EmiRecipe>> byInput = new O2OOpenCustomCacheHashMap<>(STACK_STRATEGY);
      O2OOpenCustomCacheHashMap<EmiStack, Set<EmiRecipe>> byOutput = new O2OOpenCustomCacheHashMap<>(STACK_STRATEGY);

      for (EmiRecipe recipe : recipes) {
         ResourceLocation id = recipe.getId();
         EmiRecipeCategory category = recipe.getCategory();
         this.byCategory.computeIfAbsent(category, a -> new ArrayList<>()).add(recipe);
         if (id != null) {
            this.byId.put(id, recipe);
         }

         for (EmiIngredient input : recipe.getInputs()) {
            for (EmiStack stack : input.getEmiStacks()) {
               byInput.computeIfAbsent(stack, b -> new OpenCacheHashSet<>()).add(recipe);
            }
         }

         for (EmiIngredient catalyst : recipe.getCatalysts()) {
            for (EmiStack stack : catalyst.getEmiStacks()) {
               byInput.computeIfAbsent(stack, b -> new OpenCacheHashSet<>()).add(recipe);
            }
         }

         for (EmiStack output : recipe.getOutputs()) {
            byOutput.computeIfAbsent(output, b -> new OpenCacheHashSet<>()).add(recipe);
         }
      }

      for (Entry<EmiRecipeCategory, List<EmiRecipe>> entry : this.byCategory.entrySet()) {
         EmiRecipeCategory cat = entry.getKey();
         List<EmiRecipe> list = entry.getValue();
         if (cat instanceof GTRecipeEMICategory) {
            list.sort(Comparator.comparingInt(recipe -> recipe instanceof GTEMIRecipe gtemiRecipe ? -gtemiRecipe.displayPriority.getAsInt() : 0));
         }

         for (EmiIngredient ingredient : workstations.getOrDefault(cat, Collections.emptyList())) {
            for (EmiStack stack : ingredient.getEmiStacks()) {
               EmiRecipes.byWorkstation.computeIfAbsent(stack, s -> new ArrayList<>()).addAll(list);
            }
         }
      }

      byInput.object2ObjectEntrySet().fastForEach(e -> this.byInputList.put(e.getKey(), Arrays.asList(e.getValue().toArray(new EmiRecipe[0]))));
      byOutput.object2ObjectEntrySet().fastForEach(e -> this.byOutputList.put(e.getKey(), Arrays.asList(e.getValue().toArray(new EmiRecipe[0]))));
      EmiLog.info("Baked " + recipes.size() + " recipes in " + (System.currentTimeMillis() - start) + "ms");
   }

   @Override
   public List<EmiRecipeCategory> getCategories() {
      return this.categories;
   }

   @Override
   public List<EmiIngredient> getWorkstations(EmiRecipeCategory category) {
      return this.workstations.getOrDefault(category, Collections.emptyList());
   }

   @Override
   public List<EmiRecipe> getRecipes() {
      return this.recipes;
   }

   @Override
   public List<EmiRecipe> getRecipes(EmiRecipeCategory category) {
      return this.byCategory.getOrDefault(category, Collections.emptyList());
   }

   @Nullable
   @Override
   public EmiRecipe getRecipe(ResourceLocation id) {
      return this.byId.getOrDefault(id, null);
   }

   @Override
   public List<EmiRecipe> getRecipesByInput(EmiStack stack) {
      return this.byInputList.getOrDefault(stack, Collections.emptyList());
   }

   @Override
   public List<EmiRecipe> getRecipesByOutput(EmiStack stack) {
      return this.byOutputList.getOrDefault(stack, Collections.emptyList());
   }
}
