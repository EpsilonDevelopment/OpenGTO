package com.gtolib.api.lang

import java.util.Comparator
import kotlin.jvm.functions.Function1
import kotlin.jvm.internal.SourceDebugExtension

// $VF: Class flags could not be determined
@SourceDebugExtension(["SMAP\nComparisons.kt\nKotlin\n*S Kotlin\n*F\n+ 1 Comparisons.kt\nkotlin/comparisons/ComparisonsKt__ComparisonsKt$compareBy$2\n+ 2 TooltipsSortedWrapper.kt\ncom/gtolib/api/lang/TooltipsSortedWrapper$Companion\n+ 3 _Arrays.kt\nkotlin/collections/ArraysKt___ArraysKt\n+ 4 fake.kt\nkotlin/jvm/internal/FakeKt\n*L\n1#1,328:1\n21#2:329\n1899#3,6:330\n1#4:336\n*S KotlinDebug\n*F\n+ 1 TooltipsSortedWrapper.kt\ncom/gtolib/api/lang/TooltipsSortedWrapper$Companion\n*L\n21#1:330,6\n*E\n"])
internal class `TooltipsSortedWrapper$Companion$sortByConditions$$inlined$compareBy$1`<T> : Comparator {
   fun `TooltipsSortedWrapper$Companion$sortByConditions$$inlined$compareBy$1`(var1: Array<Array<Function1>>) {
      this.$conditions$inlined = var1
   }

   override final fun compare(a: T, b: T): Int {
      var var10000: Int
      run label73@{
         val item: Any = a
         val `$this$indexOfLast$iv`: Array<Any> = this.$conditions$inlined
         var it: Int = this.$conditions$inlined.length + -1
         if (0 <= this.$conditions$inlined.length + -1) {
            do {
               val var8: Int = it--
               if (`$this$indexOfLast$iv`[var8](item) as java.lang.Boolean) {
                  var10000 = var8
                  return@label73
               }
            } while (0 <= it)
         }

         var10000 = -1
      }

      run label77@{
         val var14: Int = var10000
         val var17: Int = var14.intValue()
         var31 = if ((if (var17 >= 0) var14 else null) != null) (if (var17 >= 0) var14 else null).intValue() + 1 else 0
         val var12: Any = b
         val var15: Array<Any> = this.$conditions$inlined
         var var22: Int = this.$conditions$inlined.length + -1
         if (0 <= this.$conditions$inlined.length + -1) {
            do {
               val var26: Int = var22--
               if (var15[var26](var12) as java.lang.Boolean) {
                  var10000 = var26
                  return@label77
               }
            } while (0 <= var22)
         }

         var10000 = -1
      }

      val var16: Int = var10000
      val var19: Int = var16.intValue()
      ComparisonsKt.compareValues(
         (T)var31,
         (T)(if ((if (var19 >= 0) var16 else null) != null)
            (if (var19 >= 0) var16 else null).intValue() + 1 as java.lang.Comparable
            else
            0 as java.lang.Comparable)
      )
   }
}
