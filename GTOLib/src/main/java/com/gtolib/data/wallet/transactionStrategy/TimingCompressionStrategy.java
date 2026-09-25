package com.gtolib.data.wallet.transactionStrategy;

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;

public class TimingCompressionStrategy implements TransactionTypeStrategy {
   private final long time;

   public TimingCompressionStrategy(long time) {
      this.time = time;
   }

   @Override
   public String getType() {
      return "timing_compression";
   }

   @Override
   public long getExpireMinutes() {
      return -1L;
   }

   @Override
   public void cleanExpired(Long2LongOpenHashMap minuteAmountMap, long currentMinute) {
      if (minuteAmountMap != null && !minuteAmountMap.isEmpty()) {
         int originalSize = minuteAmountMap.size();
         int initialCapacity = Math.max(16, originalSize / 2);
         Long2LongOpenHashMap processedMap = new Long2LongOpenHashMap(initialCapacity);
         processedMap.defaultReturnValue(0L);
         ObjectIterator<Entry> fastIterator = minuteAmountMap.long2LongEntrySet().fastIterator();

         while (fastIterator.hasNext()) {
            Entry entry = fastIterator.next();
            long originalMinuteKey = entry.getLongKey();
            long amount = entry.getLongValue();
            long targetKey = originalMinuteKey / this.time * this.time;
            processedMap.addTo(targetKey, amount);
            fastIterator.remove();
         }

         minuteAmountMap.clear();
         minuteAmountMap.putAll(processedMap);
      }
   }

   @Override
   public Tag serializeParams() {
      return LongTag.valueOf(this.time);
   }

   @Override
   public TransactionTypeStrategy deserializeParams(Tag tag) {
      return new TimingCompressionStrategy(((LongTag)tag).getAsLong());
   }
}
