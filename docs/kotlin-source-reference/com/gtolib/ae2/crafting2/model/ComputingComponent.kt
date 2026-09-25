package com.gtolib.ae2.crafting2.model

import appeng.api.config.FuzzyMode
import appeng.api.crafting.IPatternDetails
import appeng.api.crafting.IPatternDetails.IInput
import appeng.api.stacks.AEItemKey
import appeng.api.stacks.AEKey
import appeng.api.stacks.GenericStack
import appeng.api.stacks.KeyCounter
import appeng.crafting.pattern.AECraftingPattern
import appeng.crafting.pattern.AEProcessingPattern
import appeng.crafting.pattern.AESmithingTablePattern
import appeng.crafting.pattern.AEStonecuttingPattern
import appeng.menu.AutoCraftingMenu
import com.gto.fastcollection.fastutil.O2LOpenCacheHashMap
import com.gtolib.ae2.crafting2.logic.ComputingComponentBuildFailedException
import com.gtolib.ae2.crafting2.logic.SendComponentToPlayerException
import com.gtolib.ae2.crafting2.model.SuriedCraftingPlan.FinalPlan
import com.gtolib.ae2.crafting2.model.SuriedCraftingPlan.MiddlePlan
import com.gtolib.ae2.crafting2.utils.AE2CraftingTranslation
import com.gtolib.ae2.crafting2.utils.AECrafting2Utils
import com.gtolib.ae2.crafting2.utils.DisplayLevel
import com.gtolib.ae2.crafting2.utils.PerfLogger
import com.gtolib.ae2.crafting2.utils.RequirementFulfillmentResult
import com.gtolib.ae2.crafting2.utils.RequirementsManager
import java.util.ArrayList
import kotlin.jvm.internal.SourceDebugExtension
import net.minecraft.core.NonNullList
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.inventory.CraftingContainer
import net.minecraft.world.inventory.TransientCraftingContainer
import net.minecraft.world.item.ItemStack

@SourceDebugExtension(["SMAP\nISuriedCraftingPlan.kt\nKotlin\n*S Kotlin\n*F\n+ 1 ISuriedCraftingPlan.kt\ncom/gtolib/ae2/crafting2/model/ComputingComponent\n+ 2 _Maps.kt\nkotlin/collections/MapsKt___MapsKt\n*L\n1#1,842:1\n221#2,2:843\n*S KotlinDebug\n*F\n+ 1 ISuriedCraftingPlan.kt\ncom/gtolib/ae2/crafting2/model/ComputingComponent\n*L\n71#1:843,2\n*E\n"])
public sealed class ComputingComponent protected constructor(middleOutput: AEKey, finalPlan: FinalPlan) {
   public final val middleOutput: AEKey
   public final val finalPlan: FinalPlan

   public final lateinit var middlePlan: MiddlePlan
      internal set

   public final var isFinish: Boolean
      internal set

   public final var totalStep: Int
      internal set

   public final var finishedStep: Int
      internal set

   public final var minParallelMaterialPerCycle: Long
      internal set

   public final var componentInSCC: Boolean
      internal set

   init {
      this.middleOutput = middleOutput
      this.finalPlan = finalPlan
      this.minParallelMaterialPerCycle = 1L
   }

   public fun getMiddlePlanOptional(): MiddlePlan? {
      return if (this.middlePlan != null) this.middlePlan else null
   }

   public open fun initRuntimeData(middlePlan: MiddlePlan) {
      this.middlePlan = middlePlan
      this.isFinish = false
      this.totalStep = 0
      this.finishedStep = 0
   }

   public open fun beforeCalculate() {
   }

   public open fun startCalculate() {
   }

   public open fun afterCalculate() {
      RequirementsManager.INSTANCE.addAllRequirements(this.finalPlan.getRequirements(), this.middlePlan.getRequirements())

      for (`element$iv` in (this.middlePlan.getPatternTimes() as java.util.Map).entrySet()) {
         val key: IPatternDetails = `element$iv`.getKey() as IPatternDetails
         val value: java.lang.Long = `element$iv`.getValue() as java.lang.Long
         val var10000: O2LOpenCacheHashMap = this.finalPlan.getPatternTimes()
         var10000.addTo(key, value)
      }
   }

   public open fun getCraftAmountPerUnit(): Long {
      return this.middlePlan.getFinalOutputAmount().get()
   }

   public fun request(what: AEKey, amount: Long, finalPlan: FinalPlan, parentPlan: SuriedCraftingPlan) {
      finalPlan.perfLogger.push("请求 ${this.middleOutput.getDisplayName().getString()} x$amount, 算子类型 ${this.getComponentTypeName()}")
      val plan: SuriedCraftingPlan.MiddlePlan = SuriedCraftingPlan.MiddlePlan(what, finalPlan, this, parentPlan)
      plan.setFinalOutputAmount(amount)
      plan.initRuntimeData()
      this.initRuntimeData(plan)
      this.beforeCalculate()
      this.startCalculate()
      this.afterCalculate()
      finalPlan.perfLogger.pop()
   }

