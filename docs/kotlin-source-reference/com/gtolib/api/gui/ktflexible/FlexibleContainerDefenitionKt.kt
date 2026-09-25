package com.gtolib.api.gui.ktflexible

import com.lowdragmc.lowdraglib.utils.Size

public fun Size.check(): Size {
   if (`$this$check`.width >= 0 && `$this$check`.height >= 0) {
      return `$this$check`
   } else {
      throw IllegalArgumentException("Flexible Container : size can not be negative")
   }
}
