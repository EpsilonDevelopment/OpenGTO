package com.gtolib.api.gui.ktflexible

import com.lowdragmc.lowdraglib.gui.widget.Widget
import java.util.ArrayList
import kotlin.jvm.internal.Intrinsics
import kotlin.jvm.internal.SourceDebugExtension

@SourceDebugExtension(["SMAP\nFlexibleContainerBuilder.kt\nKotlin\n*S Kotlin\n*F\n+ 1 FlexibleContainerBuilder.kt\ncom/gtolib/api/gui/ktflexible/VBoxBuilder\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,227:1\n1915#2,2:228\n1915#2,2:230\n*S KotlinDebug\n*F\n+ 1 FlexibleContainerBuilder.kt\ncom/gtolib/api/gui/ktflexible/VBoxBuilder\n*L\n95#1:228,2\n104#1:230,2\n*E\n"])
public class VBoxBuilder(width: Int, style: Style = Style({ $this$Style: Style ->
      Unit.INSTANCE
   }), alwaysHorizonCenter: Boolean = false) : LayoutBuilder<VBoxBuilder> {
   private final val width: Int
   public final val style: Style
   public final val alwaysHorizonCenter: Boolean
   private final lateinit var vbox: VBox
   private final val tempHorizonCenteredWidget: ArrayList<Widget>

   init {
      this.width = width
      this.style = style
      this.alwaysHorizonCenter = alwaysHorizonCenter
      this.tempHorizonCenteredWidget = ArrayList<>()
   }

   public fun widgetCenter(widget: Widget) {
      this.widget(widget)
      this.tempHorizonCenteredWidget.add(widget)
   }

   public override fun widget(widget: Widget): Widget {
      super.widget(widget)
      if (this.alwaysHorizonCenter) {
         this.tempHorizonCenteredWidget.add(widget)
      }

      return widget
   }

   public override fun buildAndInit(init: (VBoxBuilder) -> Unit) {
      this.vbox = VBox(this.width, this.style.spacing)
      var var10001: VBox = this.vbox
      if (this.vbox == null) {
         Intrinsics.throwUninitializedPropertyAccessException("vbox")
         var10001 = null
      }

      this.setContainerInfo$gtocore_forge_1_20_1(var10001)
      init(this)
      var var10000: VBox = this.vbox
      if (this.vbox == null) {
         Intrinsics.throwUninitializedPropertyAccessException("vbox")
         var10000 = null
      }

      var10000.horizonCenteredWidget.addAll(this.tempHorizonCenteredWidget)

      for (`element$iv` in this.getChildren()) {
         val it: Widget = `element$iv` as Widget
         var10000 = this.vbox
         if (this.vbox == null) {
            Intrinsics.throwUninitializedPropertyAccessException("vbox")
            var10000 = null
         }

         var10000.addWidget(it)
      }
   }

   public override fun getBuiltWidget(): Widget {
      var var10000: VBox = this.vbox
      if (this.vbox == null) {
         Intrinsics.throwUninitializedPropertyAccessException("vbox")
         var10000 = null
      }

      return var10000 as Widget
   }

   public override fun build(): Widget {
      if (this.vbox == null) {
         this.vbox = VBox(this.width, this.style.spacing)

         for (`element$iv` in this.getChildren()) {
            val it: Widget = `element$iv` as Widget
            var var10000: VBox = this.vbox
            if (this.vbox == null) {
               Intrinsics.throwUninitializedPropertyAccessException("vbox")
               var10000 = null
            }

            var10000.addWidget(it)
         }
      }

      var var7: VBox = this.vbox
      if (this.vbox == null) {
         Intrinsics.throwUninitializedPropertyAccessException("vbox")
         var7 = null
      }

      return var7 as Widget
   }
}
