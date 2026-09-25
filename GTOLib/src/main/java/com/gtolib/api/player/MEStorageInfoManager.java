package com.gtolib.api.player;

import appeng.api.networking.IGridNode;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyMap;
import appeng.api.stacks.AmountFormat;
import appeng.api.stacks.KeyCounter;
import appeng.hooks.ticking.TickHandler;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.menu.me.common.IClientRepo;
import com.gtolib.api.network.NetworkPack;
import com.lowdragmc.lowdraglib.LDLib;
import dev.emi.emi.screen.RecipeScreen;
import gto_ae.hooks.gui.menu.IRepoMenu;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.Collections;
import java.util.Set;
import lombok.Generated;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

public class MEStorageInfoManager {
   private static final NetworkPack C2SFetchAEKeyChannel = NetworkPack.registerC2S(
      "updateAEKeyC2S", (player, buf) -> IEnhancedPlayer.of(player).getPlayerData().getMeStorageInfoManager().handleC2SRequestAEKey(AEKey.readKey(buf))
   );
   private static final NetworkPack S2CUpdateAEKeyChannel = NetworkPack.registerS2C(
      "updateAEKeyS2C", (player, buf) -> IEnhancedPlayer.of(player).getPlayerData().getMeStorageInfoManager().handleS2CUpdateAEKey(buf)
   );
   private static final int serverPreservedRequestSeconds = 5;
   private static final int clientPreservedRequestSeconds = 2;
   private final Player player;
   private final Reference2IntOpenHashMap<AEKey> fetchedKeys = new Reference2IntOpenHashMap<>();
   private boolean reachable = true;
   private final Set<AEKey> clientKnownCraftables = new ReferenceOpenHashSet<>();
   private final AEKeyMap<AEKey> clientKnownAvailableAmounts = new AEKeyMap<>();
   MEStorageInfoManager.Wrapper wrapper;

   private void handleC2SRequestAEKey(@Nullable AEKey aeKey) {
      if (aeKey != null) {
         this.fetchedKeys.put(aeKey, 5);
      }
   }

   public MEStorageInfoManager(Player player) {
      this.player = player;
      if (LDLib.isRemote()) {
         this.wrapper = new MEStorageInfoManager.Wrapper();
      }
   }

   public void fetchAEKey(AEKey aeKey) {
      if (!this.fetchedKeys.containsKey(aeKey)) {
         this.fetchedKeys.put(aeKey, 2);
         C2SFetchAEKeyChannel.send(buf -> AEKey.writeKey(buf, aeKey));
      }
   }

   public void hookBroadcastChanges() {
      if (TickHandler.instance().getCurrentTick() % 10L == 0L) {
         IGridNode node = IEnhancedPlayer.of(this.player).getActionableNode();
         if (node != null && this.player instanceof ServerPlayer serverPlayer && !this.fetchedKeys.isEmpty()) {
            IStorageService storage = IEnhancedPlayer.getMEStorageService(serverPlayer);
            KeyCounter availableStacks = storage == null ? new KeyCounter() : storage.getCachedInventory();
            Set<AEKey> craftables = node.getGrid().getCraftingService().getCraftables(this.fetchedKeys.keySet()::contains);
            this.reachable = true;
            this.writeKeysToClient(this.fetchedKeys.keySet(), availableStacks, craftables);
         } else {
            this.reachable = false;
            this.writeKeysToClient(Collections.emptySet(), new KeyCounter(), Collections.emptySet());
         }
      }
   }

   void updateSeconds() {
      this.fetchedKeys.reference2IntEntrySet().removeIf(entry -> {
         int seconds = entry.getIntValue() - 1;
         if (seconds <= 0) {
            return true;
         }

         entry.setValue(seconds);
         return false;
      });
   }

   private void writeKeysToClient(Set<AEKey> keys, KeyCounter availableStacks, Set<AEKey> craftables) {
      S2CUpdateAEKeyChannel.send(buf -> {
         buf.writeBoolean(this.reachable);
         if (this.reachable) {
            buf.writeVarInt(keys.size());

            for (AEKey key : keys) {
               writeEntry(buf, key, availableStacks.get(key), craftables.contains(key));
            }
         }
      }, this.player);
   }

