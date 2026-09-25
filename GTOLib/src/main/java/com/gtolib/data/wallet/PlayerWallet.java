package com.gtolib.data.wallet;

import com.gto.fastcollection.fastutil.O2LOpenCacheHashMap;
import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import com.gto.fastcollection.fastutil.OpenCacheHashSet;
import com.gtolib.data.wallet.transactionStrategy.TransactionStrategyFactory;
import com.gtolib.data.wallet.transactionStrategy.TransactionTypeStrategy;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongMaps;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import lombok.Generated;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;

public class PlayerWallet {
   private final PlayerWallet.WalletChangeListener changeListener;
   private final UUID playerUUID;
   private String playerName;
   private final O2LOpenCacheHashMap<String> currencyMap;
   private final O2OOpenCacheHashMap<String, PlayerWallet.TransactionKeyData> txKeyDataMap;
   private final O2OOpenCacheHashMap<String, Set<String>> tagTable;

   private void triggerSave() {
      if (this.changeListener != null) {
         this.changeListener.onWalletChanged(this);
      }
   }

   public PlayerWallet(UUID playerUUID, String playerName, PlayerWallet.WalletChangeListener changeListener) {
      if (playerUUID == null) {
         throw new IllegalArgumentException("Player UUID cannot be null");
      }

      if (playerName != null && !playerName.isEmpty()) {
         this.playerUUID = playerUUID;
         this.playerName = playerName;
         this.changeListener = changeListener;
         this.currencyMap = new O2LOpenCacheHashMap<>();
         this.currencyMap.defaultReturnValue(0L);
         this.txKeyDataMap = new O2OOpenCacheHashMap<>();
         this.tagTable = new O2OOpenCacheHashMap<>();
      } else {
         throw new IllegalArgumentException("Player names cannot be null or empty.");
      }
   }

   public PlayerWallet(CompoundTag tag, PlayerWallet.WalletChangeListener changeListener) {
      if (tag == null) {
         throw new IllegalArgumentException("NBT tags cannot be null.");
      }

      this.playerUUID = tag.getUUID("player_uuid");
      this.playerName = tag.getString("player_name");
      this.changeListener = changeListener;
      this.currencyMap = new O2LOpenCacheHashMap<>();
      ListTag currencyList = tag.getList("currencies", 10);

      for (int i = 0; i < currencyList.size(); i++) {
         CompoundTag currTag = currencyList.getCompound(i);
         String currencyType = currTag.getString("type");
         long amount = currTag.getLong("amount");
         this.currencyMap.put(currencyType, amount);
      }

      this.currencyMap.defaultReturnValue(0L);
      this.txKeyDataMap = new O2OOpenCacheHashMap<>();
      ListTag txKeyList = tag.getList("tx_keys", 10);

      for (int i = 0; i < txKeyList.size(); i++) {
         CompoundTag txKeyTag = txKeyList.getCompound(i);
         String txKey = txKeyTag.getString("tx_key");
         PlayerWallet.TransactionKeyData keyData = PlayerWallet.TransactionKeyData.fromNBT(txKeyTag);
         if (!keyData.minuteAmountMap.isEmpty()) {
            this.txKeyDataMap.put(txKey, keyData);
         }
      }

      this.tagTable = new O2OOpenCacheHashMap<>();
      ListTag tagTableList = tag.getList("tag_table", 10);

      for (int i = 0; i < tagTableList.size(); i++) {
         CompoundTag tagTableTag = tagTableList.getCompound(i);
         String tagKey = tagTableTag.getString("tag_key");
         ListTag tagValueList = tagTableTag.getList("tag_values", 8);
         Set<String> tagValues = new OpenCacheHashSet<>();

         for (int j = 0; j < tagValueList.size(); j++) {
            tagValues.add(tagValueList.getString(j));
         }

         this.tagTable.put(tagKey, tagValues);
      }
   }

   public CompoundTag toNBT() {
      CompoundTag tag = new CompoundTag();
      tag.putUUID("player_uuid", this.playerUUID);
      tag.putString("player_name", this.playerName);
      ListTag currencyList = new ListTag();
      this.currencyMap.object2LongEntrySet().fastForEach(entry -> {
         CompoundTag currTag = new CompoundTag();
         currTag.putString("type", entry.getKey());
         currTag.putLong("amount", entry.getLongValue());
         currencyList.add(currTag);
      });
      tag.put("currencies", currencyList);
      ListTag txKeyList = new ListTag();
      this.txKeyDataMap.forEach((txKey, keyData) -> {
         if (!keyData.minuteAmountMap.isEmpty()) {
            CompoundTag txKeyTag = keyData.toNBT();
            txKeyTag.putString("tx_key", txKey);
            txKeyList.add(txKeyTag);
         }
      });
      tag.put("tx_keys", txKeyList);
      ListTag tagTableList = new ListTag();
      this.tagTable.forEach((tagKey, tagValues) -> {
         CompoundTag tagTableTag = new CompoundTag();
         tagTableTag.putString("tag_key", tagKey);
         ListTag tagValueList = new ListTag();
         tagValues.forEach(tagValue -> tagValueList.add(StringTag.valueOf(tagValue)));
         tagTableTag.put("tag_values", tagValueList);
         tagTableList.add(tagTableTag);
      });
      tag.put("tag_table", tagTableList);
      return tag;
   }

