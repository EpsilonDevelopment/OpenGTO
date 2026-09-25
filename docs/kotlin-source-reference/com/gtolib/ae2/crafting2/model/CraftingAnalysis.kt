package com.gtolib.ae2.crafting2.model

import appeng.api.crafting.IPatternDetails
import appeng.api.stacks.AEKey
import appeng.api.stacks.AEKeyMap
import com.gtolib.ae2.crafting2.model.SuriedCraftingPlan.FinalPlan
import com.gtolib.ae2.crafting2.model.SuriedCraftingPlan.MiddlePlan
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap
import it.unimi.dsi.fastutil.objects.Reference2LongMap.Entry
import it.unimi.dsi.fastutil.objects.Reference2LongMap.FastEntrySet
import kotlin.jvm.internal.SourceDebugExtension

@SourceDebugExtension(["SMAP\nPatternAnalyzer.kt\nKotlin\n*S Kotlin\n*F\n+ 1 PatternAnalyzer.kt\ncom/gtolib/ae2/crafting2/model/CraftingAnalysis\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,282:1\n1807#2,3:283\n*S KotlinDebug\n*F\n+ 1 PatternAnalyzer.kt\ncom/gtolib/ae2/crafting2/model/CraftingAnalysis\n*L\n231#1:283,3\n*E\n"])
public data class CraftingAnalysis(pattern: IPatternDetails?,
   targetOutput: AEKey,
   requestedAmount: Long,
   calculatedForAmount: Long,
   craftsRequired: Long,
   netOutputPerCraft: Long,
   patternAnalyzer: PatternAnalyzer,
   inputRequirements: AEKeyMap<AEKey>,
   outputProducts: AEKeyMap<AEKey>,
   actualParallelCycles: Long
) {
   public final val pattern: IPatternDetails?
   public final val targetOutput: AEKey
   public final val requestedAmount: Long
   public final val calculatedForAmount: Long
   public final val craftsRequired: Long
   public final val netOutputPerCraft: Long
   public final val patternAnalyzer: PatternAnalyzer
   public final val inputRequirements: AEKeyMap<AEKey>
   public final val outputProducts: AEKeyMap<AEKey>
   public final val actualParallelCycles: Long

   init {
      this.pattern = pattern
      this.targetOutput = targetOutput
      this.requestedAmount = requestedAmount
      this.calculatedForAmount = calculatedForAmount
      this.craftsRequired = craftsRequired
      this.netOutputPerCraft = netOutputPerCraft
      this.patternAnalyzer = patternAnalyzer
      this.inputRequirements = inputRequirements
      this.outputProducts = outputProducts
      this.actualParallelCycles = actualParallelCycles
   }

   public fun applyToPlans(finalPlan: FinalPlan, middlePlan: MiddlePlan) {
      if (this.pattern != null) {
         val genericStack: IPatternDetails = this.pattern
         middlePlan.getPatternTimes().addTo(genericStack, this.craftsRequired)
      }

      this.inputRequirements.reference2LongEntrySet().fastForEach({ p0: Any ->
         `$tmp0`(p0)
      })
      val var10000: FastEntrySet = this.outputProducts.reference2LongEntrySet()
      val `$this$any$iv`: java.lang.Iterable = var10000 as java.lang.Iterable
      var var19: Boolean
      if (var10000 as java.lang.Iterable is java.util.Collection && ((var10000 as java.lang.Iterable) as java.util.Collection).isEmpty()) {
         var19 = false
      } else {
         val var18: java.util.Iterator = `$this$any$iv`.iterator()

         while (true) {
            if (!var18.hasNext()) {
               var19 = false
               break
            }

            val entry: Entry = var18.next() as Entry
            val key: AEKey = entry.getKey() as AEKey
            if (this.inputRequirements.containsKey(key) && entry.getLongValue() > this.inputRequirements.getLong(key)) {
               var19 = true
               break
            }
         }
      }

      if (var19) {
         for (var17 in this.patternAnalyzer.inputs) {
            (finalPlan.highPriorityPush.computeIfAbsent(var17.what(), { it: Any ->
               Object2LongOpenHashMap()
            }) as Object2LongOpenHashMap).addTo(this.pattern, var17.amount() * this.craftsRequired)
         }
      }

      this.outputProducts.reference2LongEntrySet().fastForEach({ p0: Any ->
         `$tmp0`(p0)
      })
   }

   public operator fun component1(): IPatternDetails? {
      return this.pattern
   }

   public operator fun component2(): AEKey {
      return this.targetOutput
   }

   public operator fun component3(): Long {
      return this.requestedAmount
   }

   public operator fun component4(): Long {
      return this.calculatedForAmount
   }

   public operator fun component5(): Long {
      return this.craftsRequired
   }

   public operator fun component6(): Long {
      return this.netOutputPerCraft
   }

   public operator fun component7(): PatternAnalyzer {
      return this.patternAnalyzer
   }

   public operator fun component8(): AEKeyMap<AEKey> {
      return this.inputRequirements
   }

   public operator fun component9(): AEKeyMap<AEKey> {
      return this.outputProducts
   }

   public operator fun component10(): Long {
      return this.actualParallelCycles
   }

   public fun copy(
      pattern: IPatternDetails? = this.pattern,
      targetOutput: AEKey = this.targetOutput,
      requestedAmount: Long = this.requestedAmount,
      calculatedForAmount: Long = this.calculatedForAmount,
      craftsRequired: Long = this.craftsRequired,
      netOutputPerCraft: Long = this.netOutputPerCraft,
      patternAnalyzer: PatternAnalyzer = this.patternAnalyzer,
      inputRequirements: AEKeyMap<AEKey> = this.inputRequirements,
      outputProducts: AEKeyMap<AEKey> = this.outputProducts,
      actualParallelCycles: Long = this.actualParallelCycles
   ): CraftingAnalysis {
      return CraftingAnalysis(
         pattern,
         targetOutput,
         requestedAmount,
         calculatedForAmount,
         craftsRequired,
         netOutputPerCraft,
         patternAnalyzer,
         inputRequirements,
         outputProducts,
         actualParallelCycles
      )
   }

   public override fun toString(): String {
      return "CraftingAnalysis(pattern=${this.pattern}, targetOutput=${this.targetOutput}, requestedAmount=${this.requestedAmount}, calculatedForAmount=${this.calculatedForAmount}, craftsRequired=${this.craftsRequired}, netOutputPerCraft=${this.netOutputPerCraft}, patternAnalyzer=${this.patternAnalyzer}, inputRequirements=${this.inputRequirements}, outputProducts=${this.outputProducts}, actualParallelCycles=${this.actualParallelCycles})"
   }

   public override fun hashCode(): Int {
      return (
               (
                        (
                                 (
                                          (
                                                   (
                                                            (
                                                                     (
                                                                              (if (this.pattern == null) 0 else this.pattern.hashCode()) * 31
                                                                                 + this.targetOutput.hashCode()
                                                                           )
                                                                           * 31
                                                                        + java.lang.Long.hashCode(this.requestedAmount)
                                                                  )
                                                                  * 31
                                                               + java.lang.Long.hashCode(this.calculatedForAmount)
                                                         )
                                                         * 31
                                                      + java.lang.Long.hashCode(this.craftsRequired)
                                                )
                                                * 31
                                             + java.lang.Long.hashCode(this.netOutputPerCraft)
                                       )
                                       * 31
                                    + this.patternAnalyzer.hashCode()
                              )
                              * 31
                           + this.inputRequirements.hashCode()
                     )
                     * 31
                  + this.outputProducts.hashCode()
            )
            * 31
         + java.lang.Long.hashCode(this.actualParallelCycles)
      }

   public override operator fun equals(other: Any?): Boolean {
      label76@
      if (this === other) {
         return true
      } else {
         return other is CraftingAnalysis
            && this.pattern == (other as CraftingAnalysis).pattern
            && this.targetOutput == (other as CraftingAnalysis).targetOutput
            && this.requestedAmount == (other as CraftingAnalysis).requestedAmount
            && this.calculatedForAmount == (other as CraftingAnalysis).calculatedForAmount
            && this.craftsRequired == (other as CraftingAnalysis).craftsRequired
            && this.netOutputPerCraft == (other as CraftingAnalysis).netOutputPerCraft
            && this.patternAnalyzer == (other as CraftingAnalysis).patternAnalyzer
            && this.inputRequirements == (other as CraftingAnalysis).inputRequirements
            && this.outputProducts == (other as CraftingAnalysis).outputProducts
            && this.actualParallelCycles == (other as CraftingAnalysis).actualParallelCycles
         }
   }
}
