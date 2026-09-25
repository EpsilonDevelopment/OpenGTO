package com.gtolib.ae2.crafting.reborn.api;

import appeng.api.stacks.AEKey;
import com.gtolib.ae2.crafting.reborn.Context;

public abstract class ItemProcess {
   protected ItemProcess next;

   private ItemProcess setNext(ItemProcess next) {
      this.next = next;
      return this;
   }

   public void process(Context context, AEKey item, long needed) {
      if (this.canHandle(context, item)) {
         this.handle(context, item, needed);
      } else if (this.next != null) {
         this.next.process(context, item, needed);
      } else if (needed > 0L) {
         context.missingItems.add(item, needed);
         context.requireItems.remove(item, needed);
      }
   }

   protected abstract boolean canHandle(Context var1, AEKey var2);

   protected abstract void handle(Context var1, AEKey var2, long var3);

   public static final class Factory {
      public static ItemProcess createChain(ItemProcess... processors) {
         for (int i = 0; i < processors.length - 1; i++) {
            processors[i].setNext(processors[i + 1]);
         }

         return processors[0];
      }
   }
}