   private static void writeEntry(FriendlyByteBuf buffer, AEKey key, long availableAmount, boolean isCraftable) {
      AEKey.writeOptionalKey(buffer, key);
      buffer.writeVarLong(availableAmount);
      buffer.writeBoolean(isCraftable);
   }

   private void handleS2CUpdateAEKey(FriendlyByteBuf buf) {
      this.reachable = buf.readBoolean();
      if (this.reachable) {
         int size = buf.readVarInt();

         for (int i = 0; i < size; i++) {
            AEKey key = AEKey.readOptionalKey(buf);
            long availableAmount = buf.readVarLong();
            boolean isCraftable = buf.readBoolean();
            this.clientKnownAvailableAmounts.put(key, availableAmount);
            if (isCraftable) {
               this.clientKnownCraftables.add(key);
            }
         }
      }
   }

   @Generated
   public void setReachable(boolean reachable) {
      this.reachable = reachable;
   }

   @Generated
   public Set<AEKey> getClientKnownCraftables() {
      return this.clientKnownCraftables;
   }

   @Generated
   public AEKeyMap<AEKey> getClientKnownAvailableAmounts() {
      return this.clientKnownAvailableAmounts;
   }

   class Wrapper {
      @OnlyIn(Dist.CLIENT)
      MutableComponent getAEKeyStatusText(AEKey key, AmountFormat format) {
         IClientRepo repo = getRepo();
         boolean hasData;
         long availableAmount;
         boolean isCraftable;
         if (repo != null) {
            hasData = true;
            GridInventoryEntry e = repo.getByKey(key);
            if (e == null) {
               availableAmount = 0L;
               isCraftable = false;
            } else {
               availableAmount = repo.getByKey(key).getStoredAmount();
               isCraftable = repo.getByKey(key).isCraftable();
            }
         } else {
            MEStorageInfoManager.this.fetchAEKey(key);
            if (!MEStorageInfoManager.this.reachable) {
               return Component.translatable("block.gtceu.long_distance_item_pipeline_no_network");
            }

            hasData = MEStorageInfoManager.this.clientKnownAvailableAmounts.containsKey(key);
            availableAmount = MEStorageInfoManager.this.clientKnownAvailableAmounts.getOrDefault(key, 0L);
            isCraftable = MEStorageInfoManager.this.clientKnownCraftables.contains(key);
         }

         MutableComponent status = Component.translatable("gtocore.ae.appeng.me_storage_amount");
         status.append(": ");
         if (!hasData) {
            status.append(Component.translatable("gtocore.ae.appeng.fetching_items"));
            return status;
         }

         status.append(key.formatAmount(availableAmount, format));
         if (isCraftable) {
            status.append(" [").append(Component.translatable("gui.tooltips.ae2.Craftable")).append("]");
         }

         return status;
      }

      @OnlyIn(Dist.CLIENT)
      long getAEKeyAvailableAmount(AEKey key) {
         IClientRepo repo = getRepo();
         if (repo != null) {
            GridInventoryEntry e = repo.getByKey(key);
            return e == null ? 0L : e.getStoredAmount();
         } else {
            MEStorageInfoManager.this.fetchAEKey(key);
            return !MEStorageInfoManager.this.reachable ? 0L : MEStorageInfoManager.this.clientKnownAvailableAmounts.getOrDefault(key, 0L);
         }
      }

      @OnlyIn(Dist.CLIENT)
      @Nullable
      private static IClientRepo getRepo() {
         Screen screen = Minecraft.getInstance().screen;
         Screen old;
         if (screen instanceof RecipeScreen recipeScreen) {
            old = recipeScreen.old;
         } else {
            old = screen;
         }

         IClientRepo repo;
         if (old instanceof AbstractContainerScreen<?> scr && scr.getMenu() instanceof IRepoMenu me) {
            repo = me.getClientRepo();
         } else {
            repo = null;
         }

         return repo;
      }

      @OnlyIn(Dist.CLIENT)
      boolean isReachable() {
         return MEStorageInfoManager.this.reachable || getRepo() != null;
      }
   }
}
