package com.gtolib.ae2.me2in1.panel;

import appeng.client.Point;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Tooltip;
import appeng.client.gui.me.common.Repo;
import appeng.client.gui.me.common.RepoSlot;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.StyleManager;
import appeng.client.gui.widgets.AETextField;
import appeng.client.gui.widgets.Scrollbar;
import appeng.client.gui.widgets.UpgradesPanel;
import appeng.client.gui.widgets.VerticalButtonBar;
import appeng.core.localization.GuiText;
import appeng.menu.SlotSemantics;
import com.gtolib.api.ae2.gui.BlitterHelper;
import com.gtolib.api.ae2.me2in1.ME2in1Helper;
import com.gtolib.api.ae2.me2in1.Me2in1Screen;
import com.gtolib.api.ae2.me2in1.Wireless;
import com.gtolib.api.ae2.me2in1.panel.PanelSizeMap;
import de.mari_023.ae2wtlib.AE2wtlibSlotSemantics;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import lombok.Generated;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;

public class MePanel extends Panel {
   public static final int DEFAULT_GRID_SIZE = 4;
   private static final Blitter PANEL = Blitter.texture("guis/me_panel.png").src(0, 0, 101, 112);
   private static final Blitter PANEL_HEADER_LPART = PANEL.copy().src(0, 0, 11, 31);
   private static final Blitter PANEL_HEADER_MPART = PANEL.copy().src(11, 0, 81, 31);
   private static final Blitter PANEL_HEADER_RPART = PANEL.copy().src(92, 0, 9, 31);
   private static final Blitter PANEL_LMARGIN = PANEL.copy().src(0, 31, 11, 72);
   private static final Blitter PANEL_RMARGIN = PANEL.copy().src(92, 31, 9, 72);
   private static final Blitter PANEL_SCROLLBAR_UPART = PANEL.copy().src(11, 31, 9, 4);
   private static final Blitter PANEL_SCROLLBAR_MPART = PANEL.copy().src(11, 35, 9, 64);
   private static final Blitter PANEL_SCROLLBAR_DPART = PANEL.copy().src(11, 99, 9, 4);
   private static final Blitter PANEL_SCROLLBAR_SLOT_1_1 = PANEL.copy().src(20, 31, 18, 18);
   private static final Blitter PANEL_SCROLLBAR_SLOT_2_1 = PANEL.copy().src(38, 31, 18, 18);
   private static final Blitter PANEL_SCROLLBAR_SLOT_3_1 = PANEL.copy().src(74, 31, 18, 18);
   private static final Blitter PANEL_SCROLLBAR_SLOT_1_2 = PANEL.copy().src(20, 49, 18, 18);
   private static final Blitter PANEL_SCROLLBAR_SLOT_2_2 = PANEL.copy().src(38, 49, 18, 18);
   private static final Blitter PANEL_SCROLLBAR_SLOT_3_2 = PANEL.copy().src(74, 49, 18, 18);
   private static final Blitter PANEL_SCROLLBAR_SLOT_1_3 = PANEL.copy().src(20, 85, 18, 18);
   private static final Blitter PANEL_SCROLLBAR_SLOT_2_3 = PANEL.copy().src(38, 85, 18, 18);
   private static final Blitter PANEL_SCROLLBAR_SLOT_3_3 = PANEL.copy().src(74, 85, 18, 18);
   private static final Blitter PANEL_FOOTER_LPART = PANEL.copy().src(0, 103, 11, 31);
   private static final Blitter PANEL_FOOTER_MPART = PANEL.copy().src(11, 103, 81, 31);
   private static final Blitter PANEL_FOOTER_RPART = PANEL.copy().src(92, 103, 9, 31);
   private static final Blitter[] PANEL_SCROLLBAR_SLOTS = new Blitter[]{
      PANEL_SCROLLBAR_SLOT_1_1,
      PANEL_SCROLLBAR_SLOT_2_1,
      PANEL_SCROLLBAR_SLOT_3_1,
      PANEL_SCROLLBAR_SLOT_1_2,
      PANEL_SCROLLBAR_SLOT_2_2,
      PANEL_SCROLLBAR_SLOT_3_2,
      PANEL_SCROLLBAR_SLOT_1_3,
      PANEL_SCROLLBAR_SLOT_2_3,
      PANEL_SCROLLBAR_SLOT_3_3
   };
   private static final Blitter PINNED_SLOT_OVERLAY = Blitter.texture("guis/terminal.png").src(2, 206, 14, 14);
   private final Blitter background;
   private static final Blitter singularityBackground = Blitter.texture("guis/extra_panels.png", 128, 128).src(0, 0, 32, 32);
   public static String rememberedSearch;
   private static final int MEPANEL_BG_OFFSET_X = -9;
   private static final int MEPANEL_BG_OFFSET_Y = -8;
   private static final int MEPANEL_SLOT_OFFSET_X = 12;
   private static final int MEPANEL_SLOT_OFFSET_Y = 24;
   private static final int METOOLBOX_SLOT_OFFSET_Y = 108;
   private static final int MEVIEWCELL_SLOT_OFFSET_X = 93;
   private static final int MEVIEWCELL_SLOT_OFFSET_Y = -8;
   private static final int SEARCH_FIELD_OFFSET_X = 10;
   private static final int SEARCH_FIELD_OFFSET_Y = 2;
   private static final int TOOLBAR_OFFSET_X = -10;
   private static final int TOOLBAR_OFFSET_Y = -2;
   private static final int SCROLLBAR_OFFSET_X = 3;
   private static final int SCROLLBAR_OFFSET_Y = 24;
   private static final int DRAG_MARK_ME_X = -8;
   private static final int DRAG_MARK_ME_Y = -6;
   private final AETextField searchField;
   private RepoSlot[] repoSlots;
   private final VerticalButtonBar toolbar;
   private final Scrollbar scrollbar;
   private final UpgradesPanel viewCells;
   private final boolean isWT;
   private static boolean collapsedToolbar = true;

