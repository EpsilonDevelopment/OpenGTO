package com.gtolib.ae2.crafting.utils;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import com.gtolib.utils.FluidUtils;
import com.gtolib.utils.ItemUtils;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.ClickEvent.Action;

public final class ErrorMessageUtils {
   public static Component buildCycleErrorMessage(List<List<AEKey>> cycles) {
      MutableComponent mainMessage = createMainErrorMessage(cycles.size());
      appendCycleDetails(mainMessage, cycles);
      appendFooterInstructions(mainMessage, cycles.size());
      return mainMessage;
   }

   private static MutableComponent createMainErrorMessage(int cycleCount) {
      return Component.translatable("gtocore.ae.appeng.crafting.cycle_error.main")
         .withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
         .append(Component.translatable("gtocore.ae.appeng.crafting.cycle_error.count", cycleCount).withStyle(ChatFormatting.YELLOW));
   }

   private static void appendCycleDetails(MutableComponent mainMessage, List<List<AEKey>> cycles) {
      int displayCount = Math.min(cycles.size(), 5);

      for (int i = 0; i < displayCount; i++) {
         List<AEKey> cycle = cycles.get(i);
         appendLineCycle(mainMessage, cycle, i + 1);
      }

      if (cycles.size() > 5) {
         mainMessage.append(Component.translatable("gtocore.ae.appeng.crafting.cycle_error.more_cycles", cycles.size() - 5).withStyle(ChatFormatting.GRAY));
      }
   }

   private static void appendLineCycle(MutableComponent mainMessage, List<AEKey> cycle, int cycleNumber) {
      mainMessage.append(Component.translatable("gtocore.ae.appeng.crafting.cycle_error.cycle_number", cycleNumber).withStyle(ChatFormatting.AQUA));

      for (AEKey item : cycle) {
         MutableComponent itemComponent = buildItemComponent(item);
         mainMessage.append(Component.translatable("gtocore.ae.appeng.crafting.cycle_error.indent")).append(itemComponent);
      }

      if (!cycle.isEmpty()) {
         MutableComponent firstItemComponent = buildItemComponent(cycle.get(0));
         mainMessage.append(Component.translatable("gtocore.ae.appeng.crafting.cycle_error.indent")).append(firstItemComponent);
      }
   }

   private static void appendFooterInstructions(MutableComponent mainMessage, int cycleCount) {
      mainMessage.append(Component.translatable("gtocore.ae.appeng.crafting.cycle_error.footer").withStyle(ChatFormatting.RED))
         .append(Component.translatable("gtocore.ae.appeng.crafting.cycle_error.click_instruction").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
   }

   private static MutableComponent buildItemComponent(AEKey item) {
      String itemId = getItemIdString(item);
      String displayName = getItemDisplayName(item);
      return Component.translatable("gtocore.ae.appeng.crafting.cycle_error.item_prefix")
         .withStyle(ChatFormatting.GRAY)
         .append(createClickableItemId(itemId))
         .append(Component.translatable("gtocore.ae.appeng.crafting.cycle_error.bracket_open").withStyle(ChatFormatting.GRAY))
         .append(createClickableDisplayName(displayName))
         .append(Component.translatable("gtocore.ae.appeng.crafting.cycle_error.bracket_close").withStyle(ChatFormatting.GRAY));
   }

   private static MutableComponent createClickableItemId(String itemId) {
      return Component.literal(itemId)
         .withStyle(ChatFormatting.YELLOW)
         .withStyle(
            Style.EMPTY
               .withClickEvent(new ClickEvent(Action.COPY_TO_CLIPBOARD, itemId))
               .withHoverEvent(
                  new HoverEvent(
                     net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT,
                     Component.translatable("gtocore.ae.appeng.crafting.cycle_error.click_to_copy", itemId).withStyle(ChatFormatting.GREEN)
                  )
               )
         );
   }

   private static MutableComponent createClickableDisplayName(String displayName) {
      return Component.literal(displayName)
         .withStyle(ChatFormatting.WHITE)
         .withStyle(
            Style.EMPTY
               .withClickEvent(new ClickEvent(Action.COPY_TO_CLIPBOARD, displayName))
               .withHoverEvent(
                  new HoverEvent(
                     net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT,
                     Component.translatable("gtocore.ae.appeng.crafting.cycle_error.click_to_copy", displayName).withStyle(ChatFormatting.GREEN)
                  )
               )
         );
   }

   private static String getItemIdString(AEKey item) {
      if (item instanceof AEItemKey itemKey) {
         return ItemUtils.getId(itemKey.getItem());
      } else {
         return item instanceof AEFluidKey fluidKey ? FluidUtils.getId(fluidKey.getFluid()) : item.toString();
      }
   }

   private static String getItemDisplayName(AEKey item) {
      try {
         if (item instanceof AEItemKey itemKey) {
            return itemKey.getItem().getName(itemKey.toStack()).getString();
         } else {
            return item instanceof AEFluidKey fluidKey ? fluidKey.getFluid().getFluidType().getDescription().getString() : item.getDisplayName().getString();
         }
      } catch (Exception e) {
         return getItemIdString(item);
      }
   }
}
