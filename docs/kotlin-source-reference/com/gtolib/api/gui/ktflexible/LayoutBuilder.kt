package com.gtolib.api.gui.ktflexible

import com.lowdragmc.lowdraglib.gui.widget.Widget
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup
import java.util.ArrayList

public abstract class LayoutBuilder<T extends LayoutBuilder<T>> {
   protected final val children: ArrayList<Widget> = ArrayList()

   protected final var containerInfo: ContainerSizeProvider?
      internal set

   public abstract fun build(): Widget {
   }

   public open fun widget(widget: Widget): Widget {
      this.children.add(widget)
      return widget
   }

   internal fun setContainerInfo(provider: ContainerSizeProvider) {
      this.containerInfo = provider
   }

   public final val availableWidth: Int
      public final get() {
         return if (this.containerInfo != null) this.containerInfo.containerWidth else Integer.MAX_VALUE
      }


   public final val availableHeight: Int
      public final get() {
         return if (this.containerInfo != null) this.containerInfo.containerHeight else Integer.MAX_VALUE
      }


   public fun vBox(width: Int, style: (Style) -> Unit = { var0: Style ->
         Unit.INSTANCE
      }, alwaysHorizonCenter: Boolean = false, init: (VBoxBuilder) -> Unit = { var0: VBoxBuilder ->
         Unit.INSTANCE
      }) {
      val builder: VBoxBuilder = VBoxBuilder(width, Style(style), alwaysHorizonCenter)
      builder.buildAndInit(init)
      this.widget(builder.getBuiltWidget())
   }

   public fun hBox(height: Int, style: (Style) -> Unit = { var0: Style ->
         Unit.INSTANCE
      }, alwaysVerticalCenter: Boolean = false, init: (HBoxBuilder) -> Unit = { var0: HBoxBuilder ->
         Unit.INSTANCE
      }) {
      val builder: HBoxBuilder = HBoxBuilder(height, Style(style), alwaysVerticalCenter)
      builder.buildAndInit(init)
      this.widget(builder.getBuiltWidget())
   }

   public fun vScroll(width: Int, height: Int, style: (Style) -> Unit = { var0: Style ->
         Unit.INSTANCE
      }, init: (VScrollBuilder) -> Unit = { var0: VScrollBuilder ->
         Unit.INSTANCE
      }) {
      val builder: VScrollBuilder = VScrollBuilder(width, height, Style(style))
      builder.buildAndInit(init)
      this.widget(builder.getBuiltWidget())
   }

   public fun hScroll(width: Int, height: Int, style: (Style) -> Unit = { var0: Style ->
         Unit.INSTANCE
      }, init: (HScrollBuilder) -> Unit = { var0: HScrollBuilder ->
         Unit.INSTANCE
      }) {
      val builder: HScrollBuilder = HScrollBuilder(width, height, Style(style))
      builder.buildAndInit(init)
      this.widget(builder.getBuiltWidget())
   }

   public fun custom(customWidget: WidgetGroup, style: (Style) -> Unit = { var0: Style ->
         Unit.INSTANCE
      }, init: (CustomBuilder) -> Unit = { var0: CustomBuilder ->
         Unit.INSTANCE
      }) {
      val builder: CustomBuilder = CustomBuilder(customWidget)
      builder.buildAndInit(init)
      this.widget(builder.getBuiltWidget())
   }

   public abstract fun buildAndInit(init: (Any) -> Unit) {
   }

   public abstract fun getBuiltWidget(): Widget {
   }
}
