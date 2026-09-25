package com.gtolib.api.wireless;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.hepdd.gtmthings.api.misc.ITransferData;
import com.hepdd.gtmthings.utils.TeamUtil;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.HoverEvent.Action;

public record ExtendTransferData(UUID UUID, long Throughput, long loss, MetaMachine machine) implements ITransferData {
   public Component getInfo() {
      MetaMachine machine = this.machine();
      if (machine.getLevel() == null) {
         return Component.empty();
      }

      long eut = this.Throughput();
      String pos = machine.getPos().toShortString();
      return eut > 0L
         ? Component.translatable(machine.getBlockState().getBlock().getDescriptionId())
            .withStyle(
               Style.EMPTY
                  .withHoverEvent(
                     new HoverEvent(
                        Action.SHOW_TEXT,
                        Component.translatable("recipe.condition.dimension.tooltip", machine.getLevel().dimension().location())
                           .append(" [")
                           .append(pos)
                           .append("] ")
                           .append(
                              Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.0", TeamUtil.GetName(machine.getLevel(), this.UUID()))
                           )
                     )
                  )
            )
            .append(" +")
            .append(FormattingUtil.formatNumbers(eut))
            .append(" EU/t (")
            .append(GTValues.VNF[GTUtil.getFloorTierByVoltage(eut)])
            .append(") ")
            .append(
               Component.translatable(
                  "gtocore.machine.energy_loss",
                  Component.literal(FormattingUtil.formatNumbers(this.loss()))
                     .append(" EU/t (")
                     .append(GTValues.VNF[GTUtil.getFloorTierByVoltage(this.loss())])
                     .append(")")
               )
            )
            .append(ComponentPanelWidget.withButton(Component.literal(" [ ] "), pos))
         : Component.translatable(machine.getBlockState().getBlock().getDescriptionId())
            .withStyle(
               Style.EMPTY
                  .withHoverEvent(
                     new HoverEvent(
                        Action.SHOW_TEXT,
                        Component.translatable("recipe.condition.dimension.tooltip", machine.getLevel().dimension().location())
                           .append(" [")
                           .append(pos)
                           .append("] ")
                           .append(
                              Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.0", TeamUtil.GetName(machine.getLevel(), this.UUID()))
                           )
                     )
                  )
            )
            .append(" -")
            .append(FormattingUtil.formatNumbers(-eut))
            .append(" EU/t (")
            .append(GTValues.VNF[GTUtil.getFloorTierByVoltage(-eut)])
            .append(")")
            .append(ComponentPanelWidget.withButton(Component.literal(" [ ] "), pos));
   }
}
