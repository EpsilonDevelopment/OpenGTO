package com.gtolib.utils;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.ItemIngredient;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Ingredient.ItemValue;
import net.minecraft.world.item.crafting.Ingredient.TagValue;
import net.minecraft.world.item.crafting.Ingredient.Value;
import net.minecraft.world.level.material.Fluid;

public final class StringConverter {
   private StringConverter() {
   }

   public static String fromItem(ItemIngredient ingredient, int re) {
      if (ingredient.isEmpty()) {
         return null;
      }

      ItemStack stack = ingredient.getInnerItemStack();
      int amount = ingredient.getAmount();
      Ingredient inner = ingredient.inner;

      for (Value value : inner.values) {
         if (value instanceof ItemValue itemValue) {
            for (ItemStack itemStack : itemValue.getItems()) {
               MaterialEntry entry = ChemicalHelper.getMaterialEntry(itemStack.getItem());
               if (!entry.isEmpty()) {
                  String material;
                  if (StringIndex.MATERIAL_MAP.containsKey(entry.material())) {
                     material = StringIndex.MATERIAL_MAP.get(entry.material());
                  } else {
                     material = "GTOMaterials." + FormattingUtil.lowerUnderscoreToUpperCamel(entry.material().getName());
                  }

                  String tagPrefix;
                  if (StringIndex.TAGPREFIX_MAP.containsKey(entry.tagPrefix())) {
                     tagPrefix = StringIndex.TAGPREFIX_MAP.get(entry.tagPrefix());
                  } else {
                     tagPrefix = "GTOTagPrefix." + entry.tagPrefix().name().toUpperCase();
                  }

                  if (re == 2) {
                     return "new MaterialEntry(" + tagPrefix + ", " + material + (amount > 1 ? ", " + amount : "") + ")";
                  }

                  if (re == 1) {
                     return tagPrefix + ", " + material + (amount > 1 ? ", " + amount : "");
                  }

                  return amount > 1
                     ? "ChemicalHelper.get(" + tagPrefix + ", " + material + ", " + amount + ")"
                     : "ChemicalHelper.getItem(" + tagPrefix + ", " + material + ")";
               }
            }
         } else if (value instanceof TagValue tagValue) {
            TagKey<Item> tag = tagValue.tag;
            if (StringIndex.TAG_MAP.containsKey(tag)) {
               return StringIndex.TAG_MAP.get(tag) + (amount > 1 ? ", " + amount : "");
            }

            ResourceLocation resourceLocation = tag.location();

            String s = switch (resourceLocation.getNamespace()) {
               case "gtocore" -> "GTOCore.id(\"" + resourceLocation.getPath() + "\")";
               case "gtceu" -> "GTCEu.id(\"" + resourceLocation.getPath() + "\")";
               case "forge" -> "new ResourceLocation(\"forge\", \"" + resourceLocation.getPath() + "\")";
               case "minecraft" -> "new ResourceLocation(\"minecraft\", \"" + resourceLocation.getPath() + "\")";
               default -> "";
            };
            return "TagUtil.createTag(" + s + ")";
         }
      }

      if (stack.getItem() instanceof BlockItem blockItem && !ItemUtils.getIdLocation(blockItem.getBlock()).getNamespace().equals("minecraft")) {
         if (StringIndex.BLOCK_LINK_MAP.containsKey(blockItem.getBlock())) {
            String str = StringIndex.BLOCK_LINK_MAP.get(blockItem.getBlock());
            return re == 1 ? str + ".asItem()" + amount(amount) : str + (amount > 1 ? ".asStack(" + amount + ")" : ".asItem()");
         }

         if (StringIndex.BLOCK_MAP.containsKey(blockItem.getBlock())) {
            String str = StringIndex.BLOCK_MAP.get(blockItem.getBlock()) + ".asItem()";
            return re == 1 ? str + amount(amount) : (amount > 1 ? "new ItemStack(" + str + amount(amount) + ")" : str);
         }
      } else {
         if (StringIndex.ITEM_LINK_MAP.containsKey(stack.getItem())) {
            String str = StringIndex.ITEM_LINK_MAP.get(stack.getItem());
            return re == 1 ? str + amount(amount) : str + (amount > 1 ? ".asStack(" + amount + ")" : ".asItem()");
         }

         if (StringIndex.ITEM_MAP.containsKey(stack.getItem())) {
            String str = StringIndex.ITEM_MAP.get(stack.getItem());
            return re == 1 ? str + amount(amount) : (amount > 1 ? "new ItemStack(" + str + amount(amount) + ")" : str);
         }

         if (ItemUtils.getIdLocation(stack.getItem()).getNamespace().equals("minecraft")) {
            String str = "Items." + ItemUtils.getIdLocation(stack.getItem()).getPath().toUpperCase();
            return re == 1 ? str + amount(amount) : (amount > 1 ? "new ItemStack(" + str + amount(amount) + ")" : str);
         }
      }

      if (re == 1) {
         return "\"" + ItemUtils.getId(ingredient.getInnerItemStack()) + (amount > 1 ? "\", " + amount : "\"");
      } else {
         return amount > 1
            ? "RegistriesUtils.getItemStack(\"" + ItemUtils.getId(ingredient.getInnerItemStack()) + "\", " + amount + ")"
            : "RegistriesUtils.getItem(\"" + ItemUtils.getId(ingredient.getInnerItemStack()) + "\")";
      }
   }

