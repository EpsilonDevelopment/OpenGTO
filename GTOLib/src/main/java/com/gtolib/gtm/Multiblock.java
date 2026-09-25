package com.gtolib.gtm;

import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.pattern.predicates.SimplePredicate;
import com.gtolib.api.machine.MultiblockDefinition;
import com.lowdragmc.lowdraglib.utils.BlockInfo;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import kotlin.Triple;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public final class Multiblock {
   private static final int REGION_SIZE = 512;
   private static int LAST_OFFSET_INDEX = 0;

   private static BlockPos locateNextRegion() {
      int currentIndex = LAST_OFFSET_INDEX++;
      int x = 0;
      int z = 0;
      if (currentIndex > 0) {
         int v = (int)(Mth.sqrt(currentIndex + 0.25F) - 0.5F);
         int nextV = v + 1;
         int spiralBaseIndex = v * nextV;
         int flipFlop = (v & 1) * 2 - 1;
         int offset = flipFlop * nextV / 2;
         int var9 = x + offset;
         z += offset;
         int cornerIndex = spiralBaseIndex + nextV;
         if (currentIndex < cornerIndex) {
            x = var9 - flipFlop * (currentIndex - spiralBaseIndex + 1);
         } else {
            x = var9 - flipFlop * nextV;
            z -= flipFlop * (currentIndex - cornerIndex + 1);
         }
      }

      return new BlockPos(x * 512, 50, z * 512);
   }

   public static Triple<BlockPos, BlockPattern, Reference2ReferenceOpenHashMap<BlockInfo, LongOpenHashSet>> initializeInfo(
      Map partsMap, MultiblockDefinition definition, MultiblockShapeInfo info
   ) {
      Reference2ReferenceOpenHashMap<BlockInfo, LongOpenHashSet> blockMap = new Reference2ReferenceOpenHashMap<>();
      BlockPos multiController = null;
      BlockPos multiPos = locateNextRegion();
      BlockInfo[][][] blocks = info.getBlocks();
      int lengthX = blocks.length;

      for (int x = 0; x < lengthX; x++) {
         BlockInfo[][] aisle = blocks[x];
         int lengthY = aisle.length;

         for (int y = 0; y < lengthY; y++) {
            BlockInfo[] column = aisle[y];
            int lengthZ = column.length;

            for (int z = 0; z < lengthZ; z++) {
               BlockInfo blockInfo = column[z];
               if (blockInfo != null) {
                  Block block = blockInfo.getBlockState().getBlock();
                  if (block != Blocks.AIR) {
                     BlockPos pos = multiPos.offset(x, y, z);
                     boolean isController;
                     if (multiController == null && definition.get() == blockInfo.getBlockState().getBlock()) {
                        multiController = pos;
                        isController = true;
                     } else {
                        isController = false;
                     }

                     blockMap.computeIfAbsent(blockInfo, k -> new LongOpenHashSet()).add(pos.asLong());
                     if (partsMap != null) {
                        Item item = SimplePredicate.toItem(block);
                        if (item != Items.AIR) {
                           ((Multiblock.PartInfo)((Reference2ObjectOpenHashMap)partsMap).computeIfAbsent(item, key -> new Multiblock.PartInfo(item, blockInfo, isController))).amount++;
                        }
                     }
                  }
               }
            }
         }
      }

      return new Triple<>(multiController, info.pattern.get(), blockMap);
   }

   public static List<ItemStack> initializePattern(MultiblockDefinition definition, MultiblockShapeInfo info) {
      Reference2ObjectOpenHashMap<Item, Multiblock.PartInfo> partsMap = new Reference2ObjectOpenHashMap<>();
      initializeInfo(partsMap, definition, info);
      return partsMap.values().stream().sorted((one, two) -> {
         if (one.isController) {
            return -1;
         } else if (two.isController) {
            return 1;
         } else if (one.isTile && !two.isTile) {
            return -1;
         } else {
            return two.isTile && !one.isTile ? 1 : two.amount - one.amount;
         }
      }).map(Multiblock.PartInfo::getItemStack).filter(list -> !list.isEmpty()).collect(Collectors.toList());
   }

   private static final class PartInfo {
      private final Item item;
      private final boolean isController;
      private final boolean isTile;
      private int amount;

      private PartInfo(Item item, BlockInfo blockInfo, boolean isController) {
         this.item = item;
         this.isController = isController;
         this.isTile = blockInfo.hasBlockEntity();
      }

      private ItemStack getItemStack() {
         return this.item.getDefaultInstance().copyWithCount(this.amount);
      }
   }
}
