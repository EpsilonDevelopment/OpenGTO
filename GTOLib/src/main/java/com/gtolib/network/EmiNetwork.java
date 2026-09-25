package com.gtolib.network;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import appeng.helpers.IMenuCraftingPacket;
import appeng.me.helpers.PlayerSource;
import appeng.menu.AEBaseMenu;
import appeng.menu.locator.MenuLocator;
import appeng.menu.me.common.MEStorageMenu;
import appeng.menu.me.crafting.CraftAmountMenu;
import appeng.menu.me.items.PatternEncodingTermMenu;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gtolib.api.ae2.me2in1.Me2in1Menu;
import com.gtolib.api.network.NetworkPack;
import de.mari_023.ae2wtlib.wct.CraftingTerminalHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class EmiNetwork {
   public static final NetworkPack INTERACTION = NetworkPack.registerC2S(
      "emiInteraction",
      (serverPlayer, data) -> {
         if (data.readVarInt() == serverPlayer.containerMenu.containerId) {
            MEStorage ae = null;
            switch (serverPlayer.containerMenu) {
               case PatternEncodingTermMenu ignored:
                  return;
               case Me2in1Menu ignored:
                  return;
               case MEStorageMenu menu:
                  ae = menu.getHost().getInventory();
                  break;
               default:
            }

            if (ae == null) {
               CraftingTerminalHandler cTHandler = CraftingTerminalHandler.getCraftingTerminalHandler(serverPlayer);
               if (cTHandler.getLocator() == null || cTHandler.getTargetGrid() == null) {
                  return;
               }

               if (cTHandler.getTargetGrid().getStorageService() == null) {
                  return;
               }

               ae = cTHandler.getTargetGrid().getStorageService().getInventory();
            }

            if (ae != null) {
               GenericStack stack = GenericStack.readBuffer(data);
               if (stack != null) {
                  AEKey what = stack.what();
                  long amount = stack.amount();
                  PlayerSource playerSource = new PlayerSource(serverPlayer, null);
                  Inventory playerInv = serverPlayer.getInventory();
                  int mode = data.readVarInt();
                  AEItemKey itemKey;
                  if (what instanceof AEItemKey aeItemKey) {
                     itemKey = aeItemKey;
                     if (mode == 2) {
                        long inserted = ae.insert(itemKey, amount, Actionable.MODULATE, playerSource);
                        if (inserted < amount) {
                           ItemStack returnStack = itemKey.toStack((int)(amount - inserted));
                           serverPlayer.containerMenu.setCarried(returnStack);
                        } else {
                           serverPlayer.containerMenu.setCarried(ItemStack.EMPTY);
                        }

                        return;
                     }

                     amount = ae.extract(what, amount, Actionable.MODULATE, playerSource);
                  } else {
                     if (!(what instanceof AEFluidKey fluidKey)) {
                        return;
                     }

                     long fluidCellRemaining = ae.extract(AEItemKey.of(GTItems.FLUID_CELL.asItem()), amount, Actionable.MODULATE, playerSource);
                     long fluidAmount = ae.extract(what, fluidCellRemaining * 1000L, Actionable.SIMULATE, playerSource);
                     long exactFluidCells = fluidAmount / 1000L;
                     if (exactFluidCells <= 0L) {
                        return;
                     }

                     ItemStack is = GTItems.FLUID_CELL.asStack();
                     CompoundTag fluidTag = is.getOrCreateTag();
                     CompoundTag fluid = new CompoundTag();
                     fluid.putString("FluidName", fluidKey.getId().toString());
                     fluid.putInt("Amount", 1000);
                     fluidTag.put("Fluid", fluid);
                     is.setTag(fluidTag);
                     itemKey = AEItemKey.of(is);
                     amount = exactFluidCells;
                     ae.extract(what, exactFluidCells * 1000L, Actionable.MODULATE, playerSource);
                     ae.insert(what, fluidCellRemaining - exactFluidCells, Actionable.MODULATE, playerSource);
                  }

                  serverPlayer.playNotifySound(
                     SoundEvents.ITEM_PICKUP,
                     SoundSource.PLAYERS,
                     0.2F,
                     ((serverPlayer.getRandom().nextFloat() - serverPlayer.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F
                  );
                  if (mode == 1) {
                     ItemStack carried = serverPlayer.containerMenu.getCarried();
                     if (carried.isEmpty()) {
                        ItemStack toCarried = itemKey.toStack((int)amount);
                        serverPlayer.containerMenu.setCarried(toCarried);
                        return;
                     }

                     if (ItemStack.isSameItemSameTags(carried, itemKey.toStack(1))) {
                        long canAdd = carried.getMaxStackSize() - carried.getCount();
                        long toAdd = Math.min(canAdd, amount);
                        carried.grow((int)toAdd);
                        serverPlayer.containerMenu.setCarried(carried);
                        amount -= toAdd;
                     }

                     if (amount <= 0L) {
                        return;
                     }
                  }

                  if (!playerInv.add(itemKey.toStack((int)amount))) {
                     ItemStack toDrop = itemKey.toStack((int)amount);
                     serverPlayer.drop(toDrop, false);
                  }
               }
            }
         }
      }
   );
   public static final NetworkPack AUTO_CRAFT = NetworkPack.registerC2S("emiAutoCraft", (serverPlayer, data) -> {
      if (data.readVarInt() == serverPlayer.containerMenu.containerId) {
         IGrid ae = null;
         MenuLocator menuLocator = null;
         if (serverPlayer.containerMenu instanceof IMenuCraftingPacket aeMenu) {
            IGridNode node = aeMenu.getNetworkNode();
            if (node == null) {
               return;
            }

            ae = node.getGrid();
         }

         if (serverPlayer.containerMenu instanceof AEBaseMenu aeMenu) {
            menuLocator = aeMenu.getLocator();
         }

         if (ae == null) {
            CraftingTerminalHandler cTHandler = CraftingTerminalHandler.getCraftingTerminalHandler(serverPlayer);
            if (cTHandler.getLocator() == null || cTHandler.getTargetGrid() == null) {
               return;
            }

            menuLocator = cTHandler.getLocator();
            ae = cTHandler.getTargetGrid();
         }

         if (ae != null && menuLocator != null) {
            GenericStack stack = GenericStack.readBuffer(data);
            if (stack != null) {
               AEKey what = stack.what();
               long amount = stack.amount();
               if (!ae.getCraftingService().getCraftingFor(what).isEmpty()) {
                  CraftAmountMenu.open(serverPlayer, menuLocator, what, amount);
               }
            }
         }
      }
   });

   public static void init() {
   }
}
