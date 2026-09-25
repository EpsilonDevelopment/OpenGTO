package com.gtolib.api.machine.feature.multiblock;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton.Toggle;
import com.gtocore.client.forge.ForgeClientEvent;
import com.gtocore.client.forge.ForgeClientEvent.HighlightNeed;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public interface ICustomHighlightMachine extends IHighlightMachine {
   List<HighlightNeed> getCustomHighlights();

   default List<Component> getHighlightText() {
      return List.of(Component.translatable("gtocore.machine.highlight_module"));
   }

   @Override
   default List<BlockPos> getHighlightPos() {
      return Collections.emptyList();
   }

   @Override
   default void attachHighlightConfigurators(ConfiguratorPanel configuratorPanel) {
      configuratorPanel.attachConfigurators(new Toggle(GuiTextures.LIGHT_ON, GuiTextures.LIGHT_ON, () -> false, (clickData, pressed) -> {
         if (clickData.isRemote && this.isFormed() && this.self().getLevel() != null) {
            this.getCustomHighlights().forEach(need -> ForgeClientEvent.CUstomHighlightNeeds.computeIfAbsent(need, k -> this.getHighlightMilliseconds() / 50));
         }
      }).setTooltipsSupplier(pressed -> this.getHighlightText()));
   }
}