   public open fun getComponentTypeName(): String {
      val var10000: java.lang.String
      if (this is ComputingComponent.CompositeComponent) {
         var10000 = "复合算子"
      } else if (this is ComputingComponent.MultiCraftComponent) {
         var10000 = "批量合成算子"
      } else if (this is ComputingComponent.MultiPathSelectionComponent) {
         var10000 = "多路径算子"
      } else if (this is ComputingComponent.MultiProcessComponent) {
         var10000 = "批量处理算子"
      } else {
         if (this !is ComputingComponent.OnceCraftComponent) {
            throw NoWhenBranchMatchedException()
         }

         var10000 = "循环合成算子"
      }

      return var10000
   }

   public abstract fun getDependencies(): List<AEKey> {
   }

   @SourceDebugExtension(["SMAP\nISuriedCraftingPlan.kt\nKotlin\n*S Kotlin\n*F\n+ 1 ISuriedCraftingPlan.kt\ncom/gtolib/ae2/crafting2/model/ComputingComponent$CompositeComponent\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,842:1\n1586#2:843\n1661#2,3:844\n1586#2:847\n1661#2,3:848\n1586#2:851\n1661#2,3:852\n*S KotlinDebug\n*F\n+ 1 ISuriedCraftingPlan.kt\ncom/gtolib/ae2/crafting2/model/ComputingComponent$CompositeComponent\n*L\n185#1:843\n185#1:844,3\n186#1:847\n186#1:848,3\n190#1:851\n190#1:852,3\n*E\n"])
   public class CompositeComponent(middleOutput: AEKey, components: List<ComputingComponent>, finalPlan: FinalPlan) : ComputingComponent(
            middleOutput, finalPlan
         ),
      GetSubComponent {
      public final val components: List<ComputingComponent>

      init {
         this.components = components
      }

      public override fun getDependencies(): List<AEKey> {
         return SequencesKt.toList(
            SequencesKt.distinct(SequencesKt.filter(SequencesKt.flatMap(CollectionsKt.asSequence(this.components), { it: ComputingComponent ->
               CollectionsKt.asSequence(it.getDependencies())
            }), { it: AEKey ->
               !`$internal`.contains(it)
            }))
         )
      }

      public override operator fun equals(other: Any?): Boolean {
         if (this === other) {
            return true
         } else if (other !is ComputingComponent.CompositeComponent) {
            return false
         } else if (!(this.getMiddleOutput() == (other as ComputingComponent.CompositeComponent).getMiddleOutput())) {
            return false
         } else {
            val `$i$f$map`: java.lang.Iterable = this.components
            val `$this$mapTo$iv$iv`: java.util.Collection = ArrayList(CollectionsKt.collectionSizeOrDefault(this.components, 10))

            for (`item$iv$iv` in `$i$f$map`) {
               `$this$mapTo$iv$iv`.add((`item$iv$iv` as ComputingComponent).middleOutput)
            }

            val thisSet: java.util.Set = CollectionsKt.toSet(`$this$mapTo$iv$iv` as java.util.List)
            val var17: java.lang.Iterable = (other as ComputingComponent.CompositeComponent).components
            val `destination$iv$ivx`: java.util.Collection = ArrayList(
               CollectionsKt.collectionSizeOrDefault((other as ComputingComponent.CompositeComponent).components, 10)
            )

            for (var21 in var17) {
               `destination$iv$ivx`.add((var21 as ComputingComponent).middleOutput)
            }

            return thisSet == CollectionsKt.toSet(`destination$iv$ivx` as java.util.List)
         }
      }

      public override fun hashCode(): Int {
         val `$this$mapTo$iv$iv`: java.lang.Iterable = this.components
         val `destination$iv$iv`: java.util.Collection = ArrayList(CollectionsKt.collectionSizeOrDefault(this.components, 10))

         for (`item$iv$iv` in `$this$mapTo$iv$iv`) {
            `destination$iv$iv`.add((`item$iv$iv` as ComputingComponent).middleOutput)
         }

         return 31 * this.getMiddleOutput().hashCode() + CollectionsKt.toSet(`destination$iv$iv` as java.util.List).hashCode()
      }

      public override fun toString(): String {
         return "MultiCompositeComponent(${this.getMiddleOutput()}, ${this.components})"
      }

      public override fun getSubComponent(): List<ComputingComponent> {
         return this.components
      }

      @SourceDebugExtension(["SMAP\nISuriedCraftingPlan.kt\nKotlin\n*S Kotlin\n*F\n+ 1 ISuriedCraftingPlan.kt\ncom/gtolib/ae2/crafting2/model/ComputingComponent$CompositeComponent$Companion\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,842:1\n1586#2:843\n1661#2,3:844\n*S KotlinDebug\n*F\n+ 1 ISuriedCraftingPlan.kt\ncom/gtolib/ae2/crafting2/model/ComputingComponent$CompositeComponent$Companion\n*L\n195#1:843\n195#1:844,3\n*E\n"])
      public companion object {
         public fun canBuild(components: List<ComputingComponent>): Boolean {
            val `$this$map$iv`: java.lang.Iterable = components
            val var11: AE2CraftingTranslation.Companion = AE2CraftingTranslation.Companion
            val `destination$iv$iv`: java.util.Collection = ArrayList(CollectionsKt.collectionSizeOrDefault(`$this$map$iv`, 10))

            for (`item$iv$iv` in `$this$map$iv`) {
               `destination$iv$iv`.add((`item$iv$iv` as ComputingComponent).middleOutput)
            }

            throw SendComponentToPlayerException(var11.ERR_CRAFTING_CYCLE_DETECTED(`destination$iv$iv` as MutableList<AEKey>))
         }
      }
   }

