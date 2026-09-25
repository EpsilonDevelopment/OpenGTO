package com.gtolib.ae2.crafting.reborn;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.crafting.ICraftingSimulationRequester;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import com.gto.fastcollection.fastutil.O2LOpenCacheHashMap;
import com.gtolib.ae2.crafting.models.DependencyGraph;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import net.minecraft.world.entity.player.Player;

public final class Context {
   public final ICraftingSimulationRequester requester;
   public final CalculationStrategy strategy;
   private final IGrid grid;
   public final SelectStrategy selectStrategy;
   public KeyCounter networkStorage;
   public boolean isComplete = false;
   public DependencyGraph graph;
   public ArrayList<AEKey> sortedItems;
   public CycleDetectedResult cycleDetectedResult = new CycleDetectedResult(this, new ArrayList<>());
   public Instant startTime;
   public long bytes = 0L;
   public GenericStack target;
   public boolean isSimulate;
   public KeyCounter usedItems = new KeyCounter();
   public KeyCounter emittedItems = new KeyCounter();
   public KeyCounter missingItems = new KeyCounter();
   public KeyCounter requireItems = new KeyCounter();
   public Object2LongOpenHashMap<IPatternDetails> patternTimes = new O2LOpenCacheHashMap<>();
   public ICraftingPlan plan;

   public static Context create(
      ICraftingSimulationRequester requester, CalculationStrategy strategy, IGrid grid, GenericStack target, SelectStrategy selectStrategy, boolean isSimulate
   ) {
      Context context = new Context(requester, strategy, grid, target, selectStrategy, isSimulate);
      selectStrategy.setContext(context);
      IStorageService storage = grid.getStorageService();
      context.networkStorage = new KeyCounter();
      context.networkStorage.addAll(storage.getCachedInventory());
      return context;
   }

   public Context shallowCopy() {
      Context context = new Context(this.requester, this.strategy, this.grid, this.target, this.selectStrategy, this.isSimulate);
      context.graph = this.graph;
      context.sortedItems = this.sortedItems;
      context.cycleDetectedResult = this.cycleDetectedResult;
      context.isComplete = this.isComplete;
      context.startTime = this.startTime;
      context.networkStorage = this.networkStorage;
      return context;
   }

   public void copyDataFrom(Context other) {
      this.bytes = other.bytes;
      this.target = other.target;
      this.isSimulate = other.isSimulate;
      this.usedItems = other.usedItems;
      this.emittedItems = other.emittedItems;
      this.missingItems = other.missingItems;
      this.requireItems = other.requireItems;
      this.patternTimes = other.patternTimes;
      this.plan = other.plan;
   }

   private Context(
      ICraftingSimulationRequester requester, CalculationStrategy strategy, IGrid grid, GenericStack target, SelectStrategy selectStrategy, boolean isSimulate
   ) {
      this.requester = requester;
      this.strategy = strategy;
      this.grid = grid;
      this.target = target;
      this.selectStrategy = selectStrategy;
      this.isSimulate = isSimulate;
   }

   public Optional<IGrid> getGrid() {
      return this.requester.getGridNode() != null && this.requester.getGridNode().isActive()
         ? Optional.of(this.requester.getGridNode().getGrid())
         : Optional.empty();
   }

   public Optional<ICraftingService> getCraftingService() {
      return this.getGrid().isPresent() && this.getGrid().get().getCraftingService() != null
         ? Optional.of(this.getGrid().get().getCraftingService())
         : Optional.empty();
   }

   public long getUnOccupiedStorage(AEKey item) {
      return this.networkStorage.get(item) - this.usedItems.get(item);
   }

   public Optional<Player> getPlayer() {
      return this.requester.getActionSource() != null ? this.requester.getActionSource().player() : Optional.empty();
   }
}
