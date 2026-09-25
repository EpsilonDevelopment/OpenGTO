package com.gtolib.api.lang

import com.gtocore.common.data.translation.MultiblockSlang
import com.gtolib.api.annotation.NewDataAttributes
import java.util.ArrayList
import java.util.Arrays
import java.util.function.Supplier
import kotlin.jvm.functions.Function1
import kotlin.jvm.internal.SourceDebugExtension
import net.minecraft.network.chat.Component

@SourceDebugExtension(["SMAP\nTooltipsSortedWrapper.kt\nKotlin\n*S Kotlin\n*F\n+ 1 TooltipsSortedWrapper.kt\ncom/gtolib/api/lang/TooltipsSortedWrapper\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,67:1\n1807#2,3:68\n1807#2,3:71\n1807#2,3:74\n1807#2,3:77\n1807#2,3:80\n1807#2,3:83\n1807#2,3:86\n1807#2,3:89\n1807#2,3:92\n1807#2,3:95\n1807#2,3:98\n*S KotlinDebug\n*F\n+ 1 TooltipsSortedWrapper.kt\ncom/gtolib/api/lang/TooltipsSortedWrapper\n*L\n31#1:68,3\n32#1:71,3\n33#1:74,3\n34#1:77,3\n35#1:80,3\n36#1:83,3\n37#1:86,3\n38#1:89,3\n39#1:92,3\n40#1:95,3\n41#1:98,3\n*E\n"])
public class TooltipsSortedWrapper {
   private final val tooltips: ArrayList<com.gtolib.api.lang.TooltipsSortedWrapper.SupplierWrapper> = ArrayList()

   public final var isInitialized: Boolean
      internal set

