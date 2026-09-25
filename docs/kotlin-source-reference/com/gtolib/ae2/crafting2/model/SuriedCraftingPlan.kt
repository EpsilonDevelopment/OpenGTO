package com.gtolib.ae2.crafting2.model

import appeng.api.config.FuzzyMode
import appeng.api.crafting.IPatternDetails
import appeng.api.networking.IGrid
import appeng.api.networking.crafting.ICraftingPlan
import appeng.api.stacks.AEFluidKey
import appeng.api.stacks.AEItemKey
import appeng.api.stacks.AEKey
import appeng.api.stacks.GenericStack
import appeng.api.stacks.KeyCounter
import com.gto.datasynclib.util.holder.LongHolder
import com.gto.fastcollection.fastutil.O2LOpenCacheHashMap
import com.gtolib.ae2.crafting2.utils.PerfLogger
import com.gtolib.ae2.crafting2.utils.RequirementsManager
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Reference2LongMap.Entry
import java.util.ArrayList
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicLong
import kotlin.jvm.internal.SourceDebugExtension
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level

public sealed class SuriedCraftingPlan protected constructor(finalOutput: AEKey) : ICraftingPlan {
   public open val finalOutput: AEKey

   public final lateinit var patternTimes: O2LOpenCacheHashMap<IPatternDetails>
      internal set

   public final lateinit var nmissingItems: KeyCounter
      internal set

   public final lateinit var nemittedItems: KeyCounter
      internal set

   public final lateinit var nusedItems: KeyCounter
      internal set

   public final lateinit var catalystItems: KeyCounter
      internal set

   public final lateinit var netOutputItems: KeyCounter
      internal set

   public final lateinit var finalOutputAmount: AtomicLong
      internal set

   public final lateinit var requirements: KeyCounter
      internal set

   init {
      this.finalOutput = finalOutput
   }

   public fun setFinalOutputAmount(amount: Long) {
      this.finalOutputAmount = java.util.concurrent.atomic.AtomicLong(amount)
   }

   public open fun patternTimes(): O2LOpenCacheHashMap<IPatternDetails> {
      return this.patternTimes
   }

   public override fun missingItems(): KeyCounter {
      return this.nmissingItems
   }

   public override fun emittedItems(): KeyCounter {
      return this.nemittedItems
   }

   public override fun usedItems(): KeyCounter {
      return this.nusedItems
   }

   public fun catalystItems(): KeyCounter {
      return this.catalystItems
   }

   public override fun finalOutput(): GenericStack {
      return GenericStack(this.finalOutput, this.finalOutputAmount.get())
   }

   public open fun initRuntimeData() {
      this.patternTimes = O2LOpenCacheHashMap<>()
      this.nmissingItems = KeyCounter()
      this.nemittedItems = KeyCounter()
      this.nusedItems = KeyCounter()
      this.catalystItems = KeyCounter()
      this.requirements = KeyCounter()
      this.netOutputItems = KeyCounter()
   }

   @SourceDebugExtension(["SMAP\nISuriedCraftingPlan.kt\nKotlin\n*S Kotlin\n*F\n+ 1 ISuriedCraftingPlan.kt\ncom/gtolib/ae2/crafting2/model/SuriedCraftingPlan$FinalPlan\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n+ 3 fake.kt\nkotlin/jvm/internal/FakeKt\n*L\n1#1,842:1\n1807#2,3:843\n1915#2,2:847\n1807#2,3:849\n1807#2,3:852\n1#3:846\n*S KotlinDebug\n*F\n+ 1 ISuriedCraftingPlan.kt\ncom/gtolib/ae2/crafting2/model/SuriedCraftingPlan$FinalPlan\n*L\n731#1:843,3\n755#1:847,2\n764#1:849,3\n765#1:852,3\n*E\n"])
   public class FinalPlan(finalOutput: AEKey, perfLogger: PerfLogger) : SuriedCraftingPlan(finalOutput) {
      public final val perfLogger: PerfLogger
      public final val middleOutputToComponent: Reference2ObjectOpenHashMap<AEKey, ArrayList<ComputingComponent>>

      public final lateinit var inventory: KeyCounter
         internal set

      public final lateinit var isSimulated: AtomicBoolean
         internal set

      public final lateinit var level: Level
         internal set

      public final lateinit var highPriorityPush: Reference2ObjectOpenHashMap<AEKey, Object2LongOpenHashMap<IPatternDetails>>
         internal set

      public final lateinit var processedComputingComponentCount: AtomicLong
         internal set

      public final var sortedComputingComponent: ArrayList<ComputingComponent>
         internal set

      init {
         this.perfLogger = perfLogger
         this.middleOutputToComponent = Reference2ObjectOpenHashMap()
         this.sortedComputingComponent = ArrayList<>()
      }

      public fun initInventory(keyCounter: KeyCounter, grid: IGrid) {
         this.inventory = keyCounter
         val var10001: ServerLevel = grid.getPivot().getLevel()
         this.level = var10001 as Level
      }

