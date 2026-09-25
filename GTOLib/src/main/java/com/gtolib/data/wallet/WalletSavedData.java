package com.gtolib.data.wallet;

import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

public class WalletSavedData extends SavedData {
   private static final String NBT_ALL_WALLETS = "all_wallets";
   private final O2OOpenCacheHashMap<UUID, PlayerWallet> allWallets;
   private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
   private final ReadLock readLock = this.rwLock.readLock();
   private final WriteLock writeLock = this.rwLock.writeLock();
   private final PlayerWallet.WalletChangeListener saveCallback = wallet -> {
      this.writeLock.lock();

      try {
         this.setDirty();
      } finally {
         this.writeLock.unlock();
      }
   };

   private WalletSavedData() {
      this.allWallets = new O2OOpenCacheHashMap<>();
   }

   public static WalletSavedData fromNBT(CompoundTag tag) {
      if (tag == null) {
         throw new IllegalArgumentException("NBT tags cannot be empty.");
      }

      WalletSavedData data = new WalletSavedData();
      ListTag walletList = tag.getList("all_wallets", 10);

      for (int i = 0; i < walletList.size(); i++) {
         CompoundTag walletTag = walletList.getCompound(i);
         PlayerWallet wallet = new PlayerWallet(walletTag, data.saveCallback);
         data.allWallets.put(wallet.getPlayerUUID(), wallet);
      }

      return data;
   }

   @NotNull
   @Override
   public CompoundTag save(@NotNull CompoundTag tag) {
      ListTag walletList = new ListTag();
      this.readLock.lock();

      try {
         this.allWallets.values().forEach(wallet -> walletList.add(wallet.toNBT()));
      } finally {
         this.readLock.unlock();
      }

      tag.put("all_wallets", walletList);
      return tag;
   }

   public PlayerWallet getOrCreateWallet(UUID playerUUID, String playerName) {
      if (playerUUID == null) {
         throw new IllegalArgumentException("Player UUID cannot be empty.");
      }

      if (playerName != null && !playerName.isEmpty()) {
         this.writeLock.lock();

         try {
            PlayerWallet wallet = this.allWallets.get(playerUUID);
            if (wallet == null) {
               wallet = new PlayerWallet(playerUUID, playerName, this.saveCallback);
               this.allWallets.put(playerUUID, wallet);
               this.setDirty();
            } else if (!wallet.getPlayerName().equals(playerName)) {
               wallet.updatePlayerName(playerName);
            }

            return wallet;
         } finally {
            this.writeLock.unlock();
         }
      } else {
         throw new IllegalArgumentException("Player names cannot be empty.");
      }
   }

   public PlayerWallet getWallet(UUID playerUUID) {
      if (playerUUID == null) {
         throw new IllegalArgumentException("Player UUID cannot be empty.");
      }

      this.readLock.lock();

      try {
         return this.allWallets.get(playerUUID);
      } finally {
         this.readLock.unlock();
      }
   }

   public Collection<PlayerWallet> getAllWallets() {
      this.readLock.lock();

      try {
         return Collections.unmodifiableCollection(this.allWallets.values());
      } finally {
         this.readLock.unlock();
      }
   }

   public static WalletSavedData get(ServerLevel level) {
      if (level == null) {
         throw new IllegalArgumentException("The server world cannot be empty.");
      } else {
         ServerLevel overworld = level.getServer().getLevel(Level.OVERWORLD);
         if (overworld == null) {
            throw new IllegalStateException("Unable to access the Overworld.");
         } else {
            return overworld.getDataStorage().computeIfAbsent(WalletSavedData::fromNBT, WalletSavedData::new, "gto_player_wallets");
         }
      }
   }
}