   private fun add(component: Supplier<List<Component?>?>, supplier: Boolean): TooltipsSortedWrapper {
      this.tooltips.add(TooltipsSortedWrapper.SupplierWrapper(this, component, supplier))
      val conditions: Array<Array<Function1>> = arrayOf(
         { s: Supplier ->
            val var10000: java.util.List = s.get() as java.util.List
            var var9: Boolean
            if (var10000 != null) {
               val `$this$any$iv`: java.lang.Iterable = var10000
               if (var10000 is java.util.Collection && (var10000 as java.util.Collection).isEmpty()) {
                  var9 = false
               } else {
                  val var3: java.util.Iterator = `$this$any$iv`.iterator()

                  while (true) {
                     if (!var3.hasNext()) {
                        var9 = false
                        break
                     }

                     run label44@{
                        val it: Component = var3.next() as Component
                        if (it != null) {
                           val var7: java.lang.String = it.getString()
                           if (var7 != null) {
                              var8 = StringsKt.contains(var7, "gtocore.difficulty_config")
                              return@label44
                           }
                        }

                        var8 = false
                     }

                     if (var8) {
                        var9 = true
                        break
                     }
                  }
               }
            } else {
               var9 = false
            }

            var9
         },
         { s: Supplier ->
            val var10000: java.util.List = s.get() as java.util.List
            var var9: Boolean
            if (var10000 != null) {
               val `$this$any$iv`: java.lang.Iterable = var10000
               if (var10000 is java.util.Collection && (var10000 as java.util.Collection).isEmpty()) {
                  var9 = false
               } else {
                  val var3: java.util.Iterator = `$this$any$iv`.iterator()

                  while (true) {
                     if (!var3.hasNext()) {
                        var9 = false
                        break
                     }

                     run label44@{
                        val it: Component = var3.next() as Component
                        if (it != null) {
                           val var7: java.lang.String = it.getString()
                           if (var7 != null) {
                              var8 = StringsKt.contains(var7, "gtocore.lang.template")
                              return@label44
                           }
                        }

                        var8 = false
                     }

                     if (var8) {
                        var9 = true
                        break
                     }
                  }
               }
            } else {
               var9 = false
            }

            var9
         },
         { s: Supplier ->
            val var10000: java.util.List = s.get() as java.util.List
            var var9: Boolean
            if (var10000 != null) {
               val `$this$any$iv`: java.lang.Iterable = var10000
               if (var10000 is java.util.Collection && (var10000 as java.util.Collection).isEmpty()) {
                  var9 = false
               } else {
                  val var3: java.util.Iterator = `$this$any$iv`.iterator()

                  while (true) {
                     if (!var3.hasNext()) {
                        var9 = false
                        break
                     }

                     run label44@{
                        val it: Component = var3.next() as Component
                        if (it != null) {
                           val var7: java.lang.String = it.getString()
                           if (var7 != null) {
                              var8 = StringsKt.contains(var7, "gtocore.lang.template.${NewDataAttributes.ALLOW_PARALLEL_SPECIAL.getKey()}")
                              return@label44
                           }
                        }

                        var8 = false
                     }

                     if (var8) {
                        var9 = true
                        break
                     }
                  }
               }
            } else {
               var9 = false
            }

            var9
         },
         { s: Supplier ->
            val var10000: java.util.List = s.get() as java.util.List
            var var9: Boolean
            if (var10000 != null) {
               val `$this$any$iv`: java.lang.Iterable = var10000
               if (var10000 is java.util.Collection && (var10000 as java.util.Collection).isEmpty()) {
                  var9 = false
               } else {
                  val var3: java.util.Iterator = `$this$any$iv`.iterator()

                  while (true) {
                     if (!var3.hasNext()) {
                        var9 = false
                        break
                     }

                     run label44@{
                        val it: Component = var3.next() as Component
                        if (it != null) {
                           val var7: java.lang.String = it.getString()
                           if (var7 != null) {
                              var8 = StringsKt.contains(var7, "gtocore.lang.template.${NewDataAttributes.ALLOW_PARALLEL.getKey()}")
                              return@label44
                           }
                        }

                        var8 = false
                     }

                     if (var8) {
                        var9 = true
                        break
                     }
                  }
               }
            } else {
               var9 = false
            }

            var9
         },
         { s: Supplier ->
            val var10000: java.util.List = s.get() as java.util.List
            var var9: Boolean
            if (var10000 != null) {
               val `$this$any$iv`: java.lang.Iterable = var10000
               if (var10000 is java.util.Collection && (var10000 as java.util.Collection).isEmpty()) {
                  var9 = false
               } else {
                  val var3: java.util.Iterator = `$this$any$iv`.iterator()

                  while (true) {
                     if (!var3.hasNext()) {
                        var9 = false
                        break
                     }

                     run label44@{
                        val it: Component = var3.next() as Component
                        if (it != null) {
                           val var7: java.lang.String = it.getString()
                           if (var7 != null) {
                              var8 = StringsKt.contains(var7, "gtocore.lang.template.${NewDataAttributes.ALLOW_PARALLEL_NUMBER.getKey()}")
                              return@label44
                           }
                        }

                        var8 = false
                     }

                     if (var8) {
                        var9 = true
                        break
                     }
                  }
               }
            } else {
               var9 = false
            }

            var9
         },
         { s: Supplier ->
            val var10000: java.util.List = s.get() as java.util.List
            var var9: Boolean
            if (var10000 != null) {
               val `$this$any$iv`: java.lang.Iterable = var10000
               if (var10000 is java.util.Collection && (var10000 as java.util.Collection).isEmpty()) {
                  var9 = false
               } else {
                  val var3: java.util.Iterator = `$this$any$iv`.iterator()

                  while (true) {
                     if (!var3.hasNext()) {
                        var9 = false
                        break
                     }

                     run label44@{
                        val it: Component = var3.next() as Component
                        if (it != null) {
                           val var7: java.lang.String = it.getString()
                           if (var7 != null) {
                              var8 = StringsKt.contains(var7, "gtocore.lang.template.${NewDataAttributes.LASER_ENERGY_HATCH.getKey()}")
                              return@label44
                           }
                        }

                        var8 = false
                     }

                     if (var8) {
                        var9 = true
                        break
                     }
                  }
               }
            } else {
               var9 = false
            }

            var9
         },
         { s: Supplier ->
            val var10000: java.util.List = s.get() as java.util.List
            var var9: Boolean
            if (var10000 != null) {
               val `$this$any$iv`: java.lang.Iterable = var10000
               if (var10000 is java.util.Collection && (var10000 as java.util.Collection).isEmpty()) {
                  var9 = false
               } else {
                  val var3: java.util.Iterator = `$this$any$iv`.iterator()

                  while (true) {
                     if (!var3.hasNext()) {
                        var9 = false
                        break
                     }

                     run label44@{
                        val it: Component = var3.next() as Component
                        if (it != null) {
                           val var7: java.lang.String = it.getString()
                           if (var7 != null) {
                              var8 = StringsKt.contains(var7, MultiblockSlang.INSTANCE.getNot_allow_standard_energy_hatch().getTranslationPrefix())
                              return@label44
                           }
                        }

                        var8 = false
                     }

                     if (var8) {
                        var9 = true
                        break
                     }
                  }
               }
            } else {
               var9 = false
            }

            var9
         },
         { s: Supplier ->
            val var10000: java.util.List = s.get() as java.util.List
            var var9: Boolean
            if (var10000 != null) {
               val `$this$any$iv`: java.lang.Iterable = var10000
               if (var10000 is java.util.Collection && (var10000 as java.util.Collection).isEmpty()) {
                  var9 = false
               } else {
                  val var3: java.util.Iterator = `$this$any$iv`.iterator()

                  while (true) {
                     if (!var3.hasNext()) {
                        var9 = false
                        break
                     }

                     run label44@{
                        val it: Component = var3.next() as Component
                        if (it != null) {
                           val var7: java.lang.String = it.getString()
                           if (var7 != null) {
                              var8 = StringsKt.contains(var7, "gtocore.lang.template.${NewDataAttributes.PREFECT_OVERCLOCK.getKey()}")
                              return@label44
                           }
                        }

                        var8 = false
                     }

                     if (var8) {
                        var9 = true
                        break
                     }
                  }
               }
            } else {
               var9 = false
            }

            var9
         },
         { s: Supplier ->
            val var10000: java.util.List = s.get() as java.util.List
            var var9: Boolean
            if (var10000 != null) {
               val `$this$any$iv`: java.lang.Iterable = var10000
               if (var10000 is java.util.Collection && (var10000 as java.util.Collection).isEmpty()) {
                  var9 = false
               } else {
                  val var3: java.util.Iterator = `$this$any$iv`.iterator()

                  while (true) {
                     if (!var3.hasNext()) {
                        var9 = false
                        break
                     }

                     run label44@{
                        val it: Component = var3.next() as Component
                        if (it != null) {
                           val var7: java.lang.String = it.getString()
                           if (var7 != null) {
                              var8 = StringsKt.contains(var7, "gtocore.lang.template.${NewDataAttributes.ALLOW_MULTI_RECIPE_PARALLEL.getKey()}")
                              return@label44
                           }
                        }

                        var8 = false
                     }

                     if (var8) {
                        var9 = true
                        break
                     }
                  }
               }
            } else {
               var9 = false
            }

            var9
         },
         { s: Supplier ->
            val var10000: java.util.List = s.get() as java.util.List
            var var9: Boolean
            if (var10000 != null) {
               val `$this$any$iv`: java.lang.Iterable = var10000
               if (var10000 is java.util.Collection && (var10000 as java.util.Collection).isEmpty()) {
                  var9 = false
               } else {
                  val var3: java.util.Iterator = `$this$any$iv`.iterator()

                  while (true) {
                     if (!var3.hasNext()) {
                        var9 = false
                        break
                     }

                     run label44@{
                        val it: Component = var3.next() as Component
                        if (it != null) {
                           val var7: java.lang.String = it.getString()
                           if (var7 != null) {
                              var8 = StringsKt.contains(var7, "gtocore.lang.template.${NewDataAttributes.NOT_ALLOW_SHARED.getKey()}")
                              return@label44
                           }
                        }

                        var8 = false
                     }

                     if (var8) {
                        var9 = true
                        break
                     }
                  }
               }
            } else {
               var9 = false
            }

            var9
         },
         { s: Supplier ->
            val var10000: java.util.List = s.get() as java.util.List
            var var9: Boolean
            if (var10000 != null) {
               val `$this$any$iv`: java.lang.Iterable = var10000
               if (var10000 is java.util.Collection && (var10000 as java.util.Collection).isEmpty()) {
                  var9 = false
               } else {
                  val var3: java.util.Iterator = `$this$any$iv`.iterator()

                  while (true) {
                     if (!var3.hasNext()) {
                        var9 = false
                        break
                     }

                     run label44@{
                        val it: Component = var3.next() as Component
                        if (it != null) {
                           val var7: java.lang.String = it.getString()
                           if (var7 != null) {
                              var8 = StringsKt.contains(var7, "gtocore.lang.template.${NewDataAttributes.RECIPES_TYPE.getKey()}")
                              return@label44
                           }
                        }

                        var8 = false
                     }

                     if (var8) {
                        var9 = true
                        break
                     }
                  }
               }
            } else {
               var9 = false
            }

            var9
         }
      )
      Companion.sortByConditions(this.tooltips, Arrays.copyOf(conditions, conditions.length))
      return this
   }

