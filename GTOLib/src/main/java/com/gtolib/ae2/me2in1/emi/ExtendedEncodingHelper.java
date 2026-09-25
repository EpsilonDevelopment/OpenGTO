package com.gtolib.ae2.me2in1.emi;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.InventoryActionPacket;
import appeng.helpers.InventoryAction;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.menu.me.common.MEStorageMenu;
import appeng.menu.slot.FakeSlot;
import appeng.util.CraftingRecipeUtil;
import com.google.common.collect.Sets;
import com.google.common.math.LongMath;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gtocore.common.data.GTORecipeTypes;
import com.gtocore.common.item.ItemMap;
import com.gtocore.integration.emi.GTEMIRecipe;
import com.gtolib.Client;
import com.gtolib.api.ae2.me2in1.ExtendedEncodingMenu;
import com.gtolib.api.ae2.me2in1.encoding.ExtendedEncodingMode;
import com.gtolib.api.ae2.stacks.TagPrefixKey;
import com.gtolib.emi.EMIFavouriteAEKeyCache;
import com.gtolib.utils.AEChemicalHelper;
import dev.emi.emi.api.recipe.EmiRecipe;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

public class ExtendedEncodingHelper {
   private static final Set<AEKey> UNIVERSAL_CIRCUIT_KEYS = ItemMap.UNIVERSAL_CIRCUITS.stream().map(AEItemKey::of).collect(Collectors.toUnmodifiableSet());
   static final Comparator<GridInventoryEntry> ENTRY_COMPARATOR = Comparator.<GridInventoryEntry, Boolean>comparing(
         entry -> EMIFavouriteAEKeyCache.INSTANCE.cache.contains(entry.getWhat())
      )
      .thenComparing(entry -> UNIVERSAL_CIRCUIT_KEYS.contains(entry.getWhat()))
      .thenComparing(GridInventoryEntry::isCraftable)
      .thenComparing(ExtendedEncodingHelper::isUndamaged)
      .thenComparing(GridInventoryEntry::getStoredAmount);
   private static final Set<RecipeType<?>> GT_BATCH_ENCODE_SUPPORTED_TYPES = Set.of(
      GTORecipeTypes.FURNACE_RECIPES,
      GTORecipeTypes.BENDER_RECIPES,
      GTORecipeTypes.MACERATOR_RECIPES,
      GTORecipeTypes.COMPRESSOR_RECIPES,
      GTORecipeTypes.CUTTER_RECIPES,
      GTORecipeTypes.EXTRUDER_RECIPES,
      GTORecipeTypes.FLUID_SOLIDFICATION_RECIPES,
      GTORecipeTypes.FORGE_HAMMER_RECIPES,
      GTORecipeTypes.LATHE_RECIPES,
      GTORecipeTypes.PACKER_RECIPES,
      GTORecipeTypes.LASER_WELDER_RECIPES,
      GTORecipeTypes.WIREMILL_RECIPES,
      GTORecipeTypes.BLAST_RECIPES,
      GTORecipeTypes.IMPLOSION_RECIPES,
      GTORecipeTypes.UNPACKER_RECIPES,
      GTORecipeTypes.CLUSTER_RECIPES,
      GTORecipeTypes.ROLLING_RECIPES,
      GTORecipeTypes.LAMINATOR_RECIPES,
      GTORecipeTypes.LOOM_RECIPES,
      GTORecipeTypes.ELECTRIC_IMPLOSION_COMPRESSOR_RECIPES,
      GTORecipeTypes.LIQUEFACTION_FURNACE_RECIPES,
      GTORecipeTypes.THREE_DIMENSIONAL_PRINTER_RECIPES,
      GTORecipeTypes.SINTERING_FURNACE_RECIPES,
      GTORecipeTypes.ISOSTATIC_PRESSING_RECIPES
   );
   private static final Set<RecipeType<?>> GT_ASSEMBLY_LINE_SUPPORTED_TYPES = Set.of(
      GTORecipeTypes.ASSEMBLY_LINE_RECIPES, GTORecipeTypes.CIRCUIT_ASSEMBLY_LINE_RECIPES, GTORecipeTypes.AGGREGATION_DEVICE_RECIPES
   );

   public static Comparator<GridInventoryEntry> getEntryComparators() {
      return ENTRY_COMPARATOR;
   }

   private static Boolean isUndamaged(GridInventoryEntry entry) {
      return !(entry.getWhat() instanceof AEItemKey itemKey && itemKey.isDamaged());
   }

