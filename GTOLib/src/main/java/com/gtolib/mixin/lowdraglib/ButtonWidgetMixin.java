package com.gtolib.mixin.lowdraglib;

import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import java.util.function.Consumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ButtonWidget.class)
public abstract class ButtonWidgetMixin extends Widget {
   @Shadow(remap = false)
   protected boolean isClicked;
   @Shadow(remap = false)
   protected Consumer<ClickData> onPressCallback;

   protected ButtonWidgetMixin(Position var1, Size var2) {
      super(var1, var2);
   }

   @Override
   public boolean mouseClicked(double var1, double var3, int var5) {
      if (this.isMouseOverElement(var1, var3)) {
         this.isClicked = true;
         ClickData var6 = new ClickData();
         this.writeClientAction(1, var6::writeToBuf);
         if (this.onPressCallback != null) {
            this.onPressCallback.accept(var6);
         }

         playButtonClickSound();
         return true;
      } else {
         return false;
      }
   }
}
