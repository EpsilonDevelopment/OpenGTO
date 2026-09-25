package com.gtolib.utils;

import com.gtolib.api.item.IItem;
import com.gtolib.utils.iostream.IOStreamCodec;
import com.gtolib.utils.iostream.IOStreamCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public final class ItemUtils {
   public static final IOStreamCodec<Item> IO_CODEC = IOStreamCodecs.ITEM;
   public static final IOStreamCodec<ItemStack> STACK_IO_CODEC = IOStreamCodecs.ITEM_STACK;

   private ItemUtils() {
   }

   @NotNull
   public static String getId(@NotNull Block block) {
      return ((IItem)block.asItem()).gtolib$getIdString();
   }

   @NotNull
   public static String getId(@NotNull ItemStack item) {
      return ((IItem)item.getItem()).gtolib$getIdString();
   }

   @NotNull
   public static String getId(@NotNull Item item) {
      return ((IItem)item).gtolib$getIdString();
   }

   @NotNull
   public static ResourceLocation getIdLocation(@NotNull Block block) {
      return ((IItem)block.asItem()).gtolib$getIdLocation();
   }

   @NotNull
   public static ResourceLocation getIdLocation(@NotNull Item item) {
      return ((IItem)item).gtolib$getIdLocation();
   }
}
