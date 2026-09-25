package com.gtolib.ae2.crafting2.utils

import appeng.api.stacks.AEKey
import appeng.api.stacks.AEKeyMap
import appeng.api.stacks.KeyCounter

data class RequirementFulfillmentResult(val fulfilledAmount: Long, val remainingOutput: Long)

object RequirementsManager {

    fun removeRequirement(requirements: KeyCounter, key: AEKey, amount: Long): Long {
        require(amount >= 0L) { "移除数量不能为负数: $amount" }
        val actualRemoved = minOf(amount, requirements.get(key))
        if (actualRemoved > 0L) {
            requirements.remove(key, actualRemoved)
        }
        return actualRemoved
    }

    fun addRequirement(requirements: KeyCounter, key: AEKey, amount: Long) {
        require(amount >= 0L) { "添加数量不能为负数: $amount" }
        if (amount > 0L) {
            requirements.add(key, amount)
        }
    }

    fun addAllRequirements(requirements: KeyCounter, sourceRequirements: KeyCounter) {
        requirements.addAll(sourceRequirements)
    }

    fun fulfillRequirement(requirements: KeyCounter, key: AEKey, producedAmount: Long): RequirementFulfillmentResult {
        require(producedAmount >= 0L) { "产出数量不能为负数: $producedAmount" }
        val requirementDone = minOf(producedAmount, requirements.get(key))
        val remainingOutput = producedAmount - requirementDone
        if (requirementDone > 0L) {
            requirements.remove(key, requirementDone)
        }
        return RequirementFulfillmentResult(requirementDone, remainingOutput)
    }

    fun getRequirement(requirements: KeyCounter, key: AEKey): Long = requirements.get(key)

    fun getUnfulfilledRequirements(requirements: KeyCounter): AEKeyMap<AEKey> {
        val map = AEKeyMap<AEKey>(requirements.size())
        for (entry in requirements) {
            if (entry.longValue > 0L) {
                map.put(entry.key, entry.longValue)
            }
        }
        return map
    }
}