   @SourceDebugExtension(["SMAP\nISuriedCraftingPlan.kt\nKotlin\n*S Kotlin\n*F\n+ 1 ISuriedCraftingPlan.kt\ncom/gtolib/ae2/crafting2/model/ComputingComponent$MultiCraftComponent\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,842:1\n1586#2:843\n1661#2,3:844\n777#2:847\n873#2,2:848\n*S KotlinDebug\n*F\n+ 1 ISuriedCraftingPlan.kt\ncom/gtolib/ae2/crafting2/model/ComputingComponent$MultiCraftComponent\n*L\n101#1:843\n101#1:844,3\n101#1:847\n101#1:848,2\n*E\n"])
   public class MultiCraftComponent(middleOutput: AEKey, detail: AECraftingPattern, finalPlan: FinalPlan) : ComputingComponent(middleOutput, finalPlan) {
      public final val detail: AECraftingPattern
      public final val analyzer: PatternAnalyzer

      init {
         this.detail = detail
         this.analyzer = PatternAnalyzer.Companion.fromPattern(this.detail, middleOutput)
      }

      public override fun getDependencies(): List<AEKey> {
         val `$this$filter$iv`: java.lang.Iterable = this.analyzer.inputs
         var `destination$iv$iv`: java.util.Collection = ArrayList(CollectionsKt.collectionSizeOrDefault(`$this$filter$iv`, 10))

         for (`element$iv$iv` in `$this$filter$iv`) {
            `destination$iv$iv`.add((`element$iv$iv` as GenericStack).what())
         }

         val var13: java.lang.Iterable = `destination$iv$iv` as java.util.List
         `destination$iv$iv` = ArrayList()

         for (var17 in var13) {
            if (!(var17 as AEKey == this.getMiddleOutput())) {
               `destination$iv$iv`.add(var17)
            }
         }

         return `destination$iv$iv` as MutableList<AEKey>
      }

      public override fun initRuntimeData(middlePlan: MiddlePlan) {
         super.initRuntimeData(middlePlan)
         this.setTotalStep(1)
      }

      public override fun getCraftAmountPerUnit(): Long {
         return this.analyzer.netOutputPerCraft
      }

      public override fun startCalculate() {
         super.startCalculate()
         if (this.getFinalPlan().processedComputingComponentCount.get() != 0L) {
            val analysis: Long = Math.min(
               this.analyzer
                  .maxExternalExtractableForSelfIncrease(
                     this.getFinalPlan().inventory.get(this.getMiddleOutput()),
                     this.getMiddlePlan().finalOutput().amount(),
                     this.getMinParallelMaterialPerCycle()
                  ),
               this.getMiddlePlan().finalOutput().amount()
            )
            if (analysis > 0L) {
               SuriedCraftingPlan.FinalPlan.this.getFinalPlan().useGlobalItem(this.getMiddleOutput(), analysis)
               this.getFinalPlan().getRequirements().remove(this.getMiddleOutput(), analysis)
               if (this.getMiddlePlan().finalOutput().amount() <= 0L) {
                  this.setFinishedStep(1)
                  this.setFinish(true)
                  return
               }
            }

            this.getMiddlePlan().setFinalOutputAmount(this.getMiddlePlan().getFinalOutputAmount().get() - analysis)
         }

         val var5: Long = this.getMiddlePlan().finalOutput().amount()
         this.getFinalPlan().perfLogger.push("使用样板分析器计算", DisplayLevel.DETAIL)
         this.analyzer.calculateCrafting(var5, pattern = this.detail).applyToPlans(this.getFinalPlan(), this.getMiddlePlan())
         this.getFinalPlan().perfLogger.pop()
         this.setFinishedStep(1)
         this.setFinish(true)
      }

      public override fun toString(): String {
         return "MultiCraftComponent(${this.getMiddleOutput()}, ${this.detail})"
      }

      public companion object {
         public fun canBuild(detail: IPatternDetails): Boolean {
            if (detail !is AECraftingPattern) {
               return false
            } else if ((detail as AECraftingPattern).canSubstitute) {
               return false
            } else if (AECrafting2Utils.Companion.hasUnhandledRemainingItems(detail as AECraftingPattern)) {
               return false
            } else {
               var var2: Boolean
               try {
                  val var10000: PatternAnalyzer.Companion = PatternAnalyzer.Companion
                  val var10002: AEKey = (detail as AECraftingPattern).getPrimaryOutput().what()
                  var10000.fromPattern(detail, var10002)
                  var2 = true
               } catch (var4: Exception) {
                  var2 = false
               }

               return var2
            }
         }
      }
   }

