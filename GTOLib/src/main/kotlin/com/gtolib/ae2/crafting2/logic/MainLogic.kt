package com.gtolib.ae2.crafting2.logic

import appeng.api.crafting.IPatternDetails
import appeng.api.networking.IGrid
import appeng.api.networking.crafting.CalculationStrategy
import appeng.api.networking.crafting.ICraftingPlan
import appeng.api.networking.crafting.ICraftingService
import appeng.api.networking.crafting.ICraftingSimulationRequester
import appeng.api.stacks.AEKey
import appeng.api.stacks.GenericStack
import appeng.api.stacks.KeyCounter
import appeng.crafting.CraftingPlan
import appeng.crafting.pattern.AECraftingPattern
import appeng.crafting.pattern.AEProcessingPattern
import appeng.crafting.pattern.AESmithingTablePattern
import appeng.crafting.pattern.AEStonecuttingPattern
import com.gto.fastcollection.fastutil.O2IOpenCacheHashMap
import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap
import com.gto.fastcollection.fastutil.OpenCacheHashSet
import com.gtocore.config.GTOConfig
import com.gtolib.GTOCore
import com.gtolib.ae2.crafting2.model.ComputingComponent
import com.gtolib.ae2.crafting2.model.SuriedCraftingPlan
import com.gtolib.ae2.crafting2.utils.AE2CraftingTranslation
import com.gtolib.ae2.crafting2.utils.AECrafting2Utils
import com.gtolib.ae2.crafting2.utils.DisplayLevel
import com.gtolib.ae2.crafting2.utils.PerfLogger
import com.gtolib.ae2.crafting2.utils.RequirementsManager
import it.unimi.dsi.fastutil.objects.Object2LongMaps
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import java.util.ArrayDeque
import java.util.ArrayList
import java.util.Collections
import java.util.PriorityQueue
import kotlin.jvm.internal.Ref.IntRef
import net.minecraft.network.chat.Component

class ComputingComponentBuildFailedException(
    message: Component,
    detail: IPatternDetails? = null,
    cycle: List<ComputingComponent>? = null,
    cause: Throwable? = null
) : RuntimeException(message.string, cause)

class CycleTopologyBuildError(message: Component, cycle: List<ComputingComponent>) : RuntimeException(message.string)

class SendComponentToPlayerException(val components: List<Component>) :
    RuntimeException(components.joinToString(" | ") { it.string })

object MainLogic {

