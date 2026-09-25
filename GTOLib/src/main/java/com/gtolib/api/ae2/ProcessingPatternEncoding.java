package com.gtolib.api.ae2;

import appeng.api.networking.IGrid;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import com.google.common.base.Preconditions;
import com.gtocore.integration.ae.PatternContentAccessTerminalPart;
import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

class ProcessingPatternEncoding {
   private static final String NBT_INPUTS = "in";
   private static final String NBT_OUTPUTS = "out";

   public static GenericStack[] getProcessingInputs(CompoundTag nbt, IGrid grid) {
      return getMixedList(nbt, "in", 81, grid);
   }

   public static GenericStack[] getProcessingOutputs(CompoundTag nbt) {
      return getMixedList(nbt, "out", 27, null);
   }

   public static GenericStack[] getMixedList(CompoundTag nbt, String nbtKey, int maxSize, IGrid grid) {
      Objects.requireNonNull(nbt, "Pattern must have a tag.");
      ListTag tag = nbt.getList(nbtKey, 10);
      Preconditions.checkArgument(tag.size() <= maxSize, "Cannot use more than " + maxSize + " ingredients");
      GenericStack[] result = new GenericStack[tag.size()];

      for (int x = 0; x < tag.size(); x++) {
         CompoundTag entry = tag.getCompound(x);
         if (!entry.isEmpty()) {
            GenericStack stack = GenericStack.readTag(entry);
            if (stack == null) {
               throw new IllegalArgumentException("Pattern references missing stack: " + entry);
            }

            if (nbtKey.equals("in") && grid != null) {
               for (PatternContentAccessTerminalPart meSortMachine : grid.getMachines(PatternContentAccessTerminalPart.class)) {
                  if (meSortMachine != null) {
                     AEKey replaced = meSortMachine.getReplacement(stack.what());
                     if (replaced != stack.what()) {
                        stack = new GenericStack(replaced, stack.amount());
                        break;
                     }
                  }
               }
            }

            result[x] = stack;
         }
      }

      return result;
   }

   public static void encodeProcessingPattern(CompoundTag tag, GenericStack[] sparseInputs, GenericStack[] sparseOutputs) {
      tag.put("in", encodeStackList(sparseInputs));
      tag.put("out", encodeStackList(sparseOutputs));
   }

   private static ListTag encodeStackList(GenericStack[] stacks) {
      ListTag tag = new ListTag();
      boolean foundStack = false;

      for (GenericStack stack : stacks) {
         tag.add(GenericStack.writeTag(stack));
         if (stack != null && stack.amount() > 0L) {
            foundStack = true;
         }
      }

      Preconditions.checkArgument(foundStack, "List passed to pattern must contain at least one stack.");
      return tag;
   }
}
