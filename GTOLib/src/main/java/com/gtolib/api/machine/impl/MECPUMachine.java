package com.gtolib.api.machine.impl;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.IParallelHatch;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gtocore.api.pattern.GTOPredicates.DataKeys;
import com.gtocore.common.block.MEStorageCoreBlock;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTORecipeDataKeys;
import com.gtolib.api.machine.impl.part.CraftingInterfacePartMachine;
import com.gtolib.api.machine.multiblock.NoRecipeLogicMultiblockMachine;
import com.gtolib.utils.MathUtil;
import com.gtolib.utils.NumberUtils;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public final class MECPUMachine extends NoRecipeLogicMultiblockMachine {
   private CraftingInterfacePartMachine craftingInterfacePartMachine;
   private IParallelHatch parallelHatch;

   public MECPUMachine(MetaMachineBlockEntity holder) {
      super(holder);
   }

   @Override
   public void onPartScan(IMultiPart part) {
      if (part instanceof IParallelHatch parallelHatch) {
         this.parallelHatch = parallelHatch;
      }
   }

   @Override
   public void onStructureInvalid() {
      super.onStructureInvalid();
      this.parallelHatch = null;
      if (this.craftingInterfacePartMachine != null) {
         this.craftingInterfacePartMachine.setThread(0);
         this.craftingInterfacePartMachine = null;
      }
   }

   @Override
   public void onStructureFormed() {
      super.onStructureFormed();
      Level level = this.getLevel();
      if (level != null) {
         for (IMultiPart part : this.getParts()) {
            if (part instanceof CraftingInterfacePartMachine interfacePartMachine) {
               this.craftingInterfacePartMachine = interfacePartMachine;
               break;
            }
         }

         if (this.craftingInterfacePartMachine != null) {
            double[] storage = this.getMultiblockState().getMatchContext().get(DataKeys.CRAFTING_STORAGE_CORE);
            if (storage != null) {
               long ca = (long)storage[0];
               int acc = (int)storage[1];
               if (acc == 481 && ca == ((MEStorageCoreBlock)GTOBlocks.T5_ME_STORAGE_CORE.get()).getCapacity() * 481L) {
                  ca = Long.MAX_VALUE;
               }

               int glass = this.getMultiblockState().getMatchContext().get(GTORecipeDataKeys.GLASS_TIER);
               this.craftingInterfacePartMachine
                  .setAccelerator(MathUtil.saturatedCast(acc * (this.parallelHatch == null ? 1L : this.parallelHatch.getCurrentParallel())));
               this.craftingInterfacePartMachine.setStorage(ca);
               this.craftingInterfacePartMachine.setThread(Math.min(256, 1 << glass));
            }
         }
      }
   }

   @Override
   public void customText(@NotNull List<Component> textList) {
      super.customText(textList);
      if (this.craftingInterfacePartMachine != null) {
         textList.add(Component.translatable("gui.tooltips.ae2.CpuStatusStorage", NumberUtils.formatLong(this.craftingInterfacePartMachine.storage)));
         textList.add(Component.translatable("gui.tooltips.ae2.CpuStatusCoProcessor", NumberUtils.formatLong(this.craftingInterfacePartMachine.accelerator)));
         textList.add(Component.translatable("gtocore.lang.template.allow_multi_recipe_parallel.1627326288", this.craftingInterfacePartMachine.thread));
      }
   }
}
