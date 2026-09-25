package com.gtolib.ae2.crafting2.model

import appeng.api.config.FuzzyMode
import appeng.api.crafting.IPatternDetails
import appeng.api.crafting.IPatternDetails.IInput
import appeng.api.networking.IGrid
import appeng.api.networking.crafting.ICraftingPlan
import appeng.api.stacks.AEFluidKey
import appeng.api.stacks.AEItemKey
import appeng.api.stacks.AEKey
import appeng.api.stacks.GenericStack
import appeng.api.stacks.KeyCounter
import appeng.crafting.pattern.AECraftingPattern
import appeng.crafting.pattern.AEProcessingPattern
import appeng.crafting.pattern.AESmithingTablePattern
import appeng.crafting.pattern.AEStonecuttingPattern
import appeng.menu.AutoCraftingMenu
import com.gto.datasynclib.util.holder.LongHolder
import com.gto.fastcollection.fastutil.O2LOpenCacheHashMap
import com.gtolib.ae2.crafting2.logic.ComputingComponentBuildFailedException
import com.gtolib.ae2.crafting2.logic.SendComponentToPlayerException
import com.gtolib.ae2.crafting2.utils.AE2CraftingTranslation
import com.gtolib.ae2.crafting2.utils.AECrafting2Utils
import com.gtolib.ae2.crafting2.utils.DisplayLevel
import com.gtolib.ae2.crafting2.utils.PerfLogger
import com.gtolib.ae2.crafting2.utils.RequirementFulfillmentResult
import com.gtolib.ae2.crafting2.utils.RequirementsManager
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import java.util.ArrayDeque
import java.util.ArrayList
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.ceil
import net.minecraft.core.NonNullList
import net.minecraft.network.chat.Component
import net.minecraft.world.inventory.CraftingContainer
import net.minecraft.world.inventory.TransientCraftingContainer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

interface GetSubComponent {
    fun getSubComponent(): List<ComputingComponent>
}

