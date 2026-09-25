package com.gtolib.api.gui.ktflexible

import com.lowdragmc.lowdraglib.LDLib
import com.lowdragmc.lowdraglib.gui.widget.Widget
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup
import com.lowdragmc.lowdraglib.utils.Position
import com.lowdragmc.lowdraglib.utils.Size
import net.minecraft.network.FriendlyByteBuf

fun root(width: Int, height: Int, init: RootBuilder.() -> Unit): WidgetGroup {
    val builder = RootBuilder(width, height)
    builder.buildAndInit(init)
    return builder.getBuiltWidget() as WidgetGroup
}

fun rootFresh(width: Int, height: Int, init: RootBuilder.() -> Unit): FreshWidgetGroupAbstract {
    val rootContainer = object : FreshWidgetGroupAbstract(Position.ORIGIN, Size(width, height)) {

        override fun fresh() {
            clearAllWidgets()
            val builder = RootBuilder(width, height)
            builder.buildAndInit(init)
            addWidget(builder.getBuiltWidget() as WidgetGroup)
            initWidget()
        }

        override fun requireFresh() {
            if (LDLib.isRemote()) {
                writeClientAction(947) { buf: FriendlyByteBuf -> buf.writeBoolean(true) }
            }
        }

        override fun serverFresh() {
            writeUpdateInfo(947) { buf: FriendlyByteBuf -> buf.writeBoolean(true) }
            fresh()
        }

        override fun readUpdateInfo(id: Int, buffer: FriendlyByteBuf?) {
            if (id == 947) {
                fresh()
            } else {
                super.readUpdateInfo(id, buffer)
            }
        }

        override fun handleClientAction(id: Int, buffer: FriendlyByteBuf?) {
            if (id == 947) {
                serverFresh()
            } else {
                super.handleClientAction(id, buffer)
            }
        }
    }
    rootContainer.fresh()
    return rootContainer
}

fun vBox(width: Int, style: Style.() -> Unit = {}, init: VBoxBuilder.() -> Unit): WidgetGroup {
    val styleObject = Style().apply(style)
    val builder = VBoxBuilder(width, styleObject, false)
    builder.buildAndInit(init)
    return builder.getBuiltWidget() as WidgetGroup
}

fun hBox(height: Int, style: Style.() -> Unit = {}, init: HBoxBuilder.() -> Unit): WidgetGroup {
    val styleObject = Style().apply(style)
    val builder = HBoxBuilder(height, styleObject, false)
    builder.buildAndInit(init)
    return builder.getBuiltWidget() as WidgetGroup
}

fun vScroll(width: Int, height: Int, init: VScrollBuilder.() -> Unit): WidgetGroup {
    val builder = VScrollBuilder(width, height)
    builder.buildAndInit(init)
    return builder.getBuiltWidget() as WidgetGroup
}

fun hScroll(width: Int, height: Int, init: HScrollBuilder.() -> Unit): WidgetGroup {
    val builder = HScrollBuilder(width, height)
    builder.buildAndInit(init)
    return builder.getBuiltWidget() as WidgetGroup
}

abstract class FreshWidgetGroupAbstract(position: Position, size: Size) : WidgetGroup(position, size) {

    internal abstract fun fresh()

    abstract fun requireFresh()

    abstract fun serverFresh()
}