    fun executeV2(
        grid: IGrid,
        keyCounter: KeyCounter,
        simRequester: ICraftingSimulationRequester,
        what: AEKey,
        amount: Long,
        strategy: CalculationStrategy
    ): ICraftingPlan {
        try {
            if (PerfLogger.destroy) {
                throw RuntimeException()
            }
            val perfLogger = PerfLogger()
            perfLogger.push("主请求")
            perfLogger.push("初始化算子")
            val emittable = grid.craftingService.craftingProviders.emittableKeys
            perfLogger.push("初始化计划(组织最终计划，但其中的所有算子为空)")
            val finalPlan = organizationBlankPlan(what, perfLogger)
            perfLogger.pop()
            perfLogger.push("构建物品->此物品可用算子的映射")
            organizationComputingComponent(finalPlan, grid.craftingService)
            perfLogger.pop()
            perfLogger.push("提取SCC")
            val sccLists = extractComputingComponentScc(finalPlan)
            perfLogger.pop()
            perfLogger.push("构建SCC算子映射")
            organizationSCCComputingComponent(finalPlan, sccLists)
            perfLogger.pop()
            perfLogger.push("构建多路径选择算子")
            organizationMultiPathRouter(finalPlan)
            perfLogger.pop()
            perfLogger.push("拓扑排序算子")
            topoSortComputingComponents(finalPlan)
            perfLogger.pop()
            perfLogger.pop()
            perfLogger.push("策略 $strategy 计算")

            fun simulateAndCheck(mid: Long): Boolean {
                performCalculationAndSort(keyCounter, finalPlan, mid, grid, perfLogger, what)
                return !finalPlan.isSimulated.get()
            }

            val finalAmount = when (strategy) {
                CalculationStrategy.REPORT_MISSING_ITEMS -> {
                    performCalculationAndSort(keyCounter, finalPlan, amount, grid, perfLogger, what)
                    finalPlan.finalOutputAmount.get()
                }

                CalculationStrategy.CRAFT_LESS -> {
                    var low = 0L
                    var high = amount
                    if (!simulateAndCheck(high)) {
                        performCalculationAndSort(keyCounter, finalPlan, amount, grid, perfLogger, what)
                        finalPlan.finalOutputAmount.get()
                    } else {
                        while (low < high) {
                            val mid = (low + high + 1L) / 2
                            if (simulateAndCheck(mid)) {
                                low = mid
                            } else {
                                high = mid - 1L
                            }
                        }
                        low
                    }
                }
            }
            perfLogger.pop()
            val displayLevel = if (GTOConfig.INSTANCE.devMode.aeLog) DisplayLevel.MAIN else DisplayLevel.NONE
            System.out.print(perfLogger.report(displayLevel))
            val usedItemsIterator = finalPlan.usedItems().iterator()
            while (usedItemsIterator.hasNext()) {
                if (usedItemsIterator.next().longValue <= 0L) {
                    usedItemsIterator.remove()
                }
            }
            val emittedItemsIterator = finalPlan.emittedItems().iterator()
            while (emittedItemsIterator.hasNext()) {
                if (emittedItemsIterator.next().longValue <= 0L) {
                    emittedItemsIterator.remove()
                }
            }
            val missingItemsIterator = finalPlan.missingItems().iterator()
            while (missingItemsIterator.hasNext()) {
                if (missingItemsIterator.next().longValue <= 0L) {
                    missingItemsIterator.remove()
                }
            }
            val patternTimesIterator = finalPlan.patternTimes().entries.iterator()
            while (patternTimesIterator.hasNext()) {
                if (patternTimesIterator.next().value <= 0L) {
                    patternTimesIterator.remove()
                }
            }
            for (key in emittable) {
                finalPlan.emittedItems().add(key, finalPlan.missingItems().get(key))
                finalPlan.missingItems().remove(key)
            }
            val craftingPlan = CraftingPlan(
                GenericStack(what, finalAmount),
                finalPlan.bytes(),
                finalPlan.isSimulated.get(),
                false,
                finalPlan.usedItems(),
                finalPlan.emittedItems(),
                finalPlan.missingItems(),
                finalPlan.patternTimes()
            )
            craftingPlan.`setGtocore$allocations`(finalPlan.highPriorityPush)
            return craftingPlan
        } catch (e: SendComponentToPlayerException) {
            for (line in e.components) {
                AE2CraftingTranslation.sendErrorMessage(simRequester.actionSource, line)
                GTOCore.LOGGER.error(line.toString())
            }
            return CraftingPlan(
                GenericStack(what, 1L),
                1L,
                true,
                false,
                KeyCounter(),
                KeyCounter(),
                KeyCounter(),
                Object2LongMaps.emptyMap()
            )
        } catch (e: Exception) {
            System.err.println(e)
            e.stackTrace.forEach { System.err.println(it) }
            e.printStackTrace(System.err)
            throw e
        }
    }

    fun beforeMainLogicRequest(keyCounter: KeyCounter, finalPlan: SuriedCraftingPlan.FinalPlan, amount: Long, grid: IGrid) {
        finalPlan.setFinalOutputAmount(amount)
        finalPlan.initInventory(keyCounter, grid)
        finalPlan.initRuntimeData()
    }

    fun mainLogicRequest(finalPlan: SuriedCraftingPlan.FinalPlan, amount: Long) {
        RequirementsManager.addRequirement(finalPlan.requirements, finalPlan.finalOutput().what(), amount)
        finalPlan.getAllDeepComponent().forEach { component ->
            when (component) {
                is ComputingComponent.CompositeComponent -> {}
                is ComputingComponent.MultiCraftComponent -> {
                    if (component.analyzer.primaryOutputIsSelfIncrease() && component.middleOutput === finalPlan.finalOutput) {
                        throw SendComponentToPlayerException(AE2CraftingTranslation.ERR_CRAFTING_SELF_INCREASE_OPERATOR(component.middleOutput))
                    }
                }

                is ComputingComponent.MultiPathSelectionComponent -> {}
                is ComputingComponent.MultiProcessComponent -> {
                    if (component.analyzer.primaryOutputIsSelfIncrease() && component.middleOutput === finalPlan.finalOutput) {
                        throw SendComponentToPlayerException(AE2CraftingTranslation.ERR_CRAFTING_SELF_INCREASE_OPERATOR(component.middleOutput))
                    }
                }

                is ComputingComponent.OnceCraftComponent -> {
                    if (component.analyzer.primaryOutputIsSelfIncrease() && component.middleOutput === finalPlan.finalOutput) {
                        throw SendComponentToPlayerException(AE2CraftingTranslation.ERR_CRAFTING_SELF_INCREASE_OPERATOR(component.middleOutput))
                    }
                }
            }
        }
        for (component in finalPlan.sortedComputingComponent) {
            component.request(
                component.middleOutput,
                RequirementsManager.getRequirement(finalPlan.requirements, component.middleOutput),
                finalPlan,
                finalPlan
            )
            finalPlan.processedComputingComponentCount.addAndGet(1L)
        }
        for (component in finalPlan.sortedComputingComponent) {
            component.getMiddlePlanOptional()?.let { finalPlan.inventory.addAll(it.internalInventory) }
        }
    }

