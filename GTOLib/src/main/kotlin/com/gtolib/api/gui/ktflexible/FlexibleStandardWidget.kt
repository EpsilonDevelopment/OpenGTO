package com.gtolib.api.gui.ktflexible

import com.google.common.collect.ImmutableList
import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture
import com.lowdragmc.lowdraglib.gui.texture.TextTexture
import com.lowdragmc.lowdraglib.gui.util.ClickData
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget
import com.lowdragmc.lowdraglib.gui.widget.TextTextureWidget
import com.lowdragmc.lowdraglib.gui.widget.Widget
import java.util.function.Consumer
import java.util.function.Supplier
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.Component

fun LayoutBuilder<*>.button(
    width: Int = 40,
    height: Int = 16,
    text: Supplier<String>? = null,
    transKey: String? = null,
    onClick: ((ClickData) -> Unit)? = null,
    onSimpleClick: (() -> Unit)? = null,
): Widget {
    val textSup: Supplier<String> = text ?: if (transKey != null) Supplier { Component.translatable(transKey).string } else Supplier { "Button" }
    val button = object : ButtonWidget(
        0,
        0,
        width,
        height,
        GuiTextureGroup(GuiTextures.BUTTON, TextTexture { textSup.get() }),
        Consumer<ClickData> { clickData ->
            if (onClick != null) {
                onClick.invoke(clickData)
            } else {
                onSimpleClick?.invoke()
            }
        },
    ) {
        private var lastText: String = textSup.get()
        private var firstUpdate: Boolean = true

        override fun detectAndSendChanges() {
            super.detectAndSendChanges()
            if (lastText != textSup.get() || firstUpdate) {
                firstUpdate = false
                writeUpdateInfo(945) { buf: FriendlyByteBuf -> buf.writeUtf(textSup.get()) }
                lastText = textSup.get()
            }
        }

        override fun readUpdateInfo(id: Int, buffer: FriendlyByteBuf?) {
            if (id == 945 && buffer != null) {
                val newText = buffer.readUtf()
                setButtonTexture(GuiTextureGroup(GuiTextures.BUTTON, TextTexture { newText }))
                setClickedTexture(GuiTextureGroup(GuiTextures.SLOT, TextTexture { newText }))
            } else {
                super.readUpdateInfo(id, buffer)
            }
        }

        override fun updateScreen() {
            super.updateScreen()
            if (isClientSideWidget && lastText != textSup.get()) {
                setButtonTexture(GuiTextureGroup(GuiTextures.BUTTON, TextTexture { textSup.get() }))
                lastText = textSup.get()
            }
        }
    }.setClickedTexture(GuiTextureGroup(GuiTextures.SLOT, TextTexture { textSup.get() }))
    return widget(button as Widget)
}

fun LayoutBuilder<*>.iconButton(
    width: Int = 16,
    height: Int = 16,
    tooltips: Supplier<Component>? = null,
    icon: IGuiTexture? = null,
    onClick: ((ClickData) -> Unit)? = null,
    onSimpleClick: (() -> Unit)? = null,
): Widget {
    val buttonTexture = if (icon != null) GuiTextureGroup(GuiTextures.BUTTON, icon) else GuiTextureGroup(GuiTextures.BUTTON)
    val button = object : ButtonWidget(
        0,
        0,
        width,
        height,
        buttonTexture,
        Consumer<ClickData> { clickData ->
            if (onClick != null) {
                onClick.invoke(clickData)
            } else {
                onSimpleClick?.invoke()
            }
        },
    ) {
        private var lastTooltip: Component? = tooltips?.get()
        private var firstUpdate: Boolean = true

        override fun detectAndSendChanges() {
            super.detectAndSendChanges()
            val tooltipSupplier = tooltips
            if (tooltipSupplier != null) {
                val current = lastTooltip
                if (current != null) {
                    val next = tooltipSupplier.get()
                    if (current.string != next.string || firstUpdate) {
                        firstUpdate = false
                        lastTooltip = next
                        writeUpdateInfo(946) { buf: FriendlyByteBuf -> buf.writeComponent(lastTooltip!!) }
                    }
                    setHoverTooltips(ImmutableList.of(lastTooltip!!))
                }
            }
        }

        override fun readUpdateInfo(id: Int, buffer: FriendlyByteBuf?) {
            if (id == 946 && buffer != null) {
                lastTooltip = buffer.readComponent()
                setHoverTooltips(lastTooltip)
            } else {
                super.readUpdateInfo(id, buffer)
            }
        }
    }.setClickedTexture(
        if (icon != null) GuiTextureGroup(GuiTextures.SLOT, icon) else GuiTextureGroup(GuiTextures.SLOT)
    )
    if (tooltips != null) {
        button.setHoverTooltips(tooltips.get())
    }
    return widget(button as Widget)
}

fun LayoutBuilder<*>.button(
    width: Int = 40,
    height: Int = 16,
    transKet: String = "unKnownTranslateKet",
    onClick: (ClickData) -> Unit = {},
) {
    button(width, height, transKey = transKet, onClick = onClick)
}

fun LayoutBuilder<*>.text(
    width: Int = 80,
    height: Int = 10,
    text: Supplier<Component> = Supplier { Component.literal("Text") },
    init: TextTextureWidget.() -> Unit = {},
): Widget {
    val textWidget = TextTextureWidget(0, 0, width, height)
    textWidget.setText(text)
    init(textWidget)
    return widget(textWidget as Widget)
}

fun LayoutBuilder<*>.blank(width: Int = 0, height: Int = 0) {
    widget(Widget(0, 0, width, height))
}

fun LayoutBuilder<*>.field(
    width: Int = 50,
    height: Int = 16,
    getter: Supplier<String>,
    setter: Consumer<String>,
    rightClickClear: Boolean = false,
): TextFieldWidget {
    val field = object : TextFieldWidget(0, 0, width, height - 2, getter, setter) {
        override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
            if (rightClickClear && button == 1 && isMouseOverElement(mouseX, mouseY)) {
                textField.setValue("")
                return true
            }
            return super.mouseClicked(mouseX, mouseY, button)
        }
    }
    vBox(
        width,
        { spacing = 0 },
        init = {
            blank(1, 1)
            widget(field as Widget)
            blank(1, 1)
        },
    )
    return field
}
