package com.gtolib.mixin.emi.stack;

import appeng.api.stacks.AmountFormat;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.IdentityTag;
import appeng.client.gui.me.common.StackSizeRenderer;
import appeng.integration.modules.emi.EmiStackHelper;
import com.gtolib.emi.IEmiStack;
import com.gtolib.utils.NbtHolder;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ItemEmiStack;
import dev.emi.emi.runtime.EmiDrawContext;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEmiStack.class)
public abstract class ItemEmiStackMixin extends EmiStack implements IEmiStack {
   @Mutable
   @Shadow(remap = false)
   @Final
   private CompoundTag nbt;
   @Unique
   private Supplier<IdentityTag> gtolib$uniqueNbt;

   @Inject(method = "<init>(Lnet/minecraft/world/item/Item;Lnet/minecraft/nbt/CompoundTag;J)V", at = @At("TAIL"), remap = false)
   private void gtolib$init(Item var1, CompoundTag var2, long var3, CallbackInfo var5) {
      this.gtolib$uniqueNbt = NbtHolder.of(this.nbt);
   }

   @Inject(method = "copy()Ldev/emi/emi/api/stack/EmiStack;", at = @At("RETURN"), remap = false)
   private void gtolib$copy(CallbackInfoReturnable<EmiStack> var1) {
      ((ItemEmiStackMixin)var1.getReturnValue()).gtolib$uniqueNbt = this.gtolib$uniqueNbt;
   }

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
      if (this.amount > 99L) {
         GenericStack var5 = EmiStackHelper.toGenericStack(this);
         if (var5 != null) {
            String var6 = var5.what().formatAmount(var5.amount(), AmountFormat.SLOT);
            StackSizeRenderer.renderSizeLabel(var1.raw(), Minecraft.getInstance().font, var2 + 1, var3 + 1, var6, false);
            return;
         }
      }

      EmiRenderHelper.renderAmount(var1, var2, var3, var4);
   }

   @Override
   public Supplier<IdentityTag> getUniqueNbt() {
      return this.gtolib$uniqueNbt;
   }
}