    fun performCalculationAndSort(
        keyCounter: KeyCounter,
        finalPlan: SuriedCraftingPlan.FinalPlan,
        amount: Long,
        grid: IGrid,
        perfLogger: PerfLogger,
        what: AEKey
    ) {
        perfLogger.push("开始计算 ${what.displayName.string} x$amount")
        perfLogger.push("初始化计算数据")
        beforeMainLogicRequest(keyCounter, finalPlan, amount, grid)
        perfLogger.pop()
        perfLogger.push("主逻辑开始请求")
        mainLogicRequest(finalPlan, amount)
        perfLogger.pop()
        perfLogger.push("整理计划")
        sortPlan(finalPlan)
        perfLogger.pop()
        perfLogger.pop()
    }

    fun sortPlan(finalPlan: SuriedCraftingPlan.FinalPlan) {
        RequirementsManager.getUnfulfilledRequirements(finalPlan.requirements).reference2LongEntrySet().fastForEach { entry ->
            val key = entry.key
            val needed = entry.longValue
            if (needed > 0L) {
                val remainingNeeded = finalPlan.useGlobalItem(key, needed, requestRemain = false)
                if (remainingNeeded > 0L) {
                    finalPlan.nmissingItems.add(key, remainingNeeded)
                    finalPlan.isSimulated.set(true)
                }
            }
        }
    }

    fun organizationBlankPlan(finalOutput: AEKey, perfLogger: PerfLogger): SuriedCraftingPlan.FinalPlan =
        SuriedCraftingPlan.FinalPlan(finalOutput, perfLogger)

    fun organizationComputingComponent(finalPlan: SuriedCraftingPlan.FinalPlan, craftService: ICraftingService) {
        val queue = ArrayDeque<AEKey>()
        val visited = ReferenceOpenHashSet<AEKey>()
        visited.add(finalPlan.finalOutput)
        queue.add(finalPlan.finalOutput)

        while (queue.isNotEmpty()) {
            val middleOutput = queue.poll()
            val detailsIdentitySet = ReferenceOpenHashSet<IPatternDetails>()
            for (details in craftService.getCraftingFor(middleOutput)) {
                detailsIdentitySet.add(details)
            }
            for (details in detailsIdentitySet) {
                finalPlan.perfLogger.push("构建中间物品 ${middleOutput.displayName.string}", DisplayLevel.DETAIL)

                val component = try {
                    when (details) {
                        is AEProcessingPattern -> if (ComputingComponent.MultiProcessComponent.canBuild(details)) {
                            ComputingComponent.MultiProcessComponent(middleOutput, details, finalPlan)
                        } else {
                            null
                        }

                        is AEStonecuttingPattern -> if (ComputingComponent.MultiProcessComponent.canBuild(details)) {
                            ComputingComponent.MultiProcessComponent(middleOutput, details, finalPlan)
                        } else {
                            null
                        }

                        is AESmithingTablePattern -> if (ComputingComponent.MultiProcessComponent.canBuild(details)) {
                            ComputingComponent.MultiProcessComponent(middleOutput, details, finalPlan)
                        } else {
                            null
                        }

                        is AECraftingPattern -> if (ComputingComponent.MultiCraftComponent.canBuild(details)) {
                            ComputingComponent.MultiCraftComponent(middleOutput, details, finalPlan)
                        } else if (ComputingComponent.OnceCraftComponent.canBuild(details)) {
                            ComputingComponent.OnceCraftComponent(middleOutput, details, finalPlan)
                        } else {
                            null
                        }

                        else -> null
                    }
                } catch (ex: Exception) {
                    throw ComputingComponentBuildFailedException(
                        Component.literal("生成算子时出错: ${middleOutput.displayName.string}, 原因: ${ex.message ?: ex.javaClass.simpleName}"),
                        details,
                        cause = ex
                    )
                }

                if (component == null) {
                    throw ComputingComponentBuildFailedException(
                        Component.literal("无法为物品 ${middleOutput.displayName.string} 的 $details 生成算子，可能是其算子未定义"),
                        details
                    )
                }

                val computingComponentList = finalPlan.middleOutputToComponent.computeIfAbsent(middleOutput) { ArrayList() }
                if (!computingComponentList.contains(component)) {
                    computingComponentList.add(component)
                }
                for (depend in component.getDependencies()) {
                    if (visited.add(depend)) {
                        queue.offer(depend)
                    }
                }
                finalPlan.perfLogger.pop()
            }
        }
    }

