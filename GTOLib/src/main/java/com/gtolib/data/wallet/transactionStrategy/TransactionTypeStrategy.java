package com.gtolib.data.wallet.transactionStrategy;

import com.gtolib.data.wallet.PlayerWallet;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.nbt.Tag;

public interface TransactionTypeStrategy {
   String getType();

   long getExpireMinutes();

   default void cleanExpired(Long2LongOpenHashMap minuteAmountMap, long currentMinute) {
      long expireMinutes = this.getExpireMinutes();
      if (expireMinutes > 0L) {
         ObjectIterator<Entry> fastIterator = minuteAmountMap.long2LongEntrySet().fastIterator();

         while (fastIterator.hasNext()) {
            Entry entry = fastIterator.next();
            long timeDiff = currentMinute - entry.getLongKey();
            if (timeDiff > expireMinutes) {
               fastIterator.remove();
            }
         }
      }
   }

   default boolean needCallback() {
      return false;
   }

   default void execute(PlayerWallet wallet) {
   }

   Tag serializeParams();

   TransactionTypeStrategy deserializeParams(Tag var1);
}