   public MePanel(Me2in1Screen<?> screen, Scrollbar scrollbar, Consumer<String> responder, RepoSlot[] repoSlots) {
      super(screen, -130, 12, "mePanel");
      this.isWT = screen instanceof Wireless.Screen;
      ScreenStyle styleDoc = StyleManager.loadStyleDoc("/screens/terminals/terminal.json");
      this.toolbar = new VerticalButtonBar();
      List<Component> tooltip = Collections.singletonList(GuiText.TerminalViewCellsTooltip.text());
      this.viewCells = new UpgradesPanel(screen.getMenu().getSlots(SlotSemantics.VIEW_CELL), () -> tooltip);
      this.scrollbar = scrollbar;
      this.background = styleDoc.getImage("toolbox");
      this.searchField = new AETextField(styleDoc, Minecraft.getInstance().font, 0, 0, 0, 0);
      this.searchField.setBordered(false);
      this.searchField.setMaxLength(25);
      this.searchField.setTextColor(16777215);
      this.searchField.setSelectionColor(-16777088);
      this.searchField.setVisible(true);
      this.searchField.setWidth(64);
      this.searchField.setHeight(9 + 2);
      this.searchField.setPlaceholder(GuiText.SearchPlaceholder.text());
      this.searchField.setResponder(responder);
      screen.getWidgets().add("mePanelSearchField", this.searchField);
      screen.getWidgets().add("mePanelWidgetScrollbar", scrollbar);
      this.repoSlots = repoSlots;
      PanelSizeMap.PanelSize storedSize = screen.getMenu().getPanelSizeMap() == null
         ? null
         : screen.getMenu().getPanelSizeMap().map().getPanelSize(this.getName());
      if (storedSize != null) {
         this.setPanelSize(storedSize.rows, storedSize.columns);
      }

      this.enableSizeNotifications();
   }

   @Override
   public void populateScreen(Consumer<AbstractWidget> addWidget, Rect2i bounds, AEBaseScreen<?> screen) {
      this.toolbar.populateScreen(addWidget, bounds, screen);
   }

