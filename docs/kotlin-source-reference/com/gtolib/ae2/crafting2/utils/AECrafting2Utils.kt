package com.gtolib.ae2.crafting2.utils

import appeng.api.crafting.IPatternDetails
import appeng.api.stacks.AEItemKey
import appeng.api.stacks.AEKey
import appeng.api.stacks.GenericStack
import appeng.crafting.pattern.AECraftingPattern
import appeng.menu.AutoCraftingMenu
import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator
import it.unimi.dsi.fastutil.objects.Reference2LongLinkedOpenHashMap
import it.unimi.dsi.fastutil.objects.ReferenceSortedSet
import java.util.ArrayList
import java.util.RandomAccess
import kotlin.jvm.internal.SourceDebugExtension
import net.minecraft.core.NonNullList
import net.minecraft.world.inventory.CraftingContainer
import net.minecraft.world.inventory.TransientCraftingContainer
import net.minecraft.world.item.ItemStack

public class AECrafting2Utils {
   @SourceDebugExtension(["SMAP\nAECrafting2Utils.kt\nKotlin\n*S Kotlin\n*F\n+ 1 AECrafting2Utils.kt\ncom/gtolib/ae2/crafting2/utils/AECrafting2Utils$Companion\n+ 2 fake.kt\nkotlin/jvm/internal/FakeKt\n+ 3 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,228:1\n1#2:229\n1915#3,2:230\n1915#3,2:232\n*S KotlinDebug\n*F\n+ 1 AECrafting2Utils.kt\ncom/gtolib/ae2/crafting2/utils/AECrafting2Utils$Companion\n*L\n130#1:230,2\n174#1:232,2\n*E\n"])
   public companion object {
      private final val inputsCache: O2OOpenCacheHashMap<IPatternDetails, List<GenericStack>>
      private final val outputsCache: O2OOpenCacheHashMap<IPatternDetails, List<GenericStack>>

      public fun condenseInputs(detail: IPatternDetails, sparseInput: Array<GenericStack?>): List<GenericStack> {
         val cached: java.util.List = AECrafting2Utils.inputsCache.get(detail)
         if (cached != null) {
            return cached
         } else {
            val computed: java.util.List = this.condenseStacks(sparseInput)
            (AECrafting2Utils.inputsCache as java.util.Map).put(detail, computed)
            return computed
         }
      }

      public fun condenseOutputs(detail: IPatternDetails, sparseOutput: Array<GenericStack?>): List<GenericStack> {
         val cached: java.util.List = AECrafting2Utils.outputsCache.get(detail)
         if (cached != null) {
            return cached
         } else {
            val computed: java.util.List = this.condenseStacks(sparseOutput)
            (AECrafting2Utils.outputsCache as java.util.Map).put(detail, computed)
            return computed
         }
      }

      public fun condenseStacks(sparseInput: Array<GenericStack?>): List<GenericStack> {
         val map: Reference2LongLinkedOpenHashMap = Reference2LongLinkedOpenHashMap(sparseInput.length)

         for (k in sparseInput) {
            if (k != null) {
               map.addTo(k.what(), k.amount())
            }
         }

         if (map.isEmpty()) {
            throw IllegalStateException("No pattern here!".toString())
         } else {
            val var9: ArrayList = ArrayList(map.size())
            val var10000: ObjectBidirectionalIterator = (map.keySet() as ReferenceSortedSet).iterator()
            val var10: ObjectBidirectionalIterator = var10000

            while (var10.hasNext()) {
               val var13: AEKey = var10.next() as AEKey
               var9.add(GenericStack(var13, map.getLong(var13)))
            }

            return var9
         }
      }

      public fun <T> getCircularList(list: List<T>, startElement: T): List<T> {
         val size: Int = list.size()
         if (size == 0) {
            throw IllegalArgumentException("列表为空")
         } else {
            val startIndex: Int = list.indexOf(startElement)
            if (startIndex == -1) {
               throw IllegalArgumentException("元素 $startElement 不在列表中")
            } else if (size != 1 && startIndex != 0) {
               val out: ArrayList = ArrayList(size)
               if (list is RandomAccess) {
                  repeat(size) {
                     out.add(list.get((startIndex + it) % size))
                  }
               } else {
                  val var9: java.util.ListIterator = list.listIterator(startIndex)

                  while (var9.hasNext()) {
                     out.add(var9.next())
                  }

                  val it2: java.util.Iterator = list.iterator()

                  var count: Int = 0
                  while (count < startIndex && it2.hasNext()) {
                     out.add(it2.next())

                     count++
                  }
               }

               return out
            } else {
               return list
            }
         }
      }

      public fun extractPatternInputsWithFluidReplacement(detail: AECraftingPattern, enableFluidReplacement: Boolean = true): Array<GenericStack?> {
         if (enableFluidReplacement && detail.canSubstituteFluids) {
            val testFrame: TransientCraftingContainer = TransientCraftingContainer(AutoCraftingMenu(), 3, 3)
            var remainingItems: Int = 0
            val i: Array<GenericStack> = arrayOfNulls(9)

            while (remainingItems < 9) {
               i[remainingItems] = null
               remainingItems++
            }

            val replacedInputs: Array<GenericStack> = i
            val var17: java.util.Iterator = IntRange(0, 8).iterator()

            while (var17.hasNext()) {
               val ix: Int = (var17 as IntIterator).nextInt()
               val gs: GenericStack = detail.getSparseInputs()[ix]
               val key: AEKey = if (gs != null) gs.what() else null
               if (key is AEItemKey) {
                  testFrame.setItem(ix, (key as AEItemKey).toStack())
               } else {
                  testFrame.setItem(ix, ItemStack.EMPTY)
               }

               replacedInputs[ix] = gs
            }

            var var20: ItemStack = detail.getRemainingItems(testFrame as CraftingContainer)
            val var14: NonNullList = var20

            repeat(8) { var16 ->
               var20 = (ItemStack)var14.get(var16)
               if (!var20.isEmpty()) {
                  val var19: GenericStack = detail.getValidFluid(var16)
                  if (var19 != null) {
                     replacedInputs[var16] = var19
                  }
               }
            }

            return replacedInputs
         } else {
            val var10000: Array<GenericStack> = detail.getSparseInputs()
            return var10000
         }
      }

      public fun checkRemainingItemsAndFluidReplacement(detail: AECraftingPattern): Pair<Boolean, Boolean> {
         val testFrame: TransientCraftingContainer = TransientCraftingContainer(AutoCraftingMenu(), 3, 3)
         val allCanReplaceWithFluids: java.util.Iterator = IntRange(0, 8).iterator()

         while (allCanReplaceWithFluids.hasNext()) {
            val item: Int = (allCanReplaceWithFluids as IntIterator).nextInt()
            val gs: GenericStack = detail.getSparseInputs()[item]
            val key: AEKey = if (gs != null) gs.what() else null
            if (key is AEItemKey) {
               testFrame.setItem(item, (key as AEItemKey).toStack())
            } else {
               testFrame.setItem(item, ItemStack.EMPTY)
            }
         }

         val var10000: NonNullList = detail.getRemainingItems(testFrame as CraftingContainer)
         val var11: NonNullList = var10000
         var var12: Boolean = false
         var var13: Boolean = true
         if (!detail.canSubstituteFluids) {
            val var19: java.util.Iterator = var10000.iterator()
            val var14: java.util.Iterator = var19

            while (var14.hasNext()) {
               if (!(var14.next() as ItemStack).isEmpty()) {
                  var12 = true
                  var13 = false
                  break
               }
            }
         } else {
            repeat(8) { var15 ->
               val var20: Any = var11.get(var15)
               if (!(var20 as ItemStack).isEmpty()) {
                  var12 = true
                  if (detail.getValidFluid(var15) == null) {
                     var13 = false
                     break
                  }
               }
            }
         }

         return Pair<>(var12, var13)
      }

      public fun hasUnhandledRemainingItems(detail: AECraftingPattern): Boolean {
         val var2: Pair = this.checkRemainingItemsAndFluidReplacement(detail)
         return var2.component1() as java.lang.Boolean && !var2.component2() as java.lang.Boolean
      }
   }
}
