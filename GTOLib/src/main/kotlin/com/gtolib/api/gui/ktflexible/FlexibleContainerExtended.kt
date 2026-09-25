package com.gtolib.api.gui.ktflexible

import com.lowdragmc.lowdraglib.gui.widget.Widget
import net.minecraft.client.gui.GuiGraphics
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn

fun LayoutBuilder<*>.vBoxThreeColumn(
    width: Int,
    spacing: Int = 0,
    between: Int = 0,
    drawInBackgroundInit: (GuiGraphics, Int, Int, Float, VBox) -> Unit = { _, _, _, _, _ -> },
    init: VBoxBuilder.() -> Unit,
) {
    custom(
        object : VBox(width, spacing) {
            @OnlyIn(Dist.CLIENT)
            override fun drawInBackground(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTicks: Float) {
                super.drawInBackground(graphics, mouseX, mouseY, partialTicks)
                drawInBackgroundInit(graphics, mouseX, mouseY, partialTicks, this)
            }
        },
        init = {
            hBox(
                Int.MAX_VALUE,
                { this.spacing = 0 },
                init = {
                    widget(Widget(0, 0, between, 1))
                    vBox(
                        width - between - between,
                        { this.spacing = spacing },
                        init = {
                            init()
                        },
                    )
                    widget(Widget(0, 0, between, 1))
                },
            )
        },
    )
}
