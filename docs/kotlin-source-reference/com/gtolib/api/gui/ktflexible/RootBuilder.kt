package com.gtolib.api.gui.ktflexible

import com.lowdragmc.lowdraglib.gui.widget.Widget
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup
import com.lowdragmc.lowdraglib.utils.Position
import com.lowdragmc.lowdraglib.utils.Size
import kotlin.jvm.internal.Intrinsics
import kotlin.jvm.internal.SourceDebugExtension

@SourceDebugExtension(["SMAP\nFlexibleContainerBuilder.kt\nKotlin\n*S Kotlin\n*F\n+ 1 FlexibleContainerBuilder.kt\ncom/gtolib/api/gui/ktflexible/RootBuilder\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,227:1\n1915#2,2:228\n1915#2,2:230\n*S KotlinDebug\n*F\n+ 1 FlexibleContainerBuilder.kt\ncom/gtolib/api/gui/ktflexible/RootBuilder\n*L\n214#1:228,2\n222#1:230,2\n*E\n"])
public class RootBuilder(width: Int, height: Int) : LayoutBuilder<RootBuilder> {
   public final val width: Int
   public final val height: Int
   private final lateinit var root: WidgetGroup

   init {
      this.width = width
      this.height = height
   }

   public override fun buildAndInit(init: (RootBuilder) -> Unit) {
      this.root = WidgetGroup(Position.ORIGIN, Size(this.width, this.height))
      this.setContainerInfo$gtocore_forge_1_20_1(object : ContainerSizeProvider {
         public open val containerWidth: Int
         public open val containerHeight: Int

         {
            this.containerWidth = `$receiver`.width
            this.containerHeight = `$receiver`.height
         }
      })
      init(this)

      for (`element$iv` in this.getChildren()) {
         val it: Widget = `element$iv` as Widget
         var var10000: WidgetGroup = this.root
         if (this.root == null) {
            Intrinsics.throwUninitializedPropertyAccessException("root")
            var10000 = null
         }

         var10000.addWidget(it)
      }
   }

   public override fun getBuiltWidget(): Widget {
      var var10000: WidgetGroup = this.root
      if (this.root == null) {
         Intrinsics.throwUninitializedPropertyAccessException("root")
         var10000 = null
      }

      return var10000 as Widget
   }

   public override fun build(): Widget {
      if (this.root == null) {
         this.root = WidgetGroup(Position.ORIGIN, Size(this.width, this.height))

         for (`element$iv` in this.getChildren()) {
            val it: Widget = `element$iv` as Widget
            var var10000: WidgetGroup = this.root
            if (this.root == null) {
               Intrinsics.throwUninitializedPropertyAccessException("root")
               var10000 = null
            }

            var10000.addWidget(it)
         }
      }

      var var7: WidgetGroup = this.root
      if (this.root == null) {
         Intrinsics.throwUninitializedPropertyAccessException("root")
         var7 = null
      }

      return var7 as Widget
   }
}
