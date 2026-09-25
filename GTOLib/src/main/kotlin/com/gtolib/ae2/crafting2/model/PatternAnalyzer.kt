package com.gtolib.ae2.crafting2.model

import appeng.api.crafting.IPatternDetails
import appeng.api.stacks.AEKey
import appeng.api.stacks.AEKeyMap
import appeng.api.stacks.GenericStack
import appeng.crafting.pattern.AECraftingPattern
import appeng.crafting.pattern.AEProcessingPattern
import appeng.crafting.pattern.AESmithingTablePattern
import appeng.crafting.pattern.AEStonecuttingPattern
import com.gtolib.ae2.crafting2.model.SuriedCraftingPlan.FinalPlan
import com.gtolib.ae2.crafting2.model.SuriedCraftingPlan.MiddlePlan
import com.gtolib.ae2.crafting2.utils.AECrafting2Utils
import com.gtolib.ae2.crafting2.utils.RequirementsManager
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap
import kotlin.math.ceil

class PatternAnalyzer(
    val sparseInputs: Array<GenericStack?>,
    val sparseOutputs: Array<GenericStack?>,
    val targetOutput: AEKey
) {
    val inputs = AECrafting2Utils.condenseStacks(sparseInputs)
    val outputs = AECrafting2Utils.condenseStacks(sparseOutputs)
    val primaryInputAmount = inputs.firstOrNull { it.what() == targetOutput }?.amount() ?: 0L
    val primaryOutputAmount = outputs.firstOrNull { it.what() == targetOutput }?.amount() ?: 0L
    val netOutputPerCraft = primaryOutputAmount - primaryInputAmount

    init {
        require(primaryOutputAmount > 0L) { "Pattern does not produce the target output $targetOutput" }
        require(netOutputPerCraft > 0L) {
            "Pattern does not have positive net output for $targetOutput (input: $primaryInputAmount, output: $primaryOutputAmount)"
        }
    }

    fun calculateCrafting(
        requestedAmount: Long,
        minParallelCycles: Long = 1L,
        pattern: IPatternDetails? = null
    ): CraftingAnalysis {
        val requiredCrafts = if (netOutputPerCraft > 0L) {
            ceil(requestedAmount.toDouble() / netOutputPerCraft).toLong()
        } else {
            requestedAmount
        }
        val actualParallel = minOf(minParallelCycles, requiredCrafts)
        val inputRequirements = AEKeyMap<AEKey>()
        val outputProducts = AEKeyMap<AEKey>()

        for (output in inputs) {
            val currentOutput = output.what()
            val netAmountPerCraft = output.amount()
            val requiredAmount = if (outputs.any { it.what() === currentOutput }) {
                val netInputPerCraft = netAmountPerCraft - outputs.first { it.what() === currentOutput }.amount()
                if (netInputPerCraft > 0L) {
                    netAmountPerCraft * actualParallel + (requiredCrafts - actualParallel) * netInputPerCraft
                } else {
                    netAmountPerCraft * actualParallel
                }
            } else {
                netAmountPerCraft * requiredCrafts
            }
            if (requiredAmount > 0L) {
                inputRequirements.put(currentOutput, requiredAmount)
            }
        }

        for (output in outputs) {
            val currentOutput = output.what()
            val netInput = inputs.firstOrNull { it.what() === currentOutput }
            outputProducts.put(
                currentOutput,
                (output.amount() - (netInput?.amount() ?: 0L)).coerceAtLeast(0L) * requiredCrafts
            )
        }

        if (outputProducts.containsKey(targetOutput)) {
            outputProducts.put(
                targetOutput,
                maxOf(netOutputPerCraft * requiredCrafts, maxOf(outputProducts.getLong(targetOutput), requestedAmount))
            )
        }

        return CraftingAnalysis(
            pattern,
            targetOutput,
            requestedAmount,
            requestedAmount,
            requiredCrafts,
            netOutputPerCraft,
            this,
            inputRequirements,
            outputProducts,
            actualParallel
        )
    }

    fun primaryOutputIsSelfIncrease(): Boolean =
        inputs.firstOrNull { it.what() == targetOutput } != null && netOutputPerCraft > 0L

    fun maxExternalExtractableForSelfIncrease(
        availableInventory: Long,
        requestedAmount: Long,
        minParallelCycles: Long = 1L
    ): Long {
        val inputPerCraft = primaryInputAmount
        val netIncreasePerCraft = netOutputPerCraft
        require(netOutputPerCraft > 0L) {
            "Not a self-increase pattern for target output or net output per craft <= 0"
        }
        val demandAmount = requestedAmount.coerceAtLeast(0L)
        val inventoryAmount = availableInventory.coerceAtLeast(0L)
        val minParallelism = minParallelCycles.coerceAtLeast(1L)
        if (!primaryOutputIsSelfIncrease()) {
            return inventoryAmount
        }

        fun ceilDiv(numerator: Long, denominator: Long): Long {
            require(denominator > 0L) { "divisor must be > 0" }
            if (numerator <= 0L) {
                return 0L
            }
            return if (numerator % denominator == 0L) numerator / denominator else numerator / denominator + 1L
        }

        val craftsNeededIgnoringInventory = ceilDiv(demandAmount, netIncreasePerCraft)
        return if (craftsNeededIgnoringInventory > minParallelism) {
            (inventoryAmount - inputPerCraft * minParallelism).coerceAtLeast(0L)
        } else {
            maxOf(0L, maxOf(inventoryAmount - demandAmount, inventoryAmount - inputPerCraft * craftsNeededIgnoringInventory))
        }
    }

    companion object {
        fun fromPattern(detail: IPatternDetails, targetOutput: AEKey): PatternAnalyzer = when (detail) {
            is AEProcessingPattern -> PatternAnalyzer(detail.sparseInputs, detail.sparseOutputs, targetOutput)

            is AECraftingPattern -> {
                val inputsWithFluidReplacement = AECrafting2Utils.extractPatternInputsWithFluidReplacement(detail, true)
                PatternAnalyzer(inputsWithFluidReplacement, arrayOf(detail.primaryOutput), targetOutput)
            }

            is AEStonecuttingPattern -> PatternAnalyzer(
                arrayOf(GenericStack(detail.input, 1L)),
                detail.outputs,
                targetOutput
            )

            is AESmithingTablePattern -> PatternAnalyzer(
                arrayOf(
                    GenericStack(detail.template, 1L),
                    GenericStack(detail.base, 1L),
                    GenericStack(detail.addition, 1L)
                ),
                detail.outputs,
                targetOutput
            )

            else -> throw IllegalArgumentException("Unsupported pattern type: $detail")
        }
    }
}