   @SourceDebugExtension(["SMAP\nISuriedCraftingPlan.kt\nKotlin\n*S Kotlin\n*F\n+ 1 ISuriedCraftingPlan.kt\ncom/gtolib/ae2/crafting2/model/ComputingComponent$MultiPathSelectionComponent\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,842:1\n1924#2,3:843\n2045#2,14:846\n1586#2:860\n1661#2,3:861\n1586#2:864\n1661#2,3:865\n1586#2:868\n1661#2,3:869\n*S KotlinDebug\n*F\n+ 1 ISuriedCraftingPlan.kt\ncom/gtolib/ae2/crafting2/model/ComputingComponent$MultiPathSelectionComponent\n*L\n270#1:843,3\n318#1:846,14\n359#1:860\n359#1:861,3\n360#1:864\n360#1:865,3\n364#1:868\n364#1:869,3\n*E\n"])
   public class MultiPathSelectionComponent(middleOutput: AEKey, components: List<ComputingComponent>, finalPlan: FinalPlan) : ComputingComponent(
            middleOutput, finalPlan
         ),
      GetSubComponent {
      public final val components: List<ComputingComponent>
      public final val componentsToWeight: O2LOpenCacheHashMap<ComputingComponent>
      public final val componentsToCraftAmountPerUnit: O2LOpenCacheHashMap<ComputingComponent>

      init {
         this.components = CollectionsKt.toList(components)
         this.componentsToWeight = O2LOpenCacheHashMap<>()
         this.componentsToCraftAmountPerUnit = O2LOpenCacheHashMap<>()
         val `$this$forEachIndexed$iv`: java.lang.Iterable = components
         var `index$iv`: Int = 0

         for (`item$iv` in `$this$forEachIndexed$iv`) {
            val var9: Int = `index$iv`++
            if (var9 < 0) {
               CollectionsKt.throwIndexOverflow()
            }

            val component: ComputingComponent = `item$iv` as ComputingComponent
            this.componentsToWeight.put(`item$iv` as ComputingComponent, if (var9 == 0) 1L else 1L)
            this.componentsToCraftAmountPerUnit.put(component, component.getCraftAmountPerUnit())
         }

         this.setTotalStep(components.size())
      }

      public override fun startCalculate() {
         super.startCalculate()
         val totalNeeded: Long = this.getMiddlePlan().finalOutput().amount()
         val var10000: java.util.Collection = this.componentsToWeight.values()
         val totalWeight: Long = CollectionsKt.sumOfLong(var10000)
         if (totalWeight <= 0L) {
            CollectionsKt.first(this.components).request(this.getMiddleOutput(), totalNeeded, this.getFinalPlan(), this.getMiddlePlan())
            this.setFinishedStep(this.components.size())
            this.setFinish(true)
         } else {
            var remainingToAllocate: Long = totalNeeded
            val allocations: O2LOpenCacheHashMap = O2LOpenCacheHashMap()

            for (currentAllocation in this.components) {
               val component: Long = this.componentsToWeight.getLong(currentAllocation)
               if (component > 0L) {
                  val `maxElem$iv`: Long = this.componentsToCraftAmountPerUnit.getLong(currentAllocation)
                  if (`maxElem$iv` > 0L) {
                     val actualAmount: Long = (totalNeeded * component / totalWeight + `maxElem$iv` - 1L) / `maxElem$iv` * `maxElem$iv`
                     allocations.put(currentAllocation, (totalNeeded * component / totalWeight + `maxElem$iv` - 1L) / `maxElem$iv` * `maxElem$iv`)
                     remainingToAllocate -= actualAmount
                  }
               }
            }

            if (remainingToAllocate != 0L) {
               val amount: java.util.Iterator = this.components.iterator()
               val var41: Any
               if (!amount.hasNext()) {
                  var41 = null
               } else {
                  var var32: Any = amount.next()
                  if (!amount.hasNext()) {
                     var41 = var32
                  } else {
                     var var33: Long = this.componentsToWeight.getLong(var32 as ComputingComponent)

                     do {
                        val var37: Any = amount.next()
                        val var38: Long = this.componentsToWeight.getLong(var37 as ComputingComponent)
                        if (var33 < var38) {
                           var32 = var37
                           var33 = var38
                        }
                     } while (amount.hasNext())

                     var41 = var32
                  }
               }

               val var23: ComputingComponent = var41 as ComputingComponent
               if (var41 as ComputingComponent != null) {
                  val var26: Long = allocations.getLong(var23)
                  val var30: Long = this.componentsToCraftAmountPerUnit.getLong(var23)
                  if (remainingToAllocate > 0L) {
                     allocations.put(var23, var26 + (long)Math.ceil((double)(remainingToAllocate + var30 - 1L) / (double)var30) * var30)
                  } else {
                     allocations.put(var23, Math.max(0L, var26 - -remainingToAllocate / var30 * var30))
                  }
               }
            }

            this.getFinalPlan().perfLogger.push("多路径算子任务分配", DisplayLevel.DETAIL)

            for (var27 in (allocations as java.util.Map).entrySet()) {
               val var29: ComputingComponent = var27.getKey() as ComputingComponent
               val var31: java.lang.Long = var27.getValue() as java.lang.Long
               if (var31 > 0L) {
                  val var10001: AEKey = this.getMiddleOutput()
                  var29.request(var10001, var31, this.getFinalPlan(), this.getMiddlePlan())
                  this.setFinishedStep(this.getFinishedStep() + 1)
               }
            }

            this.getFinalPlan().perfLogger.pop()
            this.setFinishedStep(this.components.size())
            this.setFinish(true)
         }
      }

      public override fun getDependencies(): List<AEKey> {
         return SequencesKt.toList(SequencesKt.distinct(SequencesKt.flatMap(CollectionsKt.asSequence(this.components), { it: ComputingComponent ->
            CollectionsKt.asSequence(it.getDependencies())
         })))
      }

      public override operator fun equals(other: Any?): Boolean {
         if (this === other) {
            return true
         } else if (other !is ComputingComponent.MultiPathSelectionComponent) {
            return false
         } else if (!(this.getMiddleOutput() == (other as ComputingComponent.MultiPathSelectionComponent).getMiddleOutput())) {
            return false
         } else {
            val `$i$f$map`: java.lang.Iterable = this.components
            val `$this$mapTo$iv$iv`: java.util.Collection = ArrayList(CollectionsKt.collectionSizeOrDefault(this.components, 10))

            for (`item$iv$iv` in `$i$f$map`) {
               `$this$mapTo$iv$iv`.add((`item$iv$iv` as ComputingComponent).middleOutput)
            }

            val thisSet: java.util.Set = CollectionsKt.toSet(`$this$mapTo$iv$iv` as java.util.List)
            val var17: java.lang.Iterable = (other as ComputingComponent.MultiPathSelectionComponent).components
            val `destination$iv$ivx`: java.util.Collection = ArrayList(
               CollectionsKt.collectionSizeOrDefault((other as ComputingComponent.MultiPathSelectionComponent).components, 10)
            )

            for (var21 in var17) {
               `destination$iv$ivx`.add((var21 as ComputingComponent).middleOutput)
            }

            return thisSet == CollectionsKt.toSet(`destination$iv$ivx` as java.util.List)
         }
      }

      public override fun hashCode(): Int {
         val `$this$mapTo$iv$iv`: java.lang.Iterable = this.components
         val `destination$iv$iv`: java.util.Collection = ArrayList(CollectionsKt.collectionSizeOrDefault(this.components, 10))

         for (`item$iv$iv` in `$this$mapTo$iv$iv`) {
            `destination$iv$iv`.add((`item$iv$iv` as ComputingComponent).middleOutput)
         }

         return 31 * this.getMiddleOutput().hashCode() + CollectionsKt.toSet(`destination$iv$iv` as java.util.List).hashCode()
      }

      public override fun toString(): String {
         return "MultiPathSelectionComponent(${this.getMiddleOutput()}, ${this.components})"
      }

      public override fun getSubComponent(): List<ComputingComponent> {
         return this.components
      }

      public companion object {
         public fun canBuild(components: List<ComputingComponent>): Boolean {
            return components.size() >= 2
         }
      }
   }

