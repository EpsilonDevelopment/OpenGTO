package com.gtolib.api.machine.impl;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.utils.TaskHandler;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.gtolib.api.annotation.Scanned;
import com.gtolib.api.annotation.dynamic.DynamicInitialValue;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.capability.IExtendWirelessEnergyContainerHolder;
import com.gtolib.api.capability.IIWirelessInteractor;
import com.gtolib.api.machine.multiblock.StorageMultiblockMachine;
import com.gtolib.api.wireless.ExtendWirelessEnergyContainer;
import com.hepdd.gtmthings.api.misc.WirelessEnergyContainer;
import com.hepdd.gtmthings.utils.BigIntegerUtils;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Generated;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Scanned
public final class WirelessChargerMachine extends StorageMultiblockMachine implements IExtendWirelessEnergyContainerHolder {
   @RegisterLanguage(cn = "只为玩家提供能量", en = "Only Provide Energy to Player")
   public static final String ONLY_PROVIDE_TO_PLAYER = "gtocore.machine.only_provide_to_player";
   @RegisterLanguage(cn = "为多方块结构的仓室提供能量", en = "Provide Energy to Multiblock Hatch")
   public static final String PROVIDE_TO_MULTIBLOCK_PART = "gtocore.machine.provide_to_multiblock_part";
   @DynamicInitialValue(
      key = "wireless_charger.amount",
      easyValue = "1",
      normalValue = "16",
      expertValue = "64",
      typeKey = "amount",
      cn = "力场发生器需求数量",
      cnComment = "放入%s个对应等级的力场发生器，即可开启无线功能。\n等级越高，综合电流越大，FE物品速度无限。\n当等级达到HV时，其他机器会自动添加到无线网络中。\n当等级达到EV时，机器为玩家的充电范围变为无限。\n机器内可查看为机器远程充电的距离和最大电流",
      en = "Field Generator Required Amount",
      enComment = "Put %s Field Generator into the machine, and it will be enabled.\nThe higher the level, the greater the total current, and the speed of FE items is infinite.\nWhen the level is HV or above, other machines will be automatically added to the wireless network.\nWhen the level is EV or above, the charging range for players becomes infinite.\nYou can view the charging range and maximum current for remote charging in the machine."
   )
   private static int amount = 16;
   private int range;
   private int rate;
   private boolean infinite;
   private boolean machine;
   private WirelessEnergyContainer WirelessEnergyContainerCache;
   @SaveToDisk(defaultValue = "false")
   public boolean isOnlyProvideToPlayer = false;
   @SaveToDisk(defaultValue = "false")
   public boolean isProvideToMultiBlockHatch = false;
   @SaveToDisk(defaultValue = "false")
   private boolean isw;

   public WirelessChargerMachine(MetaMachineBlockEntity holder) {
      super(holder, amount, i -> WirelessChargerMachine.Wrapper.GENERATOR.containsKey(i.getItem()));
      this.setWorkingEnabled(false);
   }

   @Override
   public boolean hasOverclockConfig() {
      return false;
   }

   @Override
   public boolean hasBatchConfig() {
      return false;
   }

   @Override
   public boolean isRecipeLogicAvailable() {
      return false;
   }

   @Override
   public void onMachineChanged() {
      this.machine = false;
      this.infinite = false;
      this.rate = 0;
      this.range = 0;
      ItemStack stack = this.getStorageStack();
      if (stack.getCount() == amount) {
         Integer tier = WirelessChargerMachine.Wrapper.GENERATOR.get(stack.getItem());
         if (tier != null) {
            this.getRecipeLogic().setStatus(1);
            if (tier > 2) {
               this.machine = true;
            }

            if (tier > 3) {
               this.infinite = true;
            }

            this.rate = 10 * tier;
            this.range = 8 * (2 << tier);
         } else if (this.getRecipeLogic().isWorking()) {
            this.getRecipeLogic().setStatus(0);
         }
      }
   }

