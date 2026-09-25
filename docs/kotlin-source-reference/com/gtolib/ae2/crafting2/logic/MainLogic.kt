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
import com.gtolib.ae2.crafting2.model.SuriedCraftingPlan.FinalPlan
import com.gtolib.ae2.crafting2.utils.AE2CraftingTranslation
import com.gtolib.ae2.crafting2.utils.AECrafting2Utils
import com.gtolib.ae2.crafting2.utils.DisplayLevel
import com.gtolib.ae2.crafting2.utils.PerfLogger
import com.gtolib.ae2.crafting2.utils.RequirementsManager
import it.unimi.dsi.fastutil.objects.Object2LongMap
import it.unimi.dsi.fastutil.objects.Object2LongMaps
import it.unimi.dsi.fastutil.objects.ObjectIterator
import it.unimi.dsi.fastutil.objects.ObjectListIterator
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import it.unimi.dsi.fastutil.objects.Reference2LongMap.Entry
import java.util.ArrayDeque
import java.util.ArrayList
import java.util.Collections
import java.util.PriorityQueue
import kotlin.jvm.internal.SourceDebugExtension
import kotlin.jvm.internal.Ref.IntRef
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

@SourceDebugExtension(["SMAP\nMainLogic.kt\nKotlin\n*S Kotlin\n*F\n+ 1 MainLogic.kt\ncom/gtolib/ae2/crafting2/logic/MainLogic\n+ 2 _Arrays.kt\nkotlin/collections/ArraysKt___ArraysKt\n+ 3 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n+ 4 fake.kt\nkotlin/jvm/internal/FakeKt\n*L\n1#1,558:1\n14048#2,2:559\n1915#3,2:561\n1586#3:564\n1661#3,3:565\n296#3,2:568\n1391#3:570\n1480#3,5:571\n1586#3:576\n1661#3,3:577\n777#3:580\n873#3,2:581\n777#3:583\n873#3,2:584\n1391#3:586\n1480#3,5:587\n1#4:563\n*S KotlinDebug\n*F\n+ 1 MainLogic.kt\ncom/gtolib/ae2/crafting2/logic/MainLogic\n*L\n177#1:559,2\n192#1:561,2\n405#1:564\n405#1:565,3\n407#1:568,2\n323#1:570\n323#1:571,5\n464#1:576\n464#1:577,3\n473#1:580\n473#1:581,2\n483#1:583\n483#1:584,2\n489#1:586\n489#1:587,5\n*E\n"])
public object MainLogic {
   public fun executeV2(
      grid: IGrid,
      keyCounter: KeyCounter,
      simRequester: ICraftingSimulationRequester,
      what: AEKey,
      amount: Long,
      strategy: CalculationStrategy
   ): ICraftingPlan {
      try {
         if (PerfLogger.Companion.destroy) {
            throw RuntimeException()
         } else {
            val perfLogger: PerfLogger = PerfLogger()
perfLogger.push("主请求")
perfLogger.push("初始化算子")
val var42: ICraftingService = grid.getCraftingService()
val e: java.util.Set = var42.getCraftingProviders().getEmittableKeys()
perfLogger.push("初始化计划(组织最终计划，但其中的所有算子为空)")
val var25: SuriedCraftingPlan.FinalPlan = this.organizationBlankPlan(what, perfLogger)
perfLogger.pop()
perfLogger.push("构建物品->此物品可用算子的映射")
val var10002: ICraftingService = grid.getCraftingService()
this.organizationComputingComponent(var25, var10002)
perfLogger.pop()
perfLogger.push("提取SCC")
val var27: java.util.List = this.extractComputingComponentScc(var25)
perfLogger.pop()
perfLogger.push("构建SCC算子映射")
this.organizationSCCComputingComponent(var25, var27)
perfLogger.pop()
perfLogger.push("构建多路径选择算子")
this.organizationMultiPathRouter(var25)
perfLogger.pop()
perfLogger.push("拓扑排序算子")
this.topoSortComputingComponents(var25)
perfLogger.pop()
perfLogger.pop()
perfLogger.push("策略 $strategy 计算")
var var28: Long = 0L
            when (strategy) {
               CalculationStrategy.REPORT_MISSING_ITEMS -> {
                  this.performCalculationAndSort(keyCounter, var25, amount, grid, perfLogger, what)
                  var28 = var25.getFinalOutputAmount().get()
               }
               CalculationStrategy.CRAFT_LESS -> {
                  var var33: Long = 0L
                  var iterator4: Long = amount
                  if (!executeV2$simulateAndCheck(keyCounter, var25, grid, perfLogger, what, amount)) {
                     this.performCalculationAndSort(keyCounter, var25, amount, grid, perfLogger, what)
                     var28 = var25.getFinalOutputAmount().get()
                  } else {
                     while (var33 < iterator4) {
                        val key: Long = (var33 + iterator4 + 1L) / 2
                        if (executeV2$simulateAndCheck(keyCounter, var25, grid, perfLogger, what, (var33 + iterator4 + 1L) / (long)2)) {
                           var33 = key
                        } else {
                           iterator4 = key - 1L
                        }
                     }

                     var28 = var33
                  }
               }
               else -> throw NoWhenBranchMatchedException()
            }

            perfLogger.pop()
            val var43: DisplayLevel
            if (GTOConfig.INSTANCE.devMode.aeLog) {
               var43 = DisplayLevel.MAIN
            } else {
               if (GTOConfig.INSTANCE.devMode.aeLog) {
                  throw NoWhenBranchMatchedException()
               }

               var43 = DisplayLevel.NONE
            }

            System.out.print((Object)perfLogger.report(var43))
            val var44: java.util.Iterator = var25.usedItems().iterator()
            val var32: java.util.Iterator = var44

            while (var32.hasNext()) {
               if ((var32.next() as Entry).getLongValue() <= 0L) {
                  var32.remove()
               }
            }

            val var45: java.util.Iterator = var25.emittedItems().iterator()
            val var35: java.util.Iterator = var45

            while (var35.hasNext()) {
               if ((var35.next() as Entry).getLongValue() <= 0L) {
                  var35.remove()
               }
            }

            val var46: java.util.Iterator = var25.missingItems().iterator()
            val var36: java.util.Iterator = var46

            while (var36.hasNext()) {
               if ((var36.next() as Entry).getLongValue() <= 0L) {
                  var36.remove()
               }
            }

            val var38: java.util.Iterator = (var25.patternTimes() as java.util.Map).entrySet().iterator()

            while (var38.hasNext()) {
               if (((var38.next() as java.util.Map.Entry).getValue() as java.lang.Number).longValue() <= 0L) {
                  var38.remove()
               }
            }

            for (var41 in e) {
               var25.emittedItems().add(var41, var25.missingItems().get(var41))
               var25.missingItems().remove(var41)
            }

            val var40: CraftingPlan = CraftingPlan(
               GenericStack(what, var28),
               var25.bytes(),
               var25.isSimulated.get(),
               false,
               var25.usedItems(),
               var25.emittedItems(),
               var25.missingItems(),
               var25.patternTimes() as Object2LongMap<IPatternDetails>
            )
            var40.setGtocore$allocations(var25.highPriorityPush)
            return var40
         }
      } catch (var22: SendComponentToPlayerException) {
         for (var26 in var22.components) {
            AE2CraftingTranslation.Companion.sendErrorMessage(simRequester.getActionSource(), var26)
            GTOCore.LOGGER.error(var26.toString())
         }

         return CraftingPlan(GenericStack(what, 1L), 1L, true, false, KeyCounter(), KeyCounter(), KeyCounter(), Object2LongMaps.emptyMap())
      } catch (var23: Exception) {
         System.err.println(var23)
         val var10000: Array<StackTraceElement> = var23.getStackTrace()

         for (`element$iv` in var10000) {
            System.err.println(`element$iv` as StackTraceElement)
         }

         var23.printStackTrace(System.err)
         throw var23
      }
   }

