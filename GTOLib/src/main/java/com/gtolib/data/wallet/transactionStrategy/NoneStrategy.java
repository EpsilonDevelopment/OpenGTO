package com.gtolib.data.wallet.transactionStrategy;

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import net.minecraft.nbt.Tag;

public class NoneStrategy implements TransactionTypeStrategy {
   @Override
   public String getType() {
      return "none";
   }

   @Override
   public long getExpireMinutes() {
      return -1L;
   }

   @Override
   public void cleanExpired(Long2LongOpenHashMap minuteAmountMap, long currentMinute) {
   }

   @Override
   public Tag serializeParams() {
      return null;
   }

   @Override
   public TransactionTypeStrategy deserializeParams(Tag tag) {
      return new NoneStrategy();
   }
}
