package com.gtolib.utils;

import com.gto.fastcollection.fastutil.O2LOpenCacheHashMap;
import com.gtolib.GTOCore;
import com.gtolib.data.wallet.PlayerWallet;
import com.gtolib.data.wallet.WalletSavedData;
import com.gtolib.data.wallet.transactionStrategy.LoanCollectionStrategy;
import com.gtolib.data.wallet.transactionStrategy.NoneStrategy;
import com.gtolib.data.wallet.transactionStrategy.ScheduledDeletionStrategy;
import com.gtolib.data.wallet.transactionStrategy.TimingCompressionStrategy;
import com.gtolib.data.wallet.transactionStrategy.TransactionTypeStrategy;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class WalletUtils {
   private WalletUtils() {
   }

   private static WalletSavedData getWalletData(Level world) {
      ServerLevel serverLevel = world instanceof ServerLevel ? (ServerLevel)world : null;
      if (serverLevel == null) {
         GTOCore.LOGGER.error("获取钱包数据失败：服务器世界实例为null");
         return null;
      }

      try {
         return WalletSavedData.get(serverLevel);
      } catch (Exception e) {
         GTOCore.LOGGER.error("获取WalletSavedData实例异常", e);
         return null;
      }
   }

   private static PlayerWallet getWalletInternal(UUID playerUUID, Level world) {
      if (playerUUID != null && world != null) {
         WalletSavedData data = getWalletData(world);
         return data != null ? data.getWallet(playerUUID) : null;
      } else {
         return null;
      }
   }

   private static PlayerWallet getOrCreateWalletInternal(UUID playerUUID, String playerName, Level world) {
      if (playerUUID != null && world != null) {
         String safeName = playerName != null && !playerName.isEmpty() ? playerName : "Unknown player";
         WalletSavedData data = getWalletData(world);
         if (data == null) {
            return null;
         }

         try {
            return data.getOrCreateWallet(playerUUID, safeName);
         } catch (Exception e) {
            GTOCore.LOGGER.error("获取/创建玩家[{}]钱包异常", playerUUID, e);
            return null;
         }
      } else {
         GTOCore.LOGGER.error("获取/创建钱包失败：玩家UUID={}, 世界实例={}", playerUUID, world);
         return null;
      }
   }

   public static boolean hasWallet(UUID playerUUID, Level world) {
      return playerUUID != null && world != null ? getWalletInternal(playerUUID, world) != null : false;
   }

   public static boolean createAndInitializeWallet(UUID playerUUID, Level world, String playerName) {
      if (playerUUID != null && world != null) {
         PlayerWallet wallet = getOrCreateWalletInternal(playerUUID, playerName, world);
         return wallet != null;
      } else {
         GTOCore.LOGGER.error("创建钱包失败：玩家UUID={}, 世界实例={}", playerUUID, world);
         return false;
      }
   }

   public static Object2ObjectMap<UUID, String> getAllWalletPlayers(Level world) {
      if (world == null) {
         return Object2ObjectMaps.emptyMap();
      }

      WalletSavedData data = getWalletData(world);
      if (data == null) {
         return Object2ObjectMaps.emptyMap();
      }

      Object2ObjectMap<UUID, String> playerMap = new Object2ObjectOpenHashMap<>();
      data.getAllWallets().forEach(wallet -> playerMap.put(wallet.getPlayerUUID(), wallet.getPlayerName()));
      return Object2ObjectMaps.unmodifiable(playerMap);
   }

   public static long addCurrency(UUID playerUUID, Level world, String currencyType, long amount) {
      if (amount > 0L && currencyType != null && !currencyType.isEmpty() && playerUUID != null && world != null) {
         PlayerWallet wallet = getWalletInternal(playerUUID, world);
         return wallet == null ? 0L : wallet.adjustCurrency(currencyType, amount);
      } else {
         return 0L;
      }
   }

   public static long subtractCurrency(UUID playerUUID, Level world, String currencyType, long amount) {
      if (amount > 0L && currencyType != null && !currencyType.isEmpty() && playerUUID != null && world != null) {
         PlayerWallet wallet = getWalletInternal(playerUUID, world);
         return wallet == null ? 0L : wallet.adjustCurrency(currencyType, -amount);
      } else {
         return 0L;
      }
   }

   public static void setCurrencies(UUID playerUUID, Level world, O2LOpenCacheHashMap<String> currencies) {
      if (playerUUID != null && world != null) {
         PlayerWallet wallet = getWalletInternal(playerUUID, world);
         if (wallet != null) {
            wallet.setCurrencies(currencies);
         }
      }
   }

   public static void updatePlayerName(UUID playerUUID, Level world, String newName) {
      if (playerUUID != null && world != null && newName != null && !newName.isEmpty()) {
         PlayerWallet wallet = getWalletInternal(playerUUID, world);
         if (wallet != null) {
            wallet.updatePlayerName(newName);
         }
      }
   }

   public static long getCurrencyAmount(UUID playerUUID, Level world, String currencyType) {
      PlayerWallet wallet = getWalletInternal(playerUUID, world);
      return wallet != null ? wallet.getCurrencyAmount(currencyType) : 0L;
   }

   public static Object2LongMap<String> getCurrencyMap(UUID playerUUID, Level world) {
      PlayerWallet wallet = getWalletInternal(playerUUID, world);
      return wallet != null ? wallet.getCurrencyMap() : Object2LongMaps.emptyMap();
   }

   public static long addTransaction(UUID playerUUID, Level world, String txKey, TransactionTypeStrategy strategy, long amount) {
      if (playerUUID != null && txKey != null && !txKey.isEmpty() && strategy != null && world != null && amount > 0L) {
         PlayerWallet wallet = getWalletInternal(playerUUID, world);
         if (wallet == null) {
            return 0L;
         }

         try {
            return wallet.addTransaction(txKey, strategy, amount, world);
         } catch (Exception e) {
            GTOCore.LOGGER.error("玩家[{}]添加交易[{}]异常", playerUUID, txKey, e);
            return 0L;
         }
      } else {
         return 0L;
      }
   }

   public static long addNoneStrategyTransaction(UUID playerUUID, Level world, String txKey, long amount) {
      return addTransaction(playerUUID, world, txKey, new NoneStrategy(), amount);
   }

   public static long addTimingCompressionStrategyTransaction(UUID playerUUID, Level world, String txKey, long time, long amount) {
      return addTransaction(playerUUID, world, txKey, new TimingCompressionStrategy(time), amount);
   }

   public static long addScheduledDeletion(UUID playerUUID, Level world, String txKey, long time, long amount) {
      return addTransaction(playerUUID, world, txKey, new ScheduledDeletionStrategy(time), amount);
   }

   public static long addLoanCollectionTransaction(UUID playerUUID, Level world, String txKey, long amount, long time, String deductCurrency, long deductAmount) {
      TransactionTypeStrategy strategy = new LoanCollectionStrategy(time, deductCurrency, deductAmount);
      return addTransaction(playerUUID, world, txKey, strategy, amount);
   }

   public static long getTransactionMinuteAmount(UUID playerUUID, Level world, String txKey, long minuteKey) {
      if (playerUUID != null && txKey != null && world != null) {
         PlayerWallet wallet = getWalletInternal(playerUUID, world);
         return wallet != null ? wallet.getTransactionMinuteAmount(txKey, minuteKey, world) : 0L;
      } else {
         return 0L;
      }
   }

   public static long getTransactionTotalAmount(UUID playerUUID, Level world, String txKey) {
      if (playerUUID != null && txKey != null && world != null) {
         PlayerWallet wallet = getWalletInternal(playerUUID, world);
         return wallet != null ? wallet.getTransactionTotalAmount(txKey, world) : 0L;
      } else {
         return 0L;
      }
   }

   public static Set<String> getTransactionKeys(UUID playerUUID, Level world) {
      if (playerUUID != null && world != null) {
         PlayerWallet wallet = getWalletInternal(playerUUID, world);
         return wallet != null ? wallet.getTransactionKeys() : Collections.emptySet();
      } else {
         return Collections.emptySet();
      }
   }

   public static String getTransactionType(UUID playerUUID, Level world, String txKey) {
      if (playerUUID != null && txKey != null && world != null) {
         PlayerWallet wallet = getWalletInternal(playerUUID, world);
         return wallet != null ? wallet.getTransactionKeyType(txKey) : null;
      } else {
         return null;
      }
   }

   public static Long2LongMap getTransactionMinuteMap(UUID playerUUID, Level world, String txKey) {
      if (playerUUID != null && txKey != null) {
         PlayerWallet wallet = getWalletInternal(playerUUID, world);
         return wallet != null ? wallet.getTransactionMinuteMap(txKey, world) : Long2LongMaps.EMPTY_MAP;
      } else {
         return Long2LongMaps.EMPTY_MAP;
      }
   }

   public static long getGameMinuteKey(Level world) {
      return PlayerWallet.getGameMinuteKey(world.getGameTime());
   }

   public static long getGameMinuteKey(Player player) {
      return PlayerWallet.getGameMinuteKey(player.getCommandSenderWorld().getGameTime());
   }

   public static void addTagToWallet(UUID playerUUID, Level world, String tagKey, String tagValue) {
      if (playerUUID != null && world != null && tagKey != null && tagValue != null) {
         PlayerWallet wallet = getWalletInternal(playerUUID, world);
         if (wallet != null) {
            wallet.addTag(tagKey, tagValue);
         }
      }
   }

   public static void removeTagFromWallet(UUID playerUUID, Level world, String tagKey, String tagValue) {
      if (playerUUID != null && world != null && tagKey != null && tagValue != null) {
         PlayerWallet wallet = getWalletInternal(playerUUID, world);
         if (wallet != null) {
            wallet.removeTag(tagKey, tagValue);
         }
      }
   }

   public static boolean containsTagValueInWallet(UUID playerUUID, Level world, String tagKey, String tagValue) {
      if (playerUUID != null && world != null && tagKey != null && tagValue != null) {
         PlayerWallet wallet = getWalletInternal(playerUUID, world);
         return wallet == null ? false : wallet.containsTagValue(tagKey, tagValue);
      } else {
         return false;
      }
   }

   public static Set<String> getAllTagKeysFromWallet(UUID playerUUID, Level world) {
      if (playerUUID != null && world != null) {
         PlayerWallet wallet = getWalletInternal(playerUUID, world);
         return wallet == null ? Collections.emptySet() : wallet.getAllTagKeys();
      } else {
         return Collections.emptySet();
      }
   }

   public static Set<String> getTagsFromWallet(UUID playerUUID, Level world, String tagKey) {
      if (playerUUID != null && world != null && tagKey != null) {
         PlayerWallet wallet = getWalletInternal(playerUUID, world);
         return wallet == null ? Collections.emptySet() : wallet.getTags(tagKey);
      } else {
         return Collections.emptySet();
      }
   }
}
