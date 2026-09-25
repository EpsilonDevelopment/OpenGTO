package com.gtolib.mixin.emi;

import appeng.integration.modules.jeirei.EncodingHelper;
import appeng.menu.me.common.GridInventoryEntry;
import com.gto.fastcollection.fastutil.OpenCacheHashSet;
import com.gtolib.ae2.me2in1.emi.ExtendedEncodingHelper;
import com.gtolib.emi.EMIFavouriteAEKeyCache;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EncodingHelper.class)
public interface EncodingHelperAccesspr {
   @Accessor(value = "ENTRY_COMPARATOR", remap = false)
   static Comparator<GridInventoryEntry> entryComparator() {
      throw new AssertionError();
   }

   @Mixin(value = EncodingHelper.class, remap = false)
   abstract class EncodingHelperMixin {
      @ModifyVariable(method = "getIngredientPriorities", at = @At("HEAD"), argsOnly = true, index = 1, remap = false)
      private static Comparator<GridInventoryEntry> modifyComparator(Comparator<GridInventoryEntry> var0) {
         return Objects.equals(var0, EncodingHelperAccesspr.entryComparator()) ? ExtendedEncodingHelper.getEntryComparators() : var0;
      }

      @ModifyExpressionValue(
         method = "getIngredientPriorities",
         at = @At(value = "INVOKE", target = "Lappeng/menu/me/common/IClientRepo;getAllEntries()Ljava/util/Set;"),
         remap = false
      )
      private static Set<GridInventoryEntry> modifyMEStorageEntriesForSorting(Set<GridInventoryEntry> var0) {
         Set var1 = EMIFavouriteAEKeyCache.INSTANCE.cache.stream().map(var0x -> new GridInventoryEntry(-1L, var0x, 0L, 0L, false)).collect(Collectors.toSet());
         if (!var1.isEmpty()) {
            OpenCacheHashSet var2 = new OpenCacheHashSet(var0);
            var2.addAll(var1);
            return var2;
         } else {
            return var0;
         }
      }
   }
}
