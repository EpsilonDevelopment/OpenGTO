package com.gtolib.gtm;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.pattern.predicates.SimplePredicate;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gto.fastcollection.fastutil.O2IOpenCacheHashMap;
import com.gtocore.common.block.BlockMap;
import com.gtocore.common.data.GTOMachines;
import com.hepdd.gtmthings.GTMThings;
import com.hepdd.gtmthings.api.misc.Hatch;
import java.lang.StackWalker.Option;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;

class AutoBuildSetting {
   static final StackWalker stackWalker = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);
   static final ProtectionDomain LIB = RecipeLogicExt.class.getProtectionDomain();
   static final ProtectionDomain GTO = GTOMachines.class.getProtectionDomain();
   static final ProtectionDomain GTM = GTMachines.class.getProtectionDomain();
   static final ProtectionDomain GTMT = GTMThings.class.getProtectionDomain();
   O2IOpenCacheHashMap<String> categoryTierMap = new O2IOpenCacheHashMap<>();
   Set<Block> blocks = Collections.emptySet();
   int repeatCount;
   int module;
   int mainIndex;
   boolean isReplaceMode;
   boolean isDemolitionMode;
   boolean isUseAEMode;
   boolean isFlipMode;
   boolean isNoHatchMode = true;

   public List<AEKey> apply(Block[] blocks) {
      if (blocks != null && blocks.length != 0) {
         if (this.categoryTierMap != null && blocks.length > 1) {
            for (Block block : blocks) {
               String category = BlockMap.getCategory(block);
               if (this.categoryTierMap.getInt(category) > 0 && this.blocks.contains(block)) {
                  Block[] categoryBlocks = (Block[])BlockMap.MAP.get(category);
                  if (categoryBlocks != null && categoryBlocks.length > 0) {
                     Block chosenBlock = categoryBlocks[Math.min(categoryBlocks.length, this.categoryTierMap.getInt(category)) - 1];
                     return Collections.singletonList(AEItemKey.of(chosenBlock.asItem()));
                  }
               }
            }
         }

         List<AEKey> candidates = new ArrayList<>();

         for (Block block : blocks) {
            if (block instanceof LiquidBlock fluid) {
               candidates.add(AEFluidKey.of(fluid.getFluid().getSource()));
            } else if (block != Blocks.AIR) {
               candidates.add(AEItemKey.of(SimplePredicate.toItem(block)));
            }
         }

         return candidates;
      } else {
         return Collections.emptyList();
      }
   }

   boolean isPlaceHatch(Block[] blocks) {
      return this.isNoHatchMode && blocks != null ? !(blocks[0] instanceof MetaMachineBlock machineBlock && Hatch.Set.contains(machineBlock)) : true;
   }
}
