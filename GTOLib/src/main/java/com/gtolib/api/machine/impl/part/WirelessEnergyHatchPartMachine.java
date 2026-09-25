package com.gtolib.api.machine.impl.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.WorkableTieredIOPartMachine;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.gtolib.api.machine.trait.WirelessEnergyContainerTrait;
import com.hepdd.gtmthings.api.capability.IBindable;
import com.hepdd.gtmthings.utils.TeamUtil;
import java.util.UUID;
import lombok.Generated;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public final class WirelessEnergyHatchPartMachine extends WorkableTieredIOPartMachine implements IInteractedMachine, IBindable {
   @SaveToDisk
   private final WirelessEnergyContainerTrait energyContainer;
   private final int amperage;

   public WirelessEnergyHatchPartMachine(MetaMachineBlockEntity holder, int tier, IO io, int amperage) {
      super(holder, tier, io);
      this.amperage = amperage;
      this.energyContainer = this.createEnergyContainer();
   }

   private WirelessEnergyContainerTrait createEnergyContainer() {
      WirelessEnergyContainerTrait container;
      if (this.io == IO.OUT) {
         container = WirelessEnergyContainerTrait.emitterContainer(this, getHatchEnergyCapacity(this.tier, this.amperage), GTValues.V[this.tier], this.amperage);
      } else {
         container = WirelessEnergyContainerTrait.receiverContainer(
            this, getHatchEnergyCapacity(this.tier, this.amperage), GTValues.V[this.tier], this.amperage
         );
      }

      return container;
   }

   @Override
   public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
      return false;
   }

   @Override
   public int tintColor(int index) {
      return index == 2 ? GTValues.VC[this.getTier()] : super.tintColor(index);
   }

   @Override
   public InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      if (player.getItemInHand(hand).is(GTItems.TOOL_DATA_STICK.asItem())) {
         this.setOwnerUUID(player.getUUID());
         if (this.isRemote()) {
            player.sendSystemMessage(Component.translatable("gtmthings.machine.wireless_energy_hatch.tooltip.bind", TeamUtil.GetName(player)));
         }

         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.PASS;
      }
   }

   @Override
   public boolean onLeftClick(Player player, Level world, InteractionHand hand, BlockPos pos, Direction direction) {
      if (player.getItemInHand(hand).is(GTItems.TOOL_DATA_STICK.asItem())) {
         this.setOwnerUUID(null);
         if (this.isRemote()) {
            player.sendSystemMessage(Component.translatable("gtmthings.machine.wireless_energy_hatch.tooltip.unbind"));
         }

         return true;
      } else {
         return false;
      }
   }

   @Nullable
   public UUID getUUID() {
      return this.energyContainer.getUUID();
   }

   public boolean preferTeamName() {
      return true;
   }

   public static long getHatchEnergyCapacity(int tier, int amperage) {
      return GTValues.V[tier] * 64L * amperage;
   }

   @Generated
   public WirelessEnergyContainerTrait getEnergyContainer() {
      return this.energyContainer;
   }
}
