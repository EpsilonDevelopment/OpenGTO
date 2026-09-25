package com.gtolib.api.gui.ktflexible

import com.lowdragmc.lowdraglib.gui.widget.Widget
import net.minecraft.client.gui.GuiGraphics
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn

public fun LayoutBuilder<*>.vBoxThreeColumn(
   width: Int,
   spacing: Int = 0,
   between: Int = 0,
   drawInBackgroundInit: (GuiGraphics, Int, Int, Float, VBox) -> Unit = { var0: GuiGraphics, var1: Int, var2: Int, var3: Float, var4: VBox ->
         Unit.INSTANCE
      },
   init: (VBoxBuilder) -> Unit
) {
   `$this$vBoxThreeColumn`.custom(object : VBox {
      @OnlyIn(Dist.CLIENT)
      public open fun drawInBackground(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTicks: Float) {
         super.drawInBackground(graphics, mouseX, mouseY, partialTicks)
         drawInBackgroundInit(graphics, mouseX, mouseY, partialTicks, this)
      }
   }, init = { $this$custom: CustomBuilder ->
      `$this$custom`.hBox(Integer.MAX_VALUE, { $this$hBox: Style ->
         `$this$hBox`.spacing = 0
         Unit.INSTANCE
      }, init = { $this$hBox: HBoxBuilder ->
         `$this$hBox`.widget(Widget(0, 0, `$between`, 1))
         `$this$hBox`.vBox(`$width` - `$between` - `$between`, { $this$vBox: Style ->
            `$this$vBox`.spacing = `$spacing`
            Unit.INSTANCE
         }, init = { $this$vBox: VBoxBuilder ->
            `$init`(`$this$vBox`)
            Unit.INSTANCE
         }, 4, null)
         `$this$hBox`.widget(Widget(0, 0, `$between`, 1))
         Unit.INSTANCE
      }, 4, null)
      Unit.INSTANCE
   }, 2, null)
}