   public fun beforeMainLogicRequest(keyCounter: KeyCounter, finalPlan: FinalPlan, amount: Long, grid: IGrid) {
      finalPlan.setFinalOutputAmount(amount)
      finalPlan.initInventory(keyCounter, grid)
      finalPlan.initRuntimeData()
   }

   public fun mainLogicRequest(finalPlan: FinalPlan, amount: Long) {
      val var10000: RequirementsManager = RequirementsManager.INSTANCE
      val var10001: KeyCounter = finalPlan.getRequirements()
      val var10002: AEKey = finalPlan.finalOutput().what()
      var10000.addRequirement(var10001, var10002, amount)

      for (`element$iv` in finalPlan.getAllDeepComponent()) {
         val middlePlan: ComputingComponent = `element$iv` as ComputingComponent
         val var10: ComputingComponent = `element$iv` as ComputingComponent
         if ((`element$iv` as ComputingComponent) !is ComputingComponent.CompositeComponent) {
            if (var10 is ComputingComponent.MultiCraftComponent) {
               if ((middlePlan as ComputingComponent.MultiCraftComponent).analyzer.primaryOutputIsSelfIncrease()
                  && middlePlan.middleOutput === finalPlan.getFinalOutput()) {
                  throw SendComponentToPlayerException(AE2CraftingTranslation.Companion.ERR_CRAFTING_SELF_INCREASE_OPERATOR(middlePlan.middleOutput))
               }
            } else if (var10 !is ComputingComponent.MultiPathSelectionComponent) {
               if (var10 is ComputingComponent.MultiProcessComponent) {
                  if ((middlePlan as ComputingComponent.MultiProcessComponent).analyzer.primaryOutputIsSelfIncrease()
                     && middlePlan.middleOutput === finalPlan.getFinalOutput()) {
                     throw SendComponentToPlayerException(AE2CraftingTranslation.Companion.ERR_CRAFTING_SELF_INCREASE_OPERATOR(middlePlan.middleOutput))
                  }
               } else {
                  if (var10 !is ComputingComponent.OnceCraftComponent) {
                     throw NoWhenBranchMatchedException()
                  }

                  if ((middlePlan as ComputingComponent.OnceCraftComponent).analyzer.primaryOutputIsSelfIncrease()
                     && middlePlan.middleOutput === finalPlan.getFinalOutput()) {
                     throw SendComponentToPlayerException(AE2CraftingTranslation.Companion.ERR_CRAFTING_SELF_INCREASE_OPERATOR(middlePlan.middleOutput))
                  }
               }
            }
         }
      }

      val var17: java.util.Iterator = finalPlan.sortedComputingComponent.iterator()
      var var11: java.util.Iterator = var17

      while (var11.hasNext()) {
         val var18: Any = var11.next()
         (var18 as ComputingComponent)
            .request(
               (var18 as ComputingComponent).middleOutput,
               RequirementsManager.INSTANCE.getRequirement(finalPlan.getRequirements(), (var18 as ComputingComponent).middleOutput),
               finalPlan,
               finalPlan
            )
            finalPlan.processedComputingComponentCount.addAndGet(1L)
      }

      val var19: java.util.Iterator = finalPlan.sortedComputingComponent.iterator()
      var11 = var19

      while (var11.hasNext()) {
         val var20: Any = var11.next()
         val var21: SuriedCraftingPlan.MiddlePlan = (var20 as ComputingComponent).getMiddlePlanOptional()
         if (var21 != null) {
            finalPlan.inventory.addAll(var21.internalInventory)
         }
      }
   }

