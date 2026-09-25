package com.gtolib.ae2.crafting2.utils

import appeng.api.stacks.AEKey
import appeng.api.stacks.AEKeyMap
import appeng.api.stacks.KeyCounter
import it.unimi.dsi.fastutil.objects.Reference2LongMap.Entry
import kotlin.jvm.internal.SourceDebugExtension

@SourceDebugExtension(["SMAP\nRequirementsManager.kt\nKotlin\n*S Kotlin\n*F\n+ 1 RequirementsManager.kt\ncom/gtolib/ae2/crafting2/utils/RequirementsManager\n+ 2 fake.kt\nkotlin/jvm/internal/FakeKt\n*L\n1#1,130:1\n1#2:131\n*E\n"])
public object RequirementsManager {
   public fun removeRequirement(requirements: KeyCounter, key: AEKey, amount: Long): Long {
      if (amount < 0L) {
         throw IllegalArgumentException(("移除数量不能为负数: $amount").toString())
      } else {
         val actualRemoved: Long = Math.min(amount, requirements.get(key))
         if (actualRemoved > 0L) {
            requirements.remove(key, actualRemoved)
         }

         return actualRemoved
      }
   }

   public fun addRequirement(requirements: KeyCounter, key: AEKey, amount: Long) {
      if (amount < 0L) {
         throw IllegalArgumentException(("添加数量不能为负数: $amount").toString())
      } else {
         if (amount > 0L) {
            requirements.add(key, amount)
         }
      }
   }

   public fun addAllRequirements(requirements: KeyCounter, sourceRequirements: KeyCounter) {
      requirements.addAll(sourceRequirements)
   }

   public fun fulfillRequirement(requirements: KeyCounter, key: AEKey, producedAmount: Long): RequirementFulfillmentResult {
      if (producedAmount < 0L) {
         throw IllegalArgumentException(("产出数量不能为负数: $producedAmount").toString())
      } else {
         val requirementDone: Long = Math.min(producedAmount, requirements.get(key))
         val remainingOutput: Long = producedAmount - requirementDone
         if (requirementDone > 0L) {
            requirements.remove(key, requirementDone)
         }

         return RequirementFulfillmentResult(requirementDone, remainingOutput)
      }
   }

   public fun getRequirement(requirements: KeyCounter, key: AEKey): Long {
      return requirements.get(key)
   }

   public fun getUnfulfilledRequirements(requirements: KeyCounter): AEKeyMap<AEKey> {
      val map: AEKeyMap = AEKeyMap(requirements.size())
      val var10000: java.util.Iterator = requirements.iterator()
      val var3: java.util.Iterator = var10000

      while (var3.hasNext()) {
         val object2LongMapEntry: Entry = var3.next() as Entry
         if (object2LongMapEntry.getLongValue() > 0L) {
            map.put(object2LongMapEntry.getKey() as AEKey, object2LongMapEntry.getLongValue())
         }
      }

      return map
   }
}