   public static String fromFluid(FluidIngredient ingredient, boolean r) {
      if (ingredient.isEmpty()) {
         return null;
      }

      Fluid fluid = ingredient.getFluid();
      ResourceLocation resourceLocation = FluidUtils.getIdLocation(fluid);
      boolean plasma = false;
      boolean liquid = false;
      boolean molten = false;
      Material material = GTMaterials.get(resourceLocation.toString());
      if (material.isNull() && resourceLocation.toString().contains("_plasma")) {
         material = GTMaterials.get(resourceLocation.toString().replace("_plasma", ""));
         if (!material.isNull()) {
            plasma = true;
         }
      }

      if (material.isNull() && resourceLocation.toString().contains("liquid_")) {
         material = GTMaterials.get(resourceLocation.toString().replace("liquid_", ""));
         if (!material.isNull()) {
            liquid = true;
         }
      }

      if (material.isNull() && resourceLocation.toString().contains("molten_")) {
         material = GTMaterials.get(resourceLocation.toString().replace("molten_", ""));
         if (!material.isNull()) {
            molten = true;
         }
      }

      String s;
      if (!material.isNull()) {
         String a = "";
         if (plasma) {
            a = "FluidStorageKeys.PLASMA, ";
         }

         if (liquid) {
            a = "FluidStorageKeys.LIQUID, ";
         }

         if (molten) {
            a = "FluidStorageKeys.MOLTEN, ";
         }

         String m;
         if (StringIndex.MATERIAL_MAP.containsKey(material)) {
            m = StringIndex.MATERIAL_MAP.get(material);
         } else {
            m = "GTOMaterials." + FormattingUtil.lowerUnderscoreToUpperCamel(material.getName());
         }

         s = r ? m + ", " + a + ingredient.getAmount() : m + ".getFluid(" + a + ingredient.getAmount() + ")";
      } else {
         s = StringIndex.FLUID_MAP.get(fluid);
         if (s != null) {
            s = r ? s + ", " + ingredient.getAmount() : "new FluidStack(" + s + amount(ingredient.getAmount()) + ")";
         } else {
            s = "RegistriesUtils.getFluidStack(\"" + resourceLocation + "\", " + ingredient.getAmount() + ")";
         }
      }

      return s;
   }

   private static String amount(int amount) {
      return amount > 1 ? ", " + amount : "";
   }
}