   public long adjustCurrency(String currencyType, long amount) {
      if (currencyType != null && !currencyType.isEmpty()) {
         long newAmount = this.currencyMap.getLong(currencyType) + amount;
         this.currencyMap.put(currencyType, newAmount);
         this.triggerSave();
         return newAmount;
      } else {
         throw new IllegalArgumentException("Currency type cannot be null/empty");
      }
   }

   public long getCurrencyAmount(String currencyType) {
      return currencyType == null ? 0L : this.currencyMap.getLong(currencyType);
   }

   public void setCurrencies(O2LOpenCacheHashMap<String> currencies) {
      if (currencies != null) {
         currencies.object2LongEntrySet().forEach(entry -> {
            String type = entry.getKey();
            if (type != null && !type.isEmpty()) {
               this.currencyMap.put(type, entry.getLongValue());
            }
         });
         this.triggerSave();
      }
   }

   public void checkExpiredCallbacks(Level world) {
      long currentMinute = getGameMinuteKey(world.getGameTime());
      boolean dataChanged = false;
      ObjectIterator<PlayerWallet.TransactionKeyData> iterator = this.txKeyDataMap.values().iterator();

      while (iterator.hasNext()) {
         PlayerWallet.TransactionKeyData keyData = iterator.next();
         if (keyData.checkAndExecuteCallback(currentMinute, this)) {
            dataChanged = true;
         }

         if (keyData.minuteAmountMap.isEmpty()) {
            iterator.remove();
            dataChanged = true;
         }
      }

      if (dataChanged) {
         this.triggerSave();
      }
   }

   public static long getGameMinuteKey(long gameTick) {
      return gameTick / 1200L;
   }

   public long addTransaction(String txKey, TransactionTypeStrategy strategy, long amount, Level world) {
      if (txKey == null || txKey.isEmpty()) {
         throw new IllegalArgumentException("Transaction primary keys cannot be null or empty.");
      }

      if (strategy == null) {
         throw new IllegalArgumentException("Strategy instances cannot be null");
      }

      if (world == null) {
         throw new IllegalArgumentException("World instances cannot be null");
      }

      this.checkExpiredCallbacks(world);
      long currentMinute = getGameMinuteKey(world.getGameTime());
      PlayerWallet.TransactionKeyData keyData = this.txKeyDataMap.computeIfAbsent(txKey, k -> new PlayerWallet.TransactionKeyData(strategy));
      long currentAmount = keyData.minuteAmountMap.get(currentMinute);
      currentAmount += amount;
      keyData.minuteAmountMap.put(currentMinute, currentAmount);
      this.triggerSave();
      return currentAmount;
   }

   public String getTransactionKeyType(String txKey) {
      if (txKey == null) {
         return null;
      }

      PlayerWallet.TransactionKeyData keyData = this.txKeyDataMap.get(txKey);
      return keyData != null ? keyData.strategy.getType() : null;
   }

   public long getTransactionMinuteAmount(String txKey, Object minuteKeyObj, Level world) {
      if (txKey != null && minuteKeyObj != null && world != null) {
         this.checkExpiredCallbacks(world);

         long minuteKey;
         try {
            minuteKey = minuteKeyObj instanceof String ? Long.parseLong((String)minuteKeyObj) : (Long)minuteKeyObj;
         } catch (ClassCastException | NumberFormatException e) {
            return 0L;
         }

         PlayerWallet.TransactionKeyData keyData = this.txKeyDataMap.get(txKey);
         return keyData != null ? keyData.minuteAmountMap.get(minuteKey) : 0L;
      } else {
         return 0L;
      }
   }

   public long getTransactionTotalAmount(String txKey, Level world) {
      if (txKey != null && world != null) {
         this.checkExpiredCallbacks(world);
         PlayerWallet.TransactionKeyData keyData = this.txKeyDataMap.get(txKey);
         if (keyData == null) {
            return 0L;
         }

         long total = 0L;

         for (long amount : keyData.minuteAmountMap.values()) {
            total += amount;
         }

         return total;
      } else {
         return 0L;
      }
   }

   public Long2LongMap getTransactionMinuteMap(String txKey, Level world) {
      if (txKey == null) {
         return Long2LongMaps.EMPTY_MAP;
      }

      if (world != null) {
         this.checkExpiredCallbacks(world);
      }

      PlayerWallet.TransactionKeyData keyData = this.txKeyDataMap.get(txKey);
      return keyData != null ? Long2LongMaps.unmodifiable(keyData.minuteAmountMap) : Long2LongMaps.EMPTY_MAP;
   }