   public fun performCalculationAndSort(keyCounter: KeyCounter, finalPlan: FinalPlan, amount: Long, grid: IGrid, perfLogger: PerfLogger, what: AEKey) {
      perfLogger.push("开始计算 ${what.getDisplayName().getString()} x$amount")
      perfLogger.push("初始化计算数据")
      this.beforeMainLogicRequest(keyCounter, finalPlan, amount, grid)
      perfLogger.pop()
      perfLogger.push("主逻辑开始请求")
      this.mainLogicRequest(finalPlan, amount)
      perfLogger.pop()
      perfLogger.push("整理计划")
      this.sortPlan(finalPlan)
      perfLogger.pop()
      perfLogger.pop()
   }

   public fun sortPlan(finalPlan: FinalPlan) {
      RequirementsManager.INSTANCE.getUnfulfilledRequirements(finalPlan.getRequirements()).reference2LongEntrySet().fastForEach({ p0: Any ->
         `$tmp0`(p0)
      })
   }

   public fun organizationBlankPlan(finalOutput: AEKey, perfLogger: PerfLogger): FinalPlan {
      return SuriedCraftingPlan.FinalPlan(finalOutput, perfLogger)
   }

   public fun organizationComputingComponent(finalPlan: FinalPlan, craftService: ICraftingService) {
      val queue: ArrayDeque = ArrayDeque()
      val visited: ReferenceOpenHashSet = ReferenceOpenHashSet()
      visited.add(finalPlan.getFinalOutput())
      queue.add(finalPlan.getFinalOutput())

      while (!queue.isEmpty()) {
         val middleOutput: AEKey = queue.poll() as AEKey
         val detailsIdentitySet: ReferenceOpenHashSet = ReferenceOpenHashSet()

         for (details in craftService.getCraftingFor(middleOutput)) {
            detailsIdentitySet.add(details)
         }

         val var10000: ObjectIterator = detailsIdentitySet.iterator()
         val var14: ObjectIterator = var10000

         while (var14.hasNext()) {
            val var15: IPatternDetails = var14.next() as IPatternDetails
            finalPlan.perfLogger.push("构建中间物品 ${middleOutput.getDisplayName().getString()}", 构建中间物品 ${middleOutput.getDisplayName().getString()})

            var var16: ComputingComponent
            try {
               val var20: ComputingComponent
               if (var15 is AEProcessingPattern) {
                  val var19: ComputingComponent.MultiProcessComponent
                  if (ComputingComponent.MultiProcessComponent.Companion.canBuild(var15)) {
                     var19 = ComputingComponent.MultiProcessComponent(middleOutput, var15, finalPlan)
                  } else {
                     var19 = null
                  }

                  var20 = var19
               } else if (var15 is AEStonecuttingPattern) {
                  val var21: ComputingComponent.MultiProcessComponent
                  if (ComputingComponent.MultiProcessComponent.Companion.canBuild(var15)) {
                     var21 = ComputingComponent.MultiProcessComponent(middleOutput, var15, finalPlan)
                  } else {
                     var21 = null
                  }

                  var20 = var21
               } else if (var15 is AESmithingTablePattern) {
                  val var22: ComputingComponent.MultiProcessComponent
                  if (ComputingComponent.MultiProcessComponent.Companion.canBuild(var15)) {
                     var22 = ComputingComponent.MultiProcessComponent(middleOutput, var15, finalPlan)
                  } else {
                     var22 = null
                  }

                  var20 = var22
               } else if (var15 is AECraftingPattern) {
                  if (ComputingComponent.MultiCraftComponent.Companion.canBuild(var15)) {
                     var20 = ComputingComponent.MultiCraftComponent(middleOutput, var15 as AECraftingPattern, finalPlan)
                  } else if (ComputingComponent.OnceCraftComponent.Companion.canBuild(var15)) {
                     var20 = ComputingComponent.OnceCraftComponent(middleOutput, var15 as AECraftingPattern, finalPlan)
                  } else {
                     var20 = null
                  }
               } else {
                  var20 = null
               }

               var16 = var20
            } catch (var13: Exception) {
               val var10002: java.lang.String = middleOutput.getDisplayName().getString()
               var var10003: java.lang.String = var13.getMessage()
               if (var10003 == null) {
                  var10003 = var13.getClass().getSimpleName()
               }

               val var23: MutableComponent = Component.literal("生成算子时出错: $var10002, 原因: $var10003")
               throw ComputingComponentBuildFailedException(var23 as Component, var15, cause = var13)
            }

            if (var16 == null) {
               val var24: MutableComponent = Component.literal("无法为物品 ${middleOutput.getDisplayName().getString()} 的 $var15 生成算子，可能是其算子未定义")
               throw ComputingComponentBuildFailedException(var24 as Component, var15)
            }

            val var17: ArrayList = finalPlan.middleOutputToComponent.computeIfAbsent(middleOutput, { it: Any ->
               ArrayList()
            }) as ArrayList
            if (!var17.contains(var16)) {
               var17.add(var16)
            }

            for (depend in var16.getDependencies()) {
               if (visited.add(depend)) {
                  queue.offer(depend)
               }
            }

            finalPlan.perfLogger.pop()
         }
      }
   }

