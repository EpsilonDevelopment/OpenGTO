package com.gtolib;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.recipe.GTRecipeBuilder;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.common.data.GTDimensionMarkers;
import com.gregtechceu.gtceu.common.data.GTRecipeDataKeys;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.api.research.recipe.ResearchPointsRecipeExtion;
import com.gtocore.common.block.BlockMap;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOCovers;
import com.gtocore.common.data.GTOCreativeModeTabs;
import com.gtocore.common.data.GTOElements;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.GTORecipeCategories;
import com.gtocore.common.data.GTORecipeDataKeys;
import com.gtocore.common.data.GTORecipeTypes;
import com.gtocore.common.data.GTOSoundEntries;
import com.gtocore.common.data.machines.GCYMMachines;
import com.gtocore.common.data.machines.GTAEMachines;
import com.gtocore.common.data.machines.GTMachineModify;
import com.gtolib.api.data.Dimension;
import com.gtolib.api.data.GTOWorldGenLayers;
import com.gtolib.api.data.chemical.material.GTOMaterialBuilder;
import com.gtolib.api.fluid.IFluid;
import com.gtolib.api.item.IItem;
import com.gtolib.api.recipe.extension.HURecipeExtension;
import com.gtolib.api.recipe.extension.HUTRecipeExtension;
import com.gtolib.api.recipe.extension.MANARecipeExtension;
import com.gtolib.api.recipe.extension.MANATRecipeExtension;
import com.gtolib.api.registries.GTORegistration;
import com.hepdd.gtmthings.data.CreativeMachines;
import com.hepdd.gtmthings.data.CreativeModeTabs;
import com.hepdd.gtmthings.data.CustomItems;
import com.hepdd.gtmthings.data.CustomMachines;
import com.hepdd.gtmthings.data.GTMTCovers;
import com.hepdd.gtmthings.data.WirelessMachines;
import java.util.Arrays;

final class GTAddon implements IGTAddon {
   static final IGTAddon INSTANCE = new GTAddon();

   private GTAddon() {
      GTUtil.ITEM_ID = i -> ((IItem)i).gtolib$getIdLocation();
      GTUtil.FLUID_ID = f -> ((IFluid)f).gtolib$getIdLocation();
   }

   @Override
   public String addonModId() {
      return "gtocore";
   }

   @Override
   public GTRegistrate getRegistrate() {
      return GTORegistration.GTO;
   }

   @Override
   public boolean requiresHighTier() {
      return true;
   }

   @Override
   public void registerSounds() {
      GTOSoundEntries.init();
   }

   @Override
   public void registerCovers() {
      CreativeModeTabs.init();
      GTMTCovers.init();
      CustomItems.init();
      GTOCovers.init();
      GTORegistration.GTO.defaultCreativeTab(GTOCreativeModeTabs.GTO_BLOCK);
      GTOBlocks.init();
      GTORegistration.GTO.defaultCreativeTab(GTOCreativeModeTabs.GTO_ITEM);
      GTOItems.init();
   }

   @Override
   public void registerMachiness() {
      GTORegistration.GTO.defaultCreativeTab(GTOCreativeModeTabs.GTO_MACHINE);
      BlockMap.init();
      GTAEMachines.init();
      GTMachineModify.init();
      GCYMMachines.init();
      CreativeMachines.init();
      WirelessMachines.init();
      CustomMachines.init();
      GTOMachines.init();
   }

   @Override
   public void registerDimensionMarkers() {
      Arrays.stream(Dimension.values())
         .filter(d -> !d.getLocation().getNamespace().equals("minecraft"))
         .forEach(
            d -> {
               GTDimensionMarkers.createAndRegister(d.getLocation(), d.getTier(), d.getItemKey(), "gtocore.dimension." + d.getLocation().getPath());
               if (d.getOrbit() != null) {
                  GTDimensionMarkers.createAndRegister(
                     d.getOrbit().location(), d.getTier(), d.getItemKey(), "planet." + d.getOrbit().location().toString().replace(':', '.')
                  );
               }
            }
         );
      GTDimensionMarkers.createAndRegister(
         Dimension.OVERWORLD.getOrbit().location(),
         Dimension.OVERWORLD.getTier(),
         () -> GTDimensionMarkers.OVERWORLD_MARKER,
         "planet." + Dimension.OVERWORLD.getOrbit().location().toString().replace(':', '.')
      );
   }

   @Override
   public void registerElements() {
      GTOMaterialBuilder.REGISTRY.getMaterials();
      GTOElements.init();
   }

   @Override
   public void registerMaterials() {
      GTOMaterials.init();
   }

   @Override
   public void registerTagPrefixes() {
      GTOTagPrefix.init();
   }

   @Override
   public void registerWorldgenLayers() {
      GTOWorldGenLayers.init();
   }

   @Override
   public void registerRecipeTypes() {
      GTORecipeTypes.init();
      GTRecipeBuilder.RAW = GTRecipeTypes.DUMMY_RECIPES.recipeBuilder(GTCEu.id("raw"));
   }

   @Override
   public void registerRecipeCategories() {
      GTORecipeCategories.init();
   }

   @Override
   public void registerRecipeDataKey() {
      GTRecipeDataKeys.REGISTRY.register(MANATRecipeExtension.INSTANCE);
      GTRecipeDataKeys.REGISTRY.register(MANARecipeExtension.INSTANCE);
      GTRecipeDataKeys.REGISTRY.register(HUTRecipeExtension.INSTANCE);
      GTRecipeDataKeys.REGISTRY.register(HURecipeExtension.INSTANCE);
      GTRecipeDataKeys.REGISTRY.register(ResearchPointsRecipeExtion.INSTANCE);
      GTORecipeDataKeys.init();
   }
}
