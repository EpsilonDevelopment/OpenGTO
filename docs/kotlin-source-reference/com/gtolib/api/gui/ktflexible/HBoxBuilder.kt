package com.gtolib.api.gui.ktflexible

import com.lowdragmc.lowdraglib.gui.widget.Widget
import java.util.ArrayList
import kotlin.jvm.internal.Intrinsics
import kotlin.jvm.internal.SourceDebugExtension

@SourceDebugExtension(["SMAP\nFlexibleContainerBuilder.kt\nKotlin\n*S Kotlin\n*F\n+ 1 FlexibleContainerBuilder.kt\ncom/gtolib/api/gui/ktflexible/HBoxBuilder\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,227:1\n1915#2,2:228\n1915#2,2:230\n*S KotlinDebug\n*F\n+ 1 FlexibleContainerBuilder.kt\ncom/gtolib/api/gui/ktflexible/HBoxBuilder\n*L\n130#1:228,2\n138#1:230,2\n*E\n"])
public class HBoxBuilder(height: Int, style: Style = Style({ $this$Style: Style ->
      Unit.INSTANCE
   }), alwaysVerticalCenter: Boolean = false) : LayoutBuilder<HBoxBuilder> {
   private final val height: Int
   public final val style: Style
   public final val alwaysVerticalCenter: Boolean
   private final lateinit var hbox: HBox
   private final val tempVerticalCenteredWidget: ArrayList<Widget>

   init {
      this.height = height
      this.style = style
      this.alwaysVerticalCenter = alwaysVerticalCenter
      this.tempVerticalCenteredWidget = ArrayList<>()
   }

   public fun widgetCenter(widget: Widget) {
      this.widget(widget)
      this.tempVerticalCenteredWidget.add(widget)
   }

   public override fun widget(widget: Widget): Widget {
      super.widget(widget)
      if (this.alwaysVerticalCenter) {
         this.tempVerticalCenteredWidget.add(widget)
      }

      return widget
   }

   public override fun buildAndInit(init: (HBoxBuilder) -> Unit) {
      this.hbox = HBox(this.height, this.style.spacing)
      var var10001: HBox = this.hbox
      if (this.hbox == null) {
         Intrinsics.throwUninitializedPropertyAccessException("hbox")
         var10001 = null
      }

      this.setContainerInfo$gtocore_forge_1_20_1(var10001)
      init(this)
      var var10000: HBox = this.hbox
      if (this.hbox == null) {
         Intrinsics.throwUninitializedPropertyAccessException("hbox")
         var10000 = null
      }

      var10000.verticalCenteredWidget.addAll(this.tempVerticalCenteredWidget)

      for (`element$iv` in this.getChildren()) {
         val it: Widget = `element$iv` as Widget
         var10000 = this.hbox
         if (this.hbox == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hbox")
            var10000 = null
         }

         var10000.addWidget(it)
      }
   }

   public override fun getBuiltWidget(): Widget {
      var var10000: HBox = this.hbox
      if (this.hbox == null) {
         Intrinsics.throwUninitializedPropertyAccessException("hbox")
         var10000 = null
      }

      return var10000 as Widget
   }

   public override fun build(): Widget {
      if (this.hbox == null) {
         this.hbox = HBox(this.height, this.style.spacing)

         for (`element$iv` in this.getChildren()) {
            val it: Widget = `element$iv` as Widget
            var var10000: HBox = this.hbox
            if (this.hbox == null) {
               Intrinsics.throwUninitializedPropertyAccessException("hbox")
               var10000 = null
            }

            var10000.addWidget(it)
         }
      }

      var var7: HBox = this.hbox
      if (this.hbox == null) {
         Intrinsics.throwUninitializedPropertyAccessException("hbox")
         var7 = null
      }

      return var7 as Widget
   }
}
