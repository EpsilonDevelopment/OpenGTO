package com.gtolib.api.item.tool.ae2.patternTool;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.crafting.pattern.AEProcessingPattern;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gto.registrate.util.entry.ItemEntry;
import com.gtolib.GTOCore;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class Ae2BaseProcessingPattern {
   private ItemStack patternStack;
   private final ServerPlayer serverPlayer;
   private final List<Item> DefaultBlackItem = new ArrayList<>(List.of(GTItems.PROGRAMMED_CIRCUIT.asItem()));

   public void setDefaultFilter() {
      AEProcessingPattern aeProcessingPattern = Ae2BaseProcessingPatternHelper.decodeToAEProcessingPattern(this.patternStack, this.serverPlayer);
      if (aeProcessingPattern != null) {
         this.patternStack = PatternDetailsHelper.encodeProcessingPattern(
            Ae2BaseProcessingPatternHelper.GenericStackListBlackFilter(aeProcessingPattern.getSparseInputs(), this.DefaultBlackItem),
            Ae2BaseProcessingPatternHelper.GenericStackListBlackFilter(aeProcessingPattern.getSparseOutputs(), this.DefaultBlackItem)
         );
      }
   }

   private void useSetScale(int newScale, boolean div, long maxItemStack, long maxFluidStack) {
      try {
         ItemStack oldPatternStack = this.patternStack;
         ItemStack newPatternStack = Ae2BaseProcessingPatternHelper.multiplyScale(
            newScale,
            div,
            Objects.requireNonNull(Ae2BaseProcessingPatternHelper.decodeToAEProcessingPattern(oldPatternStack, this.serverPlayer)),
            maxItemStack,
            maxFluidStack
         );
         if (newPatternStack != null && !newPatternStack.isEmpty()) {
            this.patternStack = newPatternStack;
         }
      } catch (Exception e) {
         GTOCore.LOGGER.error(e.getMessage());
      }
   }

   public void setScale(int newScale, boolean div, long maxItemStack, long maxFluidStack) {
      maxFluidStack = Math.min(maxFluidStack, 1000000L);
      maxItemStack = Math.min(maxItemStack, 1000000L);
      this.useSetScale(newScale, div, maxItemStack, maxFluidStack);
   }

   public void setScale(int newScale, boolean div) {
      long maxItemStack = 9999999L;
      long maxFluidStack = 9999999L;
      this.useSetScale(newScale, div, maxItemStack, maxFluidStack);
   }

   public ItemStack getPatternItemStack() {
      return this.patternStack;
   }

   public Ae2BaseProcessingPattern(ItemStack patternStack, ServerPlayer serverPlayer) {
      this.patternStack = patternStack;
      this.serverPlayer = serverPlayer;
      Ae2BaseProcessingPatternHelper.decodeToAEProcessingPattern(patternStack, serverPlayer);

      try {
         for (ItemEntry<Item> shapeMold : GTItems.SHAPE_MOLDS) {
            if (shapeMold != null) {
               this.DefaultBlackItem.add(shapeMold.asItem());
            }
         }

         for (ItemEntry<Item> shapeMold : GTItems.SHAPE_EXTRUDERS) {
            if (shapeMold != null) {
               this.DefaultBlackItem.add(shapeMold.asItem());
            }
         }
      } catch (Exception e) {
         GTOCore.LOGGER.error(e.getMessage());
      }
   }
}
