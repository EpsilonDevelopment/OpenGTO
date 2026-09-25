package com.gtolib.emi;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.integration.modules.emi.EmiStackHelper;
import dev.emi.emi.runtime.EmiFavorites;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class EMIFavouriteAEKeyCache {
   public static final EMIFavouriteAEKeyCache INSTANCE = new EMIFavouriteAEKeyCache();
   public final List<AEKey> cache = new ArrayList<>();

   private EMIFavouriteAEKeyCache() {
      this.cache.addAll(cal());
   }

   private static List<AEKey> cal() {
      return EmiFavorites.favorites
         .stream()
         .flatMap(e -> e.getStack().getEmiStacks().stream())
         .map(EmiStackHelper::toGenericStack)
         .filter(Objects::nonNull)
         .map(GenericStack::what)
         .filter(Objects::nonNull)
         .toList();
   }

   public static void recalculate() {
      INSTANCE.cache.clear();
      INSTANCE.cache.addAll(cal());
   }
}