   @SourceDebugExtension(["SMAP\nISuriedCraftingPlan.kt\nKotlin\n*S Kotlin\n*F\n+ 1 ISuriedCraftingPlan.kt\ncom/gtolib/ae2/crafting2/model/ComputingComponent$MultiProcessComponent\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,842:1\n1586#2:843\n1661#2,3:844\n777#2:847\n873#2,2:848\n*S KotlinDebug\n*F\n+ 1 ISuriedCraftingPlan.kt\ncom/gtolib/ae2/crafting2/model/ComputingComponent$MultiProcessComponent\n*L\n252#1:843\n252#1:844,3\n252#1:847\n252#1:848,2\n*E\n"])
   public class MultiProcessComponent(middleOutput: AEKey, detail: IPatternDetails, finalPlan: FinalPlan) : ComputingComponent(middleOutput, finalPlan) {
      public final val detail: IPatternDetails
      public final val analyzer: PatternAnalyzer

      init {
         this.detail = detail
         this.analyzer = PatternAnalyzer.Companion.fromPattern(this.detail, middleOutput)
         this.setMinParallelMaterialPerCycle(1L)
      }

      public override fun initRuntimeData(middlePlan: MiddlePlan) {
         super.initRuntimeData(middlePlan)
         this.setTotalStep(1)
      }

      public override fun getCraftAmountPerUnit(): Long {
         return this.analyzer.netOutputPerCraft
      }

      public override fun startCalculate() {
         super.startCalculate()
         if (this.getFinalPlan().processedComputingComponentCount.get() != 0L) {
            val analysis: Long = Math.min(
               this.analyzer
                  .maxExternalExtractableForSelfIncrease(
                     this.getFinalPlan().inventory.get(this.getMiddleOutput()),
                     this.getMiddlePlan().finalOutput().amount(),
                     this.getMinParallelMaterialPerCycle()
                  ),
               this.getMiddlePlan().finalOutput().amount()
            )
            if (analysis > 0L) {
               SuriedCraftingPlan.FinalPlan.this.getFinalPlan().useGlobalItem(this.getMiddleOutput(), analysis)
               this.getFinalPlan().getRequirements().remove(this.getMiddleOutput(), analysis)
               if (this.getMiddlePlan().finalOutput().amount() <= 0L) {
                  this.setFinishedStep(1)
                  this.setFinish(true)
                  return
               }
            }

            this.getMiddlePlan().setFinalOutputAmount(this.getMiddlePlan().getFinalOutputAmount().get() - analysis)
         }

         val var5: Long = this.getMiddlePlan().finalOutput().amount()
         this.getFinalPlan().perfLogger.push("循环分析器开始计算", DisplayLevel.DETAIL)
         this.analyzer.calculateCrafting(var5, this.getMinParallelMaterialPerCycle(), this.detail).applyToPlans(this.getFinalPlan(), this.getMiddlePlan())
         this.getFinalPlan().perfLogger.pop()
         this.setFinishedStep(1)
         this.setFinish(true)
      }

