package com.gtolib.ae2.crafting2.utils

import java.util.ArrayList
import java.util.Arrays
import java.util.HashMap
import java.util.Map.Entry
import kotlin.jvm.internal.Ref.LongRef

public class PerfLogger {
   private final val roots: ArrayList<com.gtolib.ae2.crafting2.utils.PerfLogger.Node> = ArrayList()
   private final val stack: ArrayList<com.gtolib.ae2.crafting2.utils.PerfLogger.Node> = ArrayList()

   @Synchronized
   public fun push(name: String, displayLevel: DisplayLevel = DisplayLevel.MAIN, vars: Map<String, Any?> = MapsKt.emptyMap()) {
      val node: PerfLogger.Node = PerfLogger.Node(name, System.nanoTime(), null, this.stack.size(), displayLevel, vars, null, 64, null)
      if (this.stack.isEmpty()) {
         this.roots.add(node)
      } else {
         (this.stack.get(this.stack.size() - 1) as PerfLogger.Node).children.add(node)
      }

      this.stack.add(node)
   }

   @Synchronized
   public fun pop() {
      if (!this.stack.isEmpty()) {
         val var10000: Any = this.stack.remove(this.stack.size() - 1)
         (var10000 as PerfLogger.Node).endNs = System.nanoTime()
      }
   }

   @Synchronized
   public fun mark(name: String, displayLevel: DisplayLevel = DisplayLevel.MAIN, vars: Map<String, Any?> = MapsKt.emptyMap()) {
      this.push(name, displayLevel, vars)
      this.pop()
   }

   @Synchronized
   public fun reset() {
      this.roots.clear()
      this.stack.clear()
   }

   @Synchronized
   public fun report(maxLevel: DisplayLevel = DisplayLevel.MAIN): String {
      if (maxLevel === DisplayLevel.NONE) {
         return ""
      } else {
         val sb: StringBuilder = StringBuilder()
         val now: Long = System.nanoTime()
         val maxLenByDepth: HashMap = HashMap()
         val rootTotalNs: LongRef = LongRef()
         var var10000: PerfLogger.Node = this.roots.iterator()
         var var7: java.util.Iterator = var10000

         while (var7.hasNext()) {
            var10000 = (PerfLogger.Node)var7.next()
            report$collectMax(now, rootTotalNs, maxLevel, maxLenByDepth, var10000)
         }

         val var14: java.util.Iterator = this.roots.iterator()
         var7 = var14

         while (var7.hasNext()) {
            var10000 = (PerfLogger.Node)var7.next()
            val var16: java.lang.Long = (var10000 as PerfLogger.Node).endNs
            report$appendNode(rootTotalNs, maxLenByDepth, maxLevel, sb, var10000, (var16 ?: now) - (var10000 as PerfLogger.Node).startNs)
         }

         val var17: java.lang.String = sb.toString()
         return var17
      }
   }

   @JvmStatic
   fun `report$collectMax`(now: Long, rootTotalNs: LongRef, `$maxLevel`: DisplayLevel, maxLenByDepth: HashMap<Int, Int>, n: PerfLogger.Node) {
      val var10000: java.lang.Long = n.endNs
      val durationNs: Long = (var10000 ?: now) - n.startNs
      val durationUs: Double = durationNs / 1000.0
      if (n.depth == 0) {
         rootTotalNs.element += durationNs
      }

      if (n.displayLevel.ordinal() <= `$maxLevel`.ordinal()) {
         val var18: Array<Any> = arrayOf(durationUs)
         val var19: java.lang.String = java.lang.String.format("%.3f", Arrays.copyOf(var18, var18.length))
         val durStrLen: Int = var19.length()
         val var20: Int = maxLenByDepth.get(n.depth) as Int
         if (durStrLen > (var20 ?: 0)) {
            maxLenByDepth.put(n.depth, durStrLen)
         }
      }

      val var21: java.util.Iterator = n.children.iterator()
      val var15: java.util.Iterator = var21

      while (var15.hasNext()) {
         val var22: Any = var15.next()
         report$collectMax(now, rootTotalNs, `$maxLevel`, maxLenByDepth, var22 as PerfLogger.Node)
      }
   }

