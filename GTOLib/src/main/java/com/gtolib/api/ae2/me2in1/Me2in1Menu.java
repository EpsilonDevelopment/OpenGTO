package com.gtolib.api.ae2.me2in1;

import appeng.api.behaviors.ContainerItemStrategies;
import appeng.api.behaviors.EmptyingAction;
import appeng.api.config.Setting;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.TypeFilter;
import appeng.api.config.ViewItems;
import appeng.api.implementations.blockentities.IViewCellStorage;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGridNode;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.util.IConfigManager;
import appeng.client.gui.me.patternaccess.PatternSlot;
import appeng.helpers.InventoryAction;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.helpers.externalstorage.GenericStackInv.Mode;
import appeng.menu.MenuOpener;
import appeng.menu.SlotSemantic;
import appeng.menu.SlotSemantics;
import appeng.menu.ToolboxMenu;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.locator.MenuLocator;
import appeng.menu.me.common.IClientRepo;
import appeng.menu.slot.AppEngSlot;
import appeng.menu.slot.CraftingTermSlot;
import appeng.menu.slot.FakeSlot;
import appeng.menu.slot.RestrictedInputSlot;
import appeng.menu.slot.RestrictedInputSlot.PlacableItemType;
import appeng.util.ConfigMenuInventory;
import com.glodblock.github.extendedae.container.ContainerExPatternTerminal;
import com.gtolib.ae2.me2in1.panel.Panel;
import com.gtolib.ae2.me2in1.panel.PanelMapPosSyncable;
import com.gtolib.ae2.me2in1.panel.PanelMapSizeSyncable;
import com.gtolib.api.ae2.GTOSettings;
import com.gtolib.api.ae2.ShiftTransferTo;
import com.gtolib.api.ae2.gui.hooks.IExtendedGuiEx;
import com.gtolib.api.ae2.me2in1.emi.CategoryMappingSubMenu;
import com.gtolib.api.ae2.me2in1.encoding.ExtendedEncodingMode;
import com.gtolib.api.ae2.me2in1.panel.PanelPosMap;
import com.gtolib.api.ae2.me2in1.panel.PanelSizeMap;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.Generated;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Me2in1Menu extends ContainerExPatternTerminal implements IMe2in1Menu {
   private static final String RECORD_PANEL_POS = "recordPanelPos";
   private static final String RECORD_PANEL_SIZE = "recordPanelSize";
   public static final String ACTION_OPEN_CATEGORY_MAPPING_SUB_MENU = "openCategoryMappingSubMenu";
   public static final MenuType<Me2in1Menu> TYPE = MenuTypeBuilder.create(Me2in1Menu::new, IExtendedPatternMenuHost.class).build("me2in1");
   private final ExtendedEncodingMenu encoding;
   private final IExtendedPatternMenuHost host;
   private final List<RestrictedInputSlot> viewCellSlots;
   private IExtendedGuiEx screen;
   @GuiSync(79)
   public ShiftTransferTo shiftTransferTo = ShiftTransferTo.INVENTORY_OR_BUFFER;
   @GuiSync(78)
   public SortOrder sortBy = SortOrder.NAME;
   @GuiSync(77)
   public SortDir sortDir = SortDir.ASCENDING;
   @GuiSync(76)
   public ViewItems sortDisplay = ViewItems.ALL;
   @GuiSync(75)
   public TypeFilter typeFilter = TypeFilter.ALL;
   @GuiSync(74)
   public PanelMapPosSyncable panelPosMap;
   @GuiSync(101)
   public PanelMapSizeSyncable panelSizeMap;
   @GuiSync(87)
   public ExtendedEncodingMode mode = ExtendedEncodingMode.CRAFTING;
   @GuiSync(86)
   public boolean substitute = false;
   @GuiSync(85)
   public boolean substituteFluids = true;
   @GuiSync(84)
   @Nullable
   public ResourceLocation stonecuttingRecipeId;
   @GuiSync(83)
   public boolean autoEncodeRenaming = false;
   @GuiSync(82)
   public boolean autoSearchProviders = false;
   @GuiSync(947)
   public boolean extraInfoEnabled = true;
   @GuiSync(98)
   public boolean hasPower = false;
   @GuiSync(100)
   public int activeCraftingJobs = -1;
   private Runnable posSyncedListener;
   private final ToolboxMenu toolboxMenu;

   public Me2in1Menu(int id, Inventory ip, IExtendedPatternMenuHost anchor) {
      this(TYPE, id, ip, anchor, true);
   }

   public Me2in1Menu(MenuType<?> menuType, int id, Inventory ip, IExtendedPatternMenuHost host, boolean bindInventory) {
      super(menuType, id, ip, host, bindInventory);
      this.encoding = new ExtendedEncodingMenu(this, id, ip, host, false);
      this.host = host;
      this.toolboxMenu = new ToolboxMenu(this);
      InternalInventory viewCellStorage = ((IViewCellStorage)host).getViewCellStorage();
      this.viewCellSlots = new ArrayList<>(viewCellStorage.size());

      for (int i = 0; i < viewCellStorage.size(); i++) {
         RestrictedInputSlot slot = new RestrictedInputSlot(PlacableItemType.VIEW_CELL, viewCellStorage, i);
         this.addSlot(slot, SlotSemantics.VIEW_CELL);
         this.viewCellSlots.add(slot);
      }

      this.registerClientAction("recordPanelPos", PanelPosMap.PanelPos.class, this::updateC2SPanelPos);
      this.registerClientAction("recordPanelSize", PanelSizeMap.PanelSize.class, this::updateC2SPanelSize);
      this.registerClientAction("openCategoryMappingSubMenu", this::openCategoryMappingSubMenu);
   }

   @Override
   public void setLocator(MenuLocator locator) {
      super.setLocator(locator);
      this.encoding.setLocator(locator);
   }

   @Override
   public Slot addSlot(Slot slot, SlotSemantic semantic) {
      return super.addSlot(slot, semantic);
   }

   @Override
   public void setItem(int slotId, int stateId, @NotNull ItemStack stack) {
      super.setItem(slotId, stateId, stack);
      this.encoding.setItem(slotId, stateId, stack);
   }

   public void updateC2SPanelPos(PanelPosMap.PanelPos pos) {
      if (this.isClientSide()) {
         this.sendClientAction("recordPanelPos", pos);
      }

      this.host.getLogic().updatePanelPos(pos);
      this.broadcastChanges();
   }

   public void updateC2SPanelPos(Panel panel) {
      this.updateC2SPanelPos(new PanelPosMap.PanelPos(panel.getName(), panel.getX(), panel.getY()));
   }

   public void updateC2SPanelSize(PanelSizeMap.PanelSize size) {
      if (this.isClientSide()) {
         this.sendClientAction("recordPanelSize", size);
      }

      this.host.getLogic().updatePanelSize(size);
      this.panelSizeMap = this.host.getLogic().getPanelSizeMap().syncable();
      this.broadcastChanges();
   }

   public void updateC2SPanelSize(Panel panel) {
      this.updateC2SPanelSize(new PanelSizeMap.PanelSize(panel.getName(), panel.getRows(), panel.getColumns()));
   }

   public void receiveS2CPanelUpdate(Panel panel) {
      Point pos = this.panelPosMap.map().getPanelPos(panel.getName());
      if (pos != null) {
         panel.setPosition(new appeng.client.Point(pos.x, pos.y));
      }

      if (this.panelSizeMap != null) {
         PanelSizeMap.PanelSize size = this.panelSizeMap.map().getPanelSize(panel.getName());
         if (size != null) {
            panel.setPanelSize(size.rows, size.columns);
         }
      }
   }

   public void openCategoryMappingSubMenu() {
      if (this.isClientSide()) {
         this.sendClientAction("openCategoryMappingSubMenu");
      } else {
         MenuOpener.open(CategoryMappingSubMenu.TYPE, this.getPlayer(), this.getLocator());
      }
   }

   @Override
   public void broadcastChanges() {
      if (!this.isClientSide()) {
         this.toolboxMenu.tick();
         this.shiftTransferTo = this.host.getConfigManager().getSetting(GTOSettings.ME2IN1_SHIFT_TRANSFER_TO);
         this.sortBy = this.host.getConfigManager().getSetting(Settings.SORT_BY);
         this.sortDir = this.host.getConfigManager().getSetting(Settings.SORT_DIRECTION);
         this.sortDisplay = this.host.getConfigManager().getSetting(Settings.VIEW_MODE);
         this.typeFilter = this.host.getConfigManager().getSetting(Settings.TYPE_FILTER);
         this.panelPosMap = this.host.getLogic().getPanelPosMap().syncable();
         this.panelSizeMap = this.host.getLogic().getPanelSizeMap().syncable();
         super.broadcastChanges();
         this.encoding.broadcastChanges();
      }
   }

   public SeenProviderSlots getVisibleEmptyPatternSlots() {
      return this.slots
         .stream()
         .filter(PatternSlot.class::isInstance)
         .map(PatternSlot.class::cast)
         .filter(slot -> !slot.hasItem() && slot.isActive())
         .collect(SeenProviderSlots::new, SeenProviderSlots::add, SeenProviderSlots::combine);
   }

   @Override
   public void onServerDataSync() {
      super.onServerDataSync();
      this.encoding.onServerDataSync();

      for (Setting<?> set : this.getConfigManager().getSettings()) {
         set.copy(this.getEncoding().getConfigManager(), this.getConfigManager());
      }

      this.encoding.onSettingChanged(this.getConfigManager(), GTOSettings.ME2IN1_SHIFT_TRANSFER_TO);
      this.sendSubmenuAction("clearUsedVisibleSlot");
      this.encoding.hasPower = this.hasPower;
      this.encoding.activeCraftingJobs = this.activeCraftingJobs;
      if (this.posSyncedListener != null) {
         this.posSyncedListener.run();
      }
   }

   @Override
   public void onSlotChange(Slot s) {
      super.onSlotChange(s);
      this.encoding.onSlotChange(s);
   }

   @Override
   protected ItemStack transferStackToMenu(ItemStack input) {
      return this.encoding.transferStackToMenu(input);
   }

   public void registerSubmenuAction(String name, Runnable callback) {
      this.registerClientAction(name, callback);
   }

   public <T> void registerSubmenuAction(String name, Class<T> argClass, Consumer<T> handler) {
      this.registerClientAction(name, argClass, handler);
   }

   public void sendSubmenuAction(String action) {
      this.sendClientAction(action, (Void)null);
   }

   public <T> void sendSubmenuAction(String action, T arg) {
      this.sendClientAction(action, arg);
   }

   @Nullable
   @Override
   public IGridNode getNetworkNode() {
      return this.getHost().getActionableNode();
   }

   @Override
   public InternalInventory getCraftingMatrix() {
      return this.getEncoding().getCraftingMatrix();
   }

   @Override
   public boolean useRealItems() {
      return false;
   }

   @Override
   public List<ItemStack> getViewCells() {
      return this.viewCellSlots.stream().map(AppEngSlot::getItem).collect(Collectors.toList());
   }

   @Override
   public void handleInteraction(long serial, InventoryAction action) {
      this.getEncoding().handleInteraction(serial, action);
   }

   public void doPatternAction(ServerPlayer player, InventoryAction action, int slot, long id) {
      super.doAction(player, action, slot, id);
   }

   @Override
   public void doAction(ServerPlayer player, InventoryAction action, int slot, long id) {
      if (slot < this.slots.size()) {
         if (slot < 0) {
            super.doAction(player, action, -slot, id);
         } else {
            Slot s = this.getSlot(slot);
            if (s instanceof CraftingTermSlot craftingTermSlot) {
               switch (action) {
                  case CRAFT_SHIFT:
                  case CRAFT_ALL:
                  case CRAFT_ITEM:
                  case CRAFT_STACK:
                     craftingTermSlot.doClick(action, player);
               }
            }

            if (s instanceof FakeSlot fakeSlot) {
               this.handleFakeSlotAction(fakeSlot, action);
            } else {
               if (s instanceof AppEngSlot appEngSlot
                  && appEngSlot.getInventory() instanceof ConfigMenuInventory configInv
                  && configInv.getDelegate().getMode() == Mode.STORAGE) {
                  GenericStackInv realInv = configInv.getDelegate();
                  int realInvSlot = appEngSlot.getSlotIndex();
                  if (action == InventoryAction.FILL_ITEM) {
                     AEKey what = realInv.getKey(realInvSlot);
                     this.handleFillingHeldItem((amount, mode) -> realInv.extract(realInvSlot, what, amount, mode), what);
                  } else if (action == InventoryAction.EMPTY_ITEM) {
                     this.handleEmptyHeldItem((whatx, amount, mode) -> realInv.insert(realInvSlot, whatx, amount, mode));
                  }
               }

               if (action == InventoryAction.MOVE_REGION) {
                  SlotSemantic slotSemantic = this.getSlotSemantic(s);
                  if (slotSemantic != null) {
                     for (Slot slotToMove : List.copyOf(this.getSlots(slotSemantic))) {
                        this.quickMoveStack(player, slotToMove.index);
                     }
                  } else {
                     this.quickMoveStack(player, s.index);
                  }
               }

               super.doAction(player, action, slot, id);
            }
         }
      }
   }

   private void handleFakeSlotAction(FakeSlot fakeSlot, InventoryAction action) {
      ItemStack hand = this.getCarried();
      switch (action) {
         case PICKUP_OR_SET_DOWN:
            fakeSlot.increase(hand);
            break;
         case PLACE_SINGLE:
            if (!hand.isEmpty()) {
               ItemStack isx = hand.copy();
               isx.setCount(1);
               fakeSlot.increase(isx);
            }
            break;
         case SPLIT_OR_PLACE_SINGLE:
            ItemStack is = fakeSlot.getItem();
            if (!is.isEmpty()) {
               fakeSlot.decrease(hand);
            } else if (!hand.isEmpty()) {
               is = hand.copy();
               is.setCount(1);
               fakeSlot.set(is);
            }
            break;
         case EMPTY_ITEM:
            EmptyingAction emptyingAction = ContainerItemStrategies.getEmptyingAction(hand);
            if (emptyingAction != null) {
               fakeSlot.set(GenericStack.wrapInItemStack(emptyingAction.what(), emptyingAction.maxAmount()));
            }
      }
   }

   @NotNull
   @Override
   public ItemStack quickMoveStack(Player player, int idx) {
      return super.quickMoveStack(player, idx);
   }

   public void quickMoveToProvider(int slotId) {
      SeenProviderSlots gson = this.getVisibleEmptyPatternSlots();
      this.encoding.quickMoveToProvider(new ExtendedEncodingMenu.SeenProviderSlotsWithSlotId(gson, slotId));
   }

   @Override
   public void gtolib$modifyPatter(Integer value) {
      this.getEncoding().gtolib$modifyPatter(value);
   }

   @Override
   public void gtolib$clearSecOutput() {
      this.getEncoding().gtolib$clearSecOutput();
   }

   @Override
   public void gtolib$addRecipe(String id) {
      this.getEncoding().gtolib$addRecipe(id);
   }

   @Override
   public void gtolib$addUUID(UUID id) {
      this.getEncoding().gtolib$addUUID(id);
   }

   @Override
   public IConfigManager getConfigManager() {
      return this.host.getConfigManager();
   }

   @Nullable
   @Override
   public IClientRepo getClientRepo() {
      return this.getEncoding().getClientRepo();
   }

   public void markPatternChange() {
      this.updatePatterns = true;
   }

   @Generated
   public ExtendedEncodingMenu getEncoding() {
      return this.encoding;
   }

   @Generated
   public IExtendedPatternMenuHost getHost() {
      return this.host;
   }

   @Generated
   public List<RestrictedInputSlot> getViewCellSlots() {
      return this.viewCellSlots;
   }

   @Generated
   public IExtendedGuiEx getScreen() {
      return this.screen;
   }

   @Generated
   public ShiftTransferTo getShiftTransferTo() {
      return this.shiftTransferTo;
   }

   @Generated
   @Override
   public SortOrder getSortBy() {
      return this.sortBy;
   }

   @Generated
   @Override
   public SortDir getSortDir() {
      return this.sortDir;
   }

   @Generated
   @Override
   public ViewItems getSortDisplay() {
      return this.sortDisplay;
   }

   @Generated
   @Override
   public TypeFilter getTypeFilter() {
      return this.typeFilter;
   }

   @Generated
   public PanelMapPosSyncable getPanelPosMap() {
      return this.panelPosMap;
   }

   @Generated
   public PanelMapSizeSyncable getPanelSizeMap() {
      return this.panelSizeMap;
   }

   @Generated
   public ExtendedEncodingMode getMode() {
      return this.mode;
   }

   @Generated
   public boolean isSubstitute() {
      return this.substitute;
   }

   @Generated
   public boolean isSubstituteFluids() {
      return this.substituteFluids;
   }

   @Nullable
   @Generated
   public ResourceLocation getStonecuttingRecipeId() {
      return this.stonecuttingRecipeId;
   }

   @Generated
   public boolean isAutoEncodeRenaming() {
      return this.autoEncodeRenaming;
   }

   @Generated
   public boolean isAutoSearchProviders() {
      return this.autoSearchProviders;
   }

   @Generated
   public boolean isExtraInfoEnabled() {
      return this.extraInfoEnabled;
   }

   @Generated
   public boolean isHasPower() {
      return this.hasPower;
   }

   @Generated
   public int getActiveCraftingJobs() {
      return this.activeCraftingJobs;
   }

   @Generated
   public Runnable getPosSyncedListener() {
      return this.posSyncedListener;
   }

   @Generated
   public ToolboxMenu getToolboxMenu() {
      return this.toolboxMenu;
   }

   @Generated
   public void setScreen(IExtendedGuiEx screen) {
      this.screen = screen;
   }

   @Generated
   public void setShiftTransferTo(ShiftTransferTo shiftTransferTo) {
      this.shiftTransferTo = shiftTransferTo;
   }

   @Generated
   public void setSortBy(SortOrder sortBy) {
      this.sortBy = sortBy;
   }

   @Generated
   public void setSortDir(SortDir sortDir) {
      this.sortDir = sortDir;
   }

   @Generated
   public void setSortDisplay(ViewItems sortDisplay) {
      this.sortDisplay = sortDisplay;
   }

   @Generated
   public void setTypeFilter(TypeFilter typeFilter) {
      this.typeFilter = typeFilter;
   }

   @Generated
   public void setPanelPosMap(PanelMapPosSyncable panelPosMap) {
      this.panelPosMap = panelPosMap;
   }

   @Generated
   public void setPanelSizeMap(PanelMapSizeSyncable panelSizeMap) {
      this.panelSizeMap = panelSizeMap;
   }

   @Generated
   public void setMode(ExtendedEncodingMode mode) {
      this.mode = mode;
   }

   @Generated
   public void setSubstitute(boolean substitute) {
      this.substitute = substitute;
   }

   @Generated
   public void setSubstituteFluids(boolean substituteFluids) {
      this.substituteFluids = substituteFluids;
   }

   @Generated
   public void setStonecuttingRecipeId(@Nullable ResourceLocation stonecuttingRecipeId) {
      this.stonecuttingRecipeId = stonecuttingRecipeId;
   }

   @Generated
   public void setAutoEncodeRenaming(boolean autoEncodeRenaming) {
      this.autoEncodeRenaming = autoEncodeRenaming;
   }

   @Generated
   public void setAutoSearchProviders(boolean autoSearchProviders) {
      this.autoSearchProviders = autoSearchProviders;
   }

   @Generated
   public void setExtraInfoEnabled(boolean extraInfoEnabled) {
      this.extraInfoEnabled = extraInfoEnabled;
   }

   @Generated
   public void setHasPower(boolean hasPower) {
      this.hasPower = hasPower;
   }

   @Generated
   public void setActiveCraftingJobs(int activeCraftingJobs) {
      this.activeCraftingJobs = activeCraftingJobs;
   }

   @Generated
   public void setPosSyncedListener(Runnable posSyncedListener) {
      this.posSyncedListener = posSyncedListener;
   }
}
