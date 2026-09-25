package com.gtolib.api.gui.ktflexible

import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup
import com.lowdragmc.lowdraglib.gui.widget.Widget
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup.ScrollWheelDirection
import kotlin.jvm.internal.SourceDebugExtension

@SourceDebugExtension(["SMAP\nFlexibleContainerDefenition.kt\nKotlin\n*S Kotlin\n*F\n+ 1 FlexibleContainerDefenition.kt\ncom/gtolib/api/gui/ktflexible/HScrollBox\n+ 2 _Arrays.kt\nkotlin/collections/ArraysKt___ArraysKt\n*L\n1#1,280:1\n14048#2,2:281\n*S KotlinDebug\n*F\n+ 1 FlexibleContainerDefenition.kt\ncom/gtolib/api/gui/ktflexible/HScrollBox\n*L\n263#1:281,2\n*E\n"])
public class HScrollBox(width: Int, height: Int, spacing: Int = 2) : DraggableScrollableWidgetGroup(0, 0, width, height), ContainerSizeProvider {
   public final val width: Int
   public final val height: Int
   private final val contentContainer: HBox
   private final var isUpdatingContent: Boolean

   init {
      this.width = width
      this.height = height
      this.scrollWheelDirection = ScrollWheelDirection.HORIZONTAL
      this.setXScrollBarHeight(4)
      this.setXBarStyle(GuiTextures.SLIDER_BACKGROUND_VERTICAL as IGuiTexture, GuiTextures.BUTTON as IGuiTexture)
      this.draggable = false
      this.scrollable = true
      this.useScissor = true
      this.contentContainer = object : HBox {
         protected override fun onSizeUpdate() {
            if (!HScrollBox.this.isUpdatingContent) {
               HScrollBox.this.updateScrollArea()
            }
         }
      }
      super.addWidget(this.contentContainer as Widget)
   }

   public final val hBoxHeight: Int
      public final get() {
         return this.height - this.xBarHeight
      }


   public open val containerWidth: Int
      public open get() {
         return this.width
      }


   public open val containerHeight: Int
      public open get() {
         return this.hBoxHeight
      }


   private fun updateScrollArea() {
      if (!this.isUpdatingContent) {
         this.isUpdatingContent = true

         try {
            this.computeMax()
         } finally {
            this.isUpdatingContent = false
         }
      }
   }

   public fun addContent(widget: Widget): HScrollBox {
      this.contentContainer.addWidget(widget)
      this.updateScrollArea()
      return this
   }

   public fun addContents(vararg widgets: Widget): HScrollBox {
      for (`element$iv` in widgets) {
         this.contentContainer.addWidget((Widget)`element$iv`)
      }

      this.updateScrollArea()
      return this
   }

   public fun removeContent(widget: Widget): HScrollBox {
      this.contentContainer.removeWidget(widget)
      this.updateScrollArea()
      return this
   }

   public fun clearContent(): HScrollBox {
      this.contentContainer.clearAllWidgets()
      this.updateScrollArea()
      return this
   }
}