   public fun extractComputingComponentScc(finalPlan: FinalPlan): List<List<ComputingComponent>> {
      var var10000: java.util.Set = finalPlan.middleOutputToComponent.values()
      val all: java.util.Set = CollectionsKt.toSet(CollectionsKt.flatten(var10000))
      val index: IntRef = IntRef()
      var low: O2IOpenCacheHashMap = O2IOpenCacheHashMap()
      low.defaultReturnValue(-1)
      val idx: O2IOpenCacheHashMap = low
      val var13: O2IOpenCacheHashMap = O2IOpenCacheHashMap()
      var13.defaultReturnValue(-1)
      low = var13
      val var14: ArrayDeque = ArrayDeque()
      val var16: OpenCacheHashSet = OpenCacheHashSet()
      val var17: ArrayList = ArrayList()

      for (v in all) {
         if (!idx.containsKey(v)) {
            extractComputingComponentScc$strongConnect(idx, index, low, var14, var16, var17, finalPlan, v)
         }
      }

      val var18: ArrayList = ArrayList()
      val var20: java.util.Iterator = var17.iterator()
      val var19: java.util.Iterator = var20

      while (var19.hasNext()) {
         var10000 = (java.util.Set)var19.next()
         var18.add(extractComputingComponentScc$findCycleIn(finalPlan, var10000))
      }

      return var18
   }