    fun extractComputingComponentScc(finalPlan: SuriedCraftingPlan.FinalPlan): List<List<ComputingComponent>> {
        val all = finalPlan.middleOutputToComponent.values.flatten().toSet()
        val index = IntRef()
        val idx = O2IOpenCacheHashMap<ComputingComponent>().apply { defaultReturnValue(-1) }
        val low = O2IOpenCacheHashMap<ComputingComponent>().apply { defaultReturnValue(-1) }
        val stack = ArrayDeque<ComputingComponent>()
        val onStack = OpenCacheHashSet<ComputingComponent>()
        val sccSets = ArrayList<MutableSet<ComputingComponent>>()

        fun neighbors(c: ComputingComponent): List<ComputingComponent> =
            c.getDependencies().flatMap { k -> finalPlan.middleOutputToComponent[k] ?: emptyList() }

        fun strongConnect(v: ComputingComponent) {
            idx.put(v, index.element)
            low.put(v, index.element)
            index.element++
            stack.addFirst(v)
            onStack.add(v)
            for (w in neighbors(v)) {
                if (!idx.containsKey(w)) {
                    strongConnect(w)
                    low.put(v, minOf(low.getInt(v), low.getInt(w)))
                } else if (onStack.contains(w)) {
                    low.put(v, minOf(low.getInt(v), idx.getInt(w)))
                }
            }
            if (low.getInt(v) == idx.getInt(v)) {
                val sx = ReferenceLinkedOpenHashSet<ComputingComponent>()
                var w: ComputingComponent
                do {
                    w = stack.removeFirst()
                    onStack.remove(w)
                    sx.add(w)
                } while (w !== v)
                if (sx.size > 1) {
                    sccSets.add(sx)
                }
            }
        }

        for (v in all) {
            if (!idx.containsKey(v)) {
                strongConnect(v)
            }
        }

        fun findCycleIn(scc: MutableSet<ComputingComponent>): List<ComputingComponent> {
            val visited = OpenCacheHashSet<ComputingComponent>()
            val onPathIndex = O2IOpenCacheHashMap<ComputingComponent>().apply { defaultReturnValue(-1) }
            val path = ArrayList<ComputingComponent>()

            fun dfs(u: ComputingComponent): List<ComputingComponent>? {
                visited.add(u)
                onPathIndex.put(u, path.size)
                path.add(u)
                for (v in neighbors(u)) {
                    if (scc.contains(v)) {
                        val pos = onPathIndex.getInt(v)
                        if (pos != -1) {
                            return path.subList(pos, path.size).toList()
                        }
                        if (!visited.contains(v)) {
                            val r = dfs(v)
                            if (r != null) {
                                return r
                            }
                        }
                    }
                }
                path.removeAt(path.lastIndex)
                onPathIndex.removeInt(u)
                return null
            }

            for (start in scc) {
                if (!visited.contains(start)) {
                    val r = dfs(start)
                    if (r != null) {
                        return r
                    }
                }
            }
            throw CycleTopologyBuildError(Component.literal("无法在强连通分量中构造有向环序"), scc.toList())
        }

        val out = ArrayList<List<ComputingComponent>>()
        for (s in sccSets) {
            out.add(findCycleIn(s))
        }
        return out
    }

    fun organizationSCCComputingComponent(finalPlan: SuriedCraftingPlan.FinalPlan, sccLists: List<List<ComputingComponent>>) {
        for (cycle in sccLists) {
            val keysInCycle = cycle.map { it.middleOutput }.toSet()
            for (key in keysInCycle) {
                val head = cycle.firstOrNull { it.middleOutput === key }
                if (head != null) {
                    val circularCycle = AECrafting2Utils.getCircularList(cycle, head)
                    if (!ComputingComponent.CompositeComponent.canBuild(circularCycle)) {
                        throw ComputingComponentBuildFailedException(
                            Component.literal("无法为物品 ${key.displayName.string} 的 $circularCycle 构建组合算子，可能是其算子未定义"),
                            cycle = circularCycle
                        )
                    }
                    val composite = ComputingComponent.CompositeComponent(key, circularCycle, finalPlan)
                    val existing = finalPlan.getComputingComponentFor(key)
                    val others = ArrayList<ComputingComponent>(existing.size)
                    for (op in existing) {
                        if (!cycle.contains(op)) {
                            others.add(op)
                        }
                    }
                    val newList = ArrayList<ComputingComponent>(others.size + 1)
                    newList.add(composite)
                    for (op in others) {
                        newList.add(op)
                    }
                    val dedup = ArrayList<ComputingComponent>(newList.size)
                    val seen = OpenCacheHashSet<ComputingComponent>()
                    for (op in newList) {
                        if (seen.add(op)) {
                            dedup.add(op)
                        }
                    }
                    finalPlan.middleOutputToComponent.put(key, dedup)
                }
            }
        }
    }