   @Nullable
   @Override
   public Tooltip getTooltip(int mouseX, int mouseY) {
      if (this.isCollapsed()) {
         return super.getTooltip(mouseX, mouseY);
      } else {
         return this.viewCells.getBounds().contains(mouseX, mouseY) ? this.viewCells.getTooltip(mouseX, mouseY) : super.getTooltip(mouseX, mouseY);
      }
   }

   @Override
   public boolean onMouseWheel(Point mousePos, double delta) {
      return this.isCollapsed() ? false : !Screen.hasShiftDown() && this.scrollbar.onMouseWheel(mousePos, delta);
   }

   @Override
   public void drawBackgroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
      if (this.isCollapsed()) {
         super.drawBackgroundLayer(guiGraphics, bounds, mouse);
      } else {
         super.drawBackgroundLayer(guiGraphics, bounds, mouse);
         this.destBG(guiGraphics);
         if (!collapsedToolbar) {
            if (this.screen.getMenu().getToolboxMenu().isPresent()) {
               this.background
                  .dest(
                     13 + this.x + this.getGuiLeft() + this.getToolbarResizingExtraWidth(),
                     108 + this.y + this.getGuiTop() + this.getToolbarResizingExtraHeight()
                  )
                  .blit(guiGraphics);
            }

            Rect2i vcBounds = this.viewCells.getBounds();
            this.viewCells.setPosition(new Point(vcBounds.getX() + this.getGuiLeft(), vcBounds.getY() + this.getGuiTop()));
            this.viewCells.drawBackgroundLayer(guiGraphics, bounds, mouse);
            this.viewCells.setPosition(new Point(vcBounds.getX(), vcBounds.getY()));
            if (this.isWT) {
               singularityBackground.dest(
                     this.x + -9 + this.getGuiLeft() - 32 + 133 + this.getToolbarResizingExtraWidth(),
                     this.y + -8 + this.getGuiTop() + 110 + this.getToolbarResizingExtraHeight()
                  )
                  .blit(guiGraphics);
            }
         }
      }
   }

   @Override
   public Rect2i getBounds() {
      if (this.isCollapsed()) {
         return this.getCollapsedBounds();
      }

      int left = this.getPanelOriginX() + -10 - -9;
      int top = this.getPanelOriginY();
      return new Rect2i(left, top, this.getBoundsRight() - left, this.getBoundsBottom() - top);
   }

   @Override
   public void addExclusionZones(List<Rect2i> exclusionZones, Rect2i screenBounds) {
      super.addExclusionZones(exclusionZones, screenBounds);
      if (!this.isCollapsed()) {
         this.toolbar.addExclusionZones(exclusionZones, screenBounds);
      }
   }

   @Override
   public void updateBeforeRender() {
      super.updateBeforeRender();
      boolean collapsed = this.isCollapsed();
      this.searchField.setVisible(!collapsed);
      this.scrollbar.setVisible(!collapsed);

      for (Button button : this.toolbar.buttons) {
         button.visible = !collapsed;
         button.active = !collapsed;
      }

      RepoSlot[] reposlots = this.repoSlots == null ? new RepoSlot[0] : this.repoSlots;
      if (collapsed) {
         if (this.searchField.isFocused()) {
            this.searchField.setFocused(false);
         }

         for (RepoSlot slot : reposlots) {
            if (slot != null) {
               ME2in1Helper.setSlotPos(slot, -9999, -9999);
            }
         }

         this.screen.setSlotsHidden(SlotSemantics.TOOLBOX, true);
         this.screen.setSlotsHidden(SlotSemantics.VIEW_CELL, true);
         this.screen.setSlotsHidden(AE2wtlibSlotSemantics.SINGULARITY, true);
      } else {
         this.searchField.setPosition(this.x + 10 + this.getGuiLeft(), this.y + 2 + this.getGuiTop());
         int repoSlotIndex = 0;

         for (RepoSlot slot : reposlots) {
            if (slot != null) {
               ME2in1Helper.setSlotPos(slot, this.x + 12 + repoSlotIndex % this.columns * 18, this.y + 24 + repoSlotIndex / this.columns * 18);
            }

            repoSlotIndex++;
         }

         if (!collapsedToolbar) {
            this.screen.setSlotsHidden(SlotSemantics.TOOLBOX, false);
            this.screen.setSlotsHidden(SlotSemantics.VIEW_CELL, false);
            this.screen.setSlotsHidden(AE2wtlibSlotSemantics.SINGULARITY, !this.isWT);
            if (this.screen.getMenu().getToolboxMenu().isPresent()) {
               int toolboxSlotIndex = 0;

               for (Slot slot : this.screen.getMenu().getSlots(SlotSemantics.TOOLBOX)) {
                  int currentIndex = toolboxSlotIndex++;
                  ME2in1Helper.setSlotPos(
                     slot,
                     this.x + 12 + currentIndex % 3 * 18 + 9 + this.getToolbarResizingExtraWidth(),
                     this.y + 108 + currentIndex / 3 * 18 + 8 + (toolboxSlotIndex > 9 ? 999 : 0) + this.getToolbarResizingExtraHeight()
                  );
               }
            }

            if (this.isWT) {
               ME2in1Helper.setSlotPos(
                  ((Wireless.Screen)this.screen).getMenu().getSingularitySlot(),
                  this.x + -9 - 24 + 133 + this.getToolbarResizingExtraWidth(),
                  this.y + -8 + 110 + 8 + this.getToolbarResizingExtraHeight()
               );
            }
         } else {
            this.screen.setSlotsHidden(SlotSemantics.TOOLBOX, true);
            this.screen.setSlotsHidden(SlotSemantics.VIEW_CELL, true);
            this.screen.setSlotsHidden(AE2wtlibSlotSemantics.SINGULARITY, true);
         }

         this.scrollbar.setPosition(new Point(this.x + 3, this.y + 24));
         if (!collapsedToolbar) {
            this.viewCells.setPosition(new Point(this.x + 93 + this.getToolbarResizingExtraWidth(), this.y + -8 + this.getToolbarResizingExtraHeight()));
            this.viewCells.updateBeforeRender();
         }

         this.toolbar.setPosition(new Point(this.x + -10, this.y + -2));
         this.toolbar.updateBeforeRender();
      }
   }

   public <BUTTON extends Button> BUTTON addToolbarButton(BUTTON button) {
      this.toolbar.add(button);
      return button;
   }

   @Override
   int getDragMarkShiftX() {
      return -8;
   }

   @Override
   int getDragMarkShiftY() {
      return -6;
   }

   @Override
   protected int getDefaultRows() {
      return 4;
   }

   @Override
   protected int getDefaultColumns() {
      return 4;
   }

   @Override
   protected int getMinRows() {
      return 4;
   }

   @Override
   protected int getMinColumns() {
      return 4;
   }

   @Override
   protected int getBasePanelWidth() {
      return PANEL.getSrcWidth();
   }

   @Override
   protected int getBasePanelHeight() {
      return PANEL.getSrcHeight();
   }

   @Override
   protected int getPanelOriginX() {
      return this.x + -9;
   }

   @Override
   protected int getPanelOriginY() {
      return this.y + -8;
   }

   @Override
   protected int getResizeGripOffsetX() {
      return -2;
   }

   @Override
   protected int getResizeGripOffsetY() {
      return -2;
   }

   @Override
   protected void onPanelSizeChanged(int rows, int columns) {
      this.screen.applyMePanelGridSize(rows, columns);
   }

   private int getToolbarResizingExtraHeight() {
      return (this.rows - 4) * 18;
   }

   private int getToolbarResizingExtraWidth() {
      return (this.columns - 4) * 18;
   }

   public void updateScrollbar() {
      this.scrollbar.setHeight(this.rows * 18 - 2);
      Repo repo = (Repo)this.screen.getMenu().getClientRepo();
      int totalRows = 0;
      if (repo != null) {
         totalRows = repo.getTotalDisplayRows();
      }

      this.scrollbar.setRange(0, totalRows - this.rows, 1);
   }

   public int getVisibleRepoSlotCount() {
      return this.rows * this.columns;
   }

   private int getBoundsRight() {
      int panelRight = this.getPanelOriginX() + this.getPanelWidth();
      int toolbarRight = this.getPanelOriginX() + -1 + this.getPanelWidth() + (collapsedToolbar ? 0 : 45);
      return Math.max(panelRight, toolbarRight);
   }

   private int getBoundsBottom() {
      int panelBottom = this.getPanelOriginY() + this.getPanelHeight();
      int toolbarBottom = this.getPanelOriginY() + 10 + this.getPanelHeight() + (collapsedToolbar ? 0 : 66);
      return Math.max(panelBottom, toolbarBottom);
   }

   private void destBG(GuiGraphics guiGraphics) {
      int leftX = this.getAbsoluteLeft();
      int topY = this.getAbsoluteTop();
      BlitterHelper blitterHelper = BlitterHelper.of(guiGraphics);
      blitterHelper.hBlit(PANEL_HEADER_LPART, PANEL_HEADER_MPART, PANEL_HEADER_RPART, leftX, topY, this.getPanelWidth(), PANEL_HEADER_LPART.getSrcHeight());
      int middleAreaTopY = topY + PANEL_HEADER_LPART.getSrcHeight();
      int marginHeight = this.rows * 18;
      PANEL_LMARGIN.dest(leftX, middleAreaTopY, PANEL_LMARGIN.getSrcWidth(), marginHeight).blit(guiGraphics);
      int rightX = leftX + this.getPanelWidth() - PANEL_RMARGIN.getSrcWidth();
      PANEL_RMARGIN.dest(rightX, middleAreaTopY, PANEL_RMARGIN.getSrcWidth(), marginHeight).blit(guiGraphics);
      int scrollbarLeftX = leftX + PANEL_LMARGIN.getSrcWidth();
      blitterHelper.vBlit(
         PANEL_SCROLLBAR_UPART, PANEL_SCROLLBAR_MPART, PANEL_SCROLLBAR_DPART, scrollbarLeftX, middleAreaTopY, PANEL_SCROLLBAR_UPART.getSrcWidth(), marginHeight
      );
      int slotsLeftX = leftX + PANEL_LMARGIN.getSrcWidth() + PANEL_SCROLLBAR_UPART.getSrcWidth();
      blitterHelper.gridBlit(PANEL_SCROLLBAR_SLOTS, slotsLeftX, middleAreaTopY, this.columns, this.rows);

      for (int row = 0; row < this.rows; row++) {
         boolean isPinnedRow = row < this.screen.repo.getPinnedRowCount();

         for (int column = 0; column < this.columns; column++) {
            if (isPinnedRow) {
               PINNED_SLOT_OVERLAY.dest(slotsLeftX + column * 18 + 2, middleAreaTopY + row * 18 + 2).blit(guiGraphics);
            }
         }
      }

      int footerTopY = topY + marginHeight + PANEL_HEADER_LPART.getSrcHeight();
      blitterHelper.hBlit(
         PANEL_FOOTER_LPART, PANEL_FOOTER_MPART, PANEL_FOOTER_RPART, leftX, footerTopY, this.getPanelWidth(), PANEL_FOOTER_LPART.getSrcHeight()
      );
   }

   @Generated
   public AETextField getSearchField() {
      return this.searchField;
   }

   @Generated
   public void setRepoSlots(RepoSlot[] repoSlots) {
      this.repoSlots = repoSlots;
   }

   @Generated
   public VerticalButtonBar getToolbar() {
      return this.toolbar;
   }

   @Generated
   public boolean isWT() {
      return this.isWT;
   }

   @Generated
   public static boolean isCollapsedToolbar() {
      return collapsedToolbar;
   }

   @Generated
   public static void setCollapsedToolbar(boolean collapsedToolbar) {
      MePanel.collapsedToolbar = collapsedToolbar;
   }
}
