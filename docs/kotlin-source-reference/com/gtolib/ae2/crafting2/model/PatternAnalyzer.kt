package com.gtolib.ae2.crafting2.model

import appeng.api.crafting.IPatternDetails
import appeng.api.stacks.AEKey
import appeng.api.stacks.AEKeyMap
import appeng.api.stacks.GenericStack
import appeng.crafting.pattern.AECraftingPattern
import appeng.crafting.pattern.AEProcessingPattern
import appeng.crafting.pattern.AESmithingTablePattern
import appeng.crafting.pattern.AEStonecuttingPattern
import com.gtolib.ae2.crafting2.utils.AECrafting2Utils
import java.util.NoSuchElementException
import kotlin.jvm.internal.SourceDebugExtension

@SourceDebugExtension(["SMAP\nPatternAnalyzer.kt\nKotlin\n*S Kotlin\n*F\n+ 1 PatternAnalyzer.kt\ncom/gtolib/ae2/crafting2/model/PatternAnalyzer\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n+ 3 fake.kt\nkotlin/jvm/internal/FakeKt\n*L\n1#1,282:1\n296#2,2:283\n296#2,2:285\n1807#2,3:288\n231#2,2:291\n296#2,2:293\n296#2,2:295\n1#3:287\n*S KotlinDebug\n*F\n+ 1 PatternAnalyzer.kt\ncom/gtolib/ae2/crafting2/model/PatternAnalyzer\n*L\n25#1:283,2\n26#1:285,2\n59#1:288,3\n60#1:291,2\n87#1:293,2\n137#1:295,2\n*E\n"])
public class PatternAnalyzer(vararg sparseInputs: Any, vararg sparseOutputs: Any, targetOutput: AEKey) {
   public final val sparseInputs: Array<GenericStack?>
   public final val sparseOutputs: Array<GenericStack?>
   public final val targetOutput: AEKey
   public final val inputs: List<GenericStack>
   public final val outputs: List<GenericStack>
   public final val primaryInputAmount: Long
   public final val primaryOutputAmount: Long
   public final val netOutputPerCraft: Long

   init {
      this.sparseInputs = sparseInputs
      this.sparseOutputs = sparseOutputs
      this.targetOutput = targetOutput
      this.inputs = AECrafting2Utils.Companion.condenseStacks(this.sparseInputs)
      this.outputs = AECrafting2Utils.Companion.condenseStacks(this.sparseOutputs)
      var var6: java.util.Iterator = this.inputs.iterator()

      var var10000: Any
      while (true) {
         if (var6.hasNext()) {
            val `element$iv`: Any = var6.next()
            if ((`element$iv` as GenericStack).what() != this.targetOutput) {
               continue
            }

            var10000 = `element$iv`
            break
         }

         var10000 = null
         break
      }

      this.primaryInputAmount = if (var10000 as GenericStack != null) (var10000 as GenericStack).amount() else 0L
      var6 = this.outputs.iterator()

      while (true) {
         if (var6.hasNext()) {
            val var18: Any = var6.next()
            if ((var18 as GenericStack).what() != this.targetOutput) {
               continue
            }

            var10000 = var18
            break
         }

         var10000 = null
         break
      }

      this.primaryOutputAmount = if (var10000 as GenericStack != null) (var10000 as GenericStack).amount() else 0L
      this.netOutputPerCraft = this.primaryOutputAmount - this.primaryInputAmount
      if (this.primaryOutputAmount <= 0L) {
         throw IllegalArgumentException(("Pattern does not produce the target output ${this.targetOutput}").toString())
      } else if (this.netOutputPerCraft <= 0L) {
         throw IllegalArgumentException(
            ("Pattern does not have positive net output for ${this.targetOutput} (input: ${this.primaryInputAmount}, output: ${this.primaryOutputAmount})")
               .toString()
         )
      }
   }

   public fun calculateCrafting(requestedAmount: Long, minParallelCycles: Long = 1L, pattern: IPatternDetails? = null): CraftingAnalysis {
      val requiredCrafts: Long = if (this.netOutputPerCraft > 0L)
         (long)Math.ceil((double)requestedAmount / (double)this.netOutputPerCraft)
         else
         requestedAmount
         val actualParallel: Long = Math.min(minParallelCycles, requiredCrafts)
      val inputRequirements: AEKeyMap = AEKeyMap()
      val outputProducts: AEKeyMap = AEKeyMap()

      for (output in this.inputs) {
         val currentOutput: AEKey = output.what()
         val netAmountPerCraft: Long = output.amount()
         val `$i$f$firstOrNull`: java.lang.Iterable = this.outputs
         var var10000: Boolean
         if (this.outputs is java.util.Collection && this.outputs.isEmpty()) {
            var10000 = false
         } else {
            run label133@{
               for (it in `$i$f$firstOrNull`) {
                  if ((it as GenericStack).what() === currentOutput) {
                     var10000 = true
                     return@label133
                  }
               }

               var10000 = false
            }
         }

         val var48: Long
         if (var10000) {
            val var45: java.util.Iterator = this.outputs.iterator()

            val var47: Any
            do {
               if (!var45.hasNext()) {
                  throw NoSuchElementException("Collection contains no element matching the predicate.")
               }

               var47 = var45.next()
            } while ((var47 as GenericStack).what() != currentOutput)

            val var41: Long = netAmountPerCraft - (var47 as GenericStack).amount()
            var48 = if (var41 > 0L) netAmountPerCraft * actualParallel + (requiredCrafts - actualParallel) * var41 else netAmountPerCraft * actualParallel
         } else {
            var48 = netAmountPerCraft * requiredCrafts
         }

         if (var48 > 0L) {
            inputRequirements.put(currentOutput, var48)
         }
      }

      for (var32 in this.outputs) {
         val var33: AEKey = var32.what()

         var var50: Any
         run label145@{
            for (var42 in this.inputs) {
               if ((var42 as GenericStack).what() === var33) {
                  var50 = var42
                  return@label145
               }
            }

            var50 = null
         }

         outputProducts.put(
            var33, RangesKt.coerceAtLeast(var32.amount() - (if (var50 as GenericStack != null) (var50 as GenericStack).amount() else 0L), 0L) * requiredCrafts
         )
      }

      if (outputProducts.containsKey(this.targetOutput)) {
         outputProducts.put(
            this.targetOutput, Math.max(this.netOutputPerCraft * requiredCrafts, Math.max(outputProducts.getLong(this.targetOutput), requestedAmount))
         )
      }

      return CraftingAnalysis(
         pattern,
         this.targetOutput,
         requestedAmount,
         requestedAmount,
         requiredCrafts,
         this.netOutputPerCraft,
         this,
         inputRequirements,
         outputProducts,
         actualParallel
      )
   }

