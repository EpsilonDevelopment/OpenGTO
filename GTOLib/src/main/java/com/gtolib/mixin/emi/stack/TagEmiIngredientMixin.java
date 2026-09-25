package com.gtolib.mixin.emi.stack;

import appeng.api.stacks.AmountFormat;
import appeng.api.stacks.GenericStack;
import appeng.client.gui.me.common.StackSizeRenderer;
import appeng.integration.modules.emi.EmiStackHelper;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gtocore.common.data.GTOItems;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.TagEmiIngredient;
import dev.emi.emi.runtime.EmiDrawContext;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TagEmiIngredient.class)
public abstract class TagEmiIngredientMixin {
   @Shadow(remap = false)
   @Final
   public TagKey<?> key;

   @Shadow(remap = false)
   public abstract long getAmount();

   @Shadow(remap = false)
   public abstract List<EmiStack> getEmiStacks();

   @Redirect(
      method = "render",
      at = @At(
         value = "INVOKE",
         target = "Ldev/emi/emi/EmiRenderHelper;renderAmount(Ldev/emi/emi/runtime/EmiDrawContext;IILnet/minecraft/network/chat/Component;)V",
         remap = false
      ),
      remap = false
   )
   private void gtolib$renderAmount(EmiDrawContext var1, int var2, int var3, Component var4) {
      if (this.getAmount() > 99L) {
         GenericStack var5 = EmiStackHelper.toGenericStack(this.getEmiStacks().getFirst());
         if (var5 != null) {
            String var6 = var5.what().formatAmount(this.getAmount(), AmountFormat.SLOT);
            StackSizeRenderer.renderSizeLabel(var1.raw(), Minecraft.getInstance().font, var2 + 1, var3 + 1, var6, false);
            return;
         }
      }

      EmiRenderHelper.renderAmount(var1, var2, var3, var4);
   }

   @Redirect(method = "render", at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;", remap = false), remap = false)
   private <E> E gtolib$renderFirst(List<E> var1, int var2) {
      int var3 = List.of(CustomTags.CIRCUITS_ARRAY).indexOf(this.key);
      return (E)(var3 != -1 ? EmiStack.of(GTOItems.UNIVERSAL_CIRCUIT[var3]) : var1.get(var2));
   }
}
