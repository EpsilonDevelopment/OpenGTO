package com.gtolib.utils;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKey;
import com.gtocore.integration.ae.hooks.IAEKeyExtension;
import com.gtolib.api.ae2.stacks.TagPrefixKey;
import com.gtolib.api.ae2.stacks.TagPrefixKeyType;
import java.util.Collection;
import java.util.Objects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AEChemicalHelper {
   @NotNull
   public static TagPrefix getTagPrefix(AEKey what) {
      return what instanceof AEItemKey itemKey ? ChemicalHelper.getPrefix(itemKey.getItem()) : TagPrefix.NULL_PREFIX;
   }

   @Nullable
   public static FluidStorageKey getFluidStorageKey(AEKey what) {
      if (what instanceof AEFluidKey fluidKey) {
         Material material = ChemicalHelper.getMaterial(fluidKey.getFluid());
         Collection<FluidStorageKey> allFluidKeys = FluidStorageKey.allKeys();
         return allFluidKeys.stream().filter(key -> {
            Fluid fluidOther = material.getProperty(PropertyKey.FLUID).getStorage().get(key);
            return fluidOther != null && fluidOther.isSame(fluidKey.getFluid());
         }).findFirst().orElse(null);
      } else {
         return null;
      }
   }

   @NotNull
   private static AEItemKey getItemKey(Material material, @NotNull AEItemKey itemKeySrc) {
      TagPrefix tagPrefix = getTagPrefix(itemKeySrc);
      if (tagPrefix != TagPrefix.NULL_PREFIX) {
         ItemStack item = ChemicalHelper.get(tagPrefix, material, 1);
         return Objects.requireNonNullElse(AEItemKey.of(item), itemKeySrc);
      } else {
         return itemKeySrc;
      }
   }

   @NotNull
   private static AEFluidKey getFluidKey(Material material, @NotNull AEFluidKey storageKeySrc) {
      FluidStorageKey storageFluidKey = getFluidStorageKey(storageKeySrc);
      if (storageFluidKey != null) {
         Fluid result = material.getProperty(PropertyKey.FLUID).getStorage().get(storageFluidKey);
         return result != null ? AEFluidKey.of(result) : storageKeySrc;
      } else {
         return storageKeySrc;
      }
   }

   @Contract("_, !null -> !null; _, null -> null")
   public static AEKey getKey(Material material, AEKey keySrc) {
      return switch (keySrc) {
         case AEItemKey itemKey -> getItemKey(material, itemKey);
         case AEFluidKey fluidKey -> getFluidKey(material, fluidKey);
         case TagPrefixKey tagPrefixKey -> tagPrefixKey.getFromMaterial(material);
         case null, default -> keySrc;
      };
   }

   @NotNull
   public static Material getMaterial(AEKey key) {
      return ((IAEKeyExtension)key).getGtocore$material();
   }

   public static TagPrefixKey getTagPrefixKey(AEKey key) {
      AEKey var1 = key;
      switch (var1) {
         case AEItemKey itemKey:
            TagPrefix tagPrefix = getTagPrefix(itemKey);
            if (tagPrefix == TagPrefix.NULL_PREFIX) {
               return null;
            }

            return (TagPrefixKey)TagPrefixKeyType.map.getCache(tagPrefix);
         case AEFluidKey fluidKey:
            FluidStorageKey storageKey = getFluidStorageKey(fluidKey);
            if (storageKey != null) {
               return (TagPrefixKey)TagPrefixKeyType.fluidMap.getCache(storageKey);
            }

            return null;
         case TagPrefixKey var8:
            return (TagPrefixKey)var1;
         default:
            return null;
      }
   }

   public static AEKey getKey(TagPrefix form, Material mat) {
      return AEItemKey.of(ChemicalHelper.get(form, mat));
   }
}