   public fun organizationSCCComputingComponent(finalPlan: FinalPlan, sccLists: List<List<ComputingComponent>>) {
      for (cycle in sccLists) {
         val head: java.lang.Iterable = cycle
         var rotated: java.util.Collection = ArrayList(CollectionsKt.collectionSizeOrDefault(cycle, 10))

         for (others in head) {
            rotated.add((others as ComputingComponent).middleOutput)
         }

         for (var20 in CollectionsKt.toSet(rotated as java.util.List)) {
            var var10000: ComputingComponent
            run label101@{
               for (var29 in cycle) {
                  if ((var29 as ComputingComponent).middleOutput === var20) {
                     var10000 = (ComputingComponent)var29
                     return@label101
                  }
               }

               var10000 = null
            }

            var10000 = var10000
            if (var10000 != null) {
               rotated = AECrafting2Utils.Companion.getCircularList(cycle, var10000)
               if (!ComputingComponent.CompositeComponent.Companion.canBuild(rotated)) {
                  val var10002: MutableComponent = Component.literal("无法为物品 ${var20.getDisplayName().getString()} 的 $rotated 构建组合算子，可能是其算子未定义")
                  throw ComputingComponentBuildFailedException(var10002 as Component, null, rotated, null, 10, null)
               }

               val var24: ComputingComponent = ComputingComponent.CompositeComponent(var20, rotated, finalPlan)
               val var26: ArrayList = finalPlan.getComputingComponentFor(var20)
               val var28: ArrayList = ArrayList(var26.size())
               val var39: java.util.Iterator = var26.iterator()
               val var30: java.util.Iterator = var39

               while (var30.hasNext()) {
                  var10000 = (ComputingComponent)var30.next()
                  val var33: ComputingComponent = var10000
                  if (!cycle.contains(var10000)) {
                     var28.add(var33)
                  }
               }

               val var31: ArrayList = ArrayList(var28.size() + 1)
               var31.add(var24)
               val var41: java.util.Iterator = var28.iterator()
               val var34: java.util.Iterator = var41

               while (var34.hasNext()) {
                  var10000 = (ComputingComponent)var34.next()
                  var31.add(var10000)
               }

               val var35: ArrayList = ArrayList(var31.size())
               val var37: OpenCacheHashSet = OpenCacheHashSet()
               val var43: java.util.Iterator = var31.iterator()
               val var16: java.util.Iterator = var43

               while (var16.hasNext()) {
                  var10000 = (ComputingComponent)var16.next()
                  val op: ComputingComponent = var10000
                  if (var37.add(var10000)) {
                     var35.add(op)
                  }
               }

               (finalPlan.middleOutputToComponent as java.util.Map).put(var20, var35)
            }
         }
      }
   }

