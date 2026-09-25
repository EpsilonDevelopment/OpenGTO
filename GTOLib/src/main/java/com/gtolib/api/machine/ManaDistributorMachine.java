package com.gtolib.api.machine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.gtolib.api.capability.IIWirelessInteractor;
import com.gtolib.api.machine.mana.feature.IManaMultiblock;
import com.gtolib.api.machine.mana.feature.IWirelessManaContainerHolder;
import com.gtolib.api.machine.mana.trait.ManaTrait;
import com.gtolib.api.machine.multiblock.NoRecipeLogicMultiblockMachine;
import com.gtolib.api.misc.ManaContainerList;
import com.gtolib.api.wireless.WirelessManaContainer;
import com.gtolib.utils.ClientUtil;
import com.gtolib.utils.GTOUtils;
import com.gtolib.utils.MachineUtils;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import java.math.BigInteger;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import lombok.Generated;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ManaDistributorMachine extends NoRecipeLogicMultiblockMachine implements IManaMultiblock, IMachineLife, IWirelessManaContainerHolder {
   private int amount = 0;
   private BlockPos centrepos;
   private final ManaTrait manaTrait;
   private final int max;
   private final int radius;
   @SaveToDisk(defaultValue = "false")
   private boolean wireless;
   private WirelessManaContainer WirelessManaContainerCache;
   private final ConditionalSubscriptionHandler tickSubs;

   public static Function<MetaMachineBlockEntity, ManaDistributorMachine> create(int max, int radius) {
      return holder -> new ManaDistributorMachine(holder, max, radius);
   }

   private ManaDistributorMachine(MetaMachineBlockEntity holder, int max, int radius) {
      super(holder);
      this.max = max;
      this.radius = radius;
      this.tickSubs = new ConditionalSubscriptionHandler(this, this::tickUpdate, 20, () -> this.isFormed && this.wireless);
      this.manaTrait = new ManaTrait(this);
   }

   private void tickUpdate() {
      WirelessManaContainer container = this.getWirelessManaContainer();
      if (container != null) {
         container.setStorage(container.getStorage().add(BigInteger.valueOf(this.removeMana(this.getManaContainer().getCurrentMana(), 20, false))));
         this.tickSubs.updateSubscription();
      } else {
         this.tickSubs.unsubscribe();
      }
   }

   public boolean add(BlockPos pos) {
      if (GTOUtils.calculateDistance(pos, this.centrepos) > this.radius) {
         return false;
      }

      if (this.amount >= this.max) {
         return false;
      }

      this.amount++;
      return true;
   }

   public void remove() {
      if (this.amount > 0) {
         this.amount--;
      }
   }

   @Override
   public void customText(@NotNull List<Component> textList) {
      super.customText(textList);
      textList.add(Component.translatable("gtocore.machine.binding_amount", this.amount));
      textList.add(ComponentPanelWidget.withButton(Component.translatable("gtocore.digital_miner.show_range"), "show"));
      if (this.radius == 128) {
         textList.add(
            ComponentPanelWidget.withButton(
               Component.translatable("gtocore.machine.wireless_mode")
                  .append("[")
                  .append(Component.translatable("gtocore.machine." + (this.wireless ? "on" : "off")))
                  .append("]"),
               "wireless"
            )
         );
      }

      if (this.wireless) {
         WirelessManaContainer container = this.getWirelessManaContainer();
         if (container != null) {
            textList.add(Component.translatable("block.gtceu.long_distance_item_pipeline_network_header"));
            textList.add(Component.translatable("gtocore.machine.mana_stored", FormattingUtil.formatNumbers(container.getStorage())));
         }
      }
   }

   @Override
   public void handleDisplayClick(String componentData, ClickData clickData) {
      if (clickData.isRemote) {
         if ("show".equals(componentData)) {
            ClientUtil.highlighting(MachineUtils.getOffsetPos(2, 2, this.getFrontFacing(), this.getPos()), this.radius);
         }
      } else if ("wireless".equals(componentData)) {
         this.wireless = !this.wireless;
         if (this.wireless) {
            this.tickSubs.updateSubscription();
         }
      }
   }

   @Override
   public void onStructureFormed() {
      super.onStructureFormed();
      if (!this.isRemote()) {
         this.centrepos = MachineUtils.getOffsetPos(2, 2, this.getFrontFacing(), this.getPos());
         IIWirelessInteractor.addToNet(this);
         if (this.radius == 128) {
            this.tickSubs.initialize(this.getLevel());
         }
      }
   }

   @Override
   public void onStructureInvalid() {
      this.centrepos = null;
      IIWirelessInteractor.removeFromNet(this);
      super.onStructureInvalid();
   }

   @Override
   public void onUnload() {
      super.onUnload();
      this.tickSubs.unsubscribe();
      IIWirelessInteractor.removeFromNet(this);
   }

   @NotNull
   @Override
   public ManaContainerList getManaContainer() {
      return this.manaTrait.getManaContainers();
   }

   @Override
   public boolean isGeneratorMana() {
      return false;
   }

   @Override
   public void setWorkingEnabled(boolean isWorkingAllowed) {
      if (isWorkingAllowed && this.isFormed()) {
         IIWirelessInteractor.addToNet(this);
      } else {
         IIWirelessInteractor.removeFromNet(this);
      }

      super.setWorkingEnabled(isWorkingAllowed);
   }

   @Override
   public void onMachinePlaced(@Nullable LivingEntity player, ItemStack stack) {
      if (player != null) {
         this.setOwnerUUID(player.getUUID());
      }
   }

   @javax.annotation.Nullable
   public UUID getUUID() {
      return this.getOwnerUUID();
   }

   @Generated
   @Override
   public void setWirelessManaContainerCache(WirelessManaContainer WirelessManaContainerCache) {
      this.WirelessManaContainerCache = WirelessManaContainerCache;
   }

   @Generated
   @Override
   public WirelessManaContainer getWirelessManaContainerCache() {
      return this.WirelessManaContainerCache;
   }
}