   public fun primaryOutputIsSelfIncrease(): Boolean {
      val var3: java.util.Iterator = this.inputs.iterator()

      var var10000: Any
      while (true) {
         if (var3.hasNext()) {
            val `element$iv`: Any = var3.next()
            if ((`element$iv` as GenericStack).what() != this.targetOutput) {
               continue
            }

            var10000 = `element$iv`
            break
         }

         var10000 = null
         break
      }

      return var10000 != null && this.netOutputPerCraft > 0L
   }

   public fun maxExternalExtractableForSelfIncrease(availableInventory: Long, requestedAmount: Long, minParallelCycles: Long = 1L): Long {
      val inputPerCraft: Long = this.primaryInputAmount
      val netIncreasePerCraft: Long = this.netOutputPerCraft
      if (this.netOutputPerCraft <= 0L) {
         throw IllegalArgumentException("Not a self-increase pattern for target output or net output per craft <= 0".toString())
      } else {
         val demandAmount: Long = RangesKt.coerceAtLeast(requestedAmount, 0L)
         val inventoryAmount: Long = RangesKt.coerceAtLeast(availableInventory, 0L)
         val minParallelism: Long = RangesKt.coerceAtLeast(minParallelCycles, 1L)
         label27@
         if (!this.primaryOutputIsSelfIncrease()) {
            return inventoryAmount
         } else {
            val craftsNeededIgnoringInventory: Long = maxExternalExtractableForSelfIncrease$ceilDiv(demandAmount, netIncreasePerCraft)
            return if (craftsNeededIgnoringInventory > minParallelism)
               RangesKt.coerceAtLeast(inventoryAmount - inputPerCraft * minParallelism, 0L)
               else
               Math.max(0L, Math.max(inventoryAmount - demandAmount, inventoryAmount - inputPerCraft * craftsNeededIgnoringInventory))
            }
      }
   }

   @JvmStatic
   fun `maxExternalExtractableForSelfIncrease$ceilDiv`(numerator: Long, denominator: Long): Long {
      if (denominator <= 0L) {
         throw IllegalArgumentException("divisor must be > 0".toString())
      } else if (numerator <= 0L) {
         0L
      } else {
         if (numerator % denominator == 0L) numerator / denominator else numerator / denominator + 1L
      }
   }

   public companion object {
      public fun fromPattern(detail: IPatternDetails, targetOutput: AEKey): PatternAnalyzer {
         val var10000: PatternAnalyzer
         if (detail is AEProcessingPattern) {
            val var10002: Array<GenericStack> = (detail as AEProcessingPattern).getSparseInputs()
            val var10003: Array<GenericStack> = (detail as AEProcessingPattern).getSparseOutputs()
            var10000 = PatternAnalyzer(var10002, var10003, targetOutput)
         } else if (detail is AECraftingPattern) {
            var10000 = PatternAnalyzer(
               AECrafting2Utils.Companion.extractPatternInputsWithFluidReplacement(detail as AECraftingPattern, true),
               arrayOf((detail as AECraftingPattern).getPrimaryOutput()),
               targetOutput
            )
         } else if (detail is AEStonecuttingPattern) {
            val var6: Array<GenericStack> = arrayOf(GenericStack((detail as AEStonecuttingPattern).getInput(), 1L))
            val var8: Array<GenericStack> = (detail as AEStonecuttingPattern).getOutputs()
            var10000 = PatternAnalyzer(var6, var8, targetOutput)
         } else {
            if (detail !is AESmithingTablePattern) {
               throw IllegalArgumentException("Unsupported pattern type: $detail")
            }

            val var7: Array<GenericStack> = arrayOf(
               GenericStack((detail as AESmithingTablePattern).getTemplate(), 1L),
               GenericStack((detail as AESmithingTablePattern).getBase(), 1L),
               GenericStack((detail as AESmithingTablePattern).getAddition(), 1L)
            )
            val var9: Array<GenericStack> = (detail as AESmithingTablePattern).getOutputs()
            var10000 = PatternAnalyzer(var7, var9, targetOutput)
         }

         return var10000
      }
   }
}
