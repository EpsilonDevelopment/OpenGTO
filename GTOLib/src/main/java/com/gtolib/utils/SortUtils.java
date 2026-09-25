package com.gtolib.utils;

import com.google.common.base.Equivalence;
import com.google.common.base.Equivalence.Wrapper;
import com.google.common.collect.Streams;
import com.gto.datasynclib.util.ItemStackHashStrategy;
import com.gtolib.api.network.NetworkPack;
import com.lowdragmc.lowdraglib.gui.modular.ModularUIContainer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public final class SortUtils {
   private static final NetworkPack SORT = NetworkPack.registerC2S("containerSort", (p, b) -> {
      if (p.containerMenu instanceof ModularUIContainer container) {
         sort(container);
      }
   });
   private static final Equivalence<ItemStack> STACKABLE = new Equivalence<ItemStack>() {
      @ParametersAreNonnullByDefault
      protected boolean doEquivalent(ItemStack a, ItemStack b) {
         return ItemStackHashStrategy.ITEM_AND_TAG.equals(a, b);
      }

      protected int doHash(@NotNull ItemStack t) {
         return ItemStackHashStrategy.ITEM_AND_TAG.hashCode(t);
      }
   };
   private static <T extends Collection<ItemStack>> T collated(Iterable<ItemStack> iterable, Supplier<T> collSupp) {
      Map<Wrapper<ItemStack>, List<ItemStack>> mapping = Streams.stream(iterable)
         .collect(Collectors.groupingBy(STACKABLE::wrap, LinkedHashMap::new, Collectors.toList()));
      return mapping.values().stream().flatMap(Collection::stream).collect(Collectors.toCollection(collSupp));
   }

   private static List<ItemStack> condensed(Iterable<ItemStack> iterable) {
      List<ItemStack> coll = collated(iterable, ArrayList::new);
      ItemStackHandler stackBuffer = new ItemStackHandler(coll.size());
      int index = 0;

      for (ItemStack stack : coll) {
         stack = stack.copy();

         while (!(stack = stackBuffer.insertItem(index, stack, false)).isEmpty()) {
            index++;
         }
      }

      return IntStream.range(0, stackBuffer.getSlots())
         .mapToObj(stackBuffer::getStackInSlot)
         .filter(is -> !is.isEmpty())
         .collect(Collectors.toCollection(ArrayList::new));
   }

   public static void sort() {
      SORT.send();
   }

   public static void sort(ModularUIContainer container) {
      List<Slot> slots = container.slots.stream().filter(slot -> !(slot.container instanceof Inventory)).collect(Collectors.toCollection(ArrayList::new));
      List<ItemStack> stacks = condensed(() -> slots.stream().map(Slot::getItem).filter(is -> !is.isEmpty()).iterator());
      stacks.sort(Comparator.comparing(is -> ItemUtils.getIdLocation(is.getItem())));
      Iterator<Slot> slotIt = slots.iterator();

      for (ItemStack stack : stacks) {
         Slot cur = null;

         while (slotIt.hasNext() && !(cur = slotIt.next()).mayPlace(stack)) {
         }

         if (cur == null || !cur.mayPlace(stack)) {
            return;
         }
      }

      slots.forEach(slot -> slot.set(ItemStack.EMPTY));
      slotIt = slots.iterator();

      for (ItemStack stack : stacks) {
         Slot cur = null;

         while (slotIt.hasNext() && !(cur = slotIt.next()).mayPlace(stack)) {
         }

         assert cur != null;
         cur.set(stack);
      }
   }
}