   @Override
   public void addDisplayText(@NotNull List<Component> textList) {
      MultiblockDisplayText.builder(textList, this.isFormed())
         .setWorkingStatus(this.recipeLogic.isWorkingEnabled(), this.recipeLogic.isActive())
         .addEnergyUsageLine(this.getEnergyContainer());
      if (this.isFormed()) {
         int count = this.getStorageStack().getCount();
         if (count != amount) {
            textList.add(Component.translatable("gui.ae2.Missing", amount - count));
         }

         textList.add(
            Component.translatable("gtocore.machine.generator_array.wireless")
               .append(
                  ComponentPanelWidget.withButton(
                     Component.literal("[")
                        .append(this.isw ? Component.translatable("gtocore.machine.on") : Component.translatable("gtocore.machine.off"))
                        .append(Component.literal("]")),
                     "wireless_switch"
                  )
               )
         );
         textList.add(
            Component.translatable("gtocore.machine.only_provide_to_player")
               .append(
                  ComponentPanelWidget.withButton(
                     Component.literal("[")
                        .append(this.isOnlyProvideToPlayer ? Component.translatable("gtocore.machine.on") : Component.translatable("gtocore.machine.off"))
                        .append(Component.literal("]")),
                     "wireless_switch_player"
                  )
               )
         );
         textList.add(
            Component.translatable("gtocore.machine.provide_to_multiblock_part")
               .append(
                  ComponentPanelWidget.withButton(
                     Component.literal("[")
                        .append(this.isProvideToMultiBlockHatch ? Component.translatable("gtocore.machine.on") : Component.translatable("gtocore.machine.off"))
                        .append(Component.literal("]")),
                     "wireless_switch_part"
                  )
               )
         );
         textList.add(Component.translatable("gtceu.recipe.amperage", this.rate / 20.0));
         textList.add(Component.translatable("gui.ae2.WirelessRange", this.range));
      }
   }

   @Override
   public void handleDisplayClick(String componentData, ClickData clickData) {
      if (!clickData.isRemote) {
         if ("wireless_switch".equals(componentData)) {
            this.isw = !this.isw;
         }

         if ("wireless_switch_player".equals(componentData)) {
            this.isOnlyProvideToPlayer = !this.isOnlyProvideToPlayer;
         }

         if ("wireless_switch_part".equals(componentData)) {
            this.isProvideToMultiBlockHatch = !this.isProvideToMultiBlockHatch;
         }
      }
   }

   @Override
   public void onStructureFormed() {
      super.onStructureFormed();
      IIWirelessInteractor.addToNet(this);
      this.onMachineChanged();
   }

   @Override
   public void onStructureInvalid() {
      IIWirelessInteractor.removeFromNet(this);
      super.onStructureInvalid();
   }

   @Override
   public void onUnload() {
      super.onUnload();
      IIWirelessInteractor.removeFromNet(this);
   }

   @Override
   public void setWorkingEnabled(boolean isWorkingAllowed) {
      if (isWorkingAllowed && this.isFormed()) {
         if (this.getLevel() instanceof ServerLevel serverLevel) {
            TaskHandler.enqueueTask(serverLevel, this::onMachineChanged);
         }

         IIWirelessInteractor.addToNet(this);
      } else {
         IIWirelessInteractor.removeFromNet(this);
      }

      super.setWorkingEnabled(isWorkingAllowed);
   }

   @Nullable
   public UUID getUUID() {
      return this.getOwnerUUID();
   }

   public long getEnergyStored() {
      if (this.isw) {
         ExtendWirelessEnergyContainer container = this.getWirelessEnergyContainer();
         return container != null ? BigIntegerUtils.getLongValue(container.getStorage()) : 0L;
      } else {
         return this.getEnergyContainer().getEnergyStored();
      }
   }

   public void removeEnergy(long energy) {
      if (this.isw) {
         ExtendWirelessEnergyContainer container = this.getWirelessEnergyContainer();
         if (container != null) {
            container.unrestrictedRemoveEnergy(energy);
         }
      } else {
         this.getEnergyContainer().removeEnergy(energy);
      }
   }

   public static void setAmount(int amount) {
      WirelessChargerMachine.amount = amount;
   }

   @Generated
   public int getRange() {
      return this.range;
   }

   @Generated
   public int getRate() {
      return this.rate;
   }

   @Generated
   public boolean isInfinite() {
      return this.infinite;
   }

   @Generated
   public boolean isMachine() {
      return this.machine;
   }

   @Generated
   public void setWirelessEnergyContainerCache(WirelessEnergyContainer WirelessEnergyContainerCache) {
      this.WirelessEnergyContainerCache = WirelessEnergyContainerCache;
   }

   @Generated
   public WirelessEnergyContainer getWirelessEnergyContainerCache() {
      return this.WirelessEnergyContainerCache;
   }

   private static class Wrapper {
      private static final Map<Item, Integer> GENERATOR = Map.of(
         GTItems.FIELD_GENERATOR_LV.get(),
         1,
         GTItems.FIELD_GENERATOR_MV.get(),
         2,
         GTItems.FIELD_GENERATOR_HV.get(),
         3,
         GTItems.FIELD_GENERATOR_EV.get(),
         4,
         GTItems.FIELD_GENERATOR_IV.get(),
         5,
         GTItems.FIELD_GENERATOR_LuV.get(),
         6,
         GTItems.FIELD_GENERATOR_ZPM.get(),
         7,
         GTItems.FIELD_GENERATOR_UV.get(),
         8
      );
   }
}