      public override fun initRuntimeData() {
         super.initRuntimeData()
         this.isSimulated = java.util.concurrent.atomic.AtomicBoolean(false)
         this.processedComputingComponentCount = java.util.concurrent.atomic.AtomicLong(0L)
         this.highPriorityPush = Reference2ObjectOpenHashMap()
      }

      public fun getComputingComponentFor(aeKey: AEKey): ArrayList<ComputingComponent> {
         val var10000: Any = this.middleOutputToComponent.computeIfAbsent(aeKey, { it: Any ->
            ArrayList()
         })
         return var10000 as ArrayList<ComputingComponent>
      }

      public fun useGlobalItem(
         aeKey: AEKey,
         amount: Long,
         fuzzyMode: FuzzyMode? = null,
         requestRemain: Boolean = true,
         middlePlan: com.gtolib.ae2.crafting2.model.SuriedCraftingPlan.MiddlePlan? = null
      ): Long {
         var remainRequiredAmount: Long = amount
         val extract: Long = Math.min(this.inventory.get(aeKey), amount)
         if (extract > 0L) {
            remainRequiredAmount -= extract
            this.inventory.remove(aeKey, extract)
            this.getNusedItems().add(aeKey, extract)
            if (middlePlan != null) {
               val var10000: KeyCounter = middlePlan.getNusedItems()
               if (var10000 != null) {
                  var10000.add(aeKey, extract)
               }
            }
         }

         if (fuzzyMode != null && remainRequiredAmount > 0L) {
            val var26: java.util.Collection = this.inventory.findFuzzy(aeKey, fuzzyMode)

            for (entry in var26) {
               if (remainRequiredAmount <= 0L) {
                  break
               }

               val `element$iv`: AEKey = entry.getKey() as AEKey
               val extract2: Long = Math.min(entry.getLongValue(), remainRequiredAmount)
               if (extract2 > 0L) {
                  remainRequiredAmount -= extract2
                  this.inventory.remove(`element$iv`, extract2)
                  this.getNusedItems().add(`element$iv`, extract2)
                  if (middlePlan != null) {
                     val var27: KeyCounter = middlePlan.getNusedItems()
                     if (var27 != null) {
                        var27.add(`element$iv`, extract2)
                     }
                  }
               }
            }
         }

         if (remainRequiredAmount > 0L && requestRemain) {
            val var21: java.lang.Iterable = this.sortedComputingComponent
            var var28: Boolean
            if (this.sortedComputingComponent is java.util.Collection && this.sortedComputingComponent.isEmpty()) {
               var28 = false
            } else {
               run label95@{
                  for (var24 in var21) {
                     if ((var24 as ComputingComponent).middleOutput === aeKey) {
                        var28 = true
                        return@label95
                     }
                  }

                  var28 = false
               }
            }

            if (var28) {
               RequirementsManager.INSTANCE.addRequirement(this.getRequirements(), aeKey, remainRequiredAmount)
            } else {
               this.getNmissingItems().add(aeKey, remainRequiredAmount)
               this.isSimulated.set(true)
            }
         }

         return remainRequiredAmount
      }

      public override fun bytes(): Long {
         val var4: java.lang.Iterable = this.getAllDeepComponent()
         var var5: Long = 0L

         for (var8 in var4) {
            val var10000: SuriedCraftingPlan.MiddlePlan = (var8 as ComputingComponent).getMiddlePlanOptional()
            var5 += if (var10000 != null) var10000.bytes() else 0L
         }

         return var5
      }

      public fun getAllDeepComponent(): ArrayList<ComputingComponent> {
         val queue: ArrayDeque = ArrayDeque()
         val allComponent: ArrayList = ArrayList()

         for (`element$iv` in this.sortedComputingComponent) {
            queue.add(`element$iv` as ComputingComponent)
         }

         while (!queue.isEmpty()) {
            val var9: ComputingComponent = queue.removeFirst() as ComputingComponent
            allComponent.add(var9)
            if (var9 is GetSubComponent) {
               queue.addAll((var9 as GetSubComponent).getSubComponent())
            }
         }

         return allComponent
      }

      public override fun simulation(): Boolean {
         val `$this$any$iv`: java.lang.Iterable = this.sortedComputingComponent
         var var10000: Boolean
         if (this.sortedComputingComponent is java.util.Collection && this.sortedComputingComponent.isEmpty()) {
            var10000 = false
         } else {
            val var3: java.util.Iterator = `$this$any$iv`.iterator()

            while (true) {
               if (!var3.hasNext()) {
                  var10000 = false
                  break
               }

               if ((var3.next() as ComputingComponent).middlePlan.simulation()) {
                  var10000 = true
                  break
               }
            }
         }

         return var10000 || this.isSimulated.get()
      }

