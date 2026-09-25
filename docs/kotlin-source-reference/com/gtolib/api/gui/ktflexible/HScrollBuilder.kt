package com.gtolib.api.gui.ktflexible

import com.lowdragmc.lowdraglib.gui.widget.Widget
import kotlin.jvm.internal.Intrinsics
import kotlin.jvm.internal.SourceDebugExtension

@SourceDebugExtension(["SMAP\nFlexibleContainerBuilder.kt\nKotlin\n*S Kotlin\n*F\n+ 1 FlexibleContainerBuilder.kt\ncom/gtolib/api/gui/ktflexible/HScrollBuilder\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,227:1\n1915#2,2:228\n1915#2,2:230\n*S KotlinDebug\n*F\n+ 1 FlexibleContainerBuilder.kt\ncom/gtolib/api/gui/ktflexible/HScrollBuilder\n*L\n172#1:228,2\n180#1:230,2\n*E\n"])
public class HScrollBuilder(width: Int, height: Int, style: Style = Style({ $this$Style: Style ->
      Unit.INSTANCE
   })) : LayoutBuilder<HScrollBuilder> {
   private final val width: Int
   private final val height: Int
   public final val style: Style
   private final lateinit var hscroll: HScrollBox

   init {
      this.width = width
      this.height = height
      this.style = style
   }

   public override fun buildAndInit(init: (HScrollBuilder) -> Unit) {
      this.hscroll = HScrollBox(this.width, this.height, this.style.spacing)
      var var10001: HScrollBox = this.hscroll
      if (this.hscroll == null) {
         Intrinsics.throwUninitializedPropertyAccessException("hscroll")
         var10001 = null
      }

      this.setContainerInfo$gtocore_forge_1_20_1(var10001)
      init(this)

      for (`element$iv` in this.getChildren()) {
         val it: Widget = `element$iv` as Widget
         var var10000: HScrollBox = this.hscroll
         if (this.hscroll == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hscroll")
            var10000 = null
         }

         var10000.addContent(it)
      }
   }

   public override fun getBuiltWidget(): Widget {
      var var10000: HScrollBox = this.hscroll
      if (this.hscroll == null) {
         Intrinsics.throwUninitializedPropertyAccessException("hscroll")
         var10000 = null
      }

      return var10000 as Widget
   }

   public override fun build(): Widget {
      if (this.hscroll == null) {
         this.hscroll = HScrollBox(this.width, this.height, this.style.spacing)

         for (`element$iv` in this.getChildren()) {
            val it: Widget = `element$iv` as Widget
            var var10000: HScrollBox = this.hscroll
            if (this.hscroll == null) {
               Intrinsics.throwUninitializedPropertyAccessException("hscroll")
               var10000 = null
            }

            var10000.addContent(it)
         }
      }

      var var7: HScrollBox = this.hscroll
      if (this.hscroll == null) {
         Intrinsics.throwUninitializedPropertyAccessException("hscroll")
         var7 = null
      }

      return var7 as Widget
   }
}
