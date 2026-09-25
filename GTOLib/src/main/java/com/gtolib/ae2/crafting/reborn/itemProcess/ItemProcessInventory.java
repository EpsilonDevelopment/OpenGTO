package com.gtolib.ae2.crafting.reborn.itemProcess;

import appeng.api.stacks.AEKey;
import com.gtolib.ae2.crafting.reborn.Context;
import com.gtolib.ae2.crafting.reborn.api.ItemProcess;

public class ItemProcessInventory extends ItemProcess {
   @Override
   protected boolean canHandle(Context context, AEKey item) {
      return !context.target.what().equals(item) && context.getUnOccupiedStorage(item) > 0L;
   }

   @Override
   protected void handle(Context context, AEKey item, long needed) {
      long unOccupiedStorage = context.getUnOccupiedStorage(item);
      long occupiedStorage = Math.min(unOccupiedStorage, needed);
      context.usedItems.add(item, occupiedStorage);
      context.requireItems.remove(item, occupiedStorage);
      long var9 = needed - occupiedStorage;
      if (var9 > 0L) {
         this.next.process(context, item, var9);
      }
   }
}
