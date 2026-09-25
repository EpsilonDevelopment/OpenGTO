package com.gtolib.mixin.emi.apiimpl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import dev.emi.emi.platform.forge.EmiAgnosForge;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EmiAgnosForge.class)
public class EmiAgnosForgeMixin {
   @Inject(method = "getFluidTooltipAgnos", at = @At("RETURN"), cancellable = true, remap = false)
   protected void getFluidTooltip(Fluid var1, CompoundTag var2, CallbackInfoReturnable<List<Component>> var3) {
      Builder var4 = ImmutableList.builder();
      var4.addAll((Iterable)var3.getReturnValue());
      if (Minecraft.getInstance().options.advancedItemTooltips) {
         String var5 = var1.getFluidType().toString();
         var4.add(Component.literal(var5).withStyle(ChatFormatting.DARK_GRAY));
      }

      var3.setReturnValue(var4.build());
   }
}