    fun organizationMultiPathRouter(finalPlan: SuriedCraftingPlan.FinalPlan) {
        for (entry in finalPlan.middleOutputToComponent.entries) {
            val key = entry.key
            val router = if (ComputingComponent.MultiPathSelectionComponent.canBuild(entry.value)) {
                ComputingComponent.MultiPathSelectionComponent(key, entry.value, finalPlan)
            } else {
                null
            }
            if (router != null) {
                entry.value.clear()
                entry.value.add(router)
            }
        }
    }

    fun topoSortComputingComponents(finalPlan: SuriedCraftingPlan.FinalPlan) {
        val start = finalPlan.middleOutputToComponent[finalPlan.finalOutput] ?: ArrayList()
        if (!start.isEmpty()) {
            fun isComposite(c: ComputingComponent): Boolean = c is ComputingComponent.CompositeComponent

            fun internalKeysOf(c: ComputingComponent): Set<AEKey> =
                if (c is ComputingComponent.CompositeComponent) {
                    c.components.map { it.middleOutput }.toSet()
                } else {
                    emptySet()
                }

            fun effectiveDeps(u: ComputingComponent): MutableSet<AEKey> {
                val base = u.getDependencies().toMutableSet()
                if (isComposite(u)) {
                    base.addAll(internalKeysOf(u).filter { it !== u.middleOutput })
                }
                return base
            }

            fun producersFor(u: ComputingComponent, depKey: AEKey): List<ComputingComponent> {
                val producers = finalPlan.middleOutputToComponent[depKey] ?: return emptyList()
                val internal = internalKeysOf(u)
                return if (internal.contains(depKey)) {
                    producers.filter { !isComposite(it) }
                } else {
                    producers
                }
            }

            fun neighbors(u: ComputingComponent): List<ComputingComponent> =
                effectiveDeps(u).flatMap { producersFor(u, it) }

            val nodes = ReferenceLinkedOpenHashSet<ComputingComponent>()
            val dq = ArrayDeque<ComputingComponent>()
            for (s in start) {
                if (nodes.add(s)) {
                    dq.addLast(s)
                }
            }
            while (dq.isNotEmpty()) {
                val u = dq.removeFirst()
                for (v in neighbors(u)) {
                    if (nodes.add(v)) {
                        dq.addLast(v)
                    }
                }
            }
            val adj = O2OOpenCacheHashMap<ComputingComponent, ArrayList<ComputingComponent>>(nodes.size * 2)
            val indeg = O2IOpenCacheHashMap<ComputingComponent>(nodes.size).apply { defaultReturnValue(0) }
            for (n in nodes) {
                adj.put(n, ArrayList())
                indeg.put(n, 0)
            }
            for (u in nodes) {
                for (depKey in effectiveDeps(u)) {
                    for (v in producersFor(u, depKey)) {
                        if (nodes.contains(v) && v !== u) {
                            adj.get(u)!!.add(v)
                            indeg.addTo(v, 1)
                        }
                    }
                }
            }
            val pq = PriorityQueue<ComputingComponent> { a, b ->
                if (a.middleOutput === b.middleOutput) {
                    val ca = isComposite(a)
                    val cb = isComposite(b)
                    if (ca && !cb) -1 else if (!ca && cb) 1 else 0
                } else {
                    0
                }
            }
            indeg.object2IntEntrySet().fastForEach { e ->
                if (e.intValue == 0) {
                    pq.add(e.key)
                }
            }
            val out = ArrayList<ComputingComponent>(nodes.size)
            while (pq.isNotEmpty()) {
                val u = pq.remove()
                out.add(u)
                for (v in adj.get(u)!!) {
                    indeg.addTo(v, -1)
                    if (indeg.getInt(v) == 0) {
                        pq.add(v)
                    }
                }
            }
            if (out.size != nodes.size) {
                throw CycleTopologyBuildError(Component.literal("拓扑排序失败：检测到环或未压缩的组合算子依赖"), out)
            }
            finalPlan.sortedComputingComponent = out
        }
    }
}
