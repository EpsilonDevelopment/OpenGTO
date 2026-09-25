package com.gtolib.api.ae2.wtlib;

import appeng.menu.AEBaseMenu;
import appeng.menu.locator.MenuLocator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.gtolib.api.ae2.gui.hooks.IWUTScreen;
import com.gtolib.api.network.NetworkPack;
import de.mari_023.ae2wtlib.terminal.WTMenuHost;
import de.mari_023.ae2wtlib.wut.ItemWUT;
import de.mari_023.ae2wtlib.wut.WUTHandler;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class WUTHandlerExtended {
   public static final NetworkPack SELECT_TERMINALS_PACK = NetworkPack.registerC2S(
      "select_terminal", (data, buf) -> buf.writeUtf((String)data[0]), (player, buf) -> {
         if (player.containerMenu instanceof AEBaseMenu aeMenu) {
            MenuLocator locator = aeMenu.getLocator();
            WTMenuHost host = locator.locate(player, WTMenuHost.class);
            if (host != null) {
               ItemStack item = host.getItemStack();
               if (item.getItem() instanceof ItemWUT) {
                  String terminal = buf.readUtf();
                  WUTHandler.setCurrentTerminal(player, locator, item, terminal);
                  WUTHandler.open(player, locator, true);
               }
            }
         }
      }
   );
   public static final NetworkPack RETURN_TERMINAL_LIST = NetworkPack.registerS2C("return_terminal_list", WUTHandlerExtended.ClientWrapper::receiveTerminalList);
   public static final NetworkPack FETCH_TERMINAL_LIST = NetworkPack.registerC2S("fetch_terminal_list", (player, buf) -> {
      if (player.containerMenu instanceof AEBaseMenu aeMenu) {
         MenuLocator locator = aeMenu.getLocator();
         WTMenuHost host = locator.locate(player, WTMenuHost.class);
         if (host != null) {
            ItemStack item = host.getItemStack();
            if (item.getItem() instanceof ItemWUT) {
               List<String> terminals = getAvailableTerminals(item, false);
               RETURN_TERMINAL_LIST.send(p -> {
                  p.writeVarInt(terminals.size());

                  for (String t : terminals) {
                     p.writeUtf(t);
                  }
               }, player);
            }
         }
      }
   });

   public static List<String> getAvailableTerminals(ItemStack stack, boolean includeSelf) {
      if (stack.getTag() == null) {
         return ImmutableList.of();
      }

      Builder<String> ts = ImmutableList.builder();

      for (int i = 0; i < WUTHandler.terminalNames.size(); i++) {
         String key = WUTHandler.terminalNames.get(i);
         if ((includeSelf || !key.equals(WUTHandler.getCurrentTerminal(stack))) && stack.getTag().getBoolean(key)) {
            ts.add(key);
         }
      }

      return ts.build();
   }

   public static void init() {
   }

   public static class ClientWrapper {
      public static void receiveTerminalList(Player player, FriendlyByteBuf buf) {
         int size = buf.readVarInt();
         List<String> terminals = new ArrayList<>(size);

         for (int i = 0; i < size; i++) {
            terminals.add(buf.readUtf());
         }

         Minecraft mc = Minecraft.getInstance();
         if (mc.screen instanceof IWUTScreen wutScreen) {
            CycleTerminalButton btn = wutScreen.gto$getCycleTerminalButton();
            if (btn != null) {
               btn.setTerminals(terminals);
            }

            mc.screen.init(mc, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
         }
      }
   }
}
