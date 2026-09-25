package com.gtolib.mixin.lowdraglib;

import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import java.text.NumberFormat;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@OnlyIn(Dist.CLIENT)
@Mixin(value = TextFieldWidget.class, remap = false)
public abstract class TextFieldWidgetMixin {
   @Shadow
   protected NumberFormat numberInstance;

   @Inject(method = "setWheelDur(F)Lcom/lowdragmc/lowdraglib/gui/widget/TextFieldWidget;", at = @At("RETURN"))
   private void gtolib$disableGroupingOnSetWheelDur(float var1, CallbackInfoReturnable<TextFieldWidget> var2) {
      if (this.numberInstance != null) {
         this.numberInstance.setGroupingUsed(false);
      }
   }

   @Inject(method = "setWheelDur(IF)Lcom/lowdragmc/lowdraglib/gui/widget/TextFieldWidget;", at = @At("RETURN"))
   private void gtolib$disableGroupingOnSetWheelDurWithDigits(int var1, float var2, CallbackInfoReturnable<TextFieldWidget> var3) {
      if (this.numberInstance != null) {
         this.numberInstance.setGroupingUsed(false);
      }
   }

   @ModifyVariable(method = "mouseDragged(DDIDD)Z", at = @At("HEAD"), argsOnly = true, ordinal = 2)
   private double gtolib$ondragX(double var1) {
      return (long)var1;
   }
}
