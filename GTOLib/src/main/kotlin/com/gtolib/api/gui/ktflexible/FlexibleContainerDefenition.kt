package com.gtolib.api.gui.ktflexible

import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup.ScrollWheelDirection
import com.lowdragmc.lowdraglib.gui.widget.Widget
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup
import com.lowdragmc.lowdraglib.utils.Position
import com.lowdragmc.lowdraglib.utils.Size

fun Size.check(): Size {
    if (width >= 0 && height >= 0) {
        return this
    }
    throw IllegalArgumentException("Flexible Container : size can not be negative")
}

interface ContainerSizeProvider {
    val containerWidth: Int
    val containerHeight: Int
}

abstract class Box(width: Int, height: Int, spacing: Int) : WidgetGroup(Position.ORIGIN, Size(width, height).check()), ContainerSizeProvider {

    var spacing: Int

    protected var isLayOuting: Boolean = false

    init {
        this.spacing = spacing
        setDynamicSized(true)
    }

    override fun isMouseOverElement(mouseX: Double, mouseY: Double): Boolean {
        return super.isMouseOverElement(mouseX, mouseY)
    }

    override fun onSizeUpdate() {
        if (!isLayOuting) {
            super.onSizeUpdate()
        }
    }

    override fun removeWidget(widget: Widget) {
        super.removeWidget(widget)
        scheduleLayoutUpdate()
    }

    protected override fun recomputeLayout() {
        if (!isLayOuting) {
            isLayOuting = true
            try {
                performLayout()
            } finally {
                isLayOuting = false
            }
        }
    }

    protected fun scheduleLayoutUpdate() {
        if (!isLayOuting && isDynamicSized()) {
            recomputeLayout()
        }
    }

    protected open fun performLayout() {
    }

    protected fun updateSizeIfNeeded(newSize: Size) {
        if (isDynamicSized() && newSize != size) {
            setSize(newSize.check())
            if (parent != null) {
                super.onSizeUpdate()
            }
        }
    }

    override fun addWidget(index: Int, widget: Widget): WidgetGroup {
        super.addWidget(index, widget)
        scheduleLayoutUpdate()
        return this
    }
}

open class HBox(height: Int, spacing: Int = 0) : Box(0, height, spacing) {

    val height: Int

    var verticalCenteredWidget: ArrayList<Widget>

    init {
        this.height = height
        this.verticalCenteredWidget = ArrayList()
    }

    override fun performLayout() {
        var currentX = 0
        widgets.forEachIndexed { index, widget ->
            if (index > 0) {
                currentX += spacing
            }
            widget.setSelfPosition(
                Position(currentX, if (verticalCenteredWidget.contains(widget)) containerHeight / 2 - widget.size.height / 2 else 0)
            )
            currentX += widget.size.width
        }
        updateSizeIfNeeded(Size(currentX, size.height))
    }

    override val containerWidth: Int
        get() = Int.MAX_VALUE

    override val containerHeight: Int
        get() = height
}

open class VBox(width: Int, spacing: Int = 0) : Box(width, 0, spacing) {

    val width: Int

    var horizonCenteredWidget: ArrayList<Widget>

    init {
        this.width = width
        this.horizonCenteredWidget = ArrayList()
    }

    override fun performLayout() {
        var currentY = 0
        widgets.forEachIndexed { index, widget ->
            if (index > 0) {
                currentY += spacing
            }
            widget.setSelfPosition(
                Position(if (horizonCenteredWidget.contains(widget)) containerWidth / 2 - widget.size.width / 2 else 0, currentY)
            )
            currentY += widget.size.height
        }
        updateSizeIfNeeded(Size(size.width, currentY))
    }

    override val containerWidth: Int
        get() = width

    override val containerHeight: Int
        get() = Int.MAX_VALUE
}

class HScrollBox(width: Int, height: Int, spacing: Int = 2) : DraggableScrollableWidgetGroup(0, 0, width, height), ContainerSizeProvider {

    val width: Int
    val height: Int
    private val contentContainer: HBox
    private var isUpdatingContent: Boolean = false

    init {
        this.width = width
        this.height = height
        scrollWheelDirection = ScrollWheelDirection.HORIZONTAL
        setXScrollBarHeight(4)
        setXBarStyle(GuiTextures.SLIDER_BACKGROUND_VERTICAL as IGuiTexture, GuiTextures.BUTTON as IGuiTexture)
        draggable = false
        scrollable = true
        useScissor = true
        contentContainer = object : HBox(hBoxHeight, spacing) {
            override fun onSizeUpdate() {
                if (!isUpdatingContent) {
                    updateScrollArea()
                }
            }
        }
        super.addWidget(contentContainer as Widget)
    }

    val hBoxHeight: Int
        get() = height - xBarHeight

    override val containerWidth: Int
        get() = width

    override val containerHeight: Int
        get() = hBoxHeight

    private fun updateScrollArea() {
        if (!isUpdatingContent) {
            isUpdatingContent = true
            try {
                computeMax()
            } finally {
                isUpdatingContent = false
            }
        }
    }

    fun addContent(widget: Widget): HScrollBox {
        contentContainer.addWidget(widget)
        updateScrollArea()
        return this
    }

    fun addContents(vararg widgets: Widget): HScrollBox {
        widgets.forEach { contentContainer.addWidget(it) }
        updateScrollArea()
        return this
    }

    fun removeContent(widget: Widget): HScrollBox {
        contentContainer.removeWidget(widget)
        updateScrollArea()
        return this
    }

    fun clearContent(): HScrollBox {
        contentContainer.clearAllWidgets()
        updateScrollArea()
        return this
    }
}

class VScrollBox(width: Int, height: Int, spacing: Int = 2) : DraggableScrollableWidgetGroup(0, 0, width, height), ContainerSizeProvider {

    val width: Int
    val height: Int
    private val contentContainer: VBox
    private var isUpdatingContent: Boolean = false

    init {
        this.width = width
        this.height = height
        scrollWheelDirection = ScrollWheelDirection.VERTICAL
        setYScrollBarWidth(4)
        setYBarStyle(GuiTextures.SLIDER_BACKGROUND_VERTICAL as IGuiTexture, GuiTextures.BUTTON as IGuiTexture)
        draggable = false
        scrollable = true
        useScissor = true
        contentContainer = object : VBox(vBoxWidth, spacing) {
            override fun onSizeUpdate() {
                if (!isUpdatingContent) {
                    updateScrollArea()
                }
            }
        }
        super.addWidget(contentContainer as Widget)
    }

    val vBoxWidth: Int
        get() = width - yBarWidth

    override val containerWidth: Int
        get() = vBoxWidth

    override val containerHeight: Int
        get() = Int.MAX_VALUE

    private fun updateScrollArea() {
        if (!isUpdatingContent) {
            isUpdatingContent = true
            try {
                computeMax()
            } finally {
                isUpdatingContent = false
            }
        }
    }

    fun addContent(widget: Widget): VScrollBox {
        contentContainer.addWidget(widget)
        updateScrollArea()
        return this
    }

    fun addContents(vararg widgets: Widget): VScrollBox {
        widgets.forEach { contentContainer.addWidget(it) }
        updateScrollArea()
        return this
    }

    fun removeContent(widget: Widget): VScrollBox {
        contentContainer.removeWidget(widget)
        updateScrollArea()
        return this
    }

    fun clearContent(): VScrollBox {
        contentContainer.clearAllWidgets()
        updateScrollArea()
        return this
    }
}
