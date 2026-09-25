package com.gtolib.api.ae2.pattern;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.util.ConfigInventory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class PatternUtils {
   public static void mulPatternEncodingArea(ConfigInventory encodedInputsInv, ConfigInventory encodedOutputsInv, int data) {
      GenericStack[] output = gtolib$valid(encodedOutputsInv, data);
      if (output != null) {
         GenericStack[] input = gtolib$valid(encodedInputsInv, data);
         if (input != null) {
            for (int slot = 0; slot < output.length; slot++) {
               if (output[slot] != null) {
                  encodedOutputsInv.setStack(slot, output[slot]);
               }
            }

            for (int slot = 0; slot < input.length; slot++) {
               if (input[slot] != null) {
                  encodedInputsInv.setStack(slot, input[slot]);
               }
            }
         }
      }
   }

   private static GenericStack[] gtolib$valid(ConfigInventory inv, int data) {
      boolean flag = data > 0;
      if (!flag) {
         data = -data;
      }

      GenericStack[] result = new GenericStack[inv.size()];

      for (int slot = 0; slot < inv.size(); slot++) {
         GenericStack stack = inv.getStack(slot);
         if (stack != null) {
            if (flag) {
               if (data * stack.amount() > 2147483647L) {
                  return null;
               }

               result[slot] = new GenericStack(stack.what(), data * stack.amount());
            } else {
               if (stack.amount() % data != 0L) {
                  return null;
               }

               result[slot] = new GenericStack(stack.what(), stack.amount() / data);
            }
         }
      }

      return result;
   }

   public static void cyclePatternEncodingArea(ConfigInventory encodedInv) {
      GenericStack[] newOutputs = new GenericStack[encodedInv.size()];

      for (int i = 0; i < encodedInv.size(); i++) {
         newOutputs[i] = null;
         if (encodedInv.getStack(i) != null) {
            for (int j = 1; j < encodedInv.size(); j++) {
               GenericStack nextItem = encodedInv.getStack((i + j) % encodedInv.size());
               if (nextItem != null) {
                  newOutputs[i] = nextItem;
                  break;
               }
            }
         }
      }

      for (int i = 0; i < newOutputs.length; i++) {
         encodedInv.setStack(i, newOutputs[i]);
      }
   }

   @Nullable
   public static ItemStack encodeProcessRenamedItemPattern(AEItemKey key, long amount, String name) {
      if (key != null && amount > 0L && name != null && !name.isEmpty()) {
         ItemStack keyItem = key.toStack();
         if (keyItem.hasCustomHoverName()) {
            keyItem.resetHoverName();
         }

         AEItemKey unrenamedKey = AEItemKey.of(keyItem);
         AEItemKey renamedKey = AEItemKey.of(keyItem.copy().setHoverName(Component.literal(name)));
         return PatternDetailsHelper.encodeProcessingPattern(
            new GenericStack[]{new GenericStack(unrenamedKey, amount)}, new GenericStack[]{new GenericStack(renamedKey, amount)}
         );
      } else {
         return null;
      }
   }

   @Nullable
   public static ItemStack encodeProcessRenamedItemPattern(GenericStack stack) {
      AEKey what = stack.what();
      long amount = stack.amount();
      if (what.getType() == AEKeyType.items() && amount > 0L) {
         AEItemKey key = (AEItemKey)what;
         if (key.toStack().hasCustomHoverName()) {
            String name = key.toStack().getHoverName().getString();
            ItemStack o = key.toStack();
            o.resetHoverName();
            AEItemKey originKey = AEItemKey.of(o);
            return encodeProcessRenamedItemPattern(originKey, amount, name);
         } else {
            return null;
         }
      } else {
         return null;
      }
   }
}
