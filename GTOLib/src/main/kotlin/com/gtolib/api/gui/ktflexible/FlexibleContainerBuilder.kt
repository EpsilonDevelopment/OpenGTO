package com.gtolib.api.gui.ktflexible

import com.lowdragmc.lowdraglib.gui.widget.Widget
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup
import com.lowdragmc.lowdraglib.utils.Position
import com.lowdragmc.lowdraglib.utils.Size

data class Style(
    var paddingY: Int = 0,
    var paddingLeft: Int = 0,
    var paddingRight: Int = 0,
    var paddingBottom: Int = 0,
    var spacing: Int = 0,
) {
    constructor(init: Style.() -> Unit) : this() {
        init()
    }
}

abstract class LayoutBuilder<T : LayoutBuilder<T>> {

    protected val children: ArrayList<Widget> = ArrayList()

    protected var containerInfo: ContainerSizeProvider? = null

    abstract fun build(): Widget

    open fun widget(widget: Widget): Widget {
        children.add(widget)
        return widget
    }

    internal fun setContainerInfo(provider: ContainerSizeProvider) {
        containerInfo = provider
    }

    val availableWidth: Int
        get() = containerInfo?.containerWidth ?: Int.MAX_VALUE

    val availableHeight: Int
        get() = containerInfo?.containerHeight ?: Int.MAX_VALUE

    fun vBox(width: Int, style: Style.() -> Unit = {}, alwaysHorizonCenter: Boolean = false, init: VBoxBuilder.() -> Unit = {}) {
        val builder = VBoxBuilder(width, Style(style), alwaysHorizonCenter)
        builder.buildAndInit(init)
        widget(builder.getBuiltWidget())
    }

    fun hBox(height: Int, style: Style.() -> Unit = {}, alwaysVerticalCenter: Boolean = false, init: HBoxBuilder.() -> Unit = {}) {
        val builder = HBoxBuilder(height, Style(style), alwaysVerticalCenter)
        builder.buildAndInit(init)
        widget(builder.getBuiltWidget())
    }

    fun vScroll(width: Int, height: Int, style: Style.() -> Unit = {}, init: VScrollBuilder.() -> Unit = {}) {
        val builder = VScrollBuilder(width, height, Style(style))
        builder.buildAndInit(init)
        widget(builder.getBuiltWidget())
    }

    fun hScroll(width: Int, height: Int, style: Style.() -> Unit = {}, init: HScrollBuilder.() -> Unit = {}) {
        val builder = HScrollBuilder(width, height, Style(style))
        builder.buildAndInit(init)
        widget(builder.getBuiltWidget())
    }

    fun custom(customWidget: WidgetGroup, style: Style.() -> Unit = {}, init: CustomBuilder.() -> Unit = {}) {
        val builder = CustomBuilder(customWidget)
        builder.buildAndInit(init)
        widget(builder.getBuiltWidget())
    }

    abstract fun buildAndInit(init: T.() -> Unit)

    abstract fun getBuiltWidget(): Widget
}

class CustomBuilder(private val customWidget: WidgetGroup) : LayoutBuilder<CustomBuilder>() {

    override fun buildAndInit(init: CustomBuilder.() -> Unit) {
        setContainerInfo(object : ContainerSizeProvider {
            override val containerWidth: Int = customWidget.sizeWidth
            override val containerHeight: Int = customWidget.sizeHeight
        })
        init(this)
        children.forEach { customWidget.addWidget(it) }
    }

    override fun getBuiltWidget(): Widget {
        return customWidget
    }

    override fun build(): Widget {
        children.forEach { customWidget.addWidget(it) }
        return customWidget
    }
}

class VBoxBuilder(private val width: Int, val style: Style = Style {}, val alwaysHorizonCenter: Boolean = false) : LayoutBuilder<VBoxBuilder>() {

    private lateinit var vbox: VBox
    private val tempHorizonCenteredWidget: ArrayList<Widget> = ArrayList()

    fun widgetCenter(widget: Widget) {
        widget(widget)
        tempHorizonCenteredWidget.add(widget)
    }

