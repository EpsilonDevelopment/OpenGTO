package com.gtolib.api.gui.ktflexible

import com.lowdragmc.lowdraglib.gui.widget.Widget
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup
import kotlin.jvm.internal.SourceDebugExtension

@SourceDebugExtension(["SMAP\nFlexibleContainerBuilder.kt\nKotlin\n*S Kotlin\n*F\n+ 1 FlexibleContainerBuilder.kt\ncom/gtolib/api/gui/ktflexible/CustomBuilder\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,227:1\n1915#2,2:228\n1915#2,2:230\n*S KotlinDebug\n*F\n+ 1 FlexibleContainerBuilder.kt\ncom/gtolib/api/gui/ktflexible/CustomBuilder\n*L\n193#1:228,2\n199#1:230,2\n*E\n"])
public class CustomBuilder(customWidget: WidgetGroup) : LayoutBuilder<CustomBuilder> {
   private final val customWidget: WidgetGroup

   init {
      this.customWidget = customWidget
   }

   public override fun buildAndInit(init: (CustomBuilder) -> Unit) {
      this.setContainerInfo$gtocore_forge_1_20_1(object : ContainerSizeProvider {
         public open val containerWidth: Int
         public open val containerHeight: Int

         {
            this.containerWidth = `$receiver`.customWidget.getSizeWidth()
            this.containerHeight = `$receiver`.customWidget.getSizeHeight()
         }
      })
      init(this)

      for (`element$iv` in this.getChildren()) {
         this.customWidget.addWidget(`element$iv` as Widget)
      }
   }

   public override fun getBuiltWidget(): Widget {
      return this.customWidget as Widget
   }

   public override fun build(): Widget {
      for (`element$iv` in this.getChildren()) {
         this.customWidget.addWidget(`element$iv` as Widget)
      }

      return this.customWidget as Widget
   }
}
