package com.gtolib.api.ae2.me2in1;

import appeng.api.config.Settings;
import appeng.api.config.ShowPatternProviders;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.TypeFilter;
import appeng.api.config.ViewItems;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGrid;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.storage.MEStorage;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.parts.PartModel;
import appeng.util.ConfigManager;
import appeng.util.inv.AppEngInternalInventory;
import com.glodblock.github.extendedae.common.parts.PartExPatternAccessTerminal;
import com.gtolib.GTOCore;
import com.gtolib.ae2.me2in1.IMe2in1Host;
import com.gtolib.api.ae2.GTOSettings;
import com.gtolib.api.ae2.ShiftTransferTo;
import gto_ae.api.config.ExtendedSettings;
import gto_ae.menu.ShowMolecularAssembler;
import java.util.Arrays;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class Me2in1TerminalPart extends PartExPatternAccessTerminal implements IMe2in1Host {
   public static List<ResourceLocation> MODELS = Arrays.asList(GTOCore.id("part/me2in1_off"), GTOCore.id("part/me2in1_on"));
   public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, MODELS.get(0), MODEL_STATUS_OFF);
   public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, MODELS.get(1), MODEL_STATUS_ON);
   public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, MODELS.get(1), MODEL_STATUS_HAS_CHANNEL);
   private final ExtendedEncodingLogic logic = new ExtendedEncodingLogic(this);
   private final AppEngInternalInventory viewCell = new AppEngInternalInventory(this, 5);
   private final ConfigManager configManager = new ConfigManager(this::saveChanges);

   public Me2in1TerminalPart(IPartItem<?> partItem) {
      super(partItem);
      this.configManager.registerSetting(Settings.SORT_BY, SortOrder.NAME);
      this.configManager.registerSetting(Settings.VIEW_MODE, ViewItems.ALL);
      this.configManager.registerSetting(Settings.TYPE_FILTER, TypeFilter.ALL);
      this.configManager.registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING);
      this.configManager.registerSetting(Settings.TERMINAL_SHOW_PATTERN_PROVIDERS, ShowPatternProviders.VISIBLE);
      this.configManager.registerSetting(ExtendedSettings.TERMINAL_SHOW_MOLECULAR_ASSEMBLERS, ShowMolecularAssembler.ALL);
      this.configManager.registerSetting(GTOSettings.ME2IN1_SHIFT_TRANSFER_TO, ShiftTransferTo.INVENTORY_OR_BUFFER);
   }

   @Override
   public ExtendedEncodingLogic getLogic() {
      return this.logic;
   }

   @Override
   public MEStorage getInventory() {
      IGrid grid = this.getMainNode().getGrid();
      return grid != null ? grid.getStorageService().getInventory() : null;
   }

   public ConfigManager getConfigManager() {
      return this.configManager;
   }

   @Override
   public ItemStack getMainMenuIcon() {
      return this.getPartItem().asItem().getDefaultInstance();
   }

   @Override
   public InternalInventory getViewCellStorage() {
      return this.viewCell;
   }

   @Override
   public void saveChanges() {
      this.getHost().markForSave();
   }

   @Override
   public void onChangeInventory(InternalInventory inv, int slot) {
      this.getHost().markForSave();
   }

   @Override
   public void clearContent() {
      super.clearContent();
      this.viewCell.clear();
      this.logic.getEncodedPatternInv().clear();
   }

   @Override
   public void readFromNBT(CompoundTag data) {
      super.readFromNBT(data);
      this.configManager.readFromNBT(data);
      this.viewCell.readFromNBT(data, "viewCell");
      this.logic.readFromNBT(data);
   }

   @Override
   public void writeToNBT(CompoundTag data) {
      super.writeToNBT(data);
      this.configManager.writeToNBT(data);
      this.viewCell.writeToNBT(data, "viewCell");
      this.logic.writeToNBT(data);
   }

   @Override
   public void addAdditionalDrops(List<ItemStack> drops, boolean wrenched) {
      super.addAdditionalDrops(drops, wrenched);

      for (ItemStack is : this.logic.getEncodedPatternInv()) {
         drops.add(is);
      }
   }

   @Override
   public void markForSave() {
      this.getHost().markForSave();
   }

   public MenuType<?> getMenuType(Player p) {
      return Me2in1Menu.TYPE;
   }

   @Override
   public boolean onPartActivate(Player player, InteractionHand hand, Vec3 pos) {
      if (!player.level().isClientSide) {
         MenuOpener.open(this.getMenuType(player), player, MenuLocators.forPart(this));
      }

      return true;
   }

   @Override
   public void returnToMainMenu(Player player, ISubMenu subMenu) {
      MenuOpener.open(this.getMenuType(player), player, subMenu.getLocator(), true);
   }

   @Override
   public IPartModel getStaticModels() {
      return this.selectModel(MODELS_OFF, MODELS_ON, MODELS_HAS_CHANNEL);
   }
}
