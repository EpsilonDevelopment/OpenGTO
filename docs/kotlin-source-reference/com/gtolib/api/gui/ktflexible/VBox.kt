package com.gtolib.api.gui.ktflexible

import com.lowdragmc.lowdraglib.gui.widget.Widget
import com.lowdragmc.lowdraglib.utils.Position
import com.lowdragmc.lowdraglib.utils.Size
import java.util.ArrayList
import kotlin.jvm.internal.SourceDebugExtension

@SourceDebugExtension(["SMAP\nFlexibleContainerDefenition.kt\nKotlin\n*S Kotlin\n*F\n+ 1 FlexibleContainerDefenition.kt\ncom/gtolib/api/gui/ktflexible/VBox\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,280:1\n1924#2,3:281\n*S KotlinDebug\n*F\n+ 1 FlexibleContainerDefenition.kt\ncom/gtolib/api/gui/ktflexible/VBox\n*L\n94#1:281,3\n*E\n"])
public open class VBox(width: Int, spacing: Int = 0) : Box(width, 0, spacing) {
   public final val width: Int

   public final var horizonCenteredWidget: ArrayList<Widget>
      internal set

   init {
      this.width = width
      this.horizonCenteredWidget = ArrayList<>()
   }

   protected override fun performLayout() {
      var currentY: Int = 0
      val var10000: java.util.List = this.widgets
      val `$this$forEachIndexed$iv`: java.lang.Iterable = var10000
      var `index$iv`: Int = 0

      for (`item$iv` in `$this$forEachIndexed$iv`) {
         val var7: Int = `index$iv`++
         if (var7 < 0) {
            CollectionsKt.throwIndexOverflow()
         }

         val widget: Widget = `item$iv` as Widget
         if (var7 > 0) {
            currentY += this.getSpacing()
         }

         widget.setSelfPosition(
            Position(if (this.horizonCenteredWidget.contains(widget)) this.containerWidth / 2 - widget.getSize().width / 2 else 0, currentY)
         )
         currentY += widget.getSize().height
      }

      this.updateSizeIfNeeded(FlexibleContainerDefenitionKt.check(Size(this.getSize().width, currentY)))
   }

   public open val containerWidth: Int
      public open get() {
         return this.width
      }


   public open val containerHeight: Int
      public open get() {
         return Integer.MAX_VALUE
      }

}
