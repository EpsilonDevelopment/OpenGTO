package com.gtolib.api.gui.ktflexible

import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup
import com.lowdragmc.lowdraglib.gui.widget.Widget
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup.ScrollWheelDirection
import kotlin.jvm.internal.SourceDebugExtension

@SourceDebugExtension(["SMAP\nFlexibleContainerDefenition.kt\nKotlin\n*S Kotlin\n*F\n+ 1 FlexibleContainerDefenition.kt\ncom/gtolib/api/gui/ktflexible/VScrollBox\n+ 2 _Arrays.kt\nkotlin/collections/ArraysKt___ArraysKt\n*L\n1#1,280:1\n14048#2,2:281\n*S KotlinDebug\n*F\n+ 1 FlexibleContainerDefenition.kt\ncom/gtolib/api/gui/ktflexible/VScrollBox\n*L\n190#1:281,2\n*E\n"])
public class VScrollBox(width: Int, height: Int, spacing: Int = 2) : DraggableScrollableWidgetGroup(0, 0, width, height), ContainerSizeProvider {
   public final val width: Int
   public final val height: Int
   private final val contentContainer: VBox
   private final var isUpdatingContent: Boolean

   init {
      this.width = width
      this.height = height
      this.scrollWheelDirection = ScrollWheelDirection.VERTICAL
      this.setYScrollBarWidth(4)
      this.setYBarStyle(GuiTextures.SLIDER_BACKGROUND_VERTICAL as IGuiTexture, GuiTextures.BUTTON as IGuiTexture)
      this.draggable = false
      this.scrollable = true
      this.useScissor = true
      this.contentContainer = object : VBox {
         protected override fun onSizeUpdate() {
            if (!VScrollBox.this.isUpdatingContent) {
               VScrollBox.this.updateScrollArea()
            }
         }
      }
      super.addWidget(this.contentContainer as Widget)
   }

   public final val vBoxWidth: Int
      public final get() {
         return this.width - this.yBarWidth
      }


   public open val containerWidth: Int
      public open get() {
         return this.vBoxWidth
      }


   public open val containerHeight: Int
      public open get() {
         return Integer.MAX_VALUE
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

   public fun addContent(widget: Widget): VScrollBox {
      this.contentContainer.addWidget(widget)
      this.updateScrollArea()
      return this
   }

   public fun addContents(vararg widgets: Widget): VScrollBox {
      for (`element$iv` in widgets) {
         this.contentContainer.addWidget((Widget)`element$iv`)
      }

      this.updateScrollArea()
      return this
   }

   public fun removeContent(widget: Widget): VScrollBox {
      this.contentContainer.removeWidget(widget)
      this.updateScrollArea()
      return this
   }

   public fun clearContent(): VScrollBox {
      this.contentContainer.clearAllWidgets()
      this.updateScrollArea()
      return this
   }
}
