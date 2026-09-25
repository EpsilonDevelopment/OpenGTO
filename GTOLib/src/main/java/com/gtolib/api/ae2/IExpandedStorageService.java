package com.gtolib.api.ae2;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;

public interface IExpandedStorageService {
   static long fuzzyExtract(AEKey key, long amount, IStorageService service, Actionable mode, IActionSource source) {
      KeyCounter inventory = service.getCachedInventory();
      long extracted = 0L;

      for (AEKey k : inventory.findFuzzyKey(key, FuzzyMode.IGNORE_ALL)) {
         extracted += service.getInventory().extract(k, amount - extracted, mode, source);
         if (extracted >= amount) {
            break;
         }
      }

      return extracted;
   }
}