   @JvmStatic
   fun `report$appendNode`(
      rootTotalNs: LongRef, maxLenByDepth: HashMap<Int, Int>, `$maxLevel`: DisplayLevel, sb: StringBuilder, n: PerfLogger.Node, parentDurationNs: Long
   ) {
      val var10000: java.lang.Long = n.endNs
      val durationNs: Long = (var10000 ?: System.nanoTime()) - n.startNs
      val durationUs: Double = durationNs / 1000.0
      val indent: java.lang.String = StringsKt.repeat("  ", n.depth)
      val parentPercent: Double = if (n.depth == 0) 100.0 else (if (parentDurationNs > 0L) (double)durationNs / parentDurationNs * 100.0 else 100.0)
      val totalPercent: Double = if (rootTotalNs.element > 0L) (double)durationNs / rootTotalNs.element * 100.0 else 100.0
      val var29: Array<Any> = arrayOf(parentPercent)
      val var38: java.lang.String = java.lang.String.format("%.2f%%", Arrays.copyOf(var29, var29.length))
      val parentPctStr: java.lang.String = StringsKt.padStart(var38, 6, ' ')
      val var32: Array<Any> = arrayOf(totalPercent)
      val var39: java.lang.String = java.lang.String.format("%.2f%%", Arrays.copyOf(var32, var32.length))
      val var28: java.lang.String = StringsKt.padStart(var39, 6, ' ')
      val var35: Array<Any> = arrayOf(durationUs)
      val var40: java.lang.String = java.lang.String.format("%.3f", Arrays.copyOf(var35, var35.length))
      val var41: Int = maxLenByDepth.get(n.depth) as Int
      val var36: java.lang.String = StringsKt.padStart(var40, var41 ?: var40.length(), ' ')
      val varsStr: java.lang.String = if (!n.vars.isEmpty()) CollectionsKt.joinToString(n.vars.entrySet(), ", ", " {", "}", transform = { var0: Entry ->
         var k: java.lang.String
         var var10000: java.lang.String
         run label15@{
            k = var0.getKey() as java.lang.String
            val v: Any = var0.getValue()
            if (v != null) {
               var10000 = v.toString()
               if (var10000 != null) {
                  return@label15
               }
            }

            var10000 = "null"
         }

         ("$k=${StringsKt.replace(var10000, "\n", "\\n")}") as java.lang.CharSequence
      }) else ""
      val levelLabel: java.lang.String = "(${n.displayLevel.zhLabel()})"
      if (n.displayLevel.ordinal() <= `$maxLevel`.ordinal()) {
         sb.append("$indent- $parentPctStr $var28 ${StringsKt.replace(n.name, "\n", "\\n")} $levelLabel$varsStr — $var36 µs\n")
      }

      val var42: java.util.Iterator = n.children.iterator()
      val var37: java.util.Iterator = var42

      while (var37.hasNext()) {
         val var43: Any = var37.next()
         report$appendNode(rootTotalNs, maxLenByDepth, `$maxLevel`, sb, var43 as PerfLogger.Node, durationNs)
      }
   }

   public companion object {
      public final var destroy: Boolean
         internal set
   }

   private data class Node(name: String,
      startNs: Long,
      endNs: Long? = null,
      depth: Int,
      displayLevel: DisplayLevel,
      vars: Map<String, Any?> = MapsKt.emptyMap(),
      children: ArrayList<com.gtolib.ae2.crafting2.utils.PerfLogger.Node> = ArrayList()
   ) {
      public final val name: String
      public final val startNs: Long

      public final var endNs: Long?
         internal set

      public final val depth: Int
      public final val displayLevel: DisplayLevel
      public final val vars: Map<String, Any?>
      public final val children: ArrayList<com.gtolib.ae2.crafting2.utils.PerfLogger.Node>

      init {
         this.name = name
         this.startNs = startNs
         this.endNs = endNs
         this.depth = depth
         this.displayLevel = displayLevel
         this.vars = vars
         this.children = children
      }

      public operator fun component1(): String {
         return this.name
      }

      public operator fun component2(): Long {
         return this.startNs
      }

      public operator fun component3(): Long? {
         return this.endNs
      }

      public operator fun component4(): Int {
         return this.depth
      }

      public operator fun component5(): DisplayLevel {
         return this.displayLevel
      }

      public operator fun component6(): Map<String, Any?> {
         return this.vars
      }

      public operator fun component7(): ArrayList<com.gtolib.ae2.crafting2.utils.PerfLogger.Node> {
         return this.children
      }

      public fun copy(
         name: String = this.name,
         startNs: Long = this.startNs,
         endNs: Long? = this.endNs,
         depth: Int = this.depth,
         displayLevel: DisplayLevel = this.displayLevel,
         vars: Map<String, Any?> = this.vars,
         children: ArrayList<com.gtolib.ae2.crafting2.utils.PerfLogger.Node> = this.children
      ): com.gtolib.ae2.crafting2.utils.PerfLogger.Node {
         return PerfLogger.Node(name, startNs, endNs, depth, displayLevel, vars, children)
      }

      public override fun toString(): String {
         return "Node(name=${this.name}, startNs=${this.startNs}, endNs=${this.endNs}, depth=${this.depth}, displayLevel=${this.displayLevel}, vars=${this.vars}, children=${this.children})"
      }

      public override fun hashCode(): Int {
         return (
                  (
                           (
                                    (
                                             (this.name.hashCode() * 31 + java.lang.Long.hashCode(this.startNs)) * 31
                                                + (if (this.endNs == null) 0 else this.endNs.hashCode())
                                          )
                                          * 31
                                       + Integer.hashCode(this.depth)
                                 )
                                 * 31
                              + this.displayLevel.hashCode()
                        )
                        * 31
                     + this.vars.hashCode()
               )
               * 31
            + this.children.hashCode()
         }

      public override operator fun equals(other: Any?): Boolean {
         label58@
         if (this === other) {
            return true
         } else {
            return other is PerfLogger.Node
               && this.name == (other as PerfLogger.Node).name
               && this.startNs == (other as PerfLogger.Node).startNs
               && this.endNs == (other as PerfLogger.Node).endNs
               && this.depth == (other as PerfLogger.Node).depth
               && this.displayLevel === (other as PerfLogger.Node).displayLevel
               && this.vars == (other as PerfLogger.Node).vars
               && this.children == (other as PerfLogger.Node).children
            }
      }
   }
}
