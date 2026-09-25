package com.gtolib.utils;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.BlockDefinition;
import appeng.core.definitions.ItemDefinition;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GCYMBlocks;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import com.gto.registrate.util.entry.BlockEntry;
import com.gto.registrate.util.entry.ItemEntry;
import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.GTORecipeTypes;
import com.gtocore.common.data.machines.ExResearchMachines;
import com.gtocore.common.data.machines.GCYMMachines;
import com.gtocore.common.data.machines.GTAEMachines;
import com.gtocore.common.data.machines.GeneratorMultiblock;
import com.gtocore.common.data.machines.ManaMachine;
import com.gtocore.common.data.machines.ManaMultiBlock;
import com.gtocore.common.data.machines.OptionalMachine;
import com.gtocore.common.data.machines.SpaceMultiblock;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public final class StringIndex {
   public static final Map<GTRecipeType, String> RECIPETYPE_MAP = new Reference2ObjectOpenHashMap<>();
   public static final Map<TagKey<Item>, String> TAG_MAP = new Reference2ObjectOpenHashMap<>();
   public static final Map<Material, String> MATERIAL_MAP = new Reference2ObjectOpenHashMap<>();
   public static final Map<TagPrefix, String> TAGPREFIX_MAP = new Reference2ObjectOpenHashMap<>();
   public static final Map<Block, String> BLOCK_LINK_MAP = new Reference2ObjectOpenHashMap<>();
   public static final Map<Block, String> BLOCK_MAP = new Reference2ObjectOpenHashMap<>();
   public static final Map<Item, String> ITEM_LINK_MAP = new Reference2ObjectOpenHashMap<>();
   public static final Map<Item, String> ITEM_MAP = new Reference2ObjectOpenHashMap<>();
   public static final Map<Fluid, String> FLUID_MAP = new Reference2ObjectOpenHashMap<>();
   public static final Map<String, GTRecipeType> RECIPETYPE_INVERSE_MAP = new O2OOpenCacheHashMap<>();
   public static final Map<String, TagKey<Item>> TAG_INVERSE_MAP = new O2OOpenCacheHashMap<>();
   public static final Map<String, Material> MATERIAL_INVERSE_MAP = new O2OOpenCacheHashMap<>();
   public static final Map<String, TagPrefix> TAGPREFIX_INVERSE_MAP = new O2OOpenCacheHashMap<>();
   public static final Map<String, Block> BLOCK_LINK_INVERSE_MAP = new O2OOpenCacheHashMap<>();
   public static final Map<String, Block> BLOCK_INVERSE_MAP = new O2OOpenCacheHashMap<>();
   public static final Map<String, Item> ITEM_LINK_INVERSE_MAP = new O2OOpenCacheHashMap<>();
   public static final Map<String, Item> ITEM_INVERSE_MAP = new O2OOpenCacheHashMap<>();
   public static final Map<String, Fluid> FLUID_INVERSE_MAP = new O2OOpenCacheHashMap<>();

   private StringIndex() {
   }

   private static void indexMachineDefinitions(Class<?> registryClass) {
      String className = registryClass.getSimpleName();

      for (Field field : registryClass.getFields()) {
         try {
            if (Modifier.isStatic(field.getModifiers()) && field.canAccess(null)) {
               Object value = field.get(null);
               if (value instanceof MachineDefinition definition) {
                  indexMachineDefinition(definition, className + "." + field.getName());
               } else if (value instanceof MachineDefinition[] definitions) {
                  for (int i = 0; i < definitions.length; i++) {
                     String tier = i < GTValues.VN.length ? "GTValues." + GTValues.VN[i] : Integer.toString(i);
                     indexMachineDefinition(definitions[i], className + "." + field.getName() + "[" + tier + "]");
                  }
               }
            }
         } catch (Throwable e) {
            throw new RuntimeException(e);
         }
      }
   }

   private static void indexMachineDefinition(MachineDefinition definition, String reference) {
      if (definition != null && definition.getRecipeOutputLimits() != null) {
         MetaMachineBlock block = definition.get();
         BLOCK_LINK_MAP.putIfAbsent(block, reference);
         BLOCK_INVERSE_MAP.putIfAbsent(reference, block);
      }
   }

   static {
      for (Field field : GTORecipeTypes.class.getFields()) {
         try {
            if (field.canAccess(null) && field.get(null) instanceof GTRecipeType recipeType) {
               RECIPETYPE_MAP.putIfAbsent(recipeType, field.getName());
               RECIPETYPE_INVERSE_MAP.putIfAbsent(field.getName(), recipeType);
            }
         } catch (Throwable e) {
            throw new RuntimeException(e);
         }
      }

      for (Field field : GTItems.class.getFields()) {
         try {
            if (field.canAccess(null) && field.get(null) instanceof ItemEntry itemItemEntry) {
               Item item = itemItemEntry.asItem();
               String str = "GTItems." + field.getName();
               ITEM_LINK_MAP.putIfAbsent(item, str);
               ITEM_INVERSE_MAP.putIfAbsent(str, item);
            }
         } catch (Throwable e) {
            throw new RuntimeException(e);
         }
      }

      for (Field field : GTOItems.class.getFields()) {
         try {
            if (field.canAccess(null) && field.get(null) instanceof ItemEntry itemItemEntry) {
               Item item = itemItemEntry.asItem();
               String str = "GTOItems." + field.getName();
               ITEM_LINK_MAP.putIfAbsent(item, str);
               ITEM_INVERSE_MAP.putIfAbsent(str, item);
            }
         } catch (Throwable e) {
            throw new RuntimeException(e);
         }
      }

      for (Field field : GTBlocks.class.getFields()) {
         try {
            if (field.canAccess(null) && field.get(null) instanceof BlockEntry blockEntry) {
               Object block = blockEntry.get();
               String str = "GTBlocks." + field.getName();
               BLOCK_LINK_MAP.putIfAbsent((Block)block, str);
               BLOCK_INVERSE_MAP.putIfAbsent(str, (Block)block);
            }
         } catch (Throwable e) {
            throw new RuntimeException(e);
         }
      }

      for (Field field : GCYMBlocks.class.getFields()) {
         try {
            if (field.canAccess(null) && field.get(null) instanceof BlockEntry blockEntry) {
               Object block = blockEntry.get();
               String str = "GCYMBlocks." + field.getName();
               BLOCK_LINK_MAP.putIfAbsent((Block)block, str);
               BLOCK_INVERSE_MAP.putIfAbsent(str, (Block)block);
            }
         } catch (Throwable e) {
            throw new RuntimeException(e);
         }
      }

      for (Field field : GTOBlocks.class.getFields()) {
         try {
            if (field.canAccess(null) && field.get(null) instanceof BlockEntry blockEntry) {
               Object block = blockEntry.get();
               String str = "GTOBlocks." + field.getName();
               BLOCK_LINK_MAP.putIfAbsent((Block)block, str);
               BLOCK_INVERSE_MAP.putIfAbsent(str, (Block)block);
            }
         } catch (Throwable e) {
            throw new RuntimeException(e);
         }
      }

      indexMachineDefinitions(GTMultiMachines.class);
      indexMachineDefinitions(GCYMMachines.class);
      indexMachineDefinitions(GTMachines.class);
      indexMachineDefinitions(GTOMachines.class);

      for (char c = 'A'; c <= 'Z'; c++) {
         try {
            Class<?> clazz = Class.forName("com.gtocore.common.data.machines.MultiBlock" + c);
            indexMachineDefinitions(clazz);
         } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
            break;
         }
      }

      indexMachineDefinitions(ExResearchMachines.class);
      indexMachineDefinitions(GeneratorMultiblock.class);
      indexMachineDefinitions(GTAEMachines.class);
      indexMachineDefinitions(ManaMachine.class);
      indexMachineDefinitions(ManaMultiBlock.class);
      indexMachineDefinitions(OptionalMachine.class);
      indexMachineDefinitions(SpaceMultiblock.class);

      for (Field field : AEItems.class.getFields()) {
         try {
            if (field.canAccess(null) && field.get(null) instanceof ItemDefinition itemDefinition) {
               Item item = itemDefinition.asItem();
               String str = "AEItems." + field.getName() + ".asItem()";
               ITEM_MAP.putIfAbsent(item, str);
               ITEM_INVERSE_MAP.putIfAbsent(str, item);
            }
         } catch (Throwable e) {
            throw new RuntimeException(e);
         }
      }

      for (Field field : AEBlocks.class.getFields()) {
         try {
            if (field.canAccess(null) && field.get(null) instanceof BlockDefinition blockDefinition) {
               Block block = blockDefinition.block();
               if (block != null) {
                  String str = "AEBlocks." + field.getName();
                  BLOCK_MAP.putIfAbsent(block, str);
                  BLOCK_INVERSE_MAP.putIfAbsent(str, block);
               }
            }
         } catch (Throwable e) {
            throw new RuntimeException(e);
         }
      }

      for (Field field : GTMaterials.class.getFields()) {
         try {
            if (field.canAccess(null) && field.get(null) instanceof Material material) {
               String str = "GTMaterials." + field.getName();
               MATERIAL_MAP.putIfAbsent(material, str);
               MATERIAL_INVERSE_MAP.putIfAbsent(str, material);
            }
         } catch (Throwable e) {
            throw new RuntimeException(e);
         }
      }

      for (Field field : GTOMaterials.class.getFields()) {
         try {
            if (field.canAccess(null) && field.get(null) instanceof Material material) {
               String str = "GTOMaterials." + field.getName();
               MATERIAL_MAP.putIfAbsent(material, str);
               MATERIAL_INVERSE_MAP.putIfAbsent(str, material);
            }
         } catch (Throwable e) {
            throw new RuntimeException(e);
         }
      }

      for (Field field : TagPrefix.class.getFields()) {
         try {
            if (Modifier.isStatic(field.getModifiers()) && field.canAccess(null) && field.get(null) instanceof TagPrefix tagPrefix) {
               String str = "TagPrefix." + field.getName();
               TAGPREFIX_MAP.putIfAbsent(tagPrefix, str);
               TAGPREFIX_INVERSE_MAP.putIfAbsent(str, tagPrefix);
            }
         } catch (Throwable e) {
            throw new RuntimeException(e);
         }
      }

      for (Field field : GTOTagPrefix.class.getFields()) {
         try {
            if (Modifier.isStatic(field.getModifiers()) && field.canAccess(null) && field.get(null) instanceof TagPrefix tagPrefix) {
               String str = "GTOTagPrefix." + field.getName();
               TAGPREFIX_MAP.putIfAbsent(tagPrefix, str);
               TAGPREFIX_INVERSE_MAP.putIfAbsent(str, tagPrefix);
            }
         } catch (Throwable e) {
            throw new RuntimeException(e);
         }
      }

      for (Field field : CustomTags.class.getFields()) {
         try {
            if (field.canAccess(null) && field.get(null) instanceof TagKey tagKey) {
               String str = "CustomTags." + field.getName();
               TAG_MAP.putIfAbsent(tagKey, str);
               TAG_INVERSE_MAP.putIfAbsent(str, tagKey);
            }
         } catch (Throwable e) {
            throw new RuntimeException(e);
         }
      }
   }
}
