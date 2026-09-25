package com.gtolib.api.gui.ktflexible

import com.lowdragmc.lowdraglib.gui.widget.Widget
import com.lowdragmc.lowdraglib.utils.Position
import com.lowdragmc.lowdraglib.utils.Size
import java.util.ArrayList
import kotlin.jvm.internal.SourceDebugExtension

@SourceDebugExtension(["SMAP\nFlexibleContainerDefenition.kt\nKotlin\n*S Kotlin\n*F\n+ 1 FlexibleContainerDefenition.kt\ncom/gtolib/api/gui/ktflexible/HBox\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,280:1\n1924#2,3:281\n*S KotlinDebug\n*F\n+ 1 FlexibleContainerDefenition.kt\ncom/gtolib/api/gui/ktflexible/HBox\n*L\n120#1:281,3\n*E\n"])
public open class HBox(height: Int, spacing: Int = 0) : Box(0, height, spacing) {
   public final val height: Int

   public final var verticalCenteredWidget: ArrayList<Widget>
      internal set

   init {
      this.height = height
      this.verticalCenteredWidget = ArrayList<>()
   }

   protected override fun performLayout() {
      var currentX: Int = 0
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
            currentX += this.getSpacing()
         }

         widget.setSelfPosition(
            Position(currentX, if (this.verticalCenteredWidget.contains(widget)) this.containerHeight / 2 - widget.getSize().height / 2 else 0)
         )
         currentX += widget.getSize().width
      }

      this.updateSizeIfNeeded(FlexibleContainerDefenitionKt.check(Size(currentX, this.getSize().height)))
   }

   public open val containerWidth: Int
      public open get() {
         return Integer.MAX_VALUE
      }


   public open val containerHeight: Int
      public open get() {
         return this.height
      }

}