   public static void encodeProcessingRecipe(
      ExtendedEncodingMenu menu, List<List<GenericStack>> genericIngredients, List<GenericStack> genericResults, boolean isAssemblyLineRecipe
   ) {
      menu.setMode(ExtendedEncodingMode.PROCESSING);
      Reference2IntOpenHashMap<AEKey> ingredientPriorities = getIngredientPriorities(menu, ENTRY_COMPARATOR);
      encodeBestMatchingStacksIntoSlots(genericIngredients, ingredientPriorities, menu.getProcessingInputSlots(), isAssemblyLineRecipe, Client.autoRenameName);
      encodeBestMatchingStacksIntoSlots(
         genericResults.stream().map(List::of).toList(), ingredientPriorities, menu.getProcessingOutputSlots(), isAssemblyLineRecipe, Client.autoRenameName
      );
   }

   public static void encodeBatchRecipe(ExtendedEncodingMenu menu, List<List<GenericStack>> genericIngredients, List<GenericStack> genericResults) {
      menu.setMode(ExtendedEncodingMode.BATCH);
      Reference2IntOpenHashMap<AEKey> ingredientPriorities = getIngredientPriorities(menu, ENTRY_COMPARATOR);
      Set<Material> inputs = genericIngredients.stream()
         .flatMap(Collection::stream)
         .map(GenericStack::what)
         .map(AEChemicalHelper::getMaterial)
         .filter(mat -> mat != GTMaterials.NULL)
         .collect(Collectors.toSet());
      Set<Material> outputs = genericResults.stream()
         .map(GenericStack::what)
         .map(AEChemicalHelper::getMaterial)
         .filter(mat -> mat != GTMaterials.NULL)
         .collect(Collectors.toSet());
      Set<Material> intersection = Sets.intersection(inputs, outputs);
      if (intersection.isEmpty()) {
         encodeProcessingRecipe(menu, genericIngredients, genericResults, false);
      } else {
         Material material = intersection.iterator().next();
         encodeBatchStacksIntoSlots(genericIngredients, ingredientPriorities, menu.getProcessingInputSlots(), material);
         encodeBatchStacksIntoSlots(genericResults.stream().map(List::of).toList(), ingredientPriorities, menu.getProcessingOutputSlots(), material);
         NetworkHandler.instance()
            .sendToServer(
               new InventoryActionPacket(InventoryAction.SET_FILTER, menu.getMaterialSlots()[0].index, ChemicalHelper.getIngotOrDust(material, 3628800L))
            );
         Set<TagPrefix> collectedInputsOutputsTagPrefixes = new ReferenceOpenHashSet<>();
         genericIngredients.stream()
            .flatMap(Collection::stream)
            .map(GenericStack::what)
            .filter(ing -> AEChemicalHelper.getMaterial(ing) == material)
            .map(AEChemicalHelper::getTagPrefix)
            .filter(tagPrefix -> tagPrefix != TagPrefix.NULL_PREFIX)
            .forEach(collectedInputsOutputsTagPrefixes::add);
         genericResults.stream()
            .map(GenericStack::what)
            .filter(out -> AEChemicalHelper.getMaterial(out) == material)
            .map(AEChemicalHelper::getTagPrefix)
            .filter(tagPrefix -> tagPrefix != TagPrefix.NULL_PREFIX)
            .forEach(collectedInputsOutputsTagPrefixes::add);
         encodeMaterialFromEmiFavourites(menu, material, collectedInputsOutputsTagPrefixes);
      }
   }

   private static void encodeMaterialFromEmiFavourites(ExtendedEncodingMenu menu, Material existed, Set<TagPrefix> collectedInputsOutputsTagPrefixes) {
      ArrayList<AEKey> favorReversed = new ArrayList<>(EMIFavouriteAEKeyCache.INSTANCE.cache);
      Collections.reverse(favorReversed);
      List<Material> favourites = favorReversed.stream()
         .map(AEChemicalHelper::getMaterial)
         .filter(mat -> mat != GTMaterials.NULL)
         .filter(mat -> collectedInputsOutputsTagPrefixes.stream().allMatch(tagPrefix -> ChemicalHelper.get(tagPrefix, mat) != ItemStack.EMPTY))
         .toList();
      int counter = 0;
      Set<Material> favouritesSet = new ReferenceOpenHashSet<>(9);
      favouritesSet.add(existed);

      for (Material material : favourites) {
         if (counter >= 8) {
            return;
         }

         if (favouritesSet.add(material)) {
            NetworkHandler.instance()
               .sendToServer(
                  new InventoryActionPacket(
                     InventoryAction.SET_FILTER, menu.getMaterialSlots()[++counter].index, ChemicalHelper.getIngotOrDust(material, 3628800L)
                  )
               );
         }
      }

      for (int i = counter + 1; i < menu.getMaterialSlots().length; i++) {
         NetworkHandler.instance().sendToServer(new InventoryActionPacket(InventoryAction.SET_FILTER, menu.getMaterialSlots()[i].index, ItemStack.EMPTY));
      }
   }

