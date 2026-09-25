package com.gtolib.mixin.emi.stack;

import com.gtolib.emi.EMIManager;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.registry.EmiStackList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EmiStackList.class)
public final class EmiStackListMixin {
   @Shadow(remap = false)
   private static Object2IntMap<EmiStack> strictIndices;
   @Shadow(remap = false)
   private static Object2IntMap<Object> keyIndices;

   @Overwrite(remap = false)
   public static void reload() {
      EmiStackList.stacks = EMIManager.stacks;
   }

   @Overwrite(remap = false)
   public static void bake() {
      int var0 = EMIManager.stacks.size();

      for (int var1 = 0; var1 < var0; var1++) {
         EmiStack var2 = EMIManager.stacks.get(var1);
         strictIndices.put(var2, var1);
         keyIndices.put(var2.getKey(), var1);
      }

      bakeFiltered();
   }

   @Overwrite(remap = false)
   public static void bakeFiltered() {
      EmiStackList.filteredStacks = EMIManager.stacks;
   }
}
