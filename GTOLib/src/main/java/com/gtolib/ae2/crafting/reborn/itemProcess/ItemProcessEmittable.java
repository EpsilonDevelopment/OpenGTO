package com.gtolib.ae2.crafting.reborn.itemProcess;

import appeng.api.stacks.AEKey;
import com.gtolib.ae2.crafting.reborn.Context;
import com.gtolib.ae2.crafting.reborn.api.ItemProcess;

public class ItemProcessEmittable extends ItemProcess {
   @Override
   protected boolean canHandle(Context context, AEKey item) {
      return context.getCraftingService().map(s -> s.canEmitFor(item)).orElse(false);
   }

   @Override
   protected void handle(Context context, AEKey item, long needed) {
      context.emittedItems.add(item, needed);
      context.requireItems.remove(item, needed);
   }
}
