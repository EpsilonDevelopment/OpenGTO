package com.gtolib.ae2.crafting2.utils

public enum class DisplayLevel {
   MAIN,
   SECONDARY,
   DETAIL,
   NONE;

   public fun zhLabel(): String {
      var var10000: java.lang.String
      when (this) {
         DisplayLevel.MAIN -> var10000 = "主要"
         DisplayLevel.SECONDARY -> var10000 = "次要"
         DisplayLevel.DETAIL -> var10000 = "详情"
         DisplayLevel.NONE -> var10000 = "不打印输出"
         else -> throw NoWhenBranchMatchedException()
      }

      return var10000
   }
}
