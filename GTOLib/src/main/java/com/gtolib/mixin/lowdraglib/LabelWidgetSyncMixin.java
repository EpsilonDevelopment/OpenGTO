package com.gtolib.mixin.lowdraglib;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = LabelWidget.class, remap = false)
public abstract class LabelWidgetSyncMixin extends Widget {
   @Shadow
   @Nullable
   protected Component component;
   @Shadow
   private String lastTextValue;

   public LabelWidgetSyncMixin(Position var1, Size var2) {
      super(var1, var2);
   }

   @Overwrite
   @Override
   public void readInitialData(FriendlyByteBuf var1) {
      super.readInitialData(var1);
      if (var1.readBoolean()) {
         this.component = var1.readComponent();
         this.lastTextValue = this.component.getString();
      } else {
         this.component = null;
         this.lastTextValue = var1.readUtf();
      }
   }
}
