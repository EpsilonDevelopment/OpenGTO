package com.gtolib.api.gui;

import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfigurator;
import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
import com.gtocore.api.gui.GTOGuiTextures;
import com.gtolib.api.machine.feature.IOverclockConfigMachine;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.network.chat.Component;

public final class OverclockConfigurator implements IFancyConfigurator {
   private final IOverclockConfigMachine machine;

   public OverclockConfigurator(IOverclockConfigMachine machine) {
      this.machine = machine;
   }

   @Override
   public Component getTitle() {
      return Component.translatable("gtocore.machine.overclock_configurator");
   }

   @Override
   public IGuiTexture getIcon() {
      return GTOGuiTextures.OVERCLOCK_CONFIG;
   }

   @Override
   public Widget createConfigurator() {
      return new WidgetGroup(0, 0, 100, 20)
         .addWidget(new IntInputWidget(this.machine::getOverclockLimit, this.machine::setOverclockLimit).setMin(1).setMax(200));
   }
}
