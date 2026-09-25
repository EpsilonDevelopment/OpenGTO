package com.gtolib.api.ae2.me2in1;

import appeng.api.behaviors.EmptyingAction;
import appeng.api.config.ActionItems;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.TypeFilter;
import appeng.api.config.ViewItems;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.AEKeyFilter;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.AESubScreen;
import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.Icon;
import appeng.client.gui.me.common.PendingCraftingJobs;
import appeng.client.gui.me.common.PinnedKeys;
import appeng.client.gui.me.common.Repo;
import appeng.client.gui.me.common.RepoSlot;
import appeng.client.gui.me.common.StackSizeRenderer;
import appeng.client.gui.me.common.PinnedKeys.PinInfo;
import appeng.client.gui.me.common.PinnedKeys.PinReason;
import appeng.client.gui.me.patternaccess.PatternContainerRecord;
import appeng.client.gui.me.patternaccess.PatternSlot;
import appeng.client.gui.style.PaletteColor;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AETextField;
import appeng.client.gui.widgets.ActionButton;
import appeng.client.gui.widgets.IconButton;
import appeng.client.gui.widgets.Scrollbar;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.client.gui.widgets.TabButton;
import appeng.client.gui.widgets.ToggleButton;
import appeng.client.gui.widgets.TabButton.Style;
import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.render.SimpleRenderContext;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.InventoryActionPacket;
import appeng.core.sync.packets.SwitchGuisPacket;
import appeng.helpers.InventoryAction;
import appeng.integration.abstraction.ItemListMod;
import appeng.items.storage.ViewCellItem;
import appeng.menu.SlotSemantic;
import appeng.menu.SlotSemantics;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.menu.me.crafting.CraftingStatusMenu;
import appeng.util.inv.AppEngInternalInventory;
import com.glodblock.github.extendedae.client.button.HighlightButton;
import com.glodblock.github.extendedae.client.gui.GuiExPatternTerminal;
import com.glodblock.github.extendedae.client.gui.GuiExPatternTerminal.PatternProviderInfo;
import com.glodblock.github.extendedae.util.FCUtil;
import com.glodblock.github.extendedae.util.MessageUtil;
import com.google.common.collect.ImmutableSet;
import com.gto.datasynclib.util.holder.ObjHolder;
import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import com.gto.fastcollection.fastutil.OpenCacheHashSet;
import com.gtocore.integration.jech.PinYinUtils;
import com.gtolib.ae2.me2in1.panel.ExPatternTerminalPanel;
import com.gtolib.ae2.me2in1.panel.MePanel;
import com.gtolib.ae2.me2in1.panel.PlayerInvPanel;
import com.gtolib.api.ae2.GTOSettings;
import com.gtolib.api.ae2.ShiftTransferTo;
import com.gtolib.api.ae2.gui.hooks.IExtendedGuiEx;
import com.gtolib.api.ae2.me2in1.encoding.PatternEncodingModule;
import com.mojang.blaze3d.vertex.PoseStack;
import gto_ae.client.gui.IconsExtended;
import gto_ae.core.localization.ExtendedLangs;
import gto_ae.hooks.gui.menu.IRepoSlot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.Generated;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.BlockPos;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Me2in1Screen<MENU extends Me2in1Menu> extends GuiExPatternTerminal<MENU> implements IExtendedGuiEx {
   private static final String TEXT_ID_ENTRIES_SHOWN = "entriesShown";
   private final PatternEncodingModule encoding;
   private final ServerSettingToggleButton<ShiftTransferTo> shiftTransferToggle;
   private final SettingToggleButton<ViewItems> viewModeToggle;
   private final SettingToggleButton<TypeFilter> filterTypesToggle;
   private final SettingToggleButton<SortOrder> sortByToggle;
   private final SettingToggleButton<SortDir> sortDirToggle;
   private final ToggleButton manualPinnedRowToggle;
   private final AETextField searchMEStorageField;
   private final AETextField searchProviderField;
   private static String lastPatternSearch = "";
   private RepoSlot[] repoSlots = new RepoSlot[16];
   private final MePanel mePanel;
   private final ExPatternTerminalPanel exPatternTerminalPanel;
   private final PlayerInvPanel playerInvPanel;
   public final TabButton craftingStatusBtn;
   private final Map<String, AbstractWidget> subWidgets = new O2OOpenCacheHashMap<>();
   private ImmutableSet<ItemStack> currentViewCells = ImmutableSet.of();
   public final Repo repo;
   private final ArrayList<PatternContainerGroup> groups = new ArrayList<>();
   private final ArrayList<Me2in1Screen.Row> exPatternTerminalRows = new ArrayList<>();
   private PatternContainerGroup scrollingGroup;
   private long scrollingGroupHoverStart;
   private final ToggleButton searchToggle;
   private static final int GUI_PADDING_X = 22;
   private static final int GUI_PADDING_Y = 6;
   private static final int GUI_HEADER_HEIGHT = 51;
   private static final int PATTERN_PROVIDER_NAME_MARGIN_X = 2;
   private static final int TEXT_MAX_WIDTH = 155;
   private static final int TEXT_SCROLL_PAUSE_MS = 500;
   private static final int TEXT_SCROLL_PIXELS_PER_SECOND = 30;
   private static final int ROW_HEIGHT = 18;
   private static final int SLOT_SIZE = 18;

   public Me2in1Screen(final MENU menu, Inventory playerInventory, Component title, ScreenStyle style) {
      super(menu, playerInventory, title, style);
      menu.getEncoding().setGui((a, b) -> menu.getEncoding().getRepo().updateView());
      Scrollbar meSscrollbar = new Scrollbar(Scrollbar.SMALL);
      meSscrollbar.setCaptureMouseWheel(false);
      this.mePanel = new MePanel(this, meSscrollbar, this::setSearchText, this.repoSlots);
      this.widgets.add("mepanel", this.mePanel);
      this.exPatternTerminalPanel = new ExPatternTerminalPanel(this);
      this.widgets.add("exPatternTerminalPanel", this.exPatternTerminalPanel);
      this.playerInvPanel = new PlayerInvPanel(this);
      this.widgets.add("playerInvPanel", this.playerInvPanel);
      this.repo = new Repo(meSscrollbar, menu);
      menu.getEncoding().setClientRepo(this.repo);
      this.repo.setUpdateViewListener(this.mePanel::updateScrollbar);
      this.repo.setRowSize(this.mePanel.getColumns());
      this.repo.setShowManualPinnedRow(this.config.isShowManualPinnedRow());
      this.searchMEStorageField = this.mePanel.getSearchField();
      this.searchProviderField = this.widgets.addTextField("search_provider");
      this.searchProviderField.setResponder(str -> {
         this.gto$refreshSearch();
         lastPatternSearch = str;
      });
      this.searchProviderField.setPlaceholder(GuiText.SearchPlaceholder.text());
      this.searchProviderField.setTooltipMessage(Collections.singletonList(Component.translatable("gtocore.ae.appeng.me2in1.search_provider")));
      if ((this.getMenu().isReturnedFromSubScreen() || AEConfig.instance().isRememberLastSearch())
         && MePanel.rememberedSearch != null
         && !MePanel.rememberedSearch.isEmpty()) {
         this.searchMEStorageField.setValue(MePanel.rememberedSearch);
         this.searchMEStorageField.selectAll();
         this.setSearchText(MePanel.rememberedSearch);
      }

      this.encoding = new PatternEncodingModule(this);
      this.widgets.add("encodingModule", this.encoding);
      this.shiftTransferToggle = this.addToLeftToolbar(
         new ServerSettingToggleButton<>(GTOSettings.ME2IN1_SHIFT_TRANSFER_TO, ShiftTransferTo.INVENTORY_OR_BUFFER)
      );
      this.sortByToggle = this.mePanel.addToolbarButton(new ServerSettingToggleButton<>(Settings.SORT_BY, menu.getSortBy()));
      this.viewModeToggle = this.mePanel.addToolbarButton(new ServerSettingToggleButton<>(Settings.VIEW_MODE, menu.getSortDisplay()));
      this.filterTypesToggle = this.mePanel.addToolbarButton(new ServerSettingToggleButton<>(Settings.TYPE_FILTER, menu.getTypeFilter()));
      this.sortDirToggle = this.mePanel.addToolbarButton(new ServerSettingToggleButton<>(Settings.SORT_DIRECTION, menu.getSortDir()));
      this.manualPinnedRowToggle = this.mePanel
         .addToolbarButton(new ToggleButton(IconsExtended.MANUAL_PIN, IconsExtended.MANUAL_PIN_OFF, this::toggleManualPinnedRowVisibility));
      this.manualPinnedRowToggle.setTooltipOn(List.of(ExtendedLangs.TerminalManualPinnedRow.text(), ExtendedLangs.TerminalManualPinnedRowShown.text()));
      this.manualPinnedRowToggle.setTooltipOff(List.of(ExtendedLangs.TerminalManualPinnedRow.text(), ExtendedLangs.TerminalManualPinnedRowHidden.text()));
      this.mePanel.addToolbarButton(new ActionButton(ActionItems.TERMINAL_SETTINGS, () -> this.switchToScreen(new TerminalSettingsScreen<>(this))));
      this.mePanel
         .addToolbarButton(
            new ToggleButton(
               Icon.OVERLAY_ON,
               Icon.OVERLAY_OFF,
               Component.translatable("gtocore.ae.appeng.me2in1.collapse_or_expand_toolbar"),
               Component.translatable("gtocore.ae.appeng.me2in1.collapse_or_expand_toolbar.desc"),
               b -> MePanel.setCollapsedToolbar(!MePanel.isCollapsedToolbar())
            )
         );
      menu.setPosSyncedListener(() -> {
         this.applyInitialPanelState();
         menu.setPosSyncedListener(null);
      });
      this.craftingStatusBtn = new TabButton(Icon.CRAFT_HAMMER, GuiText.CraftingStatus.text(), btn -> this.showCraftingStatus());
      this.craftingStatusBtn.setStyle(Style.CORNER);
      this.getSubWidgets().put("craftingStatus", this.craftingStatusBtn);
      this.searchToggle = this.addToLeftToolbar(new ToggleButton(Icon.SEARCH_AUTO_FOCUS, Icon.SEARCH_DEFAULT, menu.getEncoding()::setAutoSearchProviders) {
         @Override
         public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 2 && this.isMouseOver(mouseX, mouseY)) {
               menu.openCategoryMappingSubMenu();
               return true;
            } else {
               return super.mouseClicked(mouseX, mouseY, button);
            }
         }
      });
      this.searchToggle
         .setTooltipOn(
            List.of(
               Component.translatable("gtocore.ae.appeng.me2in1.auto_search"),
               Component.translatable("gtocore.ae.appeng.me2in1.auto_search.on"),
               Component.translatable("gtocore.ae.appeng.me2in1.auto_search.config")
            )
         );
      this.searchToggle
         .setTooltipOff(
            List.of(
               Component.translatable("gtocore.ae.appeng.me2in1.auto_search"),
               Component.translatable("gtocore.ae.appeng.me2in1.auto_search.off"),
               Component.translatable("gtocore.ae.appeng.me2in1.auto_search.config")
            )
         );
      this.addToLeftToolbar(
         new IconButton(b -> this.resetAllPanels()) {
            @Override
            protected Icon getIcon() {
               return Icon.SCHEDULING_DEFAULT;
            }

            @Override
            public List<Component> getTooltipMessage() {
               return List.of(
                  Component.translatable("gtocore.ae.appeng.me2in1.reset_panel_position"),
                  Component.translatable("gtocore.ae.appeng.me2in1.reset_panel_position.1")
               );
            }
         }
      );
      if ((menu.isReturnedFromSubScreen() || this.config.isRememberLastSearch()) && !lastPatternSearch.isBlank()) {
         this.searchProviderField.setValue(lastPatternSearch);
      }

      menu.setScreen(this);
   }

   private void toggleManualPinnedRowVisibility(boolean state) {
      this.config.setShowManualPinnedRow(state);
      this.repo.setShowManualPinnedRow(state);
      this.repo.rebuildView();
      this.mePanel.updateScrollbar();
   }

   private void showCraftingStatus() {
      NetworkHandler.instance().sendToServer(SwitchGuisPacket.openSubMenu(CraftingStatusMenu.TYPE));
   }

   @Override
   public void onClose() {
      super.onClose();
      this.encoding.onClose();
   }

   @NotNull
   @Override
   public List<Component> getTooltipFromContainerItem(@NotNull ItemStack stack) {
      ArrayList<Component> list = new ArrayList<>(super.getTooltipFromContainerItem(stack));
      list.addAll(this.encoding.getTooltipFromContainerItem(stack));
      return list;
   }

   protected final boolean isViewOnlyCraftable() {
      return this.viewModeToggle != null && this.viewModeToggle.getCurrentValue() == ViewItems.CRAFTABLE;
   }

   @Override
   public void renderSlot(GuiGraphics guiGraphics, Slot s) {
      if (s instanceof IRepoSlot repoSlot) {
         repoSlot.renderSlot(guiGraphics, this.repo, this.config.isUseLargeFonts(), this.isViewOnlyCraftable());
      } else {
         super.renderSlot(guiGraphics, s);
         if (this.shouldShowCraftableIndicatorForSlot(s)) {
            PoseStack poseStack = guiGraphics.pose();
            poseStack.pushPose();
            poseStack.translate(0.0F, 0.0F, 100.0F);
            StackSizeRenderer.renderSizeLabel(guiGraphics, this.font, s.x - 11, s.y - 11, "+", false);
            poseStack.popPose();
         }
      }
   }

   @Override
   protected void renderTooltip(@NotNull GuiGraphics guiGraphics, int x, int y) {
      if (this.hoveredSlot instanceof IRepoSlot repoSlot) {
         repoSlot.renderTooltip(this.menu.getEncoding(), guiGraphics, x, y, this::drawTooltip, (g, x1, y1, lines) -> {
            if (repoSlot.getEntry() != null) {
               this.renderKeyTooltipThroughItemAPI(g, repoSlot.getEntry().getWhat(), x1, y1, lines);
            }
         }, this.isViewOnlyCraftable());
      } else if (!this.renderexPatternTerminalPanelTooltip(guiGraphics, x, y)) {
         if (this.hoveredSlot != null) {
            super.renderTooltip(guiGraphics, x, y);
         }
      }
   }

   public boolean shouldShowCraftableIndicatorForSlot(Slot s) {
      SlotSemantic semantic = this.getMenu().getSlotSemantic(s);
      if (semantic != SlotSemantics.CRAFTING_GRID
         && semantic != SlotSemantics.PROCESSING_INPUTS
         && semantic != SlotSemantics.SMITHING_TABLE_ADDITION
         && semantic != SlotSemantics.SMITHING_TABLE_BASE
         && semantic != SlotSemantics.SMITHING_TABLE_TEMPLATE
         && semantic != SlotSemantics.STONECUTTING_INPUT) {
         return false;
      }

      GenericStack slotContent = GenericStack.fromItemStack(s.getItem());
      return slotContent == null ? false : this.repo.isCraftable(slotContent.what());
   }

   @Override
   protected EmptyingAction getEmptyingAction(Slot slot, ItemStack carried) {
      return Optional.ofNullable(this.encoding.getEmptyingAction(slot, carried)).orElse(super.getEmptyingAction(slot, carried));
   }

   public void addSubWidgetToWC(String id, AbstractWidget widget, Map<String, AbstractWidget> subWidgets) {
      if (widget.isFocused()) {
         widget.setFocused(false);
      }

      widget.setX(widget.getX() + this.leftPos);
      widget.setY(widget.getY() + this.topPos);
      subWidgets.put(id, widget);
      this.widgets.getWidgets().put(id, widget);
      if (widget instanceof ICompositeWidget compositeWidget) {
         this.widgets.getCompositeWidgets().put(id, compositeWidget);
      }

      this.addRenderableWidget(widget);
   }

   public void removeSubWidgetFromWC(String widgetId) {
      AbstractWidget widget = this.widgets.getWidgets().remove(widgetId);
      this.widgets.getCompositeWidgets().remove(widgetId);
      if (widget != null) {
         this.removeWidget(widget);
      }
   }

   private void applyInitialPanelState() {
      this.menu.receiveS2CPanelUpdate(this.mePanel);
      this.applyMePanelGridSize(this.mePanel.getRows(), this.mePanel.getColumns());
      this.menu.receiveS2CPanelUpdate(this.exPatternTerminalPanel);
      this.menu.receiveS2CPanelUpdate(this.playerInvPanel);
      this.encoding.syncPanelStateFromMenu();
   }

   private void resetAllPanels() {
      this.mePanel.resetPosition();
      this.mePanel.resetSize();
      this.exPatternTerminalPanel.resetPosition();
      this.exPatternTerminalPanel.resetSize();
      this.playerInvPanel.resetPosition();
      this.playerInvPanel.resetSize();
      this.encoding.resetPanels();
   }

   public void applyMePanelGridSize(int rows, int columns) {
      boolean changed = this.mePanel.setPanelSize(rows, columns);
      this.repo.setRowSize(this.mePanel.getColumns());
      boolean rebuiltSlots = this.repoSlots.length != this.mePanel.getVisibleRepoSlotCount();
      if (rebuiltSlots) {
         this.rebuildRepoSlots();
      } else {
         this.mePanel.setRepoSlots(this.repoSlots);
      }

      if (changed || rebuiltSlots) {
         this.repo.rebuildView();
      }

      this.mePanel.updateScrollbar();
   }

   private void rebuildRepoSlots() {
      List<Slot> slots = this.menu.slots;
      slots.removeIf(slotx -> slotx instanceof RepoSlot);
      this.repoSlots = new RepoSlot[this.mePanel.getVisibleRepoSlotCount()];

      for (int i = 0; i < this.repoSlots.length; i++) {
         RepoSlot slot = new RepoSlot(this.repo, i, 0, 0);
         slots.add(slot);
         this.repoSlots[i] = slot;
      }

      this.mePanel.setRepoSlots(this.repoSlots);
      if (this.hoveredSlot instanceof RepoSlot) {
         this.hoveredSlot = null;
      }
   }

   @Override
   public void init() {
      this.detachSubWidgetsFromWidgetContainer();
      this.rebuildRepoSlots();
      super.init();
      this.mePanel.updateScrollbar();
      this.attachSubWidgetsToWidgetContainer();
   }

   private void detachSubWidgetsFromWidgetContainer() {
      for (String widgetId : new ArrayList<>(this.subWidgets.keySet())) {
         this.removeSubWidgetFromWC(widgetId);
      }
   }

   private void attachSubWidgetsToWidgetContainer() {
      this.subWidgets.forEach((key, item) -> this.addSubWidgetToWC(key, item, this.widgets.getWidgets()));
   }

   private void updateToggles() {
      this.shiftTransferToggle.set(this.menu.getShiftTransferTo());
      this.sortByToggle.set(this.menu.getSortBy());
      this.viewModeToggle.set(this.menu.getSortDisplay());
      this.filterTypesToggle.set(this.menu.getTypeFilter());
      this.sortDirToggle.set(this.menu.getSortDir());
      this.searchToggle.setState(this.menu.autoSearchProviders);
   }

   @Override
   public void updateBeforeRender() {
      super.updateBeforeRender();
      this.updateToggles();
      this.repo.setPaused(hasShiftDown());
      this.manualPinnedRowToggle.setState(this.repo.isShowManualPinnedRow());
      this.updateSearch();
      this.setTextHidden("player_inventory_title", true);
   }

   private void setSearchText(String text) {
      this.repo.setSearchString(text);
      this.repo.updateView();
      this.mePanel.updateScrollbar();
   }

   private void handleAltPinnedInteraction(RepoSlot repoSlot) {
      GridInventoryEntry entry = repoSlot.getEntry();
      if (entry != null && entry.getWhat() != null) {
         PinReason pinnedRowReason = this.repo.getPinnedRowReason(repoSlot.getRepoViewIndex());
         if (pinnedRowReason == PinReason.MANUAL) {
            PinnedKeys.unpin(entry.getWhat());
         } else {
            if (pinnedRowReason != null) {
               return;
            }

            PinnedKeys.pinKey(entry.getWhat(), PinReason.MANUAL);
         }

         this.repo.rebuildView();
         this.mePanel.updateScrollbar();
      }
   }

   @Override
   public boolean mouseClicked(double xCoord, double yCoord, int btn) {
      if (this.searchMEStorageField.isMouseOver(xCoord, yCoord) && btn == 1) {
         this.searchMEStorageField.setValue("");
         this.setSearchText("");
      }

      if (this.searchProviderField.isMouseOver(xCoord, yCoord) && btn == 1) {
         this.searchProviderField.setValue("");
         this.gto$refreshSearch();
         lastPatternSearch = "";
      }

      if (hasAltDown() && this.findSlot(xCoord, yCoord) instanceof RepoSlot repoSlot) {
         this.handleAltPinnedInteraction(repoSlot);
         return true;
      }

      if (Minecraft.getInstance().options.keyPickItem.matchesMouse(btn)) {
         Slot slot = this.findSlot(xCoord, yCoord);
         if (this.getMenu().getEncoding().canModifyAmountForSlot(slot)) {
            GenericStack currentStack = GenericStack.fromItemStack(slot.getItem());
            if (slot instanceof IRepoSlot repoSlot && repoSlot.isCraftable()) {
               repoSlot.handleGridInventoryEntryMouseClick(this.menu.getEncoding(), btn, ClickType.CLONE, this.shouldCraftOnClick(repoSlot.getEntry()));
               return true;
            }

            if (currentStack != null) {
               SetProcessingPatternAmountScreen<MENU> screen = new SetProcessingPatternAmountScreen<>(
                  this,
                  currentStack,
                  newStack -> NetworkHandler.instance()
                     .sendToServer(new InventoryActionPacket(InventoryAction.SET_FILTER, slot.index, GenericStack.wrapInItemStack(newStack)))
               );
               this.switchToScreen(screen);
               return true;
            }
         }
      }

      return super.mouseClicked(xCoord, yCoord, btn);
   }

   private boolean shouldHandleTransferToProvider(Slot slot, int slotIdx, int mouseButton, ClickType clickType) {
      return !(slot instanceof PatternSlot)
         && clickType == ClickType.QUICK_MOVE
         && PatternDetailsHelper.isEncodedPattern(slot.getItem())
         && this.menu.getShiftTransferTo() == ShiftTransferTo.CURRENTLY_VISIBLE_ACCESSOR
         && mouseButton == 0;
   }

   private boolean shouldCraftOnClick(@Nullable GridInventoryEntry entry) {
      if (this.isViewOnlyCraftable()) {
         return true;
      } else {
         return entry == null ? false : entry.getStoredAmount() == 0L && entry.isCraftable();
      }
   }

   @Override
   public boolean mouseScrolled(double x, double y, double wheelDelta) {
      if (wheelDelta != 0.0 && hasShiftDown() && this.findSlot(x, y) instanceof IRepoSlot repoSlot) {
         repoSlot.mouseScrolled(this.menu.getEncoding(), wheelDelta);
         return true;
      } else {
         return super.mouseScrolled(x, y, wheelDelta);
      }
   }

   @Override
   protected void slotClicked(Slot slot, int slotIdx, int mouseButton, ClickType clickType) {
      if (slot != null) {
         if (slot instanceof IRepoSlot repoSlot) {
            repoSlot.handleGridInventoryEntryMouseClick(this.menu.getEncoding(), mouseButton, clickType, this.shouldCraftOnClick(repoSlot.getEntry()));
         } else if (this.menu.getSlotSemantic(slot) != SlotSemantics.PLAYER_INVENTORY || !this.menu.isPlayerInventorySlotLocked(slotIdx)) {
            if (this.shouldHandleTransferToProvider(slot, slotIdx, mouseButton, clickType)) {
               this.menu.quickMoveToProvider(slotIdx);
            } else {
               if (slot instanceof PatternSlot machineSlot) {
                  InventoryAction action = null;
                  switch (clickType) {
                     case PICKUP:
                        action = mouseButton == 1 ? InventoryAction.SPLIT_OR_PLACE_SINGLE : InventoryAction.PICKUP_OR_SET_DOWN;
                        break;
                     case QUICK_MOVE:
                        action = mouseButton == 1 ? InventoryAction.PICKUP_SINGLE : InventoryAction.SHIFT_CLICK;
                        break;
                     case CLONE:
                        if (this.getPlayer().getAbilities().instabuild) {
                           action = InventoryAction.CREATIVE_DUPLICATE;
                        }
                     case THROW:
                  }

                  if (action != null) {
                     InventoryActionPacket p = new InventoryActionPacket(action, -machineSlot.getSlotIndex(), machineSlot.getMachineInv().getServerId());
                     NetworkHandler.instance().sendToServer(p);
                  }
               } else {
                  super.slotClicked(slot, slotIdx, mouseButton, clickType);
               }
            }
         }
      }
   }

   @Override
   public void containerTick() {
      this.repo.setPower(this.menu.hasPower);
      super.containerTick();
      ImmutableSet<ItemStack> viewCells = ImmutableSet.copyOf(this.menu.getViewCells());
      if (!this.currentViewCells.equals(viewCells)) {
         this.currentViewCells = viewCells;
         this.repo.setPartitionList(ViewCellItem.createFilter(AEKeyFilter.none(), viewCells));
      }
   }

   @Override
   public void drawBG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY, float partialTicks) {
      if (this.searchMEStorageField != null) {
         this.searchMEStorageField.render(guiGraphics, mouseX, mouseY, partialTicks);
      }
   }

   @Override
   public void drawFG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY) {
      if (this.menu.getEncoding().activeCraftingJobs != -1 && !this.exPatternTerminalPanel.isCollapsed()) {
         int x = this.craftingStatusBtn.getX() + (this.craftingStatusBtn.getWidth() - 16) / 2;
         int y = this.craftingStatusBtn.getY() + (this.craftingStatusBtn.getHeight() - 16) / 2;
         StackSizeRenderer.renderSizeLabel(
            guiGraphics, this.font, x - this.leftPos, y - this.topPos, String.valueOf(this.menu.getEncoding().activeCraftingJobs)
         );
      }

      if (this.repo.hasPinnedRow()) {
         this.renderPinnedRowDecorations(guiGraphics);
      }

      this.getOrCreateTextOverride("dialog_title").setHidden(true);
   }

   public Component getDialogTitle() {
      Component override = this.getOrCreateTextOverride("dialog_title").getContent();
      return override != null ? override : this.style.getText().get("dialog_title").getText();
   }

   private void renderPinnedRowDecorations(GuiGraphics guiGraphics) {
      for (Slot slot : this.menu.slots) {
         if (slot instanceof IRepoSlot repoSlot) {
            repoSlot.renderDecoration(guiGraphics);
         }
      }
   }

   public void storeState() {
      MePanel.rememberedSearch = this.searchMEStorageField.getValue();
   }

   @Override
   public boolean keyPressed(int keyCode, int scanCode, int p_keyPressed_3_) {
      if (this.searchMEStorageField.isFocused() && keyCode == 257) {
         this.searchMEStorageField.setFocused(false);
         this.setFocused(null);
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, p_keyPressed_3_);
      }
   }

   @Override
   public boolean charTyped(char character, int modifiers) {
      return character == ' ' && this.searchMEStorageField.getValue().isEmpty() ? true : super.charTyped(character, modifiers);
   }

   private void updateSearch() {
      if (this.mePanel.isCollapsed()) {
         this.searchMEStorageField.setVisible(false);
         this.setTextHidden("entriesShown", true);
      } else {
         if (this.config.isUseExternalSearch()) {
            String externalSearchText = ItemListMod.getSearchText();
            if (!Objects.equals(this.repo.getSearchString(), externalSearchText)) {
               this.setSearchText(externalSearchText);
            }

            int allEntries = this.repo.getAllEntries().size() + this.repo.getSyntheticPinnedEntryCount();
            int visibleEntries = this.repo.size();
            if (allEntries != visibleEntries) {
               this.setTextHidden("entriesShown", false);
               this.setTextContent("entriesShown", GuiText.ShowingOf.text(visibleEntries, allEntries));
            } else {
               this.setTextHidden("entriesShown", true);
            }
         } else {
            this.searchMEStorageField.setVisible(true);
            this.setTextHidden("entriesShown", true);
            this.searchMEStorageField
               .setTooltipMessage(
                  List.of(
                     GuiText.SearchTooltip.text(),
                     GuiText.SearchTooltipModId.text(),
                     GuiText.SearchTooltipTag.text(),
                     GuiText.SearchTooltipToolTips.text(),
                     GuiText.SearchTooltipItemId.text()
                  )
               );
            if (this.config.isSyncWithExternalSearch()) {
               if (this.searchMEStorageField.isFocused()) {
                  ItemListMod.setSearchText(this.searchMEStorageField.getValue());
               } else if (ItemListMod.hasSearchFocus()) {
                  String externalSearchText = ItemListMod.getSearchText();
                  if (!Objects.equals(externalSearchText, this.searchMEStorageField.getValue())) {
                     this.searchMEStorageField.setValue(externalSearchText);
                  }
               }
            }
         }
      }
   }

   @Override
   protected <P extends AEBaseScreen<MENU>> void onReturnFromSubScreen(AESubScreen<MENU, P> subScreen) {
      super.onReturnFromSubScreen(subScreen);
      if (!this.config.isUseExternalSearch()) {
         this.setSearchText(this.searchMEStorageField.getValue());
      }
   }

   @Override
   public void removed() {
      this.mePanel.resetResizeCursor();
      this.exPatternTerminalPanel.resetResizeCursor();
      super.removed();
      this.storeState();

      for (GridInventoryEntry entry : this.repo.getPinnedEntries(PinReason.CRAFTING)) {
         PinInfo info = PinnedKeys.getPinInfo(entry.getWhat());
         if (info != null && info.reason == PinReason.CRAFTING && !PendingCraftingJobs.hasPendingJob(entry.getWhat())) {
            info.canPrune = true;
         }
      }
   }

   @Override
   public AETextField gto$getSearchProviderField() {
      return this.searchProviderField;
   }

   @Override
   public void gto$refreshSearch() {
      if (this.gto$getSearchProviderField() != null) {
         this.gto$getByGroup().clear();
         this.gto$getHighlisghtsButtons().forEach((k, v) -> this.removeWidget(v));
         this.gto$getHighlisghtsButtons().clear();
         this.gto$matchedStack().clear();
         this.gto$matchedProvider().clear();
         String outputFilter = this.gto$searchOutField().getValue().toLowerCase().trim();
         String inputFilter = this.gto$searchInField().getValue().toLowerCase().trim();
         List<String> outputFilters = FCUtil.tokenize(outputFilter);
         List<String> inputFilters = FCUtil.tokenize(inputFilter);
         String patternFilter = this.gto$getSearchProviderField().getValue().toLowerCase();
         Set<Object> cachedSearch = this.getCacheForSearchTerm("out:" + outputFilter + "in:" + inputFilter + "pat:" + patternFilter);
         boolean rebuild = cachedSearch.isEmpty();

         for (PatternContainerRecord entry : this.gto$getById().values()) {
            if (rebuild || cachedSearch.contains(entry)) {
               boolean skipSearch = outputFilter.isEmpty() && inputFilter.isEmpty();
               boolean found = skipSearch && patternFilter.isEmpty();
               boolean match = PinYinUtils.match(entry.getSearchName(), patternFilter);
               if (!skipSearch && match) {
                  for (ItemStack itemStack : entry.getInventory()) {
                     boolean midRes;
                     if (!outputFilter.isEmpty()) {
                        midRes = this.gto$itemStackMatchesSearchTerm(itemStack, outputFilters, true);
                     } else {
                        midRes = true;
                     }

                     if (!inputFilter.isEmpty() && midRes) {
                        midRes = this.gto$itemStackMatchesSearchTerm(itemStack, inputFilters, false);
                     }

                     if (midRes) {
                        found = true;
                     }
                  }
               }

               if (found || match && skipSearch) {
                  this.gto$getByGroup().put(entry.getGroup(), entry);
                  cachedSearch.add(entry);
                  if (match) {
                     this.gto$matchedProvider().add(entry);
                  }
               } else {
                  cachedSearch.remove(entry);
               }
            }
         }

         this.groups.clear();
         this.groups.addAll(this.gto$getByGroup().keySet());
         this.exPatternTerminalRows.clear();
         this.exPatternTerminalRows.ensureCapacity(this.getExPatternTerminalRowCapacity());

         label81:
         for (PatternContainerGroup group : this.groups) {
            this.exPatternTerminalRows.add(new Me2in1Screen.GroupHeaderRow(group));
            ArrayList<PatternContainerRecord> containers = new ArrayList<>(this.gto$getByGroup().get(group));
            Collections.sort(containers);
            Iterator var20 = containers.iterator();

            while (true) {
               PatternContainerRecord container;
               AppEngInternalInventory inventory;
               while (true) {
                  if (!var20.hasNext()) {
                     continue label81;
                  }

                  container = (PatternContainerRecord)var20.next();
                  inventory = container.getInventory();
                  if (inventory.size() <= 0) {
                     break;
                  }

                  PatternProviderInfo info = this.gto$infoMap().get(container.getServerId());
                  if (info != null) {
                     HighlightButton btn = new HighlightButton();
                     btn.setMultiplier(this.playerToBlockDis(info.pos()));
                     btn.setTarget(info.pos(), info.face(), info.world());
                     btn.setSuccessJob(
                        () -> {
                           if (this.getPlayer() != null && info.pos() != null && info.world() != null) {
                              Component message = MessageUtil.createEnhancedHighlightMessage(
                                 this.getPlayer(), info.pos(), info.world(), "chat.ex_pattern_access_terminal.pos"
                              );
                              this.getPlayer().displayClientMessage(message, false);
                           }
                        }
                     );
                     btn.setTooltip(Tooltip.create(Component.translatable("gui.expatternprovider.ex_pattern_access_terminal.tooltip.03")));
                     btn.setVisibility(false);
                     this.gto$getHighlisghtsButtons().put(this.getExPatternTerminalRowCount(), this.addRenderableWidget(btn));
                     break;
                  }
               }

               for (int offset = 0; offset < inventory.size(); offset += this.exPatternTerminalPanel.getColumns()) {
                  int slots = Math.min(inventory.size() - offset, this.exPatternTerminalPanel.getColumns());
                  Me2in1Screen.SlotsRow containerRow = new Me2in1Screen.SlotsRow(container, offset, slots);
                  this.exPatternTerminalRows.add(containerRow);
               }
            }
         }

         this.exPatternTerminalPanel.resetScrollbar();
      }
   }

   public void gto$renderExPaT_FG(GuiGraphics guiGraphics, int left, int top, int mouseX, int mouseY) {
      this.regroupRows();
      int textColor = this.style.getColor(PaletteColor.DEFAULT_TEXT_COLOR).toARGB();
      int relLeft = left - this.leftPos;
      int relTop = top - this.topPos;
      int scrollLevel = this.exPatternTerminalPanel.getScrollbar().getCurrentScroll();
      boolean scrollingGroupHovered = false;

      for (int i = 0; i < this.exPatternTerminalPanel.getRows(); i++) {
         if (scrollLevel + i < this.getExPatternTerminalRowCount()) {
            Me2in1Screen.Row row = this.getExPatternTerminalRow(scrollLevel + i);
            if (this.gto$getHighlisghtsButtons().containsKey(scrollLevel + i)) {
               HighlightButton btn = this.gto$getHighlisghtsButtons().get(scrollLevel + i);
               btn.setPosition(left + 22 - 18, top + (i + 1) * 18 + 34);
               btn.setVisibility(true);
            }

            if (!(row instanceof Me2in1Screen.SlotsRow(PatternContainerRecord container, int offset, int slots))) {
               if (row instanceof Me2in1Screen.GroupHeaderRow(PatternContainerGroup group)) {
                  if (group.icon() != null) {
                     SimpleRenderContext renderContext = new SimpleRenderContext(LytRect.empty(), guiGraphics);
                     renderContext.renderItem(group.icon().toStack(), relLeft + 22 + 2, relTop + 6 + 51 + i * 18, 8.0F, 8.0F);
                  }

                  int rows = this.gto$getByGroup().get(group).size();
                  FormattedText displayName;
                  if (rows > 1) {
                     displayName = Component.empty().append(group.name()).append(Component.literal(" (" + rows + ")"));
                  } else {
                     displayName = group.name();
                  }

                  int textX = relLeft + 22 + 2 + 10;
                  int textY = relTop + 6 + 51 + i * 18;
                  int textWidth = 145;
                  int fullTextWidth = this.font.width(displayName);
                  boolean hovered = this.getHoveredLineIndex(mouseX, mouseY) == scrollLevel + i;
                  if (fullTextWidth > textWidth && hovered) {
                     scrollingGroupHovered = true;
                     if (!group.equals(this.scrollingGroup)) {
                        this.scrollingGroup = group;
                        this.scrollingGroupHoverStart = Util.getMillis();
                     }

                     int scrollOffset = this.getTextScrollOffset(fullTextWidth - textWidth);
                     guiGraphics.enableScissor(left + 22 + 2 + 10, top + 6 + 51 + i * 18, left + 22 + 2 + 10 + textWidth, top + 6 + 51 + i * 18 + 9);
                     guiGraphics.drawString(this.font, Language.getInstance().getVisualOrder(displayName), textX - scrollOffset, textY, textColor, false);
                     guiGraphics.disableScissor();
                  } else {
                     FormattedCharSequence text = Language.getInstance().getVisualOrder(this.font.substrByWidth(displayName, textWidth));
                     guiGraphics.drawString(this.font, text, textX, textY, textColor, false);
                  }
               }
            } else {
               for (int col = 0; col < slots; col++) {
                  PatternSlot slot = new PatternSlot(container, offset + col, relLeft + col * 18 + 22, relTop + (i + 1) * 18 + 34);
                  this.menu.slots.add(slot);
                  if (!this.gto$searchOutField().getValue().isEmpty() || !this.gto$searchOutField().getValue().isEmpty()) {
                     if (this.gto$matchedStack().contains(slot.getItem())) {
                        this.fillRect(guiGraphics, new Rect2i(slot.x, slot.y, 16, 16), -1979646208);
                     } else if (!this.gto$matchedProvider().contains(container)) {
                        this.fillRect(guiGraphics, new Rect2i(slot.x, slot.y, 16, 16), 1778384896);
                     }
                  }
               }
            }
         }
      }

      if (!scrollingGroupHovered) {
         this.scrollingGroup = null;
      }
   }

   private int getTextScrollOffset(int maxOffset) {
      long travelTime = Math.max(1L, maxOffset * 1000L / 30L);
      long cycleTime = 1000L + travelTime * 2L;
      long elapsed = (Util.getMillis() - this.scrollingGroupHoverStart) % cycleTime;
      if (elapsed < 500L) {
         return 0;
      }

      long var8 = elapsed - 500L;
      if (var8 < travelTime) {
         return (int)(maxOffset * var8 / travelTime);
      }

      elapsed = var8 - travelTime;
      if (elapsed < 500L) {
         return maxOffset;
      }

      elapsed -= 500L;
      return maxOffset - (int)(maxOffset * elapsed / travelTime);
   }

   private Set<Object> getCacheForSearchTerm(String searchTerm) {
      Set<Object> cache = this.gto$cachedSearches().computeIfAbsent(searchTerm, k -> new OpenCacheHashSet<>());
      if (cache.isEmpty() && searchTerm.length() > 1) {
         cache.addAll(this.getCacheForSearchTerm(searchTerm.substring(0, searchTerm.length() - 1)));
      }

      return cache;
   }

   private void regroupRows() {
      ArrayList<Me2in1Screen.Row> newRows = new ArrayList<>();
      ArrayList<Me2in1Screen.SlotsRow> slotRowChunk = new ArrayList<>();
      int[] slotRowTotal = new int[]{0};
      ObjHolder<PatternContainerRecord> ref = new ObjHolder<>();
      Runnable chunkCall = () -> {
         if (!slotRowChunk.isEmpty()) {
            int total = slotRowTotal[0];

            while (slotRowTotal[0] > 0) {
               newRows.add(new Me2in1Screen.SlotsRow(ref.value, total - slotRowTotal[0], Math.min(slotRowTotal[0], this.exPatternTerminalPanel.getColumns())));
               slotRowTotal[0] -= this.exPatternTerminalPanel.getColumns();
            }

            slotRowChunk.clear();
            slotRowTotal[0] = 0;
         }
      };

      for (Me2in1Screen.Row row : this.exPatternTerminalRows) {
         if (row instanceof Me2in1Screen.GroupHeaderRow) {
            chunkCall.run();
            newRows.add(row);
         } else if (row instanceof Me2in1Screen.SlotsRow s) {
            if (ref.value != s.container()) {
               chunkCall.run();
            }

            slotRowChunk.add(s);
            slotRowTotal[0] += s.slots();
            ref.value = s.container();
         }
      }

      chunkCall.run();
      this.exPatternTerminalRows.clear();
      this.exPatternTerminalRows.addAll(newRows);
      this.exPatternTerminalPanel.resetScrollbar();
   }

   private boolean renderexPatternTerminalPanelTooltip(@NotNull GuiGraphics guiGraphics, int x, int y) {
      if (this.exPatternTerminalPanel.isCollapsed()) {
         return false;
      }

      if (this.hoveredSlot == null) {
         int hoveredLineIndex = this.getHoveredLineIndex(x, y);
         if (hoveredLineIndex != -1
            && this.getExPatternTerminalRow(hoveredLineIndex) instanceof Me2in1Screen.GroupHeaderRow(PatternContainerGroup group)
            && !group.tooltip().isEmpty()) {
            guiGraphics.renderTooltip(this.font, group.tooltip(), Optional.empty(), x, y);
            return true;
         }
      }

      return false;
   }

   private int getHoveredLineIndex(int x, int y) {
      x = x - this.exPatternTerminalPanel.getX() - 22;
      int var5 = y - this.exPatternTerminalPanel.getY() - 51;
      if (x >= 0 && var5 >= 0) {
         if (x < 18 * this.exPatternTerminalPanel.getColumns() && var5 < this.exPatternTerminalPanel.getRows() * 18) {
            int rowIndex = this.exPatternTerminalPanel.getScrollbar().getCurrentScroll() + var5 / 18;
            return rowIndex >= 0 && rowIndex < this.getExPatternTerminalRowCount() ? rowIndex : -1;
         } else {
            return -1;
         }
      } else {
         return -1;
      }
   }

   private double playerToBlockDis(BlockPos pos) {
      if (pos == null) {
         return 0.0;
      }

      BlockPos ps = this.getPlayer().getOnPos();
      return pos.distSqr(ps);
   }

   private int getExPatternTerminalRowCapacity() {
      return this.groups.size() + this.gto$getById().size() + this.exPatternTerminalRows.size();
   }

   public int getExPatternTerminalRowCount() {
      return this.exPatternTerminalRows.size();
   }

   public Me2in1Screen.Row getExPatternTerminalRow(int index) {
      return this.exPatternTerminalRows.get(index);
   }

   @Generated
   public AETextField getSearchProviderField() {
      return this.searchProviderField;
   }

   @Generated
   public Map<String, AbstractWidget> getSubWidgets() {
      return this.subWidgets;
   }

   public record GroupHeaderRow(PatternContainerGroup group) implements Me2in1Screen.Row {
   }

   public sealed interface Row permits Me2in1Screen.GroupHeaderRow, Me2in1Screen.SlotsRow {
   }

   public record SlotsRow(PatternContainerRecord container, int offset, int slots) implements Me2in1Screen.Row {
   }
}
