package com.gtolib.api.ae2;

import appbot.ae2.ManaStorageExportStrategy;
import appeng.api.behaviors.StackExportStrategy;
import appeng.api.behaviors.StackTransferContext;
import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import appeng.parts.automation.HandlerStrategy;
import appeng.parts.automation.StackExportFacade;
import appeng.util.BlockApiCache;
import com.gtolib.utils.MathUtil;
import gripe._90.arseng.me.strategy.SourceStorageExportStrategy;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

public class StorageExportCacheStrategy<C, S> implements StackExportStrategy {
   private final BlockApiCache<C> apiCache;
   private final Direction fromSide;
   private final HandlerStrategy<C, S> conversion;

   private StorageExportCacheStrategy(Capability<C> capability, HandlerStrategy<C, S> conversion, ServerLevel level, BlockPos fromPos, Direction fromSide) {
      this.apiCache = BlockApiCache.create(capability, level, fromPos);
      this.fromSide = fromSide;
      this.conversion = conversion;
   }

   @Override
   public long transfer(StackTransferContext context, AEKey what, long amount) {
      if (amount >= 1L && this.conversion.isSupported(what)) {
         C adjacentHandler = this.apiCache.find(this.fromSide);
         if (adjacentHandler == null) {
            return 0L;
         }

         MEStorage inv = context.getInternalStorage().getInventory();
         long var7 = inv.extract(what, amount, Actionable.SIMULATE, context.getActionSource());
         if (var7 > 0L) {
            amount = this.conversion.insert(adjacentHandler, what, MathUtil.saturatedCast(var7), Actionable.SIMULATE);
            if (amount > 0L) {
               long var9 = inv.extract(what, amount, Actionable.MODULATE, context.getActionSource());
               if (var9 > 0L) {
                  return this.conversion.insert(adjacentHandler, what, (int)var9, Actionable.MODULATE);
               }
            }
         }

         return 0L;
      } else {
         return 0L;
      }
   }

   @Override
   public long push(AEKey what, long amount, Actionable mode) {
      if (!this.conversion.isSupported(what)) {
         return 0L;
      }

      C adjacentStorage = this.apiCache.find(this.fromSide);
      return adjacentStorage == null ? 0L : this.conversion.insert(adjacentStorage, what, MathUtil.saturatedCast(amount), mode);
   }

   public static StackExportFacade createExportFacade(ServerLevel level, BlockPos fromPos, Direction fromSidee) {
      return new StackExportFacade(
         List.of(
            new StorageExportCacheStrategy<>(ForgeCapabilities.ITEM_HANDLER, HandlerStrategy.ITEMS, level, fromPos, fromSidee),
            new StorageExportCacheStrategy<>(ForgeCapabilities.FLUID_HANDLER, HandlerStrategy.FLUIDS, level, fromPos, fromSidee),
            new ManaStorageExportStrategy(level, fromPos, fromSidee),
            new SourceStorageExportStrategy(level, fromPos, fromSidee)
         )
      );
   }
}