    override fun widget(widget: Widget): Widget {
        super.widget(widget)
        if (alwaysHorizonCenter) {
            tempHorizonCenteredWidget.add(widget)
        }
        return widget
    }

    override fun buildAndInit(init: VBoxBuilder.() -> Unit) {
        vbox = VBox(width, style.spacing)
        setContainerInfo(vbox)
        init(this)
        vbox.horizonCenteredWidget.addAll(tempHorizonCenteredWidget)
        children.forEach { vbox.addWidget(it) }
    }

    override fun getBuiltWidget(): Widget {
        return vbox
    }

    override fun build(): Widget {
        if (vbox == null) {
            vbox = VBox(width, style.spacing)
            children.forEach { vbox.addWidget(it) }
        }
        return vbox
    }
}

class HBoxBuilder(private val height: Int, val style: Style = Style {}, val alwaysVerticalCenter: Boolean = false) : LayoutBuilder<HBoxBuilder>() {

    private lateinit var hbox: HBox
    private val tempVerticalCenteredWidget: ArrayList<Widget> = ArrayList()

    fun widgetCenter(widget: Widget) {
        widget(widget)
        tempVerticalCenteredWidget.add(widget)
    }

    override fun widget(widget: Widget): Widget {
        super.widget(widget)
        if (alwaysVerticalCenter) {
            tempVerticalCenteredWidget.add(widget)
        }
        return widget
    }

    override fun buildAndInit(init: HBoxBuilder.() -> Unit) {
        hbox = HBox(height, style.spacing)
        setContainerInfo(hbox)
        init(this)
        hbox.verticalCenteredWidget.addAll(tempVerticalCenteredWidget)
        children.forEach { hbox.addWidget(it) }
    }

    override fun getBuiltWidget(): Widget {
        return hbox
    }

    override fun build(): Widget {
        if (hbox == null) {
            hbox = HBox(height, style.spacing)
            children.forEach { hbox.addWidget(it) }
        }
        return hbox
    }
}

class VScrollBuilder(private val width: Int, private val height: Int, val style: Style = Style {}) : LayoutBuilder<VScrollBuilder>() {

    private lateinit var vscroll: VScrollBox

    override fun buildAndInit(init: VScrollBuilder.() -> Unit) {
        vscroll = VScrollBox(width, height, style.spacing)
        setContainerInfo(vscroll)
        init(this)
        children.forEach { vscroll.addContent(it) }
    }

    override fun getBuiltWidget(): Widget {
        return vscroll
    }

    override fun build(): Widget {
        if (vscroll == null) {
            vscroll = VScrollBox(width, height, style.spacing)
            children.forEach { vscroll.addContent(it) }
        }
        return vscroll
    }
}

class HScrollBuilder(private val width: Int, private val height: Int, val style: Style = Style {}) : LayoutBuilder<HScrollBuilder>() {

    private lateinit var hscroll: HScrollBox

    override fun buildAndInit(init: HScrollBuilder.() -> Unit) {
        hscroll = HScrollBox(width, height, style.spacing)
        setContainerInfo(hscroll)
        init(this)
        children.forEach { hscroll.addContent(it) }
    }

    override fun getBuiltWidget(): Widget {
        return hscroll
    }

    override fun build(): Widget {
        if (hscroll == null) {
            hscroll = HScrollBox(width, height, style.spacing)
            children.forEach { hscroll.addContent(it) }
        }
        return hscroll
    }
}

class RootBuilder(val width: Int, val height: Int) : LayoutBuilder<RootBuilder>() {

    private lateinit var root: WidgetGroup

    override fun buildAndInit(init: RootBuilder.() -> Unit) {
        root = WidgetGroup(Position.ORIGIN, Size(width, height))
        setContainerInfo(object : ContainerSizeProvider {
            override val containerWidth: Int = width
            override val containerHeight: Int = height
        })
        init(this)
        children.forEach { root.addWidget(it) }
    }

    override fun getBuiltWidget(): Widget {
        return root
    }

    override fun build(): Widget {
        if (root == null) {
            root = WidgetGroup(Position.ORIGIN, Size(width, height))
            children.forEach { root.addWidget(it) }
        }
        return root
    }
}
