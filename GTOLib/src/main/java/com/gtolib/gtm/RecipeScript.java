package com.gtolib.gtm;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKey;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntCircuitIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.ItemIngredient;
import com.gto.datasynclib.datastream.DataComponentMap;
import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import com.gtolib.GTOCore;
import com.gtolib.api.recipe.RecipeBuilder;
import com.gtolib.api.recipe.RecipeType;
import com.gtolib.utils.RegistriesUtils;
import com.gtolib.utils.StringIndex;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import kotlin.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public final class RecipeScript {
   private static final Map<ResourceLocation, GTRecipeDefinition> RECIPES = new O2OOpenCacheHashMap<>(4096);
   private static final DataComponentMap MIRROR_RECIPES = new DataComponentMap();
   public static int LOADED_FILE_HASH = 0;
   private static final Map<String, FluidStorageKey> FLUID_STORAGE_KEY_MAP = Map.of(
      "FluidStorageKeys.LIQUID",
      FluidStorageKeys.LIQUID,
      "FluidStorageKeys.GAS",
      FluidStorageKeys.GAS,
      "FluidStorageKeys.PLASMA",
      FluidStorageKeys.PLASMA,
      "FluidStorageKeys.MOLTEN",
      FluidStorageKeys.MOLTEN
   );

   public static GTRecipe toRuntime(GTRecipeDefinition definition) {
      definition = MIRROR_RECIPES.getData(definition);
      return definition == null
         ? null
         : new GTRecipe(
            definition,
            definition.itemInputs,
            definition.itemOutputs,
            definition.fluidInputs,
            definition.fluidOutputs,
            definition.data.clone(),
            definition.eut,
            definition.tier,
            definition.duration
         );
   }

   public static GTRecipeDefinition get(ResourceLocation definition) {
      return RECIPES.get(definition);
   }

   public static GTRecipeDefinition remove(ResourceLocation id) {
      GTRecipeDefinition d = RECIPES.remove(id);
      if (d != null) {
         MIRROR_RECIPES.remove(d);
      }

      return d;
   }

   public static Collection<GTRecipeDefinition> values() {
      return RECIPES.values();
   }

   public static boolean add(GTRecipeDefinition recipe) {
      MIRROR_RECIPES.put(
         recipe,
         new GTRecipeDefinition(
            recipe.registered,
            recipe.recipeType,
            recipe.recipeCategory,
            recipe.id,
            recipe.itemInputs,
            recipe.itemOutputs,
            recipe.fluidInputs,
            recipe.fluidOutputs,
            Arrays.asList(recipe.recipeModifiers),
            Arrays.asList(recipe.conditions),
            Arrays.asList(recipe.recipeExtensions),
            Arrays.asList(recipe.tickRecipeExtensions),
            recipe.data,
            recipe.chanceFunction,
            recipe.eut,
            recipe.tier,
            recipe.duration,
            recipe.priority
         )
      );
      return RECIPES.put(recipe.id, recipe) == null;
   }

   public static Set<GTRecipeDefinition> loadRecipes(boolean initial) {
      long time = System.currentTimeMillis();
      if (initial) {
         LOADED_FILE_HASH = 1;
      }

      File[] gtrsFiles = GTOCore.getFile("recipe").listFiles(filex -> filex.isFile() && filex.getName().toLowerCase().endsWith(".gtrs"));
      if (gtrsFiles == null) {
         return Collections.emptySet();
      }

      Set<GTRecipeDefinition> recipes = new ReferenceOpenHashSet<>();
      Pair<GTRecipeType, String> typeAndId = null;
      List<Content<ItemIngredient>> inputItems = new ArrayList<>();
      List<Content<ItemIngredient>> outputItems = new ArrayList<>();
      List<Content<FluidIngredient>> inputFluids = new ArrayList<>();
      List<Content<FluidIngredient>> outputFluids = new ArrayList<>();
      int circuit = 0;
      int duration = 0;
      long eu = 0L;
      long mana = 0L;
      int fileContentHash = 0;

      for (File file : gtrsFiles) {
         try {
            List<String> lines = Files.readAllLines(Paths.get(file.getAbsolutePath()));
            if (initial) {
               fileContentHash += lines.hashCode();
            }

            for (String line : lines) {
               String trimmedLine = line.trim().replace(" ", "");
               if (!trimmedLine.isEmpty()) {
                  if (typeAndId == null) {
                     typeAndId = typeAndId(trimmedLine);
                  } else if (duration > 0) {
                     if (trimmedLine.equals(".save();")) {
                        RecipeBuilder r = ((RecipeType)typeAndId.getFirst()).recipeBuilder(typeAndId.getSecond());
                        if (eu != 0L) {
                           r.EUt(eu);
                        }

                        if (mana != 0L) {
                           r.MANAt(mana);
                        }

                        inputItems.forEach(r::inputItems);
                        outputItems.forEach(r::outputItems);
                        inputFluids.forEach(r::inputFluids);
                        outputFluids.forEach(r::outputFluids);
                        r.duration(duration);
                        recipes.add(r.build());
                        typeAndId = null;
                        inputItems.clear();
                        outputItems.clear();
                        inputFluids.clear();
                        outputFluids.clear();
                        circuit = 0;
                        duration = 0;
                        eu = 0L;
                        mana = 0L;
                     }
                  } else {
                     if (circuit == 0) {
                        circuit = circuit(trimmedLine);
                        if (circuit > 0) {
                           inputItems.add(new Content<>(IntCircuitIngredient.of(circuit), 0, 0));
                           continue;
                        }
                     }

                     if (duration == 0) {
                        duration = duration(trimmedLine);
                        if (duration > 0) {
                           continue;
                        }
                     }

                     if (eu == 0L) {
                        eu = eu(trimmedLine);
                     }

                     if (mana == 0L) {
                        mana = mana(trimmedLine);
                     }

                     String inputItem = inputItem(trimmedLine);
                     if (!inputItem.isEmpty()) {
                        ItemIngredient i = parseItem(inputItem);
                        if (!i.isEmpty()) {
                           inputItems.add(new Content<>(i, 10000, 0));
                        } else {
                           GTOCore.LOGGER.error("inputItem error: {}", inputItem);
                        }
                     } else {
                        String outputItem = outputItem(trimmedLine);
                        if (!outputItem.isEmpty()) {
                           ItemIngredient i = parseItem(outputItem);
                           if (!i.isEmpty()) {
                              outputItems.add(new Content<>(i, 10000, 0));
                           } else {
                              GTOCore.LOGGER.error("outputItem error: {}", outputItem);
                           }
                        } else {
                           String inputFluid = inputFluid(trimmedLine);
                           if (!inputFluid.isEmpty()) {
                              FluidIngredient f = parseFluid(inputFluid);
                              if (!f.isEmpty()) {
                                 inputFluids.add(new Content<>(f, 10000, 0));
                              } else {
                                 GTOCore.LOGGER.error("inputFluid error: {}", inputFluid);
                              }
                           } else {
                              String outputFluid = outputFluid(trimmedLine);
                              if (!outputFluid.isEmpty()) {
                                 FluidIngredient f = parseFluid(outputFluid);
                                 if (!f.isEmpty()) {
                                    outputFluids.add(new Content<>(f, 10000, 0));
                                 } else {
                                    GTOCore.LOGGER.error("outputFluid error: {}", outputFluid);
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         } catch (IOException e) {
            GTOCore.LOGGER.error("Failed to load recipes from file: {}", file.getName(), e);
         }
      }

      if (recipes.isEmpty()) {
         return Collections.emptySet();
      }

      if (initial) {
         recipes.forEach(r -> {
            if (!add(r)) {
               throw new RuntimeException("Duplicate recipe id: " + r.id);
            }
         });
         LOADED_FILE_HASH += 31 * fileContentHash;
      }

      GTOCore.LOGGER.info("Loaded {} recipes in {}ms.", recipes.size(), System.currentTimeMillis() - time);
      return recipes;
   }

   private static Pair<GTRecipeType, String> typeAndId(String line) {
      try {
         int index = line.indexOf(".builder");
         if (index < 0) {
            return null;
         } else {
            String t = line.substring(0, index);
            GTRecipeType type = StringIndex.RECIPETYPE_INVERSE_MAP.get(t);
            if (type == null) {
               GTOCore.LOGGER.error("Unknown recipe type: {}", t);
               return null;
            } else {
               int indexOfBuilder = index + ".builder(".length();
               int indexOfFirstQuote = line.indexOf("\"", indexOfBuilder);
               int indexOfSecondQuote = line.indexOf("\"", indexOfFirstQuote + 1);
               String id = line.substring(indexOfFirstQuote + 1, indexOfSecondQuote);
               if (id.isEmpty()) {
                  GTOCore.LOGGER.error("Invalid recipe id: {}", id);
                  return null;
               } else {
                  return new Pair<>(type, id);
               }
            }
         }
      } catch (Throwable e) {
         return null;
      }
   }

   private static int circuit(String line) {
      try {
         int index = line.indexOf(".circuitMeta(");
         if (index < 0) {
            return 0;
         }

         int indexOfCircuit = index + ".circuitMeta(".length();
         int indexOfCloseParenthesis = line.indexOf(")", indexOfCircuit);
         return Integer.parseInt(line.substring(indexOfCircuit, indexOfCloseParenthesis));
      } catch (Throwable e) {
         return 0;
      }
   }

   private static int duration(String line) {
      try {
         int index = line.indexOf(".duration(");
         if (index < 0) {
            return 0;
         }

         int indexOfDuration = index + ".duration(".length();
         int indexOfCloseParenthesis = line.indexOf(")", indexOfDuration);
         return Integer.parseInt(line.substring(indexOfDuration, indexOfCloseParenthesis));
      } catch (Throwable e) {
         return 0;
      }
   }

   private static long eu(String line) {
      try {
         int index = line.indexOf(".EUt(");
         if (index < 0) {
            return 0L;
         }

         int indexOfEUt = index + ".EUt(".length();
         int indexOfCloseParenthesis = line.indexOf(")", indexOfEUt);
         return Long.parseLong(line.substring(indexOfEUt, indexOfCloseParenthesis));
      } catch (Throwable e) {
         return 0L;
      }
   }

   private static long mana(String line) {
      try {
         int index = line.indexOf(".MANAt(");
         if (index < 0) {
            return 0L;
         }

         int indexOfMANAt = index + ".MANAt(".length();
         int indexOfCloseParenthesis = line.indexOf(")", indexOfMANAt);
         return Long.parseLong(line.substring(indexOfMANAt, indexOfCloseParenthesis));
      } catch (Throwable e) {
         return 0L;
      }
   }

   private static String inputItem(String line) {
      try {
         int index = line.indexOf(".inputItems(");
         return index < 0 ? "" : line.substring(index + ".inputItems(".length(), line.length() - 1);
      } catch (Throwable e) {
         return "";
      }
   }

   private static String outputItem(String line) {
      try {
         int index = line.indexOf(".outputItems(");
         return index < 0 ? "" : line.substring(index + ".outputItems(".length(), line.length() - 1);
      } catch (Throwable e) {
         return "";
      }
   }

   private static String inputFluid(String line) {
      try {
         int index = line.indexOf(".inputFluids(");
         return index < 0 ? "" : line.substring(index + ".inputFluids(".length(), line.length() - 1);
      } catch (Throwable e) {
         return "";
      }
   }

   private static String outputFluid(String line) {
      try {
         int index = line.indexOf(".outputFluids(");
         return index < 0 ? "" : line.substring(index + ".outputFluids(".length(), line.length() - 1);
      } catch (Throwable e) {
         return "";
      }
   }

   private static ItemIngredient parseItem(String str) {
      try {
         String[] parts = str.split(",");
         String last = parts[parts.length - 1];

         long number;
         try {
            number = Long.parseLong(last);
         } catch (NumberFormatException e) {
            number = 0L;
         }

         if (number != 0L) {
            Object i = parseItem(parts, 3);
            if (i instanceof Item item) {
               return ItemIngredient.of(item, number);
            }

            if (i instanceof TagKey tagKey) {
               return ItemIngredient.of(tagKey, number);
            }
         } else {
            Object i = parseItem(parts, 2);
            if (i instanceof Item item) {
               return ItemIngredient.of(item, 1L);
            }

            if (i instanceof TagKey tagKey) {
               return ItemIngredient.of(tagKey, number);
            }
         }
      } catch (Throwable e) {
         GTOCore.LOGGER.error("parseItem error: {}", str, e);
      }

      return ItemIngredient.EMPTY;
   }

   private static Object parseItem(String[] parts, int max) {
      if (parts.length == max) {
         TagPrefix tagprefix = StringIndex.TAGPREFIX_INVERSE_MAP.get(parts[0]);
         if (tagprefix != null) {
            Material material = StringIndex.MATERIAL_INVERSE_MAP.get(parts[1]);
            if (material != null) {
               return ChemicalHelper.getItem(tagprefix, material);
            }

            GTOCore.LOGGER.error("Unknown material: {}", parts[1]);
         } else {
            GTOCore.LOGGER.error("Unknown tag prefix: {}", parts[0]);
         }
      } else {
         if (parts[0].contains(":")) {
            return RegistriesUtils.getItem(parts[0].substring(1, parts[0].length() - 1));
         }

         if (parts[0].startsWith("Items.")) {
            return RegistriesUtils.getItem(parts[0].substring("Items.".length()).toLowerCase(Locale.ROOT));
         }

         Object item = StringIndex.ITEM_INVERSE_MAP.get(parts[0]);
         if (item != null) {
            return item;
         }

         item = StringIndex.TAG_INVERSE_MAP.get(parts[0]);
         if (item != null) {
            return item;
         }

         parts[0] = parts[0].replace(".asItem()", "");
         item = StringIndex.ITEM_LINK_INVERSE_MAP.get(parts[0]);
         if (item != null) {
            return item;
         }

         item = StringIndex.BLOCK_INVERSE_MAP.get(parts[0]);
         if (item != null) {
            return ((Block)item).asItem();
         }

         item = StringIndex.BLOCK_LINK_INVERSE_MAP.get(parts[0]);
         if (item != null) {
            return ((Block)item).asItem();
         }
      }

      GTOCore.LOGGER.error("Unknown item: {}", Arrays.toString(parts));
      return null;
   }

   private static FluidIngredient parseFluid(String str) {
      try {
         String[] parts = str.split(",");
         String last = parts[parts.length - 1];

         long number;
         try {
            number = Long.parseLong(last);
         } catch (NumberFormatException e) {
            number = 0L;
         }

         if (number != 0L) {
            Fluid f = parseFluid(parts);
            if (f != null) {
               return FluidIngredient.of(f, number);
            }
         }
      } catch (Throwable e) {
         GTOCore.LOGGER.error("parseFluid error: {}", str, e);
      }

      return FluidIngredient.EMPTY;
   }

   private static Fluid parseFluid(String[] parts) {
      Material material = StringIndex.MATERIAL_INVERSE_MAP.get(parts[0]);
      if (material != null) {
         return parts.length == 3 ? material.getFluid(FLUID_STORAGE_KEY_MAP.get(parts[1])) : material.getFluid();
      }

      Fluid fluid = StringIndex.FLUID_INVERSE_MAP.get(parts[0]);
      if (fluid != null) {
         return fluid;
      }

      GTOCore.LOGGER.error("Unknown fluid: {}", parts[0]);
      return null;
   }
}