      public override fun getDependencies(): List<AEKey> {
         val `$this$filter$iv`: java.lang.Iterable = this.analyzer.inputs
         var `destination$iv$iv`: java.util.Collection = ArrayList(CollectionsKt.collectionSizeOrDefault(`$this$filter$iv`, 10))

         for (`element$iv$iv` in `$this$filter$iv`) {
            `destination$iv$iv`.add((`element$iv$iv` as GenericStack).what())
         }

         val var13: java.lang.Iterable = `destination$iv$iv` as java.util.List
         `destination$iv$iv` = ArrayList()

         for (var17 in var13) {
            if (!(var17 as AEKey == this.getMiddleOutput())) {
               `destination$iv$iv`.add(var17)
            }
         }

         return `destination$iv$iv` as MutableList<AEKey>
      }

      public override fun toString(): String {
         return "MultiSelfCycleProcessComponent(${this.getMiddleOutput()}, ${this.detail})"
      }

      public companion object {
         public fun canBuild(detail: IPatternDetails): Boolean {
            if (detail !is AEProcessingPattern && detail !is AESmithingTablePattern && detail !is AEStonecuttingPattern) {
               return false
            } else {
               val var10000: PatternAnalyzer.Companion = PatternAnalyzer.Companion
               val var10002: AEKey = detail.getPrimaryOutput().what()
               var10000.fromPattern(detail, var10002)
               return true
            }
         }
      }
   }

   @SourceDebugExtension(["SMAP\nISuriedCraftingPlan.kt\nKotlin\n*S Kotlin\n*F\n+ 1 ISuriedCraftingPlan.kt\ncom/gtolib/ae2/crafting2/model/ComputingComponent$OnceCraftComponent\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,842:1\n777#2:843\n873#2,2:844\n*S KotlinDebug\n*F\n+ 1 ISuriedCraftingPlan.kt\ncom/gtolib/ae2/crafting2/model/ComputingComponent$OnceCraftComponent\n*L\n609#1:843\n609#1:844,2\n*E\n"])
   public class OnceCraftComponent(middleOutput: AEKey, detail: AECraftingPattern, finalPlan: FinalPlan) : ComputingComponent(middleOutput, finalPlan) {
      public final val detail: AECraftingPattern
      public final val analyzer: PatternAnalyzer

      init {
         this.detail = detail
         this.analyzer = PatternAnalyzer.Companion.fromPattern(this.detail, middleOutput)
      }

      public override fun initRuntimeData(middlePlan: MiddlePlan) {
         super.initRuntimeData(middlePlan)
         this.setTotalStep(1)
      }

      public override fun getCraftAmountPerUnit(): Long {
         return this.analyzer.netOutputPerCraft
      }