data class CraftingAnalysis(
    val pattern: IPatternDetails?,
    val targetOutput: AEKey,
    val requestedAmount: Long,
    val calculatedForAmount: Long,
    val craftsRequired: Long,
    val netOutputPerCraft: Long,
    val patternAnalyzer: PatternAnalyzer,
    val inputRequirements: AEKeyMap<AEKey>,
    val outputProducts: AEKeyMap<AEKey>,
    val actualParallelCycles: Long
) {
    fun applyToPlans(finalPlan: FinalPlan, middlePlan: MiddlePlan) {
        pattern?.let {
            middlePlan.patternTimes.addTo(it, craftsRequired)
        }

        inputRequirements.reference2LongEntrySet().fastForEach { entry ->
            val key = entry.key
            val required = entry.longValue
            val net = outputProducts.getLong(key) - required
            if (net > 0L) {
                finalPlan.useGlobalItem(key, required)
            } else {
                middlePlan.useInternalItem(key, required)
            }
        }

        val hasSomethingSelfIncrease = outputProducts.reference2LongEntrySet().any { entry ->
            val key = entry.key
            val amount = entry.longValue
            val required = inputRequirements.getLong(key)
            inputRequirements.containsKey(key) && amount > required
        }

        if (hasSomethingSelfIncrease) {
            for (genericStack in patternAnalyzer.inputs) {
                finalPlan.highPriorityPush.computeIfAbsent(genericStack.what()) { Object2LongOpenHashMap<IPatternDetails>() }
                    .addTo(pattern, genericStack.amount() * craftsRequired)
            }
        }

        outputProducts.reference2LongEntrySet().fastForEach { entry ->
            val key = entry.key
            val amount = entry.longValue
            if (key === middlePlan.finalOutput) {
                val requiredAmount = calculatedForAmount
                val currentAmount = middlePlan.finalOutputAmount.get()
                val remainAmount = amount - requiredAmount
                require(remainAmount >= 0L) {
                    "制作所得小于此算子的请求数量: 产出量=$amount, 计算时需求=$requiredAmount, 当前需求=$currentAmount, 差异=$remainAmount, 目标物品=$targetOutput, 样板=$pattern, 净产出/次=$netOutputPerCraft, 制作次数=$craftsRequired, 初始请求=$requestedAmount, 实际并行=$actualParallelCycles"
                }
                RequirementsManager.removeRequirement(finalPlan.requirements, key, requiredAmount)
                middlePlan.storageInternalItem(key, remainAmount)
            } else {
                middlePlan.storageInternalItem(key, amount)
            }
            middlePlan.netOutputItems.add(key, amount)
        }
    }
}

data class SelfIncreaseExtractionPlan(
    val crafts: Long,
    val seedReserved: Long,
    val inventoryUsedForDemand: Long,
    val extractable: Long
)
