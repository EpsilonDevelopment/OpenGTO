package com.gtolib.api.machine.impl;

import com.gregtechceu.gtceu.api.cover.filter.FluidFilter;
import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.gregtechceu.gtceu.api.cover.filter.TagItemFilter;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.pattern.MultiblockState;
import com.gregtechceu.gtceu.api.pattern.MultiblockWorldData;
import com.gregtechceu.gtceu.api.recipe.info.ItemRecipeInfo;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.utils.TaskHandler;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.gtolib.api.fluid.IFluid;
import com.gtolib.api.item.IItem;
import com.gtolib.api.machine.feature.IDigitalMiner;
import com.gtolib.api.recipe.RecipeBuilder;
import com.gtolib.utils.GTOUtils;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.Generated;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class DigitalMinerLogic extends RecipeLogic {
   private static final ItemFilter ORE_FILTER;
   @SaveToDisk(defaultValue = "2147483647")
   protected int x = Integer.MAX_VALUE;
   @SaveToDisk(defaultValue = "2147483647")
   protected int y = Integer.MAX_VALUE;
   @SaveToDisk(defaultValue = "2147483647")
   protected int z = Integer.MAX_VALUE;
   @SaveToDisk(defaultValue = "2147483647")
   protected int startX = Integer.MAX_VALUE;
   @SaveToDisk(defaultValue = "2147483647")
   protected int startZ = Integer.MAX_VALUE;
   @SaveToDisk(defaultValue = "2147483647")
   protected int startY = Integer.MAX_VALUE;
   @SaveToDisk(defaultValue = "2147483647")
   protected int mineX = Integer.MAX_VALUE;
   @SaveToDisk(defaultValue = "2147483647")
   protected int mineZ = Integer.MAX_VALUE;
   @SaveToDisk(defaultValue = "2147483647")
   protected int mineY = Integer.MAX_VALUE;
   @SaveToDisk(defaultValue = "false")
   private boolean isDone;
   protected final IDigitalMiner miner;
   private final LinkedList<BlockPos> oresToMine = new LinkedList<>();
   private int minBuildHeight = Integer.MAX_VALUE;
   private boolean isInventoryFull;
   private int oreAmount;
   private IDigitalMiner.MinerConfig config;
   private final Map<BlockState, List<ItemStack>> lootCache = new Reference2ReferenceOpenHashMap<>();
   private volatile boolean isSearchingBlocks = false;

   public DigitalMinerLogic(@NotNull IRecipeLogicMachine machine) {
      super(machine);
      this.miner = (IDigitalMiner)machine;
      this.config = this.miner.getMinerConfig();
      this.isDone = false;
      this.interval = 0;
   }

   public int getMinY() {
      return (int)this.config.minerArea().minY;
   }

   public int getMaxY() {
      return (int)this.config.minerArea().maxY;
   }

   public int getMinX() {
      return (int)this.config.minerArea().minX;
   }

   public int getMaxX() {
      return (int)this.config.minerArea().maxX;
   }

   public int getMinZ() {
      return (int)this.config.minerArea().minZ;
   }

   public int getMaxZ() {
      return (int)this.config.minerArea().maxZ;
   }

   @Override
   public void resetRecipeLogic() {
      this.oresToMine.clear();
      this.oreAmount = 0;
      this.config = this.miner.getMinerConfig();
      super.resetRecipeLogic();
      this.resetArea(false);
      this.setWorkingEnabled(false);
   }

   protected boolean isSilkTouchMode() {
      return this.config.silkLevel() == 1;
   }

   private boolean checkCoordinatesInvalid() {
      return this.x == Integer.MAX_VALUE && this.y == Integer.MAX_VALUE && this.z == Integer.MAX_VALUE;
   }

   protected boolean checkCanMine() {
      if (this.config.parallelMining() == 0) {
         return false;
      }

      if (!this.isDone && this.checkCoordinatesInvalid()) {
         this.initPos();
      }

      return !this.isDone && this.miner.drainInput(true);
   }

   public void initPos() {
      this.x = this.getMinX();
      this.z = this.getMinZ();
      this.y = this.getMaxY();
      this.startX = this.getMinX();
      this.startZ = this.getMinZ();
      this.startY = this.getMaxY();
      this.mineX = this.getMinX();
      this.mineZ = this.getMinZ();
      this.mineY = this.getMaxY();
   }

   public void resetArea(boolean checkToMine) {
      this.initPos();
      if (this.isDone) {
         this.setWorkingEnabled(false);
      }

      this.isDone = false;
      if (checkToMine) {
         this.oresToMine.clear();
         this.checkBlocksToMine();
      }
   }

   private void onBlocksFound(List<BlockPos> foundBlocks) {
      synchronized (this.oresToMine) {
         this.oresToMine.clear();
         this.oresToMine.addAll(foundBlocks);
         this.oreAmount = this.oresToMine.size();
         this.isSearchingBlocks = false;
      }

      if (this.oresToMine.isEmpty()) {
         this.x = this.mineX;
         this.y = this.mineY;
         this.z = this.mineZ;
         this.isDone = true;
         this.setStatus(0);
         this.miner.setWorkingEnabled(false);
      }
   }

   private LinkedList<BlockPos> getBlocksToMine(ServerLevel serverLevel, MultiblockWorldData data) {
      LinkedList<BlockPos> blocks = new LinkedList<>();
      int calcAmount = Integer.MAX_VALUE;
      int calculated = 0;
      int x = this.startX;
      int y = this.startY;
      int z = this.startZ;
      int endX = this.getMaxX();
      int endZ = this.getMaxZ();
      int minHeight = this.getMinY();
      ItemFilter itemFilter = this.config.itemFilter() instanceof ItemFilter filter ? filter : ORE_FILTER;
      FluidFilter fluidFilter = this.config.fluidFilter() instanceof FluidFilter filter ? filter : null;

      while (calculated < calcAmount) {
         if (y < minHeight) {
            return blocks;
         }

         if (z < endZ) {
            if (x < endX) {
               BlockPos blockPos = new BlockPos(x, y, z);
               BlockState state = serverLevel.getBlockState(blockPos);
               if (!state.isAir() && state.getBlock().defaultDestroyTime() >= 0.0F && !this.isInMultiblock(blockPos, data)) {
                  if (state.getBlock() instanceof LiquidBlock liq && this.config.fluidMode() != IDigitalMiner.FluidMode.Ignore && itemFilter == null) {
                     if (fluidFilter == null || fluidFilter.test(((IFluid)liq.getFluidState(state).getType()).gtolib$getReadOnlyStack())) {
                        blocks.addLast(blockPos);
                     }
                  } else if (itemFilter == null || itemFilter.test(((IItem)state.getBlock().asItem()).gtolib$getReadOnlyStack())) {
                     blocks.addLast(blockPos);
                  }
               }

               x++;
            } else {
               x = this.startX;
               z++;
            }
         } else {
            z = this.startZ;
            y--;
         }

         if (!blocks.isEmpty()) {
            calculated = blocks.size();
         }
      }

      return blocks;
   }

   private boolean isInMultiblock(BlockPos pos, MultiblockWorldData data) {
      if (data == null) {
         return false;
      }

      MultiblockState[] states = data.getControllersInChunk(ChunkPos.asLong(pos));
      if (states != null) {
         long pl = pos.asLong();

         for (MultiblockState structure : states) {
            if (structure.cache.contains(pl)) {
               return true;
            }
         }
      }

      return false;
   }

   private void checkBlocksToMine() {
      if (this.oresToMine.isEmpty() && !this.isSearchingBlocks && this.getMachine().getLevel() instanceof ServerLevel serverLevel) {
         synchronized (this.oresToMine) {
            this.isSearchingBlocks = true;
            if (this.minBuildHeight == Integer.MAX_VALUE) {
               this.minBuildHeight = this.getMachine().getLevel().getMinBuildHeight();
            }

            TaskHandler.enqueueAsyncTask(serverLevel, () -> this.onBlocksFound(this.getBlocksToMine(serverLevel, MultiblockWorldData.get(serverLevel))), 0);
         }
      }
   }

   @Override
   public void serverTick() {
      if (!this.isSuspend() && this.getMachine().getLevel() instanceof ServerLevel serverLevel && this.checkCanMine()) {
         if (!this.isInventoryFull()) {
            this.miner.drainInput(false);
            this.setStatus(1);
            if (this.lastRecipe == null) {
               this.lastRecipe = RecipeBuilder.ofRaw().EUt(this.miner.getMinerConfig().energyPerTick()).buildRawRecipe();
            }
         } else if (this.isWorking()) {
            this.setWaiting(Component.translatable("gtceu.recipe_logic.insufficient_out").append(": ").append(ItemRecipeInfo.INSTANCE.getName()));
         }

         this.checkBlocksToMine();
         if (!this.oresToMine.isEmpty() && this.miner.self().getOffsetTimer() % this.config.speed() == 0) {
            int loops = this.miner.getMinerConfig().parallelMining();
            this.lootCache.clear();

            do {
               BlockPos blockPos = this.oresToMine.getFirst();
               BlockState blockState = serverLevel.getBlockState(blockPos);
               if (!this.mineAndInsertFluids(blockState, serverLevel, blockPos)) {
                  NonNullList<ItemStack> blockDrops = NonNullList.create();
                  if (this.isSilkTouchMode()) {
                     getSilkTouchDrops(blockDrops, blockState);
                  } else {
                     this.getRegularBlockDrops(blockDrops, blockState, blockPos);
                  }

                  this.mineAndInsertItems(blockDrops, serverLevel);
                  this.oreAmount = this.oresToMine.size();
               }

               loops--;
            } while (loops > 0 && !this.oresToMine.isEmpty() && !this.isInventoryFull());
         }

         if (this.oresToMine.isEmpty() && !this.isSearchingBlocks) {
            this.x = this.mineX;
            this.y = this.mineY;
            this.z = this.mineZ;
            this.isDone = true;
            this.setStatus(0);
            this.miner.setWorkingEnabled(false);
            this.oreAmount = this.oresToMine.size();
         }
      } else {
         this.setStatus(0);
         this.miner.setWorkingEnabled(false);
         if (this.subscription != null) {
            this.subscription.unsubscribe();
            this.subscription = null;
         }

         this.lastRecipe = null;
      }
   }

   protected void getRegularBlockDrops(NonNullList<ItemStack> blockDrops, BlockState blockState, BlockPos blockPos) {
      Function<BlockState, List<ItemStack>> mappingFunction = state -> Block.getDrops(state, (ServerLevel)this.getMachine().getLevel(), blockPos, null)
         .stream()
         .map(ItemStack::copy)
         .toList();
      blockDrops.addAll(blockState.hasBlockEntity() ? mappingFunction.apply(blockState) : this.lootCache.computeIfAbsent(blockState, mappingFunction));
   }

   protected static void getSilkTouchDrops(NonNullList<ItemStack> blockDrops, BlockState blockState) {
      blockDrops.add(new ItemStack(blockState.getBlock()));
   }

   private void mineAndInsertItems(NonNullList<ItemStack> blockDrops, ServerLevel serverLevel) {
      if (this.miner.outputItem(blockDrops.toArray(new ItemStack[0]))) {
         BlockPos blockPos = this.oresToMine.removeFirst();
         GTOUtils.fastRemoveBlock(serverLevel, blockPos, false, false);
         this.mineX = blockPos.getX();
         this.mineY = blockPos.getY();
         this.mineZ = blockPos.getZ();
         this.isInventoryFull = false;
      } else {
         this.isInventoryFull = true;
      }
   }

   private boolean mineAndInsertFluids(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos) {
      if (blockState.getBlock() instanceof LiquidBlock liq && this.config.fluidMode() != IDigitalMiner.FluidMode.Ignore) {
         boolean isSource = liq.getFluidState(blockState).isSource();
         if (isSource && this.config.fluidMode() == IDigitalMiner.FluidMode.Harvest && !this.miner.outputFluid(liq.getFluidState(blockState).getType(), 1000L)) {
            this.isInventoryFull = true;
            return true;
         } else {
            this.isInventoryFull = false;
            GTOUtils.fastRemoveBlock(serverLevel, blockPos, false, false);
            this.oresToMine.removeFirst();
            this.oreAmount = this.oresToMine.size();
            return true;
         }
      } else {
         return false;
      }
   }

   @Generated
   public int getX() {
      return this.x;
   }

   @Generated
   public int getY() {
      return this.y;
   }

   @Generated
   public int getZ() {
      return this.z;
   }

   @Generated
   public int getStartX() {
      return this.startX;
   }

   @Generated
   public int getStartZ() {
      return this.startZ;
   }

   @Generated
   public int getStartY() {
      return this.startY;
   }

   @Generated
   public int getMineX() {
      return this.mineX;
   }

   @Generated
   public int getMineZ() {
      return this.mineZ;
   }

   @Generated
   public int getMineY() {
      return this.mineY;
   }

   @Generated
   public boolean isDone() {
      return this.isDone;
   }

   @Generated
   public IDigitalMiner getMiner() {
      return this.miner;
   }

   @Generated
   public int getMinBuildHeight() {
      return this.minBuildHeight;
   }

   @Generated
   public boolean isInventoryFull() {
      return this.isInventoryFull;
   }

   @Generated
   public int getOreAmount() {
      return this.oreAmount;
   }

   static {
      ItemStack is = new ItemStack(GTItems.TAG_FILTER.asItem());
      CompoundTag ft = new CompoundTag();
      ft.putString("oreDict", "forge:ores");
      is.setTag(ft);
      ORE_FILTER = TagItemFilter.loadFilter(is);
   }
}
