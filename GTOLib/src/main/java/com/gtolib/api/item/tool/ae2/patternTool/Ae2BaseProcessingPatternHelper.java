package com.gtolib.api.item.tool.ae2.patternTool;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.crafting.pattern.AEProcessingPattern;
import appeng.crafting.pattern.EncodedPatternItem;
import com.gtolib.GTOCore;
import java.util.Arrays;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

final class Ae2BaseProcessingPatternHelper {
   static ItemStack multiplyScale(int scale, boolean div, AEProcessingPattern patternDetail, long maxItemStack, long maxFluidStack) {
      GenericStack[] input = patternDetail.getSparseInputs();
      GenericStack[] output = patternDetail.getOutputs();
      if (checkModify(input, scale, div, maxItemStack, maxFluidStack) && checkModify(output, scale, div, 10000000L, 1000000L)) {
         GenericStack[] mulInput = new GenericStack[input.length];
         GenericStack[] mulOutput = new GenericStack[output.length];
         modifyStacks(input, mulInput, scale, div);
         modifyStacks(output, mulOutput, scale, div);
         return PatternDetailsHelper.encodeProcessingPattern(mulInput, mulOutput);
      } else {
         GTOCore.LOGGER.info("内部错误：无法整除 或 乘数过大");
         return null;
      }
   }

   static AEProcessingPattern decodeToAEProcessingPattern(ItemStack patternStack, ServerPlayer serverPlayer) {
      if (patternStack.getItem() instanceof EncodedPatternItem patternItem_1) {
         if (patternItem_1.decode(patternStack, serverPlayer.level(), false) instanceof AEProcessingPattern processStack) {
            return processStack;
         }

         GTOCore.LOGGER.info("Ae2BaseProcessingPattern requires a EncodedPatternItem 意外之内的输入：非处理样板");
      } else {
         GTOCore.LOGGER.info("Ae2BaseProcessingPattern requires a EncodedPatternItem 意外之内的输入：非AE样板");
      }

      return null;
   }

   static GenericStack[] GenericStackListBlackFilter(GenericStack[] genericStackArray, List<Item> matchItemList) {
      return Arrays.stream(genericStackArray).filter(genericStack -> !itemMatch(genericStack, matchItemList)).toArray(GenericStack[]::new);
   }

   private static boolean itemMatch(GenericStack genericStack, List<Item> matchItemList) {
      for (Item item : matchItemList) {
         if (genericStack.what().equals(AEItemKey.of(item))) {
            return true;
         }
      }

      return false;
   }

   private static boolean checkModify(GenericStack[] stacks, int scale, boolean div, long maxItemStack, long maxFluidStack) {
      if (div) {
         for (GenericStack stack : stacks) {
            if (stack != null && stack.amount() % scale != 0L) {
               return false;
            }
         }
      } else {
         for (GenericStack stack : stacks) {
            if (stack != null) {
               if (stack.what().getType().equals(AEKeyType.fluids())) {
                  long upper = maxFluidStack * stack.what().getAmountPerUnit();
                  if (stack.amount() * scale > upper) {
                     return false;
                  }
               }

               if (stack.what().getType().equals(AEKeyType.items())) {
                  long upper = maxItemStack * stack.what().getAmountPerUnit();
                  if (stack.amount() * scale > upper) {
                     return false;
                  }
               }
            }
         }
      }

      return true;
   }

   private static void modifyStacks(GenericStack[] stacks, GenericStack[] des, int scale, boolean div) {
      for (int i = 0; i < stacks.length; i++) {
         if (stacks[i] != null) {
            long amt = div ? stacks[i].amount() / scale : stacks[i].amount() * scale;
            des[i] = new GenericStack(stacks[i].what(), amt);
         }
      }
   }
}