      public override fun startCalculate() {
         super.startCalculate()
         if (this.getFinalPlan().processedComputingComponentCount.get() != 0L) {
            val need: Long = this.getFinalPlan().inventory.get(this.getMiddleOutput())
            if (need > 0L) {
               val craftsRequired: Long = Math.min(need, this.getMiddlePlan().finalOutput().amount())
               if (craftsRequired > 0L) {
                  SuriedCraftingPlan.FinalPlan.this.getFinalPlan().useGlobalItem(this.getMiddleOutput(), craftsRequired)
                  this.getFinalPlan().getRequirements().remove(this.getMiddleOutput(), craftsRequired)
                  if (this.getMiddlePlan().finalOutput().amount() <= 0L) {
                     this.setFinishedStep(1)
                     this.setFinish(true)
                     return
                  }
               }

               this.getMiddlePlan().setFinalOutputAmount(this.getMiddlePlan().getFinalOutputAmount().get() - craftsRequired)
            }
         }

         val var19: Long = this.getMiddlePlan().finalOutput().amount()
         if (var19 <= 0L) {
            this.setFinishedStep(1)
            this.setFinish(true)
         } else {
            this.getFinalPlan().perfLogger.push("OnceCraft 计算开始", DisplayLevel.DETAIL)
            val var20: Long = (long)Math.ceil((double)var19 / (double)this.analyzer.netOutputPerCraft)
            var hasContainer: Boolean = false
            var selfLoop: Boolean = false
            val primaryOutKey: AEKey = this.detail.getPrimaryOutput().what()
            var var10000: Array<IInput> = this.detail.getInputs()

            for (remainingNeed in var10000) {
               val net: GenericStack = remainingNeed.getPossibleInputs()[0]
               if (!hasContainer && remainingNeed.getRemainingKey(net.what()) != null) {
                  hasContainer = true
               }

               if (!selfLoop && net.what() === primaryOutKey) {
                  selfLoop = true
               }
            }

            val var21: Boolean = hasContainer || selfLoop
            if (var20 >= 10000L) {
               throw SendComponentToPlayerException(AE2CraftingTranslation.Companion.ERR_CRAFTING_TOO_LARGE(10000L, var20, this.getMiddleOutput()))
            } else {
               this.setTotalStep((int)var20)
               if (var21) {
                  var var22: Long = 0L
                  var var26: Long = var19

                  while (var22 < var20) {
                     var10000 = this.detail.getInputs()

                     for (totalRequired in var10000) {
                        val perCraft: Long = totalRequired.getPossibleInputs()[0].amount() * totalRequired.getMultiplier()
                        if (perCraft > 0L) {
                           startCalculate$consumeInputWithFuzzyReturnContainers(this, totalRequired, perCraft, perCraft, var22)
                        }
                     }

                     this.getMiddlePlan().getPatternTimes().addTo(this.detail, 1L)
                     val var32: Long = this.analyzer.netOutputPerCraft
                     val var43: RequirementsManager = RequirementsManager.INSTANCE
                     val var10001: KeyCounter = this.getFinalPlan().getRequirements()
                     val var39: RequirementFulfillmentResult = var43.fulfillRequirement(var10001, primaryOutKey, var32)
                     if (var39.remainingOutput > 0L) {
                        this.getMiddlePlan().storageInternalItem(primaryOutKey, var39.remainingOutput)
                     }

                     var22++
                     this.setFinishedStep(RangesKt.coerceAtMost(this.getFinishedStep() + 1, Integer.MAX_VALUE))
                     var26 = Math.max(0L, var26 - var32)
                  }

                  val var33: Long = var22 * this.analyzer.netOutputPerCraft
                  if (var33 > 0L) {
                     this.getMiddlePlan().getNetOutputItems().add(primaryOutKey, var33)
                  }
               } else {
                  val var23: O2LOpenCacheHashMap = O2LOpenCacheHashMap()
                  var10000 = this.detail.getInputs()

                  for (var34 in var10000) {
                     val var36: Long = var34.getPossibleInputs()[0].amount() * var34.getMultiplier()
                     if (var36 > 0L) {
                        var23.put(var34, var36 * var20)
                     }
                  }

                  val var25: Long = this.detail.getPrimaryOutput().amount() * var20
                  if (var25 > 0L) {
                     val var45: RequirementsManager = RequirementsManager.INSTANCE
                     val var47: KeyCounter = this.getFinalPlan().getRequirements()
                     val var29: RequirementFulfillmentResult = var45.fulfillRequirement(var47, primaryOutKey, var25)
                     if (var29.remainingOutput > 0L) {
                        this.getMiddlePlan().storageInternalItem(primaryOutKey, var29.remainingOutput)
                     }
                  }

                  var10000 = this.detail.getInputs()

                  for (var40 in var10000) {
                     val var41: Long = var23.getLong(var40)
                     if (var41 > 0L) {
                        startCalculate$consumeInputWithFuzzyReturnContainers(this, var40, var41, var40.getPossibleInputs()[0].amount(), 0L)
                     }
                  }

                  if (var20 > 0L) {
                     this.getMiddlePlan().getPatternTimes().addTo(this.detail, var20)
                  }

                  val var31: Long = var20 * this.analyzer.netOutputPerCraft
                  if (var31 > 0L) {
                     this.getMiddlePlan().getNetOutputItems().add(primaryOutKey, var31)
                  }

                  this.setFinishedStep(RangesKt.coerceAtMost(this.getFinishedStep() + (int)var20, Integer.MAX_VALUE))
               }

               this.getFinalPlan().perfLogger.pop()
               this.setFinish(true)
            }
         }
      }

      public override fun getDependencies(): List<AEKey> {
         val result: ArrayList = ArrayList()
         val var10000: Array<IInput> = this.detail.getInputs()

         for (`destination$iv$iv` in var10000) {
            val var19: Array<GenericStack> = `destination$iv$iv`.getPossibleInputs()

            for (it in var19) {
               result.add(it.what())
            }
         }

         val var13: java.lang.Iterable = result
         val var14: java.util.Collection = ArrayList()

         for (var17 in var13) {
            if (!(var17 as AEKey == this.getMiddleOutput())) {
               var14.add(var17)
            }
         }

         return var14 as MutableList<AEKey>
      }

      public override fun toString(): String {
         return "OnceCraftComponent(${this.getMiddleOutput()}, ${this.detail})"
      }