sealed class ComputingComponent protected constructor(
    val middleOutput: AEKey,
    val finalPlan: SuriedCraftingPlan.FinalPlan
) {
    lateinit var middlePlan: SuriedCraftingPlan.MiddlePlan

    var isFinish = false

    var totalStep = 0

    var finishedStep = 0

    var minParallelMaterialPerCycle = 1L

    var componentInSCC = false

    fun getMiddlePlanOptional(): SuriedCraftingPlan.MiddlePlan? =
        if (this::middlePlan.isInitialized) middlePlan else null

    open fun initRuntimeData(middlePlan: SuriedCraftingPlan.MiddlePlan) {
        this.middlePlan = middlePlan
        isFinish = false
        totalStep = 0
        finishedStep = 0
    }

    open fun beforeCalculate() {
    }

    open fun startCalculate() {
    }

    open fun afterCalculate() {
        RequirementsManager.addAllRequirements(finalPlan.requirements, middlePlan.requirements)
        for ((key, value) in middlePlan.patternTimes) {
            finalPlan.patternTimes.addTo(key, value)
        }
    }

    open fun getCraftAmountPerUnit(): Long = middlePlan.finalOutputAmount.get()

    fun request(what: AEKey, amount: Long, finalPlan: SuriedCraftingPlan.FinalPlan, parentPlan: SuriedCraftingPlan) {
        finalPlan.perfLogger.push("请求 ${middleOutput.displayName.string} x$amount, 算子类型 ${getComponentTypeName()}")
        val plan = SuriedCraftingPlan.MiddlePlan(what, finalPlan, this, parentPlan)
        plan.setFinalOutputAmount(amount)
        plan.initRuntimeData()
        initRuntimeData(plan)
        beforeCalculate()
        startCalculate()
        afterCalculate()
        finalPlan.perfLogger.pop()
    }

    open fun getComponentTypeName(): String = when (this) {
        is CompositeComponent -> "复合算子"
        is MultiCraftComponent -> "批量合成算子"
        is MultiPathSelectionComponent -> "多路径算子"
        is MultiProcessComponent -> "批量处理算子"
        is OnceCraftComponent -> "循环合成算子"
    }

    abstract fun getDependencies(): List<AEKey>

    class CompositeComponent(
        middleOutput: AEKey,
        val components: List<ComputingComponent>,
        finalPlan: SuriedCraftingPlan.FinalPlan
    ) : ComputingComponent(middleOutput, finalPlan), GetSubComponent {

        override fun getDependencies(): List<AEKey> {
            val internal = components.asSequence().map { it.middleOutput }.toSet()
            return components.asSequence()
                .flatMap { it.getDependencies().asSequence() }
                .filter { !internal.contains(it) }
                .distinct()
                .toList()
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other !is CompositeComponent) {
                return false
            }
            if (middleOutput != other.middleOutput) {
                return false
            }
            return components.map { it.middleOutput }.toSet() == other.components.map { it.middleOutput }.toSet()
        }

        override fun hashCode(): Int =
            31 * middleOutput.hashCode() + components.map { it.middleOutput }.toSet().hashCode()

        override fun toString(): String = "MultiCompositeComponent($middleOutput, $components)"

        override fun getSubComponent(): List<ComputingComponent> = components

        companion object {
            fun canBuild(components: List<ComputingComponent>): Boolean {
                throw SendComponentToPlayerException(
                    AE2CraftingTranslation.ERR_CRAFTING_CYCLE_DETECTED(components.map { it.middleOutput })
                )
            }
        }
    }

    class MultiCraftComponent(
        middleOutput: AEKey,
        val detail: AECraftingPattern,
        finalPlan: SuriedCraftingPlan.FinalPlan
    ) : ComputingComponent(middleOutput, finalPlan) {

        val analyzer = PatternAnalyzer.fromPattern(detail, middleOutput)

        override fun getDependencies(): List<AEKey> =
            analyzer.inputs.map { it.what() }.filter { it != middleOutput }

        override fun initRuntimeData(middlePlan: SuriedCraftingPlan.MiddlePlan) {
            super.initRuntimeData(middlePlan)
            totalStep = 1
        }

        override fun getCraftAmountPerUnit(): Long = analyzer.netOutputPerCraft

        override fun startCalculate() {
            super.startCalculate()
            if (finalPlan.processedComputingComponentCount.get() != 0L) {
                val analysis = minOf(
                    analyzer.maxExternalExtractableForSelfIncrease(
                        finalPlan.inventory.get(middleOutput),
                        middlePlan.finalOutput().amount(),
                        minParallelMaterialPerCycle
                    ),
                    middlePlan.finalOutput().amount()
                )
                if (analysis > 0L) {
                    finalPlan.useGlobalItem(middleOutput, analysis)
                    finalPlan.requirements.remove(middleOutput, analysis)
                    if (middlePlan.finalOutput().amount() <= 0L) {
                        finishedStep = 1
                        isFinish = true
                        return
                    }
                }
                middlePlan.setFinalOutputAmount(middlePlan.finalOutputAmount.get() - analysis)
            }

            val needAmount = middlePlan.finalOutput().amount()
            finalPlan.perfLogger.push("使用样板分析器计算", DisplayLevel.DETAIL)
            analyzer.calculateCrafting(needAmount, pattern = detail).applyToPlans(finalPlan, middlePlan)
            finalPlan.perfLogger.pop()
            finishedStep = 1
            isFinish = true
        }

        override fun toString(): String = "MultiCraftComponent($middleOutput, $detail)"

        companion object {
            fun canBuild(detail: IPatternDetails): Boolean {
                if (detail !is AECraftingPattern) {
                    return false
                }
                if (detail.canSubstitute) {
                    return false
                }
                if (AECrafting2Utils.hasUnhandledRemainingItems(detail)) {
                    return false
                }
                return try {
                    PatternAnalyzer.fromPattern(detail, detail.primaryOutput.what())
                    true
                } catch (e: Exception) {
                    false
                }
            }
        }
    }

    class MultiPathSelectionComponent(
        middleOutput: AEKey,
        components: List<ComputingComponent>,
        finalPlan: SuriedCraftingPlan.FinalPlan
    ) : ComputingComponent(middleOutput, finalPlan), GetSubComponent {

        val components: List<ComputingComponent>
        val componentsToWeight: O2LOpenCacheHashMap<ComputingComponent>
        val componentsToCraftAmountPerUnit: O2LOpenCacheHashMap<ComputingComponent>

        init {
            this.components = components.toList()
            componentsToWeight = O2LOpenCacheHashMap()
            componentsToCraftAmountPerUnit = O2LOpenCacheHashMap()
            components.forEachIndexed { index, component ->
                componentsToWeight.put(component, if (index == 0) 1L else 1L)
                componentsToCraftAmountPerUnit.put(component, component.getCraftAmountPerUnit())
            }
            totalStep = components.size
        }

        override fun startCalculate() {
            super.startCalculate()
            val totalNeeded = middlePlan.finalOutput().amount()
            val totalWeight = componentsToWeight.values.sumOf { it }
            if (totalWeight <= 0L) {
                components.first().request(middleOutput, totalNeeded, finalPlan, middlePlan)
                finishedStep = components.size
                isFinish = true
            } else {
                var remainingToAllocate = totalNeeded
                val allocations = O2LOpenCacheHashMap<ComputingComponent>()

                for (component in components) {
                    val weight = componentsToWeight.getLong(component)
                    if (weight > 0L) {
                        val craftAmountPerUnit = componentsToCraftAmountPerUnit.getLong(component)
                        if (craftAmountPerUnit > 0L) {
                            val proportionalAmount = totalNeeded * weight / totalWeight
                            val crafts = (proportionalAmount + craftAmountPerUnit - 1L) / craftAmountPerUnit
                            val actualAmount = crafts * craftAmountPerUnit
                            allocations.put(component, actualAmount)
                            remainingToAllocate -= actualAmount
                        }
                    }
                }

                if (remainingToAllocate != 0L) {
                    val maxWeightComponent = components.maxByOrNull { componentsToWeight.getLong(it) }
                    if (maxWeightComponent != null) {
                        val currentAllocation = allocations.getLong(maxWeightComponent)
                        val craftAmountPerUnit = componentsToCraftAmountPerUnit.getLong(maxWeightComponent)
                        if (remainingToAllocate > 0L) {
                            val additionalCrafts = ceil(
                                (remainingToAllocate + craftAmountPerUnit - 1L).toDouble() / craftAmountPerUnit
                            ).toLong()
                            allocations.put(maxWeightComponent, currentAllocation + additionalCrafts * craftAmountPerUnit)
                        } else {
                            val excessAmount = -remainingToAllocate
                            val reduceCrafts = excessAmount / craftAmountPerUnit
                            allocations.put(
                                maxWeightComponent,
                                maxOf(0L, currentAllocation - reduceCrafts * craftAmountPerUnit)
                            )
                        }
                    }
                }

                finalPlan.perfLogger.push("多路径算子任务分配", DisplayLevel.DETAIL)
                for ((component, amount) in allocations) {
                    if (amount > 0L) {
                        component.request(middleOutput, amount, finalPlan, middlePlan)
                        finishedStep += 1
                    }
                }
                finalPlan.perfLogger.pop()
                finishedStep = components.size
                isFinish = true
            }
        }

        override fun getDependencies(): List<AEKey> =
            components.asSequence().flatMap { it.getDependencies().asSequence() }.distinct().toList()

        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other !is MultiPathSelectionComponent) {
                return false
            }
            if (middleOutput != other.middleOutput) {
                return false
            }
            val thisSet = components.map { it.middleOutput }.toSet()
            val otherSet = other.components.map { it.middleOutput }.toSet()
            return thisSet == otherSet
        }

        override fun hashCode(): Int =
            31 * middleOutput.hashCode() + components.map { it.middleOutput }.toSet().hashCode()

        override fun toString(): String = "MultiPathSelectionComponent($middleOutput, $components)"

        override fun getSubComponent(): List<ComputingComponent> = components

        companion object {
            fun canBuild(components: List<ComputingComponent>): Boolean = components.size >= 2
        }
    }

    class MultiProcessComponent(
        middleOutput: AEKey,
        detail: IPatternDetails,
        finalPlan: SuriedCraftingPlan.FinalPlan
    ) : ComputingComponent(middleOutput, finalPlan) {

        val detail: IPatternDetails = detail
        val analyzer: PatternAnalyzer = PatternAnalyzer.fromPattern(detail, middleOutput)

        init {
            minParallelMaterialPerCycle = 1L
        }

        override fun initRuntimeData(middlePlan: SuriedCraftingPlan.MiddlePlan) {
            super.initRuntimeData(middlePlan)
            totalStep = 1
        }

        override fun getCraftAmountPerUnit(): Long = analyzer.netOutputPerCraft

        override fun startCalculate() {
            super.startCalculate()
            if (finalPlan.processedComputingComponentCount.get() != 0L) {
                val analysis = minOf(
                    analyzer.maxExternalExtractableForSelfIncrease(
                        finalPlan.inventory.get(middleOutput),
                        middlePlan.finalOutput().amount(),
                        minParallelMaterialPerCycle
                    ),
                    middlePlan.finalOutput().amount()
                )
                if (analysis > 0L) {
                    finalPlan.useGlobalItem(middleOutput, analysis)
                    finalPlan.requirements.remove(middleOutput, analysis)
                    if (middlePlan.finalOutput().amount() <= 0L) {
                        finishedStep = 1
                        isFinish = true
                        return
                    }
                }
                middlePlan.setFinalOutputAmount(middlePlan.finalOutputAmount.get() - analysis)
            }

            val needAmount = middlePlan.finalOutput().amount()
            finalPlan.perfLogger.push("循环分析器开始计算", DisplayLevel.DETAIL)
            analyzer.calculateCrafting(needAmount, minParallelMaterialPerCycle, detail)
                .applyToPlans(finalPlan, middlePlan)
            finalPlan.perfLogger.pop()
            finishedStep = 1
            isFinish = true
        }

        override fun getDependencies(): List<AEKey> =
            analyzer.inputs.map { it.what() }.filter { it != middleOutput }

        override fun toString(): String = "MultiSelfCycleProcessComponent($middleOutput, $detail)"

        companion object {
            fun canBuild(detail: IPatternDetails): Boolean {
                if (detail !is AEProcessingPattern && detail !is AESmithingTablePattern && detail !is AEStonecuttingPattern) {
                    return false
                }
                PatternAnalyzer.fromPattern(detail, detail.primaryOutput.what())
                return true
            }
        }
    }

    class OnceCraftComponent(
        middleOutput: AEKey,
        detail: AECraftingPattern,
        finalPlan: SuriedCraftingPlan.FinalPlan
    ) : ComputingComponent(middleOutput, finalPlan) {

        val detail: AECraftingPattern = detail
        val analyzer: PatternAnalyzer = PatternAnalyzer.fromPattern(detail, middleOutput)

        override fun initRuntimeData(middlePlan: SuriedCraftingPlan.MiddlePlan) {
            super.initRuntimeData(middlePlan)
            totalStep = 1
        }

        override fun getCraftAmountPerUnit(): Long = analyzer.netOutputPerCraft

        override fun startCalculate() {
            super.startCalculate()

            fun consumeInputWithFuzzyReturnContainers(
                inDef: IInput,
                totalRequired: Long,
                perCraftAmount: Long,
                craftDone: Long
            ): Long {
                if (totalRequired <= 0L) {
                    return 0L
                }
                var remaining = totalRequired
                for (gLike in inDef.possibleInputs) {
                    val like = gLike.what()
                    if (craftDone > 0L) {
                        for (entry in middlePlan.internalInventory.findFuzzy(like, FuzzyMode.IGNORE_ALL)) {
                            if (remaining <= 0L) {
                                break
                            }
                            val candidate = entry.key
                            if (inDef.isValid(candidate, finalPlan.level)) {
                                val want = remaining
                                val left = middlePlan.useInternalItem(candidate, want, null, false)
                                val used = want - left
                                if (used > 0L) {
                                    val rem = inDef.getRemainingKey(candidate)
                                    if (rem != null) {
                                        middlePlan.storageInternalItem(rem, used)
                                    }
                                }
                                remaining = left
                            }
                        }
                    }
                    if (remaining > 0L) {
                        for (entry in finalPlan.inventory.findFuzzy(like, FuzzyMode.IGNORE_ALL)) {
                            if (remaining <= 0L) {
                                break
                            }
                            val candidate = entry.key
                            if (inDef.isValid(candidate, finalPlan.level)) {
                                val want = remaining
                                val left = finalPlan.useGlobalItem(candidate, want, null, false)
                                val used = want - left
                                if (used > 0L) {
                                    val rem = inDef.getRemainingKey(candidate)
                                    if (rem != null) {
                                        middlePlan.storageInternalItem(rem, used)
                                    }
                                }
                                remaining = left
                            }
                        }
                    }
                    if (remaining <= 0L) {
                        break
                    }
                }
                if (remaining > 0L) {
                    val firstKey = inDef.possibleInputs[0].what()
                    middlePlan.useInternalItem(firstKey, remaining, FuzzyMode.IGNORE_ALL, true)
                }
                return remaining
            }

            if (finalPlan.processedComputingComponentCount.get() != 0L) {
                val globalHave = finalPlan.inventory.get(middleOutput)
                if (globalHave > 0L) {
                    val toUse = minOf(globalHave, middlePlan.finalOutput().amount())
                    if (toUse > 0L) {
                        finalPlan.useGlobalItem(middleOutput, toUse)
                        finalPlan.requirements.remove(middleOutput, toUse)
                        if (middlePlan.finalOutput().amount() <= 0L) {
                            finishedStep = 1
                            isFinish = true
                            return
                        }
                    }
                    middlePlan.setFinalOutputAmount(middlePlan.finalOutputAmount.get() - toUse)
                }
            }

            val need = middlePlan.finalOutput().amount()
            if (need <= 0L) {
                finishedStep = 1
                isFinish = true
            } else {
                finalPlan.perfLogger.push("OnceCraft 计算开始", DisplayLevel.DETAIL)
                val craftsRequired = ceil(need.toDouble() / analyzer.netOutputPerCraft).toLong()
                var hasContainer = false
                var selfLoop = false
                val primaryOutKey = detail.primaryOutput.what()
                for (inDef in detail.inputs) {
                    val primaryIn = inDef.possibleInputs[0]
                    if (!hasContainer && inDef.getRemainingKey(primaryIn.what()) != null) {
                        hasContainer = true
                    }
                    if (!selfLoop && primaryIn.what() == primaryOutKey) {
                        selfLoop = true
                    }
                }
                if (craftsRequired >= 10000L) {
                    throw SendComponentToPlayerException(
                        AE2CraftingTranslation.ERR_CRAFTING_TOO_LARGE(10000L, craftsRequired, middleOutput)
                    )
                }
                totalStep = craftsRequired.toInt()
                if (hasContainer || selfLoop) {
                    var craftsDone = 0L
                    var remainingNeed = need
                    while (craftsDone < craftsRequired) {
                        for (inDef in detail.inputs) {
                            val perCraft = inDef.possibleInputs[0].amount() * inDef.multiplier
                            if (perCraft > 0L) {
                                consumeInputWithFuzzyReturnContainers(inDef, perCraft, perCraft, craftsDone)
                            }
                        }
                        middlePlan.patternTimes.addTo(detail, 1L)
                        val currentNetOutput = analyzer.netOutputPerCraft
                        val fulfillmentResult =
                            RequirementsManager.fulfillRequirement(finalPlan.requirements, primaryOutKey, currentNetOutput)
                        if (fulfillmentResult.remainingOutput > 0L) {
                            middlePlan.storageInternalItem(primaryOutKey, fulfillmentResult.remainingOutput)
                        }
                        craftsDone++
                        finishedStep = minOf(finishedStep + 1, Int.MAX_VALUE)
                        remainingNeed = maxOf(0L, remainingNeed - currentNetOutput)
                    }
                    val totalNetOutput = craftsDone * analyzer.netOutputPerCraft
                    if (totalNetOutput > 0L) {
                        middlePlan.netOutputItems.add(primaryOutKey, totalNetOutput)
                    }
                } else {
                    val perInputTotalRequired = O2LOpenCacheHashMap<IInput>()
                    for (inDef in detail.inputs) {
                        val perCraft = inDef.possibleInputs[0].amount() * inDef.multiplier
                        if (perCraft > 0L) {
                            perInputTotalRequired.put(inDef, perCraft * craftsRequired)
                        }
                    }
                    val produced = detail.primaryOutput.amount() * craftsRequired
                    if (produced > 0L) {
                        val fulfillmentResult =
                            RequirementsManager.fulfillRequirement(finalPlan.requirements, primaryOutKey, produced)
                        if (fulfillmentResult.remainingOutput > 0L) {
                            middlePlan.storageInternalItem(primaryOutKey, fulfillmentResult.remainingOutput)
                        }
                    }
                    for (inDef in detail.inputs) {
                        val totalRequired = perInputTotalRequired.getLong(inDef)
                        if (totalRequired > 0L) {
                            consumeInputWithFuzzyReturnContainers(
                                inDef,
                                totalRequired,
                                inDef.possibleInputs[0].amount(),
                                0L
                            )
                        }
                    }
                    if (craftsRequired > 0L) {
                        middlePlan.patternTimes.addTo(detail, craftsRequired)
                    }
                    val net = craftsRequired * analyzer.netOutputPerCraft
                    if (net > 0L) {
                        middlePlan.netOutputItems.add(primaryOutKey, net)
                    }
                    finishedStep = minOf(finishedStep + craftsRequired.toInt(), Int.MAX_VALUE)
                }
                finalPlan.perfLogger.pop()
                isFinish = true
            }
        }

        override fun getDependencies(): List<AEKey> {
            val result = ArrayList<AEKey>()
            for (input in detail.inputs) {
                for (stack in input.possibleInputs) {
                    result.add(stack.what())
                }
            }
            return result.filter { it != middleOutput }
        }

        override fun toString(): String = "OnceCraftComponent($middleOutput, $detail)"

        companion object {
            fun canBuild(detail: IPatternDetails): Boolean {
                if (detail !is AECraftingPattern) {
                    return false
                }
                if (MultiCraftComponent.canBuild(detail)) {
                    return false
                }
                val selfLoopInput = AECrafting2Utils.condenseInputs(detail, detail.sparseInputs)
                    .firstOrNull { it.what() == detail.primaryOutput.what() }
                if (selfLoopInput != null) {
                    if (selfLoopInput.amount() >= detail.primaryOutput.amount()) {
                        throw ComputingComponentBuildFailedException(
                            Component.literal(
                                "无法为物品 ${detail.primaryOutput.what().displayName.string} 的 $detail 生成算子，因为它是一个非自增循环的复杂合成"
                            ),
                            detail,
                            null,
                            null
                        )
                    }
                }
                val testFrame = TransientCraftingContainer(AutoCraftingMenu(), 3, 3)
                for (i in 0..8) {
                    val gs = detail.sparseInputs[i]
                    val key = if (gs != null) gs.what() else null
                    if (key is AEItemKey) {
                        testFrame.setItem(i, key.toStack())
                    } else {
                        testFrame.setItem(i, ItemStack.EMPTY)
                    }
                }
                val remainingItems = detail.getRemainingItems(testFrame as CraftingContainer)
                val hasRemainingItem = remainingItems.any { !it.isEmpty }
                if (hasRemainingItem) {
                    throw SendComponentToPlayerException(
                        AE2CraftingTranslation.ERR_CRAFTING_INPUT_REQUIREMENTS(detail.primaryOutput.what())
                    )
                }
                return true
            }
        }
    }
}

