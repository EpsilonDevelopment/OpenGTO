package com.gtolib.api.machine.impl.part;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton.Toggle;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.part.AmountConfigurationPartMachine;
import java.util.List;
import lombok.Generated;
import net.minecraft.network.chat.Component;

@DataGeneratorScanned
public final class ThreadPartMachine extends AmountConfigurationPartMachine {
   @RegisterLanguage(cn = "独立线程[%s]", en = "Independent Thread [%s]")
   public static final String INDEPENDENT_THREAD = "gtocore.machine.independent_thread";
   @RegisterLanguage(cn = "并行重复配方[%s]", en = "Parallel repeated recipes [%s]")
   private static final String REPEATED_RECIPES = "gtocore.machine.repeated_recipes";
   @RegisterLanguage(cn = "独立线程需要安装无线能源仓后启用", en = "Wireless energy hatch must be installed to enable independent thread")
   private static final String WIRELESS = "gtocore.machine.wireless_enable";
   @SaveToDisk(defaultValue = "true")
   private boolean repeatedRecipes = true;
   @SaveToDisk(defaultValue = "true")
   private boolean iThread = true;

   public ThreadPartMachine(MetaMachineBlockEntity holder, int tier) {
      super(holder, tier, 1L, 1L << tier - 6);
   }

   public int getCurrentThread() {
      return (int)this.getCurrent();
   }

   @Override
   public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
      super.attachConfigurators(configuratorPanel);
      configuratorPanel.attachConfigurators(
         new Toggle(
               GuiTextures.BUTTON_WORKING_ENABLE.getSubTexture(0.0, 0.5, 1.0, 0.5),
               GuiTextures.BUTTON_WORKING_ENABLE.getSubTexture(0.0, 0.0, 1.0, 0.5),
               () -> this.repeatedRecipes,
               (clickData, pressed) -> this.repeatedRecipes = pressed
            )
            .setTooltipsSupplier(
               pressed -> List.of(
                  Component.translatable("gtocore.machine.repeated_recipes", Component.translatable(pressed ? "gtocore.machine.on" : "gtocore.machine.off"))
               )
            )
      );
      configuratorPanel.attachConfigurators(
         new Toggle(
               GuiTextures.BUTTON_CHUNK_MODE.getSubTexture(0.0, 0.0, 1.0, 0.5),
               GuiTextures.BUTTON_CHUNK_MODE.getSubTexture(0.0, 0.5, 1.0, 0.5),
               () -> this.iThread,
               (clickData, pressed) -> this.iThread = pressed
            )
            .setTooltipsSupplier(
               pressed -> List.of(
                  Component.translatable("gtocore.machine.independent_thread", Component.translatable(pressed ? "gtocore.machine.on" : "gtocore.machine.off")),
                  Component.translatable("gtocore.machine.wireless_enable")
               )
            )
      );
   }

   @Generated
   public boolean isRepeatedRecipes() {
      return this.repeatedRecipes;
   }

   @Generated
   public boolean isIThread() {
      return this.iThread;
   }
}
