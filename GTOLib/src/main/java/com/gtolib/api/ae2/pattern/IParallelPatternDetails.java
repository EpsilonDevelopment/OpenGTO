package com.gtolib.api.ae2.pattern;

import appeng.api.crafting.IPatternDetails;
import appeng.crafting.pattern.AECraftingPattern;
import appeng.crafting.pattern.AEProcessingPattern;
import appeng.crafting.pattern.AESmithingTablePattern;
import appeng.crafting.pattern.AEStonecuttingPattern;
import com.gtolib.ae2.pattern.ParallelAECraftingPattern;
import com.gtolib.ae2.pattern.ParallelAEProcessingPattern;
import com.gtolib.ae2.pattern.ParallelAESmithingTablePattern;
import com.gtolib.ae2.pattern.ParallelAEStonecuttingPattern;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public interface IParallelPatternDetails extends IDetails {
   IParallelPatternDetails copy(long var1, Level var3);

   IParallelPatternDetails getCopy();

   void parallel(long var1);

   long getParallel();

   @Nullable
   static IParallelPatternDetails of(@Nullable IPatternDetails pattern, @Nullable Level level, long parallel) {
      if (pattern instanceof AEProcessingPattern processingPattern) {
         return new ParallelAEProcessingPattern(processingPattern.getDefinition(), parallel);
      } else if (pattern instanceof AECraftingPattern craftingPattern) {
         return new ParallelAECraftingPattern(craftingPattern.getDefinition(), level, parallel);
      } else if (pattern instanceof AEStonecuttingPattern stonecuttingPattern) {
         return new ParallelAEStonecuttingPattern(stonecuttingPattern.getDefinition(), level, parallel);
      } else {
         return pattern instanceof AESmithingTablePattern smithingTablePattern
            ? new ParallelAESmithingTablePattern(smithingTablePattern.getDefinition(), level, parallel)
            : null;
      }
   }
}