sealed class SuriedCraftingPlan protected constructor(finalOutput: AEKey) : ICraftingPlan {

    open val finalOutput: AEKey = finalOutput

    lateinit var patternTimes: O2LOpenCacheHashMap<IPatternDetails>
    lateinit var nmissingItems: KeyCounter
    lateinit var nemittedItems: KeyCounter
    lateinit var nusedItems: KeyCounter
    lateinit var catalystItems: KeyCounter
    lateinit var netOutputItems: KeyCounter
    lateinit var finalOutputAmount: AtomicLong
    lateinit var requirements: KeyCounter

    fun setFinalOutputAmount(amount: Long) {
        finalOutputAmount = AtomicLong(amount)
    }

    override fun patternTimes(): O2LOpenCacheHashMap<IPatternDetails> = patternTimes

    override fun missingItems(): KeyCounter = nmissingItems

    override fun emittedItems(): KeyCounter = nemittedItems

    override fun usedItems(): KeyCounter = nusedItems

    fun catalystItems(): KeyCounter = catalystItems

    override fun finalOutput(): GenericStack = GenericStack(finalOutput, finalOutputAmount.get())

    open fun initRuntimeData() {
        patternTimes = O2LOpenCacheHashMap()
        nmissingItems = KeyCounter()
        nemittedItems = KeyCounter()
        nusedItems = KeyCounter()
        catalystItems = KeyCounter()
        requirements = KeyCounter()
        netOutputItems = KeyCounter()
    }

