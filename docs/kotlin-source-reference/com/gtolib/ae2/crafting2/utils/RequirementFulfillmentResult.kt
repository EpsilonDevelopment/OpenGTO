package com.gtolib.ae2.crafting2.utils

public data class RequirementFulfillmentResult(fulfilledAmount: Long, remainingOutput: Long) {
   public final val fulfilledAmount: Long
   public final val remainingOutput: Long

   init {
      this.fulfilledAmount = fulfilledAmount
      this.remainingOutput = remainingOutput
   }

   public operator fun component1(): Long {
      return this.fulfilledAmount
   }

   public operator fun component2(): Long {
      return this.remainingOutput
   }

   public fun copy(fulfilledAmount: Long = this.fulfilledAmount, remainingOutput: Long = this.remainingOutput): RequirementFulfillmentResult {
      return RequirementFulfillmentResult(fulfilledAmount, remainingOutput)
   }

   public override fun toString(): String {
      return "RequirementFulfillmentResult(fulfilledAmount=${this.fulfilledAmount}, remainingOutput=${this.remainingOutput})"
   }

   public override fun hashCode(): Int {
      return java.lang.Long.hashCode(this.fulfilledAmount) * 31 + java.lang.Long.hashCode(this.remainingOutput)
   }

   public override operator fun equals(other: Any?): Boolean {
      label28@
      if (this === other) {
         return true
      } else {
         return other is RequirementFulfillmentResult
            && this.fulfilledAmount == (other as RequirementFulfillmentResult).fulfilledAmount
            && this.remainingOutput == (other as RequirementFulfillmentResult).remainingOutput
         }
   }
}
