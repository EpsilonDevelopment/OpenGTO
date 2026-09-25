package com.gtolib.ae2.crafting2.logic

import net.minecraft.network.chat.Component

public class SendComponentToPlayerException(components: List<Component>) : RuntimeException(
      CollectionsKt.joinToString(components, " | ", transform = { it: Component ->
         val var10000: java.lang.String = it.getString()
         var10000 as java.lang.CharSequence
      })
   ) {
   public final val components: List<Component>

   init {
      this.components = components
   }
}
