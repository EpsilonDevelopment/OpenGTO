package com.gtolib.gtm;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IDropSaveMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineModifyDrops;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.ControllerPredicate;
import com.gregtechceu.gtceu.api.pattern.MultiblockState;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.pattern.predicates.SimplePredicate;
import com.gregtechceu.gtceu.common.block.LampBlock;
import com.gtolib.api.ae2.IExpandedStorageService;
import com.gtolib.api.machine.MultiblockDefinition;
import com.gtolib.api.player.IEnhancedPlayer;
import com.gtolib.mc.ILevel;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Triplet;

final class AdvancedBlockPattern extends BlockPattern {
   private AdvancedBlockPattern(BlockPattern pattern) {
      super(
         pattern.blockMatches,
         pattern.structureDir,
         pattern.aisleRepetitions,
         pattern.centerOffset,
         pattern.fingerLength,
         pattern.thumbLength,
         pattern.palmLength
      );
   }

   static AdvancedBlockPattern getAdvancedBlockPattern(BlockPattern blockPattern) {
      return new AdvancedBlockPattern(blockPattern);
   }

   void autoBuild(ServerPlayer player, MultiblockState worldState, AutoBuildSetting autoBuildSetting) {
      Level world = player.level();
      int minZ = -this.centerOffset[4];
      worldState.clearCache();
      worldState.clear();
      IMultiController controller = worldState.controller;
      BlockPos centerPos = worldState.controllerPos;
      Direction facing = controller.self().getFrontFacing();
      Direction upwardsFacing = controller.self().getUpwardsFacing();
      int ordinal = facing.ordinal();
      Reference2IntOpenHashMap<SimplePredicate> cacheGlobal = worldState.getGlobalCount();
      Reference2IntOpenHashMap<SimplePredicate> cacheLayer = worldState.getLayerCount();
      Long2ObjectOpenHashMap<MetaMachine> machines = new Long2ObjectOpenHashMap<>();
      LongOpenHashSet blocks = new LongOpenHashSet(1024, 0.5F);
      int[] repetitions = MultiblockDefinition.getClampedAisleRepetitions(this, autoBuildSetting.repeatCount);
      IStorageService ae = null;
      if (autoBuildSetting.isUseAEMode) {
         ae = IEnhancedPlayer.getMEStorageService(player);
      }

      int c = 0;
      int z = minZ++;

      while (c < this.fingerLength) {
         for (int r = 0; r < repetitions[c]; r++) {
            cacheLayer.clear();
            int b = 0;

            for (int y = -this.centerOffset[1]; b < this.thumbLength; y++) {
               int a = 0;

               for (int x = -this.centerOffset[0]; a < this.palmLength; x++) {
                  TraceabilityPredicate[][] bc = this.blockMatches[c];
                  if (bc != null) {
                     TraceabilityPredicate[] bb = bc[b];
                     if (bb != null) {
                        TraceabilityPredicate predicate = bb[a];
                        label239:
                        if (predicate != null && !predicate.isAir() && !(predicate instanceof ControllerPredicate)) {
                           BlockPos pos = this.setActualRelativeOffset(x, y, z, facing, ordinal, upwardsFacing, autoBuildSetting.isFlipMode)
                              .offset(centerPos.getX(), centerPos.getY(), centerPos.getZ());
                           worldState.update(pos, predicate);
                           long posLong = pos.asLong();
                           Item blockItem = null;
                           BlockState blockState = world.getBlockState(pos);
                           Block block = blockState.getBlock();
                           if (block != Blocks.AIR) {
                              if (autoBuildSetting.isDemolitionMode) {
                                 if (block instanceof LiquidBlock) {
                                    ILevel.fastRemoveBlock(world, pos, true, true);
                                    break label239;
                                 }

                                 boolean contain = false;

                                 label286:
                                 for (SimplePredicate common : predicate.common) {
                                    Block[] candidates = common.candidates == null ? null : common.candidates.get();
                                    if (candidates != null) {
                                       for (Block bl : candidates) {
                                          if (bl == block) {
                                             contain = true;
                                             break label286;
                                          }
                                       }
                                    }
                                 }

                                 if (!contain) {
                                    label270:
                                    for (SimplePredicate limited : predicate.limited) {
                                       Block[] candidates = limited.candidates == null ? null : limited.candidates.get();
                                       if (candidates != null) {
                                          for (Block bl : candidates) {
                                             if (bl == block) {
                                                contain = true;
                                                break label270;
                                             }
                                          }
                                       }
                                    }
                                 }

                                 if (!contain) {
                                    break label239;
                                 }

                                 ItemStack drop = new ItemStack(block);
                                 if (block instanceof LampBlock lamp) {
                                    drop.setTag(lamp.getTagFromState(blockState));
                                 }

                                 if (blockState.hasBlockEntity() && world.getBlockEntity(pos) instanceof MetaMachineBlockEntity holder) {
                                    MetaMachine machine = holder.getMetaMachine();
                                    if (machine instanceof IMachineModifyDrops machineModifyDrops) {
                                       machineModifyDrops.onDrops(Collections.singletonList(drop));
                                    }

                                    if (machine instanceof IDropSaveMachine dropSaveMachine && dropSaveMachine.saveBreak()) {
                                       dropSaveMachine.saveToItem(drop.getOrCreateTag());
                                    }
                                 }

                                 if ((ae == null || ae.getInventory().insert(AEItemKey.of(drop), 1L, Actionable.MODULATE, IActionSource.ofPlayer(player)) == 0L)
                                    && !player.addItem(drop)) {
                                    player.drop(drop, false);
                                 }

                                 world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                                 break label239;
                              }

                              if (!autoBuildSetting.isReplaceMode || !autoBuildSetting.blocks.contains(block)) {
                                 blocks.add(posLong);

                                 for (SimplePredicate limit : predicate.limited) {
                                    limit.testLimited(worldState);
                                 }
                                 break label239;
                              }

                              blockItem = block.asItem();
                           } else if (autoBuildSetting.isDemolitionMode) {
                              break label239;
                           }

                           boolean find = false;
                           Block[] infos = new Block[0];

                           for (SimplePredicate limit : predicate.limited) {
                              Block[] candidates = limit.candidates == null ? null : limit.candidates.get();
                              if (candidates != null && limit.minLayerCount > 0 && (predicate.isSingle() || autoBuildSetting.isPlaceHatch(candidates))) {
                                 int curr = cacheLayer.getInt(limit);
                                 if (curr < limit.minLayerCount && (limit.maxLayerCount == -1 || curr < limit.maxLayerCount)) {
                                    cacheLayer.addTo(limit, 1);
                                    infos = candidates;
                                    find = true;
                                    break;
                                 }
                              }
                           }

                           if (!find) {
                              for (SimplePredicate limit : predicate.limited) {
                                 Block[] candidates = limit.candidates == null ? null : limit.candidates.get();
                                 if (candidates != null && limit.minCount > 0 && (predicate.isSingle() || autoBuildSetting.isPlaceHatch(candidates))) {
                                    int curr = cacheGlobal.getInt(limit);
                                    if (curr < limit.minCount && (limit.maxCount == -1 || curr < limit.maxCount)) {
                                       cacheGlobal.addTo(limit, 1);
                                       infos = candidates;
                                       find = true;
                                       break;
                                    }
                                 }
                              }
                           }

                           if (!find) {
                              for (SimplePredicate limit : predicate.limited) {
                                 Block[] candidates = limit.candidates == null ? null : limit.candidates.get();
                                 if (candidates != null
                                    && (predicate.isSingle() || autoBuildSetting.isPlaceHatch(candidates))
                                    && (limit.maxLayerCount == -1 || cacheLayer.getOrDefault(limit, Integer.MAX_VALUE) != limit.maxLayerCount)
                                    && (limit.maxCount == -1 || cacheGlobal.getOrDefault(limit, Integer.MAX_VALUE) != limit.maxCount)) {
                                    cacheLayer.addTo(limit, 1);
                                    cacheGlobal.addTo(limit, 1);
                                    infos = ArrayUtils.addAll(infos, candidates);
                                 }
                              }

                              for (SimplePredicate common : predicate.common) {
                                 Block[] candidates = common.candidates == null ? null : common.candidates.get();
                                 if (candidates != null && (predicate.isSingle() || autoBuildSetting.isPlaceHatch(candidates))) {
                                    infos = ArrayUtils.addAll(infos, candidates);
                                 }
                              }
                           }

                           List<AEKey> candidates = autoBuildSetting.apply(infos);
                           if (!autoBuildSetting.isReplaceMode
                              || blockItem == null
                              || !(candidates.getFirst() instanceof AEItemKey itemKey && itemKey.getReadOnlyStack().is(blockItem))) {
                              List<ItemStack> candidatesItem = candidates.stream()
                                 .filter(c0 -> c0 instanceof AEItemKey)
                                 .map(c0 -> ((AEItemKey)c0).toStack())
                                 .toList();
                              Triplet<ItemStack, IItemHandler, Integer> result = foundItem(player, candidatesItem, ae);
                              ItemStack foundItem = result.getA();
                              IItemHandler itemHandler = result.getB();
                              int foundSlot = result.getC();
                              if (foundItem != null) {
                                 boolean isCreative = player.isCreative();
                                 if (isCreative || itemHandler != null && !itemHandler.extractItem(foundSlot, 1, true).isEmpty()) {
                                    BlockState oldState = world.getBlockState(pos);
                                    boolean isReplaceMode = autoBuildSetting.isReplaceMode && blockItem != null;
                                    if (isReplaceMode) {
                                       world.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                                    }

                                    BlockItem itemBlock = (BlockItem)foundItem.getItem();
                                    BlockPlaceContext context = new BlockPlaceContext(
                                       world, player, InteractionHand.MAIN_HAND, foundItem, BlockHitResult.miss(player.getEyePosition(0.0F), Direction.UP, pos)
                                    );
                                    InteractionResult interactionResult = itemBlock.place(context);
                                    label297:
                                    if (interactionResult.consumesAction()) {
                                       if (!isCreative) {
                                          ItemStack extracted = itemHandler.extractItem(foundSlot, 1, false);
                                          if (extracted.isEmpty()) {
                                             world.setBlock(pos, isReplaceMode ? oldState : Blocks.AIR.defaultBlockState(), 3);
                                             break label297;
                                          }
                                       }

                                       if (isReplaceMode
                                          && (
                                             ae == null
                                                || ae.getInventory().insert(AEItemKey.of(blockItem), 1L, Actionable.MODULATE, IActionSource.ofPlayer(player))
                                                   == 0L
                                          )) {
                                          ItemStack drop = new ItemStack(blockItem, 1);
                                          if (!player.addItem(drop)) {
                                             player.drop(drop, false);
                                          }
                                       }

                                       Direction direction = predicate.direction.apply(worldState);
                                       if (direction != null) {
                                          world.setBlock(pos, world.getBlockState(pos).setValue(DirectionalBlock.FACING, direction), 3);
                                       } else if (world.getBlockEntity(pos) instanceof MetaMachineBlockEntity machineBlockEntity) {
                                          machines.put(posLong, machineBlockEntity.metaMachine);
                                       }

                                       blocks.add(posLong);
                                    } else if (isReplaceMode) {
                                       world.setBlock(pos, oldState, 3);
                                    }
                                 }
                              } else {
                                 List<AEFluidKey> candidatesFluid = candidates.stream()
                                    .filter(c0 -> c0 instanceof AEFluidKey)
                                    .map(c0 -> (AEFluidKey)c0)
                                    .toList();
                                 if (!candidatesFluid.isEmpty()) {
                                    if (player.getAbilities().instabuild) {
                                       world.setBlock(pos, candidatesFluid.getFirst().getFluid().defaultFluidState().createLegacyBlock(), 11);
                                    } else if (ae != null
                                       && ae.getInventory().extract(candidatesFluid.getFirst(), 1000L, Actionable.MODULATE, IActionSource.ofPlayer(player))
                                          == 1000L) {
                                       world.setBlock(pos, candidatesFluid.getFirst().getFluid().defaultFluidState().createLegacyBlock(), 11);
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }

                  a++;
               }

               b++;
            }

            z++;
         }

         c++;
      }

      Direction frontFacing = controller.self().getFrontFacing();
      machines.long2ObjectEntrySet()
         .fastForEach(
            entry -> {
               long posLongx = entry.getLongKey();
               MetaMachine machine = entry.getValue();
               BlockPos posx = BlockPos.of(posLongx);
               this.resetFacing(
                  posx,
                  machine.getBlockState(),
                  frontFacing,
                  (p, f) -> !blocks.contains(p.relative(f).asLong()) ? machine.isFacingValid(f) : false,
                  state -> world.setBlock(posx, state, 18)
               );
            }
         );
   }

   private static Triplet<ItemStack, IItemHandler, Integer> foundItem(Player player, List<ItemStack> candidates, IStorageService ae) {
      ItemStack found = null;
      IItemHandler handler = null;
      int foundSlot = -1;
      if (!player.isCreative()) {
         IntObjectPair<IItemHandler> foundHandler = getMatchStackWithHandler(candidates, player.getCapability(ForgeCapabilities.ITEM_HANDLER), player, ae);
         if (foundHandler != null) {
            foundSlot = foundHandler.firstInt();
            handler = foundHandler.second();
            found = handler.getStackInSlot(foundSlot).copy();
         }
      } else {
         for (ItemStack candidate : candidates) {
            found = candidate.copy();
            if (!found.isEmpty() && found.getItem() instanceof BlockItem) {
               break;
            }

            found = null;
         }
      }

      return new Triplet<>(found, handler, foundSlot);
   }

   @Nullable
   private static IntObjectPair<IItemHandler> getMatchStackWithHandler(
      List<ItemStack> candidates, LazyOptional<IItemHandler> cap, Player player, IStorageService ae
   ) {
      IItemHandler handler = cap.resolve().orElse(null);
      if (handler == null) {
         return null;
      }

      for (int i = 0; i < handler.getSlots(); i++) {
         ItemStack stack = handler.getStackInSlot(i);
         if (!stack.isEmpty()) {
            LazyOptional<IItemHandler> stackCap = stack.getCapability(ForgeCapabilities.ITEM_HANDLER);
            if (stackCap.isPresent()) {
               IntObjectPair<IItemHandler> rt = getMatchStackWithHandler(candidates, stackCap, player, ae);
               if (rt != null) {
                  return rt;
               }
            } else if (ae != null) {
               for (ItemStack candidate : candidates) {
                  if (IExpandedStorageService.fuzzyExtract(AEItemKey.of(candidate), 1L, ae, Actionable.MODULATE, IActionSource.ofPlayer(player)) > 0L) {
                     NonNullList<ItemStack> stacks = NonNullList.withSize(1, candidate);
                     IItemHandler handler1 = new ItemStackHandler(stacks);
                     return IntObjectPair.of(0, handler1);
                  }
               }
            } else if (candidates.stream().anyMatch(candidatex -> ItemStack.isSameItem(candidatex, stack))
               && !stack.isEmpty()
               && stack.getItem() instanceof BlockItem) {
               return IntObjectPair.of(i, handler);
            }
         }
      }

      return null;
   }
}
