package com.gtolib.mixin.apotheosis;

import com.gregtechceu.gtceu.api.item.IGTTool;
import com.llamalad7.mixinextras.sugar.Local;
import dev.shadowsoffire.apotheosis.ench.enchantments.twisted.MinersFervorEnchant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinersFervorEnchant.class)
public class MinersFervorEnchantMixin {
   @Inject(method = "breakSpeed", at = @At("HEAD"), cancellable = true, remap = false)
   public void invokeGetExperienceForLevel(BreakSpeed var1, CallbackInfo var2) {
      Player var3 = var1.getEntity();
      ItemStack var4 = var3.getMainHandItem();
      if (var4.getItem() instanceof IGTTool var5
         && var5.getToolType().name.contains("_vajra")
         && !var4.getOrCreateTag().getBoolean("MinersFervor")
         && var4.getOrCreateTag().getFloat("ToolSpeed") != 0.0F) {
         var1.setNewSpeed(var4.getOrCreateTag().getFloat("ToolSpeed"));
         var2.cancel();
      }
   }

   @Redirect(
      method = "breakSpeed",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getEnchantmentLevel(Lnet/minecraft/world/item/enchantment/Enchantment;)I"),
      remap = false
   )
   public int redirectGetExperienceForLevel(ItemStack var1, Enchantment var2, @Local Player var3) {
      return var1.getItem() instanceof IGTTool var4 && var4.getToolType().name.contains("_vajra") && var1.getOrCreateTag().getBoolean("MinersFervor")
         ? 5
         : var1.getEnchantmentLevel(var2);
   }
}
