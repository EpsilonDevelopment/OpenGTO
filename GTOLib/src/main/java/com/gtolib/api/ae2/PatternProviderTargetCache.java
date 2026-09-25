package com.gtolib.api.ae2;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.capabilities.Capabilities;
import appeng.helpers.patternprovider.PatternProviderTarget;
import appeng.me.storage.CompositeStorage;
import appeng.me.storage.ExternalStorageFacade;
import com.gtolib.api.blockentity.IDirectionCacheBlockEntity;
import com.gtolib.utils.BlockCapabilityCache;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public final class PatternProviderTargetCache {
   private final BlockEntity blockEntity;
   private final IPatternProviderLogic logic;
   private final BlockCapabilityCache<MEStorage> cache;
   private final BlockPos pos;
   private final Direction side;
   private final Direction oppositeSide;
   private final IActionSource src;
   private static final WeakHashMap<MEStorage, WeakHashMap<IPatternProviderLogic, PatternProviderTargetCache.CacheLocation>> patternGetCache = new WeakHashMap<>();

   public PatternProviderTargetCache(BlockEntity blockEntity, IPatternProviderLogic logic, Direction side, IActionSource src) {
      this.blockEntity = blockEntity;
      this.logic = logic;
      this.cache = BlockCapabilityCache.create(Capabilities.STORAGE, blockEntity);
      this.pos = blockEntity.getBlockPos().relative(side);
      this.side = side;
      this.oppositeSide = side.getOpposite();
      this.src = src;
   }

   public static PatternProviderTarget find(@Nullable BlockEntity blockEntity, IPatternProviderLogic logic, Direction oppositeSide, IActionSource src, long pos) {
      if (blockEntity == null) {
         return null;
      }

      ExternalStorageFacade fluid = null;
      ExternalStorageFacade item = null;
      IFluidHandler fcap = blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, oppositeSide).orElse(null);
      if (fcap != null) {
         fluid = ExternalStorageFacade.of(fcap);
      }

      IItemHandler icap = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, oppositeSide).orElse(null);
      if (icap != null) {
         item = ExternalStorageFacade.of(icap);
      }

      if (fluid == null && item == null) {
         return null;
      } else if (fluid == null) {
         return new PatternProviderTargetCache.WrapMeStorage(item, src, logic, pos, oppositeSide.getOpposite().get3DDataValue());
      } else {
         return item == null
            ? new PatternProviderTargetCache.WrapMeStorage(fluid, src, logic, pos, oppositeSide.getOpposite().get3DDataValue())
            : new PatternProviderTargetCache.WrapMeStorage(
               new CompositeStorage(new AEKeyTypeMap<>(item, fluid)), src, logic, pos, oppositeSide.getOpposite().get3DDataValue()
            );
      }
   }

   @Nullable
   public PatternProviderTarget find(int dir) {
      MEStorage meStorage = this.cache.find(this.pos, this.side, this.oppositeSide);
      return meStorage != null
         ? new PatternProviderTargetCache.WrapMeStorage(meStorage, this.src, this.logic, 0L, dir)
         : find(
            IDirectionCacheBlockEntity.getBlockEntityDirectionCache(this.blockEntity)
               .getAdjacentBlockEntity(this.blockEntity.getLevel(), this.blockEntity.getBlockPos(), this.side),
            this.logic,
            this.oppositeSide,
            this.src,
            0L
         );
   }

   private record CacheLocation(long pos, int dir) {
   }

   public record WrapMeStorage(MEStorage storage, IActionSource src, IPatternProviderLogic logic, long pos, int dir) implements PatternProviderTarget {
      @Override
      public long insert(AEKey what, long amount, Actionable type) {
         return this.storage.insert(what, amount, type, this.src);
      }

      @Override
      public boolean containsPatternInput(Set<AEKey> patternInputs) {
         switch (this.logic.gtolib$getBlocking()) {
            case ALL:
               return this.isNonEmpty(null);
            case NON_CONTAIN:
               WeakHashMap<IPatternProviderLogic, PatternProviderTargetCache.CacheLocation> logicMap = PatternProviderTargetCache.patternGetCache
                  .computeIfAbsent(this.storage, s -> new WeakHashMap<>());
               logicMap.put(this.logic, new PatternProviderTargetCache.CacheLocation(this.pos, this.dir));
               IPatternDetails current = this.logic.gtolib$getCurrentPattern();
               if (current == null) {
                  return false;
               } else {
                  boolean hasCachedPattern = false;

                  for (Entry<IPatternProviderLogic, PatternProviderTargetCache.CacheLocation> entry : logicMap.entrySet()) {
                     IPatternProviderLogic l = entry.getKey();
                     PatternProviderTargetCache.CacheLocation loc = entry.getValue();
                     IPatternDetails cachePat = l.gtolib$getCachePattern(loc.pos, loc.dir);
                     if (cachePat != null) {
                        hasCachedPattern = true;
                        if (cachePat.equals(current)) {
                           return false;
                        }
                     }
                  }

                  return hasCachedPattern && this.isNonEmpty(patternInputs);
               }
            case PARALLEL:
            case CONTAIN:
               return this.isNonEmpty(patternInputs);
            case NONE:
               return false;
            default:
               return false;
         }
      }

      private boolean isNonEmpty(@Nullable Set<AEKey> patternInputs) {
         KeyCounter counter = this.storage.getAvailableStacks();
         counter.removeZeros();
         if (counter.isEmpty()) {
            return false;
         }

         if (patternInputs == null) {
            return true;
         }

         for (it.unimi.dsi.fastutil.objects.Reference2LongMap.Entry<AEKey> stack : counter) {
            if (patternInputs.contains(stack.getKey().dropSecondary())) {
               return true;
            }
         }

         return false;
      }
   }
}