   public void updatePlayerName(String newName) {
      if (newName != null && !newName.isEmpty()) {
         this.playerName = newName;
         this.triggerSave();
      } else {
         throw new IllegalArgumentException("The new name cannot be null or empty.");
      }
   }

   public Object2LongMap<String> getCurrencyMap() {
      return Object2LongMaps.unmodifiable(this.currencyMap);
   }

   public Set<String> getTransactionKeys() {
      return Collections.unmodifiableSet(this.txKeyDataMap.keySet());
   }

   public void addTag(String tagKey, String tagValue) {
      Set<String> tags = this.tagTable.computeIfAbsent(tagKey, k -> new OpenCacheHashSet<>());
      tags.add(tagValue);
      this.triggerSave();
   }

   public void removeTag(String tagKey, String tagValue) {
      Set<String> tags = this.tagTable.get(tagKey);
      if (tags != null) {
         tags.remove(tagValue);
         if (tags.isEmpty()) {
            this.tagTable.remove(tagKey);
         }

         this.triggerSave();
      }
   }

   public boolean containsTagValue(String tagKey, String tagValue) {
      Set<String> tags = this.tagTable.get(tagKey);
      return tags != null && tags.contains(tagValue);
   }

   public Set<String> getAllTagKeys() {
      return Collections.unmodifiableSet(this.tagTable.keySet());
   }

   public Set<String> getTags(String tagKey) {
      return this.tagTable.getOrDefault(tagKey, Collections.emptySet());
   }

   @Generated
   public UUID getPlayerUUID() {
      return this.playerUUID;
   }

   @Generated
   public String getPlayerName() {
      return this.playerName;
   }

   private static class TransactionKeyData {
      final TransactionTypeStrategy strategy;
      Long2LongOpenHashMap minuteAmountMap;

      TransactionKeyData(TransactionTypeStrategy strategy) {
         this.strategy = strategy;
         this.minuteAmountMap = new Long2LongOpenHashMap();
         this.minuteAmountMap.defaultReturnValue(0L);
      }

      TransactionKeyData(TransactionTypeStrategy strategy, Long2LongOpenHashMap minuteAmountMap) {
         this.strategy = strategy;
         this.minuteAmountMap = minuteAmountMap;
         this.minuteAmountMap.defaultReturnValue(0L);
      }

      CompoundTag toNBT() {
         CompoundTag tag = new CompoundTag();
         tag.putString("tx_type", this.strategy.getType());
         tag.put("strategy_params", this.strategy.serializeParams());
         ListTag minuteList = new ListTag();
         this.minuteAmountMap.long2LongEntrySet().fastForEach(entry -> {
            CompoundTag minuteTag = new CompoundTag();
            minuteTag.putString("tx_minute_key", String.valueOf(entry.getLongKey()));
            minuteTag.putLong("tx_amount", entry.getLongValue());
            minuteList.add(minuteTag);
         });
         tag.put("tx_minute_group", minuteList);
         return tag;
      }

      static PlayerWallet.TransactionKeyData fromNBT(CompoundTag tag) {
         String type = tag.getString("tx_type");
         Tag paramsTag = tag.get("strategy_params");
         TransactionTypeStrategy strategy = TransactionStrategyFactory.createStrategy(type, paramsTag);
         Long2LongOpenHashMap minuteAmountMap = new Long2LongOpenHashMap();
         minuteAmountMap.defaultReturnValue(0L);
         ListTag minuteList = tag.getList("tx_minute_group", 10);

         for (int i = 0; i < minuteList.size(); i++) {
            CompoundTag minuteTag = minuteList.getCompound(i);
            long minuteKey = Long.parseLong(minuteTag.getString("tx_minute_key"));
            long amount = minuteTag.getLong("tx_amount");
            minuteAmountMap.put(minuteKey, amount);
         }

         return new PlayerWallet.TransactionKeyData(strategy, minuteAmountMap);
      }

      public boolean checkAndExecuteCallback(long currentMinute, PlayerWallet wallet) {
         boolean isChanged = false;
         int sizeBeforeClean = this.minuteAmountMap.size();
         this.strategy.cleanExpired(this.minuteAmountMap, currentMinute);
         if (this.minuteAmountMap.size() != sizeBeforeClean) {
            isChanged = true;
         }

         if (!this.strategy.needCallback()) {
            return isChanged;
         }

         ObjectIterator<Entry> it = this.minuteAmountMap.long2LongEntrySet().fastIterator();

         while (it.hasNext()) {
            long minuteKey = it.next().getLongKey();
            if (currentMinute - minuteKey > this.strategy.getExpireMinutes()) {
               this.strategy.execute(wallet);
               it.remove();
               isChanged = true;
            }
         }

         return isChanged;
      }
   }

   public interface WalletChangeListener {
      void onWalletChanged(PlayerWallet var1);
   }
}
