package com.gtolib.api.lang

import com.gtocore.common.data.translation.MultiblockSlang
import com.gtolib.api.annotation.NewDataAttributes
import java.util.ArrayList
import java.util.function.Supplier
import net.minecraft.network.chat.Component

class TooltipsSortedWrapper {

    private val tooltips = ArrayList<SupplierWrapper>()

    var isInitialized: Boolean = false

    private fun add(component: Supplier<List<Component>?>, supplier: Boolean): TooltipsSortedWrapper {
        tooltips.add(SupplierWrapper(this, component, supplier))
        val conditions = arrayOf<(Supplier<List<Component>?>) -> Boolean>(
            { s -> s.get()?.any { it?.string?.contains("gtocore.difficulty_config") ?: false } ?: false },
            { s -> s.get()?.any { it?.string?.contains("gtocore.lang.template") ?: false } ?: false },
            { s -> s.get()?.any { it?.string?.contains("gtocore.lang.template.${NewDataAttributes.ALLOW_PARALLEL_SPECIAL.key}") ?: false } ?: false },
            { s -> s.get()?.any { it?.string?.contains("gtocore.lang.template.${NewDataAttributes.ALLOW_PARALLEL.key}") ?: false } ?: false },
            { s -> s.get()?.any { it?.string?.contains("gtocore.lang.template.${NewDataAttributes.ALLOW_PARALLEL_NUMBER.key}") ?: false } ?: false },
            { s -> s.get()?.any { it?.string?.contains("gtocore.lang.template.${NewDataAttributes.LASER_ENERGY_HATCH.key}") ?: false } ?: false },
            { s -> s.get()?.any { it?.string?.contains(MultiblockSlang.not_allow_standard_energy_hatch.translationPrefix) ?: false } ?: false },
            { s -> s.get()?.any { it?.string?.contains("gtocore.lang.template.${NewDataAttributes.PREFECT_OVERCLOCK.key}") ?: false } ?: false },
            { s -> s.get()?.any { it?.string?.contains("gtocore.lang.template.${NewDataAttributes.ALLOW_MULTI_RECIPE_PARALLEL.key}") ?: false } ?: false },
            { s -> s.get()?.any { it?.string?.contains("gtocore.lang.template.${NewDataAttributes.NOT_ALLOW_SHARED.key}") ?: false } ?: false },
            { s -> s.get()?.any { it?.string?.contains("gtocore.lang.template.${NewDataAttributes.RECIPES_TYPE.key}") ?: false } ?: false }
        )
        tooltips.sortByConditions(*conditions)
        return this
    }

    fun addTooltip(supplier: Supplier<List<Component>?>): TooltipsSortedWrapper = add(supplier, false)

    fun addTooltip(components: List<Component>): TooltipsSortedWrapper = addTooltip { components }

    fun addTooltip(component: Component): TooltipsSortedWrapper = addTooltip(listOf(component))

    fun addSupplier(component: Supplier<List<Component>?>): TooltipsSortedWrapper = add(component, true)

    fun getTooltips(): List<Supplier<List<Component>?>> = tooltips

    companion object {

        private fun <T> MutableList<T>.sortByConditions(vararg conditions: (T) -> Boolean) {
            sortWith(compareBy { item -> conditions.indexOfLast { it(item) }.takeIf { it >= 0 }?.let { it + 1 } ?: 0 })
        }
    }

    private class SupplierWrapper(
        private val wrapper: TooltipsSortedWrapper,
        private val components: Supplier<List<Component>?>,
        private val supplier: Boolean
    ) : Supplier<List<Component>?> {

        override fun get(): List<Component>? =
            if (wrapper.isInitialized) {
                components.get()
            } else if (supplier) {
                emptyList()
            } else {
                components.get()
            }
    }
}
