package com.gtolib.api.gui.ktflexible

import com.lowdragmc.lowdraglib.gui.widget.Widget
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup
import com.lowdragmc.lowdraglib.utils.Position
import com.lowdragmc.lowdraglib.utils.Size

public abstract class Box : WidgetGroup, ContainerSizeProvider {
   public final var spacing: Int
      internal set

   protected final var isLayOuting: Boolean
      internal set

   open fun Box(width: Int, height: Int, spacing: Int) {
      super(Position.ORIGIN, FlexibleContainerDefenitionKt.check(Size(width, height)))
      this.spacing = spacing
      this.setDynamicSized(true)
   }

   public open fun isMouseOverElement(mouseX: Double, mouseY: Double): Boolean {
      return super.isMouseOverElement(mouseX, mouseY)
   }

   protected open fun onSizeUpdate() {
      if (!this.isLayOuting) {
         super.onSizeUpdate()
      }
   }

   public open fun removeWidget(widget: Widget) {
      super.removeWidget(widget)
      this.scheduleLayoutUpdate()
   }

   protected open fun recomputeLayout() {
      if (!this.isLayOuting) {
         this.isLayOuting = true

         try {
            this.performLayout()
         } finally {
            this.isLayOuting = false
         }
      }
   }

   protected fun scheduleLayoutUpdate() {
      if (!this.isLayOuting && this.isDynamicSized()) {
         this.recomputeLayout()
      }
   }

   protected open fun performLayout() {
   }

   protected fun updateSizeIfNeeded(newSize: Size) {
      if (this.isDynamicSized() && !(newSize == this.getSize())) {
         this.setSize(FlexibleContainerDefenitionKt.check(newSize))
         if (this.parent != null) {
            super.onSizeUpdate()
         }
      }
   }

   public open fun addWidget(index: Int, widget: Widget): WidgetGroup {
      super.addWidget(index, widget)
      this.scheduleLayoutUpdate()
      return this
   }
}
