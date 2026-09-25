package com.gtolib.api.machine.part;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.widget.LongInputWidget;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

public class AmountConfigurationPartMachine extends TieredPartMachine {
   protected final long min;
   private final long max;
   @SaveToDisk(defaultValue = "-1")
   protected long current = -1L;

   protected AmountConfigurationPartMachine(MetaMachineBlockEntity holder, int tier, long min, long max) {
      super(holder, tier);
      this.max = max;
      this.min = min;
   }

   @Override
   public Widget createUIWidget() {
      LongInputWidget longInput = new LongInputWidget(this::getCurrent, this::setCurrent);
      longInput.setMax(this.max);
      longInput.setMin(this.min);
      return new WidgetGroup(0, 0, 100, 20).addWidget(longInput);
   }

   @Override
   public boolean canShared() {
      return false;
   }

   private void setCurrent(long amount) {
      this.current = amount < this.min ? this.min : Math.min(amount, this.max);
      this.onAmountChange(this.current);
      this.onChanged();
   }

   protected long getCurrent() {
      if (this.current == -1L) {
         this.current = this.max;
      }

      return this.current;
   }

   protected void onAmountChange(long amount) {
   }
}
