package com.gtolib.api.ae2.me2in1;

import appeng.client.gui.AESubScreen;
import appeng.client.gui.widgets.AECheckbox;
import appeng.client.gui.widgets.TabButton;
import appeng.core.localization.GuiText;
import appeng.menu.SlotSemantics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class TerminalSettingsScreen<C extends Me2in1Menu> extends AESubScreen<C, Me2in1Screen<C>> {
   private final AECheckbox pinAutoCraftedItemsCheckbox;
   private final AECheckbox notifyForFinishedCraftingJobsCheckbox;
   private final AECheckbox clearGridOnCloseCheckbox;
   private final AECheckbox useInternalSearchRadio;
   private final AECheckbox useExternalSearchRadio;
   private final AECheckbox rememberCheckbox;
   private final AECheckbox autoFocusCheckbox;
   private final AECheckbox syncWithExternalCheckbox;
   private final AECheckbox clearExternalCheckbox;

   public TerminalSettingsScreen(Me2in1Screen<C> parent) {
      super(parent, "/screens/terminals/terminal_settings.json");
      this.addBackButton();
      Component externalSearchMod = Component.literal("EMI");
      boolean hasExternalSearch = false;
      this.pinAutoCraftedItemsCheckbox = this.widgets
         .addCheckbox("pinAutoCraftedItemsCheckbox", GuiText.TerminalSettingsPinAutoCraftedItems.text(), this::save);
      this.notifyForFinishedCraftingJobsCheckbox = this.widgets
         .addCheckbox("notifyForFinishedCraftingJobsCheckbox", GuiText.TerminalSettingsNotifyForFinishedJobs.text(), this::save);
      this.clearGridOnCloseCheckbox = this.widgets.addCheckbox("clearGridOnCloseCheckbox", GuiText.TerminalSettingsClearGridOnClose.text(), this::save);
      this.useInternalSearchRadio = this.widgets.addCheckbox("useInternalSearchRadio", GuiText.SearchSettingsUseInternalSearch.text(), this::switchToAeSearch);
      this.useInternalSearchRadio.setRadio(true);
      this.useExternalSearchRadio = this.widgets
         .addCheckbox("useExternalSearchRadio", GuiText.SearchSettingsUseExternalSearch.text(externalSearchMod), this::switchToExternalSearch);
      this.useExternalSearchRadio.setRadio(true);
      this.useExternalSearchRadio.active = hasExternalSearch;
      this.rememberCheckbox = this.widgets.addCheckbox("rememberCheckbox", GuiText.SearchSettingsRememberSearch.text(), this::save);
      this.autoFocusCheckbox = this.widgets.addCheckbox("autoFocusCheckbox", GuiText.SearchSettingsAutoFocus.text(), this::save);
      this.syncWithExternalCheckbox = this.widgets
         .addCheckbox("syncWithExternalCheckbox", GuiText.SearchSettingsSyncWithExternal.text(externalSearchMod), this::save);
      this.clearExternalCheckbox = this.widgets.addCheckbox("clearExternalCheckbox", GuiText.SearchSettingsClearExternal.text(externalSearchMod), this::save);
      this.updateState();
   }

   @Override
   protected void init() {
      super.init();
      this.setSlotsHidden(SlotSemantics.TOOLBOX, true);
   }

   private void switchToAeSearch() {
      this.useInternalSearchRadio.setSelected(true);
      this.useExternalSearchRadio.setSelected(false);
      this.save();
   }

   private void switchToExternalSearch() {
      this.useInternalSearchRadio.setSelected(false);
      this.useExternalSearchRadio.setSelected(true);
      this.save();
   }

   private void addBackButton() {
      ItemStack icon = this.menu.getHost().getMainMenuIcon();
      Component label = icon.getHoverName();
      ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
      TabButton button = new TabButton(icon, label, btn -> this.returnToParent());
      this.widgets.add("back", button);
   }

   private void updateState() {
      this.pinAutoCraftedItemsCheckbox.setSelected(this.config.isPinAutoCraftedItems());
      this.notifyForFinishedCraftingJobsCheckbox.setSelected(this.config.isNotifyForFinishedCraftingJobs());
      this.clearGridOnCloseCheckbox.setSelected(this.config.isClearGridOnClose());
      this.useInternalSearchRadio.setSelected(!this.config.isUseExternalSearch());
      this.useExternalSearchRadio.setSelected(this.config.isUseExternalSearch());
      this.rememberCheckbox.setSelected(this.config.isRememberLastSearch());
      this.autoFocusCheckbox.setSelected(this.config.isAutoFocusSearch());
      this.syncWithExternalCheckbox.setSelected(this.config.isSyncWithExternalSearch());
      this.clearExternalCheckbox.setSelected(this.config.isClearExternalSearchOnOpen());
      this.rememberCheckbox.visible = this.useInternalSearchRadio.isSelected();
      this.autoFocusCheckbox.visible = this.useInternalSearchRadio.isSelected();
      this.syncWithExternalCheckbox.visible = this.useInternalSearchRadio.isSelected();
      this.clearExternalCheckbox.visible = this.useExternalSearchRadio.isSelected();
   }

   private void save() {
      this.config.setUseExternalSearch(this.useExternalSearchRadio.isSelected());
      this.config.setRememberLastSearch(this.rememberCheckbox.isSelected());
      this.config.setAutoFocusSearch(this.autoFocusCheckbox.isSelected());
      this.config.setSyncWithExternalSearch(this.syncWithExternalCheckbox.isSelected());
      this.config.setClearExternalSearchOnOpen(this.clearExternalCheckbox.isSelected());
      this.config.setPinAutoCraftedItems(this.pinAutoCraftedItemsCheckbox.isSelected());
      this.config.setNotifyForFinishedCraftingJobs(this.notifyForFinishedCraftingJobsCheckbox.isSelected());
      this.config.setClearGridOnClose(this.clearGridOnCloseCheckbox.isSelected());
      this.updateState();
   }
}