   public fun organizationMultiPathRouter(finalPlan: FinalPlan) {
      for (entry in (finalPlan.middleOutputToComponent as java.util.Map).entrySet()) {
         val key: AEKey = entry.getKey() as AEKey
         val var10000: ComputingComponent.MultiPathSelectionComponent.Companion = ComputingComponent.MultiPathSelectionComponent.Companion
         val var10001: Any = entry.getValue()
         val var6: ComputingComponent.MultiPathSelectionComponent
         if (var10000.canBuild(var10001 as MutableList<ComputingComponent>)) {
            val var10003: Any = entry.getValue()
            var6 = ComputingComponent.MultiPathSelectionComponent(key, var10003 as MutableList<ComputingComponent>, finalPlan)
         } else {
            var6 = null
         }

         if (var6 != null) {
            (entry.getValue() as ArrayList).clear()
            (entry.getValue() as ArrayList).add(var6)
         }
      }
   }

   public fun topoSortComputingComponents(finalPlan: FinalPlan) {
      var var10000: ArrayList = finalPlan.middleOutputToComponent.get(finalPlan.getFinalOutput()) as ArrayList
      if (var10000 == null) {
         var10000 = ArrayList()
      }

      if (!var10000.isEmpty()) {
         val nodes: ReferenceLinkedOpenHashSet = ReferenceLinkedOpenHashSet()
         val dq: ArrayDeque = ArrayDeque()
         val var28: java.util.Iterator = var10000.iterator()
         val adj: java.util.Iterator = var28

         while (adj.hasNext()) {
            val var29: Any = adj.next()
            val indeg: ComputingComponent = var29 as ComputingComponent
            if (nodes.add(var29 as ComputingComponent)) {
               dq.addLast(indeg)
            }
         }

         while (!dq.isEmpty()) {
            val var13: ComputingComponent = dq.removeFirst() as ComputingComponent

            for (pq in topoSortComputingComponents$neighbors(finalPlan, var13)) {
               if (nodes.add(pq)) {
                  dq.addLast(pq)
               }
            }
         }

         val var14: O2OOpenCacheHashMap = O2OOpenCacheHashMap(nodes.size() * 2)
         val var17: O2IOpenCacheHashMap = O2IOpenCacheHashMap(nodes.size())
         var17.defaultReturnValue(0)
         val var16: O2IOpenCacheHashMap = var17
         val var30: ObjectListIterator = nodes.iterator()
         val var18: ObjectListIterator = var30

         while (var18.hasNext()) {
            val var21: ComputingComponent = var18.next() as ComputingComponent
            (var14 as java.util.Map).put(var21, ArrayList())
            var16.put(var21, 0)
         }

         val var31: ObjectListIterator = nodes.iterator()
         val var19: ObjectListIterator = var31

         while (var19.hasNext()) {
            val var22: ComputingComponent = var19.next() as ComputingComponent

            for (depKey in topoSortComputingComponents$effectiveDeps(var22)) {
               for (v in topoSortComputingComponents$producersFor(finalPlan, var22, depKey)) {
                  if (nodes.contains(v) && v != var22) {
                     val var32: Any = var14.get(var22)
                     (var32 as java.util.List).add(v)
                     var16.addTo(v, 1)
                  }
               }
            }
         }

         val var20: PriorityQueue = PriorityQueue<>({ p0: Any, p1: Any ->
            (`$tmp0`(p0, p1) as java.lang.Number).intValue()
         })
         var16.object2IntEntrySet().fastForEach({ p0: Any ->
            `$tmp0`(p0)
         })
         val var23: ArrayList = ArrayList(nodes.size())

         while (!var20.isEmpty()) {
            val var25: ComputingComponent = var20.remove() as ComputingComponent
            var23.add(var25)
            val var33: Any = var14.get(var25)

            for (var27 in var33 as java.util.List) {
               var16.addTo(var27, -1)
               if (var16.getInt(var27) == 0) {
                  var20.add(var27)
               }
            }
         }

         if (var23.size() != nodes.size()) {
            val var10002: MutableComponent = Component.literal("拓扑排序失败：检测到环或未压缩的组合算子依赖")
            throw CycleTopologyBuildError(var10002 as Component, var23)
         } else {
            finalPlan.sortedComputingComponent = var23
         }
      }
   }

   @JvmStatic
   fun `executeV2$simulateAndCheck`(
      `$keyCounter`: KeyCounter, finalPlan: SuriedCraftingPlan.FinalPlan, `$grid`: IGrid, perfLogger: PerfLogger, `$what`: AEKey, mid: Long
   ): Boolean {
      INSTANCE.performCalculationAndSort(`$keyCounter`, finalPlan, mid, `$grid`, perfLogger, `$what`)
      !finalPlan.isSimulated.get()
   }