      @JvmStatic
      fun `startCalculate$consumeInputWithFuzzyReturnContainers`(
         `this$0`: ComputingComponent.OnceCraftComponent, inDef: IInput, totalRequired: Long, perCraftAmount: Long, craftDone: Long
      ): Long {
         if (totalRequired <= 0L) {
            0L
         } else {
            var remaining: Long = totalRequired
            val var10000: Array<GenericStack> = inDef.getPossibleInputs()

            for (gLike in var10000) {
               val like: AEKey = gLike.what()
               if (craftDone > 0L) {
                  for (entry in `this$0`.getMiddlePlan().internalInventory.findFuzzy(like, FuzzyMode.IGNORE_ALL)) {
                     if (remaining <= 0L) {
                        break
                     }

                     val candidate: AEKey = entry.getKey() as AEKey
                     if (inDef.isValid(candidate, `this$0`.getFinalPlan().level)) {
                        val var35: SuriedCraftingPlan.MiddlePlan = `this$0`.getMiddlePlan()
                        val left: Long = var35.useInternalItem(candidate, remaining, null, false)
                        val used: Long = remaining - left
                        if (remaining - left > 0L) {
                           val rem: AEKey = inDef.getRemainingKey(candidate)
                           if (rem != null) {
                              `this$0`.getMiddlePlan().storageInternalItem(rem, used)
                           }
                        }

                        remaining = left
                     }
                  }
               }

               if (remaining > 0L) {
                  for (var29 in `this$0`.getFinalPlan().inventory.findFuzzy(like, FuzzyMode.IGNORE_ALL)) {
                     if (remaining <= 0L) {
                        break
                     }

                     val var30: AEKey = var29.getKey() as AEKey
                     if (inDef.isValid(var30, `this$0`.getFinalPlan().level)) {
                        val var36: SuriedCraftingPlan.FinalPlan = `this$0`.getFinalPlan()
                        val var32: Long = SuriedCraftingPlan.FinalPlan.var36.useGlobalItem(var30, remaining, null, false)
                        val var33: Long = remaining - var32
                        if (remaining - var32 > 0L) {
                           val var34: AEKey = inDef.getRemainingKey(var30)
                           if (var34 != null) {
                              `this$0`.getMiddlePlan().storageInternalItem(var34, var33)
                           }
                        }

                        remaining = var32
                     }
                  }
               }

               if (remaining <= 0L) {
                  break
               }
            }

            if (remaining > 0L) {
               val var26: AEKey = inDef.getPossibleInputs()[0].what()
               val var37: SuriedCraftingPlan.MiddlePlan = `this$0`.getMiddlePlan()
               var37.useInternalItem(var26, remaining, FuzzyMode.IGNORE_ALL, true)
            }

            remaining
         }
      }

      @SourceDebugExtension(["SMAP\nISuriedCraftingPlan.kt\nKotlin\n*S Kotlin\n*F\n+ 1 ISuriedCraftingPlan.kt\ncom/gtolib/ae2/crafting2/model/ComputingComponent$OnceCraftComponent$Companion\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,842:1\n296#2,2:843\n1915#2,2:845\n296#2,2:847\n*S KotlinDebug\n*F\n+ 1 ISuriedCraftingPlan.kt\ncom/gtolib/ae2/crafting2/model/ComputingComponent$OnceCraftComponent$Companion\n*L\n615#1:843,2\n621#1:845,2\n630#1:847,2\n*E\n"])
      public companion object {
         public fun canBuild(detail: IPatternDetails): Boolean {
            if (detail !is AECraftingPattern) {
               return false
            } else if (ComputingComponent.MultiCraftComponent.Companion.canBuild(detail)) {
               return false
            } else {
               val var10000: AECrafting2Utils.Companion = AECrafting2Utils.Companion
               val var10002: Array<GenericStack> = (detail as AECraftingPattern).getSparseInputs()
               var `$i$f$firstOrNull`: java.util.Iterator = var10000.condenseInputs(detail, var10002).iterator()

               while (true) {
                  if (`$i$f$firstOrNull`.hasNext()) {
                     val `element$iv`: Any = `$i$f$firstOrNull`.next()
                     if ((`element$iv` as GenericStack).what() != (detail as AECraftingPattern).getPrimaryOutput().what()) {
                        continue
                     }

                     var26 = `element$iv`
                     break
                  }

                  var26 = null
                  break
               }

               val var27: GenericStack = var26 as GenericStack
               if (var26 as GenericStack != null) {
                  if (var27.amount() >= (detail as AECraftingPattern).getPrimaryOutput().amount()) {
                     val var30: MutableComponent = Component.literal(
                        "无法为物品 ${(detail as AECraftingPattern).getPrimaryOutput().what().getDisplayName().getString()} 的 $detail 生成算子，因为它是一个非自增循环的复杂合成"
                     )
                     throw ComputingComponentBuildFailedException(var30 as Component, detail, null, null, 12, null)
                  }
               }

               val testFrame: TransientCraftingContainer = TransientCraftingContainer(AutoCraftingMenu(), 3, 3)
               `$i$f$firstOrNull` = IntRange(0, 8).iterator()

               while (`$i$f$firstOrNull`.hasNext()) {
                  val var21: Int = (`$i$f$firstOrNull` as IntIterator).nextInt()
                  val var9: GenericStack = (detail as AECraftingPattern).getSparseInputs()[var21]
                  val key: AEKey = if (var9 != null) var9.what() else null
                  if (key is AEItemKey) {
                     testFrame.setItem(var21, (key as AEItemKey).toStack())
                  } else {
                     testFrame.setItem(var21, ItemStack.EMPTY)
                  }
               }

               val var28: NonNullList = (detail as AECraftingPattern).getRemainingItems(testFrame as CraftingContainer)
               val var20: java.util.Iterator = (var28 as java.lang.Iterable).iterator()

               while (true) {
                  if (var20.hasNext()) {
                     val var22: Any = var20.next()
                     if ((var22 as ItemStack).isEmpty()) {
                        continue
                     }

                     var29 = var22
                     break
                  }

                  var29 = null
                  break
               }

               if (var29 != null) {
                  val var31: AE2CraftingTranslation.Companion = AE2CraftingTranslation.Companion
                  val var10003: AEKey = (detail as AECraftingPattern).getPrimaryOutput().what()
                  throw SendComponentToPlayerException(var31.ERR_CRAFTING_INPUT_REQUIREMENTS(var10003))
               } else {
                  return true
               }
            }
         }
      }
   }
}