   public fun addTooltip(supplier: Supplier<List<Component?>?>): TooltipsSortedWrapper {
      return this.add(supplier, false)
   }

   public fun addTooltip(components: List<Component>): TooltipsSortedWrapper {
      return this.addTooltip({ 
         `$components`
      })
   }

   public fun addTooltip(component: Component): TooltipsSortedWrapper {
      return this.addTooltip(CollectionsKt.listOf(component))
   }

   public fun addSupplier(component: Supplier<List<Component?>?>): TooltipsSortedWrapper {
      return this.add(component, true)
   }

   public fun getTooltips(): List<Supplier<List<Component?>?>> {
      return this.tooltips
   }

   public companion object {
      private fun <T> MutableList<T>.sortByConditions(vararg conditions: (T) -> Boolean) {
         CollectionsKt.sortWith(`$this$sortByConditions`, TooltipsSortedWrapper$Companion$sortByConditions$$inlined$compareBy$1(conditions))
      }
   }

   private class SupplierWrapper(wrapper: TooltipsSortedWrapper, components: Supplier<List<Component?>?>, supplier: Boolean) :
      Supplier<java.util.List<out Component>> {
      private final val wrapper: TooltipsSortedWrapper
      private final val components: Supplier<List<Component?>?>
      private final val supplier: Boolean

      init {
         this.wrapper = wrapper
         this.components = components
         this.supplier = supplier
      }

      public open fun get(): List<Component?>? {
         if (this.wrapper.isInitialized) {
            return this.components.get()
         } else {
            return if (this.supplier) CollectionsKt.emptyList() else this.components.get()
         }
      }
   }
}
