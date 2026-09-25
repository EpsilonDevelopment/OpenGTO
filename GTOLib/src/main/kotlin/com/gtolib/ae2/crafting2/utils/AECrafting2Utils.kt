package com.gtolib.ae2.crafting2.utils

import appeng.api.crafting.IPatternDetails
import appeng.api.stacks.AEItemKey
import appeng.api.stacks.AEKey
import appeng.api.stacks.GenericStack
import appeng.crafting.pattern.AECraftingPattern
import appeng.menu.AutoCraftingMenu
import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap
import it.unimi.dsi.fastutil.objects.Reference2LongLinkedOpenHashMap
import java.util.ArrayList
import java.util.RandomAccess
import net.minecraft.world.inventory.TransientCraftingContainer
import net.minecraft.world.item.ItemStack

class AECrafting2Utils {

    companion object {

        private val inputsCache = O2OOpenCacheHashMap<IPatternDetails, List<GenericStack>>()
        private val outputsCache = O2OOpenCacheHashMap<IPatternDetails, List<GenericStack>>()

        fun condenseInputs(detail: IPatternDetails, sparseInput: Array<GenericStack?>): List<GenericStack> {
            val cached = inputsCache[detail]
            if (cached != null) {
                return cached
            }
            val computed = condenseStacks(sparseInput)
            inputsCache[detail] = computed
            return computed
        }

        fun condenseOutputs(detail: IPatternDetails, sparseOutput: Array<GenericStack?>): List<GenericStack> {
            val cached = outputsCache[detail]
            if (cached != null) {
                return cached
            }
            val computed = condenseStacks(sparseOutput)
            outputsCache[detail] = computed
            return computed
        }

        fun condenseStacks(sparseInput: Array<GenericStack?>): List<GenericStack> {
            val map = Reference2LongLinkedOpenHashMap<AEKey>(sparseInput.size)
            for (k in sparseInput) {
                if (k != null) {
                    map.addTo(k.what(), k.amount())
                }
            }
            check(map.isNotEmpty()) { "No pattern here!" }
            val out = ArrayList<GenericStack>(map.size)
            for (key in map.keys) {
                out.add(GenericStack(key, map.getLong(key)))
            }
            return out
        }

        fun <T> getCircularList(list: List<T>, startElement: T): List<T> {
            val size = list.size
            require(size != 0) { "列表为空" }
            val startIndex = list.indexOf(startElement)
            require(startIndex != -1) { "元素 $startElement 不在列表中" }
            if (size == 1 || startIndex == 0) {
                return list
            }
            val out = ArrayList<T>(size)
            if (list is RandomAccess) {
                repeat(size) {
                    out.add(list[(startIndex + it) % size])
                }
            } else {
                val iterator = list.listIterator(startIndex)
                while (iterator.hasNext()) {
                    out.add(iterator.next())
                }
                val it2 = list.iterator()
                var count = 0
                while (count < startIndex && it2.hasNext()) {
                    out.add(it2.next())
                    count++
                }
            }
            return out
        }

        fun extractPatternInputsWithFluidReplacement(
            detail: AECraftingPattern,
            enableFluidReplacement: Boolean = true
        ): Array<GenericStack?> {
            if (!enableFluidReplacement || !detail.canSubstituteFluids) {
                return detail.sparseInputs
            }
            val testFrame = TransientCraftingContainer(AutoCraftingMenu(), 3, 3)
            val replacedInputs = arrayOfNulls<GenericStack>(9)
            (0..8).forEach { ix ->
                val gs = detail.sparseInputs[ix]
                val key = gs?.what()
                testFrame.setItem(ix, if (key is AEItemKey) key.toStack() else ItemStack.EMPTY)
                replacedInputs[ix] = gs
            }
            val remainingItems = detail.getRemainingItems(testFrame)
            for (i in 0 until 8) {
                if (!remainingItems[i].isEmpty) {
                    val fluid = detail.getValidFluid(i)
                    if (fluid != null) {
                        replacedInputs[i] = fluid
                    }
                }
            }
            return replacedInputs
        }

        fun checkRemainingItemsAndFluidReplacement(detail: AECraftingPattern): Pair<Boolean, Boolean> {
            val testFrame = TransientCraftingContainer(AutoCraftingMenu(), 3, 3)
            (0..8).forEach { item ->
                val gs = detail.sparseInputs[item]
                val key = gs?.what()
                testFrame.setItem(item, if (key is AEItemKey) key.toStack() else ItemStack.EMPTY)
            }
            val remainingItems = detail.getRemainingItems(testFrame)
            var hasRemaining = false
            var allReplaceable = true
            if (!detail.canSubstituteFluids) {
                for (stack in remainingItems) {
                    if (!stack.isEmpty) {
                        hasRemaining = true
                        allReplaceable = false
                        break
                    }
                }
            } else {
                for (i in 0 until 8) {
                    if (!remainingItems[i].isEmpty) {
                        hasRemaining = true
                        if (detail.getValidFluid(i) == null) {
                            allReplaceable = false
                            break
                        }
                    }
                }
            }
            return Pair(hasRemaining, allReplaceable)
        }

        fun hasUnhandledRemainingItems(detail: AECraftingPattern): Boolean {
            val (hasRemaining, allReplaceable) = checkRemainingItemsAndFluidReplacement(detail)
            return hasRemaining && !allReplaceable
        }
    }
}
