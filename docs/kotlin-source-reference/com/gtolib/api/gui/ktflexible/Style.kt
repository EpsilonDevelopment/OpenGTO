package com.gtolib.api.gui.ktflexible

public data class Style(paddingY: Int = 0, paddingLeft: Int = 0, paddingRight: Int = 0, paddingBottom: Int = 0, spacing: Int = 0) {
   public final var paddingY: Int
      internal set

   public final var paddingLeft: Int
      internal set

   public final var paddingRight: Int
      internal set

   public final var paddingBottom: Int
      internal set

   public final var spacing: Int
      internal set

   init {
      this.paddingY = paddingY
      this.paddingLeft = paddingLeft
      this.paddingRight = paddingRight
      this.paddingBottom = paddingBottom
      this.spacing = spacing
   }

   public constructor(init: (Style) -> Unit) : this(0, 0, 0, 0, 0, 31, null) {
      init(this)
   }

   public operator fun component1(): Int {
      return this.paddingY
   }

   public operator fun component2(): Int {
      return this.paddingLeft
   }

   public operator fun component3(): Int {
      return this.paddingRight
   }

   public operator fun component4(): Int {
      return this.paddingBottom
   }

   public operator fun component5(): Int {
      return this.spacing
   }

   public fun copy(
      paddingY: Int = this.paddingY,
      paddingLeft: Int = this.paddingLeft,
      paddingRight: Int = this.paddingRight,
      paddingBottom: Int = this.paddingBottom,
      spacing: Int = this.spacing
   ): Style {
      return Style(paddingY, paddingLeft, paddingRight, paddingBottom, spacing)
   }

   public override fun toString(): String {
      return "Style(paddingY=${this.paddingY}, paddingLeft=${this.paddingLeft}, paddingRight=${this.paddingRight}, paddingBottom=${this.paddingBottom}, spacing=${this.spacing})"
   }

   public override fun hashCode(): Int {
      return (
               ((Integer.hashCode(this.paddingY) * 31 + Integer.hashCode(this.paddingLeft)) * 31 + Integer.hashCode(this.paddingRight)) * 31
                  + Integer.hashCode(this.paddingBottom)
            )
            * 31
         + Integer.hashCode(this.spacing)
      }

   public override operator fun equals(other: Any?): Boolean {
      label46@
      if (this === other) {
         return true
      } else {
         return other is Style
            && this.paddingY == (other as Style).paddingY
            && this.paddingLeft == (other as Style).paddingLeft
            && this.paddingRight == (other as Style).paddingRight
            && this.paddingBottom == (other as Style).paddingBottom
            && this.spacing == (other as Style).spacing
         }
   }

   fun Style() {
      this(0, 0, 0, 0, 0, 31, null)
   }
}