   private static void encodeBestMatchingStacksIntoSlots(
      List<List<GenericStack>> possibleInputsBySlot,
      Reference2IntOpenHashMap<AEKey> ingredientPriorities,
      FakeSlot[] slots,
      boolean assemblyLineMode,
      @Nullable String renameWhenMultiple
   ) {
      ArrayList<GenericStack> encodedInputs = new ArrayList<>();

      for (List<GenericStack> genericIngredient : possibleInputsBySlot) {
         if (!genericIngredient.isEmpty()) {
            if (assemblyLineMode) {
               addOrRename(encodedInputs, findBestIngredient(ingredientPriorities, genericIngredient), renameWhenMultiple);
            } else {
               addOrMerge(encodedInputs, findBestIngredient(ingredientPriorities, genericIngredient));
            }
         }
      }

      for (int i = 0; i < slots.length; i++) {
         FakeSlot slot = slots[i];
         ItemStack stack = i < encodedInputs.size() ? GenericStack.wrapInItemStack(encodedInputs.get(i)) : ItemStack.EMPTY;
         NetworkHandler.instance().sendToServer(new InventoryActionPacket(InventoryAction.SET_FILTER, slot.index, stack));
      }
   }

   private static void encodeBatchStacksIntoSlots(
      List<List<GenericStack>> possibleInputsBySlot, Reference2IntOpenHashMap<AEKey> ingredientPriorities, FakeSlot[] slots, Material referenceMaterial
   ) {
      ArrayList<GenericStack> encodedInputs = new ArrayList<>();

      for (List<GenericStack> genericIngredient : possibleInputsBySlot) {
         if (!genericIngredient.isEmpty()) {
            addOrMerge(encodedInputs, findBestIngredient(ingredientPriorities, genericIngredient.stream().map(gi -> {
               Material mat = AEChemicalHelper.getMaterial(gi.what());
               TagPrefixKey tagPrefixKey = AEChemicalHelper.getTagPrefixKey(gi.what());
               return mat == referenceMaterial && tagPrefixKey != null ? new GenericStack(tagPrefixKey, gi.amount()) : gi;
            }).toList()));
         }
      }

      for (int i = 0; i < slots.length; i++) {
         FakeSlot slot = slots[i];
         ItemStack stack = i < encodedInputs.size() ? GenericStack.wrapInItemStack(encodedInputs.get(i)) : ItemStack.EMPTY;
         NetworkHandler.instance().sendToServer(new InventoryActionPacket(InventoryAction.SET_FILTER, slot.index, stack));
      }
   }

   public static boolean isGTBatchEncodeSupported(@Nullable EmiRecipe recipe) {
      if (recipe instanceof GTEMIRecipe gtemiRecipe) {
         GTRecipeType recipeType = gtemiRecipe.getRecipeType();
         return GT_BATCH_ENCODE_SUPPORTED_TYPES.contains(recipeType);
      } else {
         return false;
      }
   }

   public static boolean isGTAssemblyLineRecipe(@Nullable EmiRecipe recipe) {
      if (recipe instanceof GTEMIRecipe gtemiRecipe) {
         GTRecipeType recipeType = gtemiRecipe.getRecipeType();
         return GT_ASSEMBLY_LINE_SUPPORTED_TYPES.contains(recipeType);
      } else {
         return false;
      }
   }

