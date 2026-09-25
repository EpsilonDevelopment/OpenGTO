package com.gtolib.api.machine.feature.multiblock;

import com.glodblock.github.extendedae.client.render.EAEHighlightHandler;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton.Toggle;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public interface IHighlightMachine extends IMultiController {
   List<BlockPos> getHighlightPos();

   default void attachHighlightConfigurators(ConfiguratorPanel configuratorPanel) {
      configuratorPanel.attachConfigurators(
         new Toggle(
               GuiTextures.LIGHT_ON,
               GuiTextures.LIGHT_ON,
               () -> false,
               (clickData, pressed) -> {
                  if (clickData.isRemote && this.isFormed() && this.self().getLevel() != null) {
                     this.getHighlightPos()
                        .forEach(
                           p -> EAEHighlightHandler.highlight(
                              p, this.self().getLevel().dimension(), System.currentTimeMillis() + this.getHighlightMilliseconds()
                           )
                        );
                  }
               }
            )
            .setTooltipsSupplier(pressed -> List.of(Component.translatable("gtocore.machine.highlight_module")))
      );
   }

   default int getHighlightMilliseconds() {
      return 15000;
   }
}
