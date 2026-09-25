package com.gtolib.api.gui.ktflexible

import com.google.common.collect.ImmutableList
import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture
import com.lowdragmc.lowdraglib.gui.texture.TextTexture
import com.lowdragmc.lowdraglib.gui.util.ClickData
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget
import com.lowdragmc.lowdraglib.gui.widget.TextTextureWidget
import com.lowdragmc.lowdraglib.gui.widget.Widget
import java.util.function.Consumer
import java.util.function.Supplier
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.Component

public fun LayoutBuilder<*>.button(
   width: Int = 40,
   height: Int = 16,
   text: Supplier<String>? = null,
   transKey: String? = null,
   onClick: ((ClickData) -> Unit)? = null,
   onSimpleClick: (() -> Unit)? = null
): Widget {
   var var10000: Supplier = text
   if (text == null) {
      var10000 = if (transKey != null) { 
         Component.translatable(`$transKey`).getString()
      } else { 
         "Button"
      }
   }

   val textSup: Supplier = var10000
   val button: ButtonWidget = object : ButtonWidget {
      public final var lastText: String
         internal set

      public final var firstUpdate: Boolean
         internal set

      {
         val var10001: Any = textSup.get()
         this.lastText = var10001 as java.lang.String
         this.firstUpdate = true
      }

      public open fun detectAndSendChanges() {
         super.detectAndSendChanges()
         if (!(this.lastText == textSup.get()) || this.firstUpdate) {
            this.firstUpdate = false
            this.writeUpdateInfo(945, { buf: FriendlyByteBuf ->
               buf.writeUtf(`$textSup`.get() as java.lang.String)
            })
            val var10001: Any = textSup.get()
            this.lastText = var10001 as java.lang.String
         }
      }

      public open fun readUpdateInfo(id: Int, buffer: FriendlyByteBuf?) {
         if (id == 945 && buffer != null) {
            val newText: java.lang.String = buffer.readUtf()
            this.setButtonTexture(arrayOf(GuiTextureGroup(arrayOf(GuiTextures.BUTTON, TextTexture({ 
               `$newText`
            })))))
            this.setClickedTexture(arrayOf(GuiTextureGroup(arrayOf(GuiTextures.SLOT, TextTexture({ 
               `$newText`
            })))))
         } else {
            super.readUpdateInfo(id, buffer)
         }
      }

      public open fun updateScreen() {
         super.updateScreen()
         if (this.isClientSideWidget && !(this.lastText == textSup.get())) {
            this.setButtonTexture(arrayOf(GuiTextureGroup(arrayOf(GuiTextures.BUTTON, TextTexture({ 
               `$textSup`.get() as java.lang.String
            })))))
            val var10001: Any = textSup.get()
            this.lastText = var10001 as java.lang.String
         }
      }
   }.setClickedTexture(arrayOf(GuiTextureGroup(arrayOf(GuiTextures.SLOT, TextTexture({ 
      `$textSup`.get() as java.lang.String
   })))))
   return `$this$button`.widget(button as Widget)
}

public fun LayoutBuilder<*>.iconButton(
   width: Int = 16,
   height: Int = 16,
   tooltips: Supplier<Component>? = null,
   icon: IGuiTexture? = null,
   onClick: ((ClickData) -> Unit)? = null,
   onSimpleClick: (() -> Unit)? = null
): Widget {
   val button: ButtonWidget = object : ButtonWidget {
      public final var lastTooltip: Component? = if (tooltips != null) tooltips.get() as Component else null
         internal set

      public final var firstUpdate: Boolean = true
         internal set

      public open fun detectAndSendChanges() {
         super.detectAndSendChanges()
         if (tooltips != null) {
            if (this.lastTooltip != null) {
               val var2: Supplier = tooltips
               if (!(this.lastTooltip.getString() == (var2.get() as Component).getString()) || this.firstUpdate) {
                  this.firstUpdate = false
                  this.lastTooltip = var2.get() as Component
                  this.writeUpdateInfo(946, { buf: FriendlyByteBuf ->
                     val var10001: Component = `this$0`.lastTooltip
                     buf.writeComponent(var10001)
                  })
               }

               val var10001: Component = this.lastTooltip
               this.setHoverTooltips(ImmutableList.of(var10001) as java.util.List)
            }
         }
      }

      public open fun readUpdateInfo(id: Int, buffer: FriendlyByteBuf?) {
         if (id == 946 && buffer != null) {
            this.lastTooltip = buffer.readComponent()
            this.setHoverTooltips(arrayOf(this.lastTooltip))
         } else {
            super.readUpdateInfo(id, buffer)
         }
      }
   }.setClickedTexture(arrayOf(if (icon != null) GuiTextureGroup(arrayOf(GuiTextures.SLOT, icon)) else GuiTextureGroup(arrayOf(GuiTextures.SLOT))))
   if (tooltips != null) {
      button.setHoverTooltips(arrayOf(tooltips.get()))
   }

   return `$this$iconButton`.widget(button as Widget)
}

public fun LayoutBuilder<*>.button(
   width: Int = 40,
   height: Int = 16,
   transKet: String = "unKnownTranslateKet",
   onClick: (ClickData) -> Unit = { it: ClickData ->
         Unit.INSTANCE
      }
) {
   `$this$button`.button(width, height, transKey = transKet, onClick = onClick)
}

public fun LayoutBuilder<*>.text(width: Int = 80, height: Int = 10, text: Supplier<Component> = { 
      Component.literal("Text") as Component
   }, init: (TextTextureWidget) -> Unit = { var0: TextTextureWidget ->
      Unit.INSTANCE
   }): Widget {
   val var6: TextTextureWidget = TextTextureWidget(0, 0, width, height)
   var6.setText(text)
   init(var6)
   return `$this$text`.widget(var6 as Widget)
}

public fun LayoutBuilder<*>.blank(width: Int = 0, height: Int = 0) {
   `$this$blank`.widget(Widget(0, 0, width, height))
}

public fun LayoutBuilder<*>.field(width: Int = 50, height: Int = 16, getter: Supplier<String>, setter: Consumer<String>, rightClickClear: Boolean = false): TextFieldWidget {
   val field: TextFieldWidget = object : TextFieldWidget {
      public open fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
         if (rightClickClear && button == 1 && this.isMouseOverElement(mouseX, mouseY)) {
            this.textField.setValue("")
            return true
         } else {
            return super.mouseClicked(mouseX, mouseY, button)
         }
      }
   }
   `$this$field`.vBox(width, { $this$vBox: Style ->
      `$this$vBox`.spacing = 0
      Unit.INSTANCE
   }, init = { $this$vBox: VBoxBuilder ->
      blank(`$this$vBox`, 1, 1)
      `$this$vBox`.widget(`$field` as Widget)
      blank(`$this$vBox`, 1, 1)
      Unit.INSTANCE
   }, 4, null)
   return field
}
