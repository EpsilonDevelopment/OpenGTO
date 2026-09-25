package com.gtolib.ae2.crafting2.model

public data class SelfIncreaseExtractionPlan(crafts: Long, seedReserved: Long, inventoryUsedForDemand: Long, extractable: Long) {
   public final val crafts: Long
   public final val seedReserved: Long
   public final val inventoryUsedForDemand: Long
   public final val extractable: Long

   init {
      this.crafts = crafts
      this.seedReserved = seedReserved
      this.inventoryUsedForDemand = inventoryUsedForDemand
      this.extractable = extractable
   }

   public operator fun component1(): Long {
      return this.crafts
   }

   public operator fun component2(): Long {
      return this.seedReserved
   }

   public operator fun component3(): Long {
      return this.inventoryUsedForDemand
   }

   public operator fun component4(): Long {
      return this.extractable
   }

   public fun copy(
      crafts: Long = this.crafts,
      seedReserved: Long = this.seedReserved,
      inventoryUsedForDemand: Long = this.inventoryUsedForDemand,
      extractable: Long = this.extractable
   ): SelfIncreaseExtractionPlan {
      return SelfIncreaseExtractionPlan(crafts, seedReserved, inventoryUsedForDemand, extractable)
   }

   public override fun toString(): String {
      return "SelfIncreaseExtractionPlan(crafts=${this.crafts}, seedReserved=${this.seedReserved}, inventoryUsedForDemand=${this.inventoryUsedForDemand}, extractable=${this.extractable})"
   }

   public override fun hashCode(): Int {
      return (
               (java.lang.Long.hashCode(this.crafts) * 31 + java.lang.Long.hashCode(this.seedReserved)) * 31
                  + java.lang.Long.hashCode(this.inventoryUsedForDemand)
            )
            * 31
         + java.lang.Long.hashCode(this.extractable)
      }

   public override operator fun equals(other: Any?): Boolean {
      label40@
      if (this === other) {
         return true
      } else {
         return other is SelfIncreaseExtractionPlan
            && this.crafts == (other as SelfIncreaseExtractionPlan).crafts
            && this.seedReserved == (other as SelfIncreaseExtractionPlan).seedReserved
            && this.inventoryUsedForDemand == (other as SelfIncreaseExtractionPlan).inventoryUsedForDemand
            && this.extractable == (other as SelfIncreaseExtractionPlan).extractable
         }
   }
}