   @JvmStatic
   fun `extractComputingComponentScc$findCycleIn$dfs`(
      visited: OpenCacheHashSet<ComputingComponent>,
      onPathIndex: O2IOpenCacheHashMap<ComputingComponent>,
      path: ArrayList<ComputingComponent>,
      `$scc`: MutableSet<ComputingComponent>,
      `$finalPlan`: SuriedCraftingPlan.FinalPlan,
      u: ComputingComponent
   ): MutableList<ComputingComponent> {
      visited.add(u)
      onPathIndex.put(u, path.size())
      path.add(u)

      for (v in extractComputingComponentScc$neighbors(`$finalPlan`, u)) {
         if (`$scc`.contains(v)) {
            val pos: Int = onPathIndex.getInt(v)
            if (pos != -1) {
               val var10000: java.util.List = path.subList(pos, path.size())
               CollectionsKt.toList(var10000)
            }

            if (!visited.contains(v)) {
               val r: java.util.List = extractComputingComponentScc$findCycleIn$dfs(visited, onPathIndex, path, `$scc`, `$finalPlan`, v)
               if (r != null) {
                  r
               }
            }
         }
      }

      path.remove(CollectionsKt.getLastIndex(path))
      onPathIndex.removeInt(u)
      null
   }

   @JvmStatic
   fun `extractComputingComponentScc$neighbors`(`$finalPlan`: SuriedCraftingPlan.FinalPlan, c: ComputingComponent): MutableList<ComputingComponent> {
      val `$this$flatMapTo$iv$iv`: java.lang.Iterable = c.getDependencies()
      val `destination$iv$iv`: java.util.Collection = ArrayList()

      for (`element$iv$iv` in `$this$flatMapTo$iv$iv`) {
         val var10000: ArrayList = `$finalPlan`.middleOutputToComponent.get(`element$iv$iv` as AEKey) as ArrayList
         val var11: java.util.List = if (var10000 != null) var10000 else Collections.emptyList()
         CollectionsKt.addAll(`destination$iv$iv`, var11)
      }

      `destination$iv$iv` as java.util.List
   }

   @JvmStatic
   fun `extractComputingComponentScc$strongConnect`(
      idx: O2IOpenCacheHashMap<ComputingComponent>,
      index: IntRef,
      low: O2IOpenCacheHashMap<ComputingComponent>,
      stack: ArrayDeque<ComputingComponent>,
      onStack: OpenCacheHashSet<ComputingComponent>,
      sccSets: ArrayList<MutableSet<ComputingComponent>>,
      `$finalPlan`: SuriedCraftingPlan.FinalPlan,
      v: ComputingComponent
   ) {
      idx.put(v, index.element)
      low.put(v, index.element)
      val s: Int = index.element++
      stack.addFirst(v)
      onStack.add(v)

      for (w in extractComputingComponentScc$neighbors(`$finalPlan`, v)) {
         if (!idx.containsKey(w)) {
            extractComputingComponentScc$strongConnect(idx, index, low, stack, onStack, sccSets, `$finalPlan`, w)
            low.put(v, Math.min(low.getInt(v), low.getInt(w)))
         } else if (onStack.contains(w)) {
            low.put(v, Math.min(low.getInt(v), idx.getInt(w)))
         }
      }

      if (low.getInt(v) == idx.getInt(v)) {
         val var11: ReferenceLinkedOpenHashSet = ReferenceLinkedOpenHashSet()

         val var12: ComputingComponent
         do {
            var12 = stack.removeFirst() as ComputingComponent
            onStack.remove(var12)
            var11.add(var12)
         } while (var12 != v)

         if (var11.size() > 1) {
            sccSets.add(var11)
         }
      }
   }

