package com.gtolib.data.wallet.transactionStrategy;

import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;

public class ScheduledDeletionStrategy implements TransactionTypeStrategy {
   private final long time;

   public ScheduledDeletionStrategy(long time) {
      this.time = time;
   }

   @Override
   public String getType() {
      return "scheduled_deletion";
   }

   @Override
   public long getExpireMinutes() {
      return this.time;
   }

   @Override
   public Tag serializeParams() {
      return LongTag.valueOf(this.time);
   }

   @Override
   public TransactionTypeStrategy deserializeParams(Tag tag) {
      return new ScheduledDeletionStrategy(((LongTag)tag).getAsLong());
   }
}
