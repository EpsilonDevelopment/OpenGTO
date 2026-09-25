package com.gtolib.api.gui.ktflexible

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup
import com.lowdragmc.lowdraglib.utils.Position
import com.lowdragmc.lowdraglib.utils.Size

public abstract class FreshWidgetGroupAbstract : WidgetGroup {
   open fun FreshWidgetGroupAbstract(position: Position, size: Size) {
      super(position, size)
   }

   internal abstract fun fresh() {
   }

   public abstract fun requireFresh() {
   }

   public abstract fun serverFresh() {
   }
}