   @JvmStatic
   fun `extractComputingComponentScc$findCycleIn`(`$finalPlan`: SuriedCraftingPlan.FinalPlan, scc: MutableSet<ComputingComponent>): MutableList<ComputingComponent> {
      val visited: OpenCacheHashSet = OpenCacheHashSet()
      val path: O2IOpenCacheHashMap = O2IOpenCacheHashMap()
      path.defaultReturnValue(-1)
      val onPathIndex: O2IOpenCacheHashMap = path
      val var8: ArrayList = ArrayList()

      for (var10 in scc) {
         if (!visited.contains(var10)) {
            val r: java.util.List = extractComputingComponentScc$findCycleIn$dfs(visited, onPathIndex, var8, scc, `$finalPlan`, var10)
            if (r != null) {
               r
            }
         }
      }

      val var10002: MutableComponent = Component.literal("无法在强连通分量中构造有向环序")
      throw CycleTopologyBuildError(var10002 as Component, CollectionsKt.toList(scc))
   }

   @JvmStatic
   fun `topoSortComputingComponents$isComposite`(c: ComputingComponent): Boolean {
      c is ComputingComponent.CompositeComponent
   }

   @JvmStatic
   fun `topoSortComputingComponents$internalKeysOf`(c: ComputingComponent): MutableSet<AEKey> {
      val var10000: java.util.Set
      if (c is ComputingComponent.CompositeComponent) {
         val `$this$map$iv`: java.lang.Iterable = (c as ComputingComponent.CompositeComponent).components
         val `destination$iv$iv`: java.util.Collection = ArrayList(CollectionsKt.collectionSizeOrDefault(`$this$map$iv`, 10))

         for (`item$iv$iv` in `$this$map$iv`) {
            `destination$iv$iv`.add((`item$iv$iv` as ComputingComponent).middleOutput)
         }

         var10000 = CollectionsKt.toSet(`destination$iv$iv` as java.util.List)
      } else {
         var10000 = Collections.emptySet()
      }

      var10000
   }

   @JvmStatic
   fun `topoSortComputingComponents$effectiveDeps`(u: ComputingComponent): MutableSet<AEKey> {
      val base: java.util.Set = CollectionsKt.toMutableSet(u.getDependencies())
      if (topoSortComputingComponents$isComposite(u)) {
         val `$this$filter$iv`: java.lang.Iterable = topoSortComputingComponents$internalKeysOf(u)
         val `destination$iv$iv`: java.util.Collection = ArrayList()

         for (`element$iv$iv` in `$this$filter$iv`) {
            if (!(`element$iv$iv` as AEKey == u.middleOutput)) {
               `destination$iv$iv`.add(`element$iv$iv`)
            }
         }

         base.addAll(`destination$iv$iv` as java.util.List)
      }

      base
   }

   @JvmStatic
   fun `topoSortComputingComponents$producersFor`(`$finalPlan`: SuriedCraftingPlan.FinalPlan, u: ComputingComponent, depKey: AEKey): MutableList<ComputingComponent> {
      val var10000: ArrayList = `$finalPlan`.middleOutputToComponent.get(depKey) as ArrayList
      if (var10000 == null) {
         val var15: java.util.List = Collections.emptyList()
         var15
      } else {
         val var14: java.util.List
         if (topoSortComputingComponents$internalKeysOf(u).contains(depKey)) {
            val `$this$filterTo$iv$iv`: java.lang.Iterable = var10000
            val `destination$iv$iv`: java.util.Collection = ArrayList()

            for (`element$iv$iv` in `$this$filterTo$iv$iv`) {
               if (!topoSortComputingComponents$isComposite(`element$iv$iv` as ComputingComponent)) {
                  `destination$iv$iv`.add(`element$iv$iv`)
               }
            }

            var14 = `destination$iv$iv` as java.util.List
         } else {
            var14 = var10000
         }

         var14
      }
   }

   @JvmStatic
   fun `topoSortComputingComponents$neighbors`(`$finalPlan`: SuriedCraftingPlan.FinalPlan, u: ComputingComponent): MutableList<ComputingComponent> {
      val `$this$flatMapTo$iv$iv`: java.lang.Iterable = topoSortComputingComponents$effectiveDeps(u)
      val `destination$iv$iv`: java.util.Collection = ArrayList()

      for (`element$iv$iv` in `$this$flatMapTo$iv$iv`) {
         CollectionsKt.addAll(`destination$iv$iv`, topoSortComputingComponents$producersFor(`$finalPlan`, u, `element$iv$iv` as AEKey))
      }

      `destination$iv$iv` as java.util.List
   }
}