    class FinalPlan(finalOutput: AEKey, val perfLogger: PerfLogger) : SuriedCraftingPlan(finalOutput) {

        val middleOutputToComponent = Reference2ObjectOpenHashMap<AEKey, ArrayList<ComputingComponent>>()

        lateinit var inventory: KeyCounter
        lateinit var isSimulated: AtomicBoolean
        lateinit var level: Level
        lateinit var highPriorityPush: Reference2ObjectOpenHashMap<AEKey, Object2LongOpenHashMap<IPatternDetails>>
        lateinit var processedComputingComponentCount: AtomicLong
        var sortedComputingComponent: ArrayList<ComputingComponent> = ArrayList()

        fun initInventory(keyCounter: KeyCounter, grid: IGrid) {
            inventory = keyCounter
            level = grid.pivot.level
        }

        override fun initRuntimeData() {
            super.initRuntimeData()
            isSimulated = AtomicBoolean(false)
            processedComputingComponentCount = AtomicLong(0L)
            highPriorityPush = Reference2ObjectOpenHashMap()
        }

        fun getComputingComponentFor(aeKey: AEKey): ArrayList<ComputingComponent> =
            middleOutputToComponent.computeIfAbsent(aeKey) { ArrayList() }

        fun useGlobalItem(
            aeKey: AEKey,
            amount: Long,
            fuzzyMode: FuzzyMode? = null,
            requestRemain: Boolean = true,
            middlePlan: SuriedCraftingPlan.MiddlePlan? = null
        ): Long {
            var remainRequiredAmount = amount
            val have = inventory.get(aeKey)
            val extract = minOf(have, remainRequiredAmount)
            if (extract > 0L) {
                remainRequiredAmount -= extract
                inventory.remove(aeKey, extract)
                nusedItems.add(aeKey, extract)
                if (middlePlan != null) {
                    middlePlan.nusedItems.add(aeKey, extract)
                }
            }

            if (fuzzyMode != null && remainRequiredAmount > 0L) {
                for (entry in inventory.findFuzzy(aeKey, fuzzyMode)) {
                    if (remainRequiredAmount <= 0L) {
                        break
                    }
                    val candidate = entry.key
                    val have2 = entry.longValue
                    val extract2 = minOf(have2, remainRequiredAmount)
                    if (extract2 > 0L) {
                        remainRequiredAmount -= extract2
                        inventory.remove(candidate, extract2)
                        nusedItems.add(candidate, extract2)
                        if (middlePlan != null) {
                            middlePlan.nusedItems.add(candidate, extract2)
                        }
                    }
                }
            }

            if (remainRequiredAmount > 0L && requestRemain) {
                if (sortedComputingComponent.any { it.middleOutput === aeKey }) {
                    RequirementsManager.addRequirement(requirements, aeKey, remainRequiredAmount)
                } else {
                    nmissingItems.add(aeKey, remainRequiredAmount)
                    isSimulated.set(true)
                }
            }

            return remainRequiredAmount
        }