      public override fun multiplePaths(): Boolean {
         val `$this$any$iv`: java.lang.Iterable = this.sortedComputingComponent
         var var10000: Boolean
         if (this.sortedComputingComponent is java.util.Collection && this.sortedComputingComponent.isEmpty()) {
            var10000 = false
         } else {
            val var3: java.util.Iterator = `$this$any$iv`.iterator()

            while (true) {
               if (!var3.hasNext()) {
                  var10000 = false
                  break
               }

               if ((var3.next() as ComputingComponent).middlePlan.multiplePaths()) {
                  var10000 = true
                  break
               }
            }
         }

         return var10000
      }
   }

   @SourceDebugExtension(["SMAP\nISuriedCraftingPlan.kt\nKotlin\n*S Kotlin\n*F\n+ 1 ISuriedCraftingPlan.kt\ncom/gtolib/ae2/crafting2/model/SuriedCraftingPlan$MiddlePlan\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,842:1\n1915#2,2:843\n1807#2,3:845\n*S KotlinDebug\n*F\n+ 1 ISuriedCraftingPlan.kt\ncom/gtolib/ae2/crafting2/model/SuriedCraftingPlan$MiddlePlan\n*L\n777#1:843,2\n826#1:845,3\n*E\n"])
   public class MiddlePlan(finalOutput: AEKey,
      finalPlan: com.gtolib.ae2.crafting2.model.SuriedCraftingPlan.FinalPlan,
      component: ComputingComponent,
      parentPlan: SuriedCraftingPlan
   ) : SuriedCraftingPlan(finalOutput) {
      public final val finalPlan: com.gtolib.ae2.crafting2.model.SuriedCraftingPlan.FinalPlan
      public final val component: ComputingComponent
      public final val parentPlan: SuriedCraftingPlan

      public final lateinit var internalInventory: KeyCounter
         internal set

      init {
         this.finalPlan = finalPlan
         this.component = component
         this.parentPlan = parentPlan
      }

      public override fun initRuntimeData() {
         super.initRuntimeData()
         this.internalInventory = KeyCounter()
      }

      public override fun bytes(): Long {
         val total: LongHolder = LongHolder()
         this.getPatternTimes().object2LongEntrySet().fastForEach({ p0: Any ->
            `$tmp0`(p0)
         })

         for (`element$iv` in this.getNusedItems()) {
            val it: Entry = `element$iv` as Entry
            val var10001: Long = total.value
            val var8: AEKey = it.getKey() as AEKey
            total.value = var10001
               + (
                  if (var8 is AEItemKey)
                     it.getLongValue() * 4
                     else
                     (if (var8 is AEFluidKey) (long)(it.getLongValue() / 100.0 * 4.0) else it.getLongValue() * 2)
               )
            }

         return total.value
      }

      public override fun simulation(): Boolean {
         return false
      }

      public override fun multiplePaths(): Boolean {
         return false
      }

      public fun storageInternalItem(aeKey: AEKey, amount: Long) {
         this.internalInventory.add(aeKey, amount)
      }

      public fun storageGlobalItem(aeKey: AEKey, amount: Long) {
         this.finalPlan.inventory.add(aeKey, amount)
      }

      public fun useInternalItem(aeKey: AEKey, amount: Long, fuzzyMode: FuzzyMode? = null, requestRemain: Boolean = true): Long {
         var remainRequiredAmount: Long = amount
         val extract: Long = Math.min(this.internalInventory.get(aeKey), amount)
         if (extract > 0L) {
            remainRequiredAmount -= extract
            this.internalInventory.remove(aeKey, extract)
         }

         if (fuzzyMode != null && remainRequiredAmount > 0L) {
            val var10000: java.util.Collection = this.internalInventory.findFuzzy(aeKey, fuzzyMode)

            for (entry in var10000) {
               if (remainRequiredAmount <= 0L) {
                  break
               }

               val `element$iv`: AEKey = entry.getKey() as AEKey
               val extract2: Long = Math.min(entry.getLongValue(), remainRequiredAmount)
               if (extract2 > 0L) {
                  remainRequiredAmount -= extract2
                  this.internalInventory.remove(`element$iv`, extract2)
               }
            }
         }

         if (remainRequiredAmount > 0L && requestRemain) {
            val var20: java.lang.Iterable = this.finalPlan.sortedComputingComponent
            var var26: Boolean
            if (var20 is java.util.Collection && (var20 as java.util.Collection).isEmpty()) {
               var26 = false
            } else {
               run label79@{
                  for (var24 in var20) {
                     if ((var24 as ComputingComponent).middleOutput === aeKey) {
                        var26 = true
                        return@label79
                     }
                  }

                  var26 = false
               }
            }

            if (var26) {
               RequirementsManager.INSTANCE.addRequirement(this.finalPlan.getRequirements(), aeKey, remainRequiredAmount)
            } else {
               val var21: Long = SuriedCraftingPlan.FinalPlan.this.finalPlan.useGlobalItem(aeKey, remainRequiredAmount, requestRemain = false)
               if (var21 > 0L) {
                  this.finalPlan.getNmissingItems().add(aeKey, var21)
                  this.finalPlan.isSimulated.set(true)
               }
            }
         }

         return remainRequiredAmount
      }
   }
}
