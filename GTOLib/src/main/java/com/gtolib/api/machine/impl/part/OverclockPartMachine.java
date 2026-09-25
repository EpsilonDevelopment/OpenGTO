package com.gtolib.api.machine.impl.part;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.part.AmountConfigurationPartMachine;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

@DataGeneratorScanned
public final class OverclockPartMachine extends AmountConfigurationPartMachine {
   @RegisterLanguage(cn = "超频时间除数", en = "Divisor of duration")
   private static final String DIVISOR = "gtocore.machine.overclock_hatch.divisor";

   public OverclockPartMachine(MetaMachineBlockEntity holder, int tier) {
      super(holder, tier, 2L, tier - 6);
   }

   @Override
   public Widget createUIWidget() {
      return ((WidgetGroup)super.createUIWidget()).addWidget(new LabelWidget(24, -16, () -> "gtocore.machine.overclock_hatch.divisor"));
   }

   public double getCurrentMultiplier() {
      return 1.0 / this.getCurrent();
   }
}
