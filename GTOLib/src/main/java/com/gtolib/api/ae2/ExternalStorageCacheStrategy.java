package com.gtolib.api.ae2;

import appbot.ae2.ManaExternalStorageStrategy;
import appbot.ae2.ManaKeyType;
import appeng.api.behaviors.ExternalStorageStrategy;
import appeng.api.stacks.AEKeyType;
import appeng.api.storage.MEStorage;
import appeng.me.storage.ExternalStorageFacade;
import appeng.parts.automation.HandlerStrategy;
import appeng.util.BlockApiCache;
import gripe._90.arseng.me.key.SourceKeyType;
import gripe._90.arseng.me.strategy.SourceExternalStorageStrategy;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

public class ExternalStorageCacheStrategy<C, S> implements ExternalStorageStrategy {
   private final BlockApiCache<C> apiCache;
   private final Direction fromSide;
   private final HandlerStrategy<C, S> conversion;

   private ExternalStorageCacheStrategy(Capability<C> capability, HandlerStrategy<C, S> conversion, ServerLevel level, BlockPos fromPos, Direction fromSide) {
      this.apiCache = BlockApiCache.create(capability, level, fromPos);
      this.fromSide = fromSide;
      this.conversion = conversion;
   }

   @Override
   public MEStorage createWrapper(boolean extractableOnly, Runnable injectOrExtractCallback) {
      BlockEntity be = this.apiCache.getBlockEntity();
      if (be == null) {
         return null;
      }

      C storage = this.apiCache.find(this.fromSide);
      if (storage == null) {
         return null;
      }

      ExternalStorageFacade result = this.conversion.getFacade(storage);
      result.setChangeListener(injectOrExtractCallback);
      result.setBlockEntity(be);
      result.setExtractableOnly(extractableOnly);
      return result;
   }

   public static Map<AEKeyType, ExternalStorageStrategy> createExternalStorageStrategies(ServerLevel level, BlockPos fromPos, Direction fromSide) {
      return new AEKeyTypeMap<>(
         new ExternalStorageCacheStrategy<>(ForgeCapabilities.ITEM_HANDLER, HandlerStrategy.ITEMS, level, fromPos, fromSide),
         new ExternalStorageCacheStrategy<>(ForgeCapabilities.FLUID_HANDLER, HandlerStrategy.FLUIDS, level, fromPos, fromSide)
      );
   }

   public static Map<AEKeyType, ExternalStorageStrategy> createWithManaExternalStorageStrategies(ServerLevel level, BlockPos fromPos, Direction fromSide) {
      return Map.of(
         AEKeyTypeMap.ITEM_TYPE,
         new ExternalStorageCacheStrategy<>(ForgeCapabilities.ITEM_HANDLER, HandlerStrategy.ITEMS, level, fromPos, fromSide),
         AEKeyTypeMap.FLUID_TYPE,
         new ExternalStorageCacheStrategy<>(ForgeCapabilities.FLUID_HANDLER, HandlerStrategy.FLUIDS, level, fromPos, fromSide),
         ManaKeyType.TYPE,
         new ManaExternalStorageStrategy(level, fromPos, fromSide),
         SourceKeyType.TYPE,
         new SourceExternalStorageStrategy(level, fromPos, fromSide)
      );
   }
}