   public static void encodeCraftingRecipe(
      ExtendedEncodingMenu menu, @Nullable Recipe<?> recipe, List<List<GenericStack>> genericIngredients, Predicate<ItemStack> visiblePredicate
   ) {
      if (recipe != null && recipe.getType().equals(RecipeType.STONECUTTING)) {
         menu.setMode(ExtendedEncodingMode.STONECUTTING);
         menu.setStonecuttingRecipeId(recipe.getId());
      } else if (recipe != null && recipe.getType().equals(RecipeType.SMITHING)) {
         menu.setMode(ExtendedEncodingMode.SMITHING_TABLE);
      } else {
         menu.setMode(ExtendedEncodingMode.CRAFTING);
      }

      Reference2IntOpenHashMap<AEKey> prioritizedNetworkInv = getIngredientPriorities(menu, ENTRY_COMPARATOR);
      NonNullList<ItemStack> encodedInputs = NonNullList.withSize(menu.getCraftingGridSlots().length, ItemStack.EMPTY);
      if (recipe != null) {
         NonNullList<Ingredient> ingredients3x3 = CraftingRecipeUtil.ensure3by3CraftingMatrix(recipe);

         for (int slot = 0; slot < ingredients3x3.size(); slot++) {
            Ingredient ingredient = ingredients3x3.get(slot);
            if (!ingredient.isEmpty()) {
               Optional<ItemStack> bestNetworkIngredient = prioritizedNetworkInv.entrySet()
                  .stream()
                  .filter(ni -> ni.getKey() instanceof AEItemKey itemKey && itemKey.matches(ingredient))
                  .max(Comparator.comparingInt(Entry::getValue))
                  .map(entry -> entry.getKey() instanceof AEItemKey itemKey ? itemKey.toStack() : null);
               ItemStack bestIngredient = bestNetworkIngredient.orElseGet(() -> {
                  for (ItemStack stack : ingredient.getItems()) {
                     if (visiblePredicate.test(stack)) {
                        return stack;
                     }
                  }

                  return ingredient.getItems()[0];
               });
               encodedInputs.set(slot, bestIngredient);
            }
         }
      } else {
         for (int slot = 0; slot < genericIngredients.size(); slot++) {
            List<GenericStack> genericIngredient = genericIngredients.get(slot);
            if (!genericIngredient.isEmpty()) {
               AEKey bestIngredient = findBestIngredient(prioritizedNetworkInv, genericIngredient).what();
               if (bestIngredient instanceof AEItemKey itemKey) {
                  encodedInputs.set(slot, itemKey.toStack());
               } else {
                  encodedInputs.set(slot, GenericStack.wrapInItemStack(bestIngredient, 1L));
               }
            }
         }
      }

      for (int i = 0; i < encodedInputs.size(); i++) {
         ItemStack encodedInput = encodedInputs.get(i);
         NetworkHandler.instance().sendToServer(new InventoryActionPacket(InventoryAction.SET_FILTER, menu.getCraftingGridSlots()[i].index, encodedInput));
      }

      for (FakeSlot outputSlot : menu.getProcessingOutputSlots()) {
         NetworkHandler.instance().sendToServer(new InventoryActionPacket(InventoryAction.SET_FILTER, outputSlot.index, ItemStack.EMPTY));
      }
   }

   private static GenericStack findBestIngredient(Reference2IntOpenHashMap<AEKey> ingredientPriorities, List<GenericStack> possibleIngredients) {
      return possibleIngredients.stream()
         .map(gi -> Pair.of(gi, ingredientPriorities.getOrDefault(gi.what(), Integer.MIN_VALUE)))
         .max(Comparator.comparingInt(Pair::getRight))
         .map(Pair::getLeft)
         .orElseThrow();
   }

   private static void addOrRename(List<GenericStack> stacks, GenericStack newStack, @Nullable String customName) {
      for (int i = 0; i < stacks.size(); i++) {
         GenericStack existingStack = stacks.get(i);
         if (Objects.equals(existingStack.what(), newStack.what())) {
            long newAmount = newStack.amount();
            AEKey newWhat = existingStack.what();
            if (newWhat instanceof AEItemKey itemKey && customName != null && !customName.isEmpty() && !customName.equals("{}")) {
               newWhat = AEItemKey.of(itemKey.toStack().setHoverName(Component.literal(customName)));
            }

            stacks.add(new GenericStack(newWhat, newAmount));
            return;
         }
      }

      stacks.add(newStack);
   }

   private static void addOrMerge(List<GenericStack> stacks, GenericStack newStack) {
      for (int i = 0; i < stacks.size(); i++) {
         GenericStack existingStack = stacks.get(i);
         if (Objects.equals(existingStack.what(), newStack.what())) {
            long newAmount = LongMath.saturatedAdd(existingStack.amount(), newStack.amount());
            stacks.set(i, new GenericStack(newStack.what(), newAmount));
            long overflow = newStack.amount() - (newAmount - existingStack.amount());
            if (overflow > 0L) {
               stacks.add(new GenericStack(newStack.what(), overflow));
            }

            return;
         }
      }

      stacks.add(newStack);
   }

   public static Reference2IntOpenHashMap<AEKey> getIngredientPriorities(MEStorageMenu menu, Comparator<GridInventoryEntry> comparator) {
      List<AEKey> orderedEntries = menu.getClientRepo().getAllEntries().stream().sorted(comparator).map(GridInventoryEntry::getWhat).toList();
      Reference2IntOpenHashMap<AEKey> result = new Reference2IntOpenHashMap<>(orderedEntries.size());

      for (int i = 0; i < orderedEntries.size(); i++) {
         result.put(orderedEntries.get(i), i);
      }

      for (ItemStack item : menu.getPlayerInventory().items) {
         AEItemKey key = AEItemKey.of(item);
         if (key != null) {
            result.putIfAbsent(key, -1);
         }
      }

      return result;
   }
}
