package com.gtolib.api.ae2.me2in1;

import appeng.api.config.Settings;
import appeng.api.config.ShowPatternProviders;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.TypeFilter;
import appeng.api.config.ViewItems;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.api.networking.IGridNode;
import appeng.api.util.IConfigManager;
import appeng.client.gui.style.ScreenStyle;
import appeng.core.AEConfig;
import appeng.items.tools.powered.WirelessTerminalItem;
import appeng.menu.ISubMenu;
import appeng.menu.ToolboxMenu;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.slot.RestrictedInputSlot;
import appeng.menu.slot.RestrictedInputSlot.PlacableItemType;
import appeng.util.ConfigManager;
import com.gtocore.common.data.GTOItems;
import com.gtolib.ae2.me2in1.IMe2in1Host;
import com.gtolib.api.ae2.GTOSettings;
import com.gtolib.api.ae2.ShiftTransferTo;
import de.mari_023.ae2wtlib.AE2wtlibSlotSemantics;
import de.mari_023.ae2wtlib.terminal.IUniversalWirelessTerminalItem;
import de.mari_023.ae2wtlib.terminal.WTMenuHost;
import de.mari_023.ae2wtlib.wct.WCTMenuHost;
import de.mari_023.ae2wtlib.wut.CycleTerminalButton;
import de.mari_023.ae2wtlib.wut.IUniversalTerminalCapable;
import de.mari_023.ae2wtlib.wut.ItemWUT;
import gto_ae.api.config.ExtendedSettings;
import gto_ae.client.gui.widgets.AESlotWidget;
import gto_ae.menu.ShowMolecularAssembler;
import java.util.List;
import java.util.function.BiConsumer;
import lombok.Generated;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Wireless extends Me2in1Menu {
   public static final String ID = "me2in1_wireless";
   public static final MenuType<Wireless> TYPE = MenuTypeBuilder.create(Wireless::new, Wireless.Host.class).build("me2in1_wireless");
   private final Wireless.Host host;
   private final ToolboxMenu toolboxMenu;
   private final RestrictedInputSlot singularitySlot;

   public Wireless(int id, Inventory ip, Wireless.Host host) {
      super(TYPE, id, ip, host, true);
      this.host = host;
      this.toolboxMenu = new ToolboxMenu(this);
      this.addSlot(
         this.singularitySlot = new RestrictedInputSlot(PlacableItemType.QE_SINGULARITY, this.host.getSubInventory(WCTMenuHost.INV_SINGULARITY), 0),
         AE2wtlibSlotSemantics.SINGULARITY
      );
   }

   public ToolboxMenu getToolbox() {
      return this.toolboxMenu;
   }

   @Override
   public IGridNode getNetworkNode() {
      return this.host.getActionableNode();
   }

   private boolean isWUT() {
      return this.host.getItemStack().getItem() instanceof ItemWUT;
   }

   @Generated
   public RestrictedInputSlot getSingularitySlot() {
      return this.singularitySlot;
   }

   public static class Host extends WTMenuHost implements IMe2in1Host {
      private final ExtendedEncodingLogic logic = new ExtendedEncodingLogic(this);

      public Host(Player player, @Nullable Integer inventorySlot, ItemStack is, BiConsumer<Player, ISubMenu> returnToMainMenu) {
         super(player, inventorySlot, is, returnToMainMenu);
         this.readFromNbt();
      }

      @Override
      public ItemStack getMainMenuIcon() {
         return GTOItems.WIRELESS_ME2IN1.asStack();
      }

      @Override
      protected void readFromNbt() {
         super.readFromNbt();
         this.logic.readFromNBT(this.getItemStack().getOrCreateTag());
      }

      @Override
      public void saveChanges() {
         super.saveChanges();
         this.logic.writeToNBT(this.getItemStack().getOrCreateTag());
      }

      @Override
      public ExtendedEncodingLogic getLogic() {
         return this.logic;
      }

      @Override
      public Level getLevel() {
         return this.getPlayer().level();
      }

      @Override
      public void markForSave() {
         this.saveChanges();
      }
   }

   public static class Item extends WirelessTerminalItem implements IUniversalWirelessTerminalItem {
      public Item(Properties p) {
         super(AEConfig.instance().getWirelessTerminalBattery(), p.stacksTo(1));
      }

      @NotNull
      @Override
      public MenuType<?> getMenuType(@NotNull ItemStack stack) {
         return Wireless.TYPE;
      }

      @NotNull
      @Override
      public IConfigManager getConfigManager(ItemStack target) {
         ConfigManager out = new ConfigManager((manager, settingName) -> manager.writeToNBT(target.getOrCreateTag()));
         out.registerSetting(Settings.SORT_BY, SortOrder.NAME);
         out.registerSetting(Settings.VIEW_MODE, ViewItems.ALL);
         out.registerSetting(Settings.TYPE_FILTER, TypeFilter.ALL);
         out.registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING);
         out.registerSetting(Settings.TERMINAL_SHOW_PATTERN_PROVIDERS, ShowPatternProviders.VISIBLE);
         out.registerSetting(ExtendedSettings.TERMINAL_SHOW_MOLECULAR_ASSEMBLERS, ShowMolecularAssembler.ALL);
         out.registerSetting(GTOSettings.ME2IN1_SHIFT_TRANSFER_TO, ShiftTransferTo.INVENTORY_OR_BUFFER);
         out.readFromNBT(target.getOrCreateTag().copy());
         return out;
      }

      @Override
      public MenuType<?> getMenuType() {
         return Wireless.TYPE;
      }

      @Nullable
      @Override
      public ItemMenuHost getMenuHost(Player player, int inventorySlot, ItemStack stack, @Nullable BlockPos pos) {
         return new Wireless.Host(player, inventorySlot, stack, (p, sm) -> this.openFromInventory(p, inventorySlot, true));
      }
   }

   public static class Screen extends Me2in1Screen<Wireless> implements IUniversalTerminalCapable {
      public Screen(Wireless menu, Inventory playerInventory, Component title, ScreenStyle style) {
         super(menu, playerInventory, title, style);
         if (this.getMenu().isWUT()) {
            this.addToLeftToolbar(new CycleTerminalButton(btn -> this.cycleTerminal()));
         }

         this.widgets.add("quantumSlot", new AESlotWidget(menu.singularitySlot, this));
         menu.singularitySlot
            .setEmptyTooltip(
               () -> List.of(
                  Component.translatable("gtocore.ae.appeng.me2in1.quantum_bridge"), Component.translatable("gtocore.ae.appeng.me2in1.quantum_bridge.info")
               )
            );
      }

      @Override
      public void updateBeforeRender() {
         super.updateBeforeRender();
      }
   }
}
