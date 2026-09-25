package com.gtolib.api.ae2;

import appbot.ae2.ManaStorageImportStrategy;
import appeng.api.behaviors.StackImportStrategy;
import appeng.api.behaviors.StackTransferContext;
import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyMap;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import appeng.me.storage.ExternalStorageFacade;
import appeng.parts.automation.HandlerStrategy;
import appeng.parts.automation.StackImportFacade;
import appeng.util.BlockApiCache;
import gripe._90.arseng.me.strategy.SourceStorageImportStrategy;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

public class StorageImportCacheStrategy<C, S> implements StackImportStrategy {
   private final BlockApiCache<C> apiCache;
   private final Direction fromSide;
   private final HandlerStrategy<C, S> conversion;
   private final AEKeyMap<AEKey> cache = new AEKeyMap<>();

   private StorageImportCacheStrategy(Capability<C> capability, HandlerStrategy<C, S> conversion, ServerLevel level, BlockPos fromPos, Direction fromSide) {
      this.apiCache = BlockApiCache.create(capability, level, fromPos);
      this.fromSide = fromSide;
      this.conversion = conversion;
   }

   @Override
   public boolean transfer(StackTransferContext context) {
      if (!context.isKeyTypeEnabled(this.conversion.getKeyType())) {
         return false;
      }

      C adjacentHandler = this.apiCache.find(this.fromSide);
      if (adjacentHandler == null) {
         return false;
      }

      ExternalStorageFacade adjacentStorage = this.conversion.getFacade(adjacentHandler);

      for (int i = 0; i < adjacentStorage.getSlots(); i++) {
         GenericStack resource = adjacentStorage.getStackInSlot(i);
         if (resource != null && context.isInFilter(resource.what()) != context.isInverted()) {
            this.cache.insert(resource.what(), resource.amount());
         }
      }

      if (this.cache.isEmpty()) {
         return false;
      }

      MEStorage inv = context.getInternalStorage().getInventory();
      this.cache.fastForEach((what, amount) -> {
         long var6x = adjacentStorage.extract(what, amount, Actionable.SIMULATE, context.getActionSource());
         if (var6x > 0L) {
            amount = inv.insert(what, var6x, Actionable.MODULATE, context.getActionSource());
            if (amount > 0L) {
               adjacentStorage.extract(what, amount, Actionable.MODULATE, context.getActionSource());
               context.reduceOperationsRemaining(1L);
            }
         }
      });
      this.cache.clear();
      return false;
   }

   public static StackImportFacade createImportFacade(ServerLevel level, BlockPos fromPos, Direction fromSide) {
      return new StackImportFacade(
         List.of(
            new StorageImportCacheStrategy<>(ForgeCapabilities.ITEM_HANDLER, HandlerStrategy.ITEMS, level, fromPos, fromSide),
            new StorageImportCacheStrategy<>(ForgeCapabilities.FLUID_HANDLER, HandlerStrategy.FLUIDS, level, fromPos, fromSide),
            new ManaStorageImportStrategy(level, fromPos, fromSide),
            new SourceStorageImportStrategy(level, fromPos, fromSide)
         )
      );
   }
}