        override fun bytes(): Long {
            val totalBytes = getAllDeepComponent().sumOf { it.getMiddlePlanOptional()?.bytes() ?: 0L }
            return totalBytes
        }

        fun getAllDeepComponent(): ArrayList<ComputingComponent> {
            val queue = ArrayDeque<ComputingComponent>()
            val allComponent = ArrayList<ComputingComponent>()
            sortedComputingComponent.forEach { queue.add(it) }
            while (!queue.isEmpty()) {
                val component = queue.removeFirst()
                allComponent.add(component)
                if (component is GetSubComponent) {
                    queue.addAll(component.getSubComponent())
                }
            }
            return allComponent
        }

        override fun simulation(): Boolean =
            sortedComputingComponent.any { it.middlePlan.simulation() } || isSimulated.get()

        override fun multiplePaths(): Boolean = sortedComputingComponent.any { it.middlePlan.multiplePaths() }
    }

    class MiddlePlan(
        finalOutput: AEKey,
        val finalPlan: FinalPlan,
        val component: ComputingComponent,
        val parentPlan: SuriedCraftingPlan
    ) : SuriedCraftingPlan(finalOutput) {

        lateinit var internalInventory: KeyCounter

        override fun initRuntimeData() {
            super.initRuntimeData()
            internalInventory = KeyCounter()
        }

        override fun bytes(): Long {
            val total = LongHolder()
            patternTimes.object2LongEntrySet().fastForEach { e -> total.value += e.longValue }
            nusedItems.forEach {
                total.value += if (it.key is AEItemKey) {
                    it.longValue * 4
                } else if (it.key is AEFluidKey) {
                    (it.longValue / 100.0 * 4.0).toLong()
                } else {
                    it.longValue * 2
                }
            }
            return total.value
        }

        override fun simulation(): Boolean = false

        override fun multiplePaths(): Boolean = false

        fun storageInternalItem(aeKey: AEKey, amount: Long) {
            internalInventory.add(aeKey, amount)
        }

        fun storageGlobalItem(aeKey: AEKey, amount: Long) {
            finalPlan.inventory.add(aeKey, amount)
        }

        fun useInternalItem(
            aeKey: AEKey,
            amount: Long,
            fuzzyMode: FuzzyMode? = null,
            requestRemain: Boolean = true
        ): Long {
            var remainRequiredAmount = amount
            val have = internalInventory.get(aeKey)
            val extract = minOf(have, remainRequiredAmount)
            if (extract > 0L) {
                remainRequiredAmount -= extract
                internalInventory.remove(aeKey, extract)
            }

            if (fuzzyMode != null && remainRequiredAmount > 0L) {
                for (entry in internalInventory.findFuzzy(aeKey, fuzzyMode)) {
                    if (remainRequiredAmount <= 0L) {
                        break
                    }
                    val candidate = entry.key
                    val extract2 = minOf(entry.longValue, remainRequiredAmount)
                    if (extract2 > 0L) {
                        remainRequiredAmount -= extract2
                        internalInventory.remove(candidate, extract2)
                    }
                }
            }

            if (remainRequiredAmount > 0L && requestRemain) {
                if (finalPlan.sortedComputingComponent.any { it.middleOutput === aeKey }) {
                    RequirementsManager.addRequirement(finalPlan.requirements, aeKey, remainRequiredAmount)
                } else {
                    val remain = finalPlan.useGlobalItem(aeKey, remainRequiredAmount, requestRemain = false)
                    if (remain > 0L) {
                        finalPlan.nmissingItems.add(aeKey, remain)
                        finalPlan.isSimulated.set(true)
                    }
                }
            }

            return remainRequiredAmount
        }
    }
}
