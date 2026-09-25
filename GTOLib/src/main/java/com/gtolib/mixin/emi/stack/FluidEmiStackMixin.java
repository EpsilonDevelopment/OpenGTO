package com.gtolib.mixin.emi.stack;

import appeng.api.stacks.IdentityTag;
import com.gtolib.emi.IEmiStack;
import com.gtolib.utils.NbtHolder;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.FluidEmiStack;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FluidEmiStack.class, priority = 5000)
public abstract class FluidEmiStackMixin extends EmiStack implements IEmiStack {
   @Mutable
   @Shadow(remap = false)
   @Final
   private CompoundTag nbt;
   @Unique
   private Supplier<IdentityTag> gtolib$uniqueNbt;

   @Inject(method = "<init>(Lnet/minecraft/world/level/material/Fluid;Lnet/minecraft/nbt/CompoundTag;J)V", at = @At("TAIL"), remap = false)
   private void gtolib$init(Fluid var1, CompoundTag var2, long var3, CallbackInfo var5) {
      this.gtolib$uniqueNbt = NbtHolder.of(this.nbt);
   }

   @Inject(method = "copy()Ldev/emi/emi/api/stack/EmiStack;", at = @At("RETURN"), remap = false)
   private void gtolib$copy(CallbackInfoReturnable<EmiStack> var1) {
      ((FluidEmiStackMixin)var1.getReturnValue()).gtolib$uniqueNbt = this.gtolib$uniqueNbt;
   }

   @Override
   public Supplier<IdentityTag> getUniqueNbt() {
      return this.gtolib$uniqueNbt;
   }
}
