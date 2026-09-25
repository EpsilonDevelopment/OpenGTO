package com.gtolib.mixin.apotheosis;

import dev.shadowsoffire.apotheosis.Apoth.Enchantments;
import dev.shadowsoffire.apotheosis.adventure.Adventure.Items;
import dev.shadowsoffire.apotheosis.spawn.enchantment.CapturingEnchant;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(CapturingEnchant.class)
public class CapturingEnchantMixin {
   @Overwrite(remap = false)
   public void handleCapturing(LivingDropsEvent var1) {
      if (var1.getSource().getEntity() instanceof Player var3) {
         int var4 = var3.getMainHandItem().getEnchantmentLevel(Enchantments.CAPTURING.get());
         LivingEntity var5 = var1.getEntity();
         boolean var6 = var5.getPersistentData().getBoolean("apoth.boss");
         if (var6 && var5.level().random.nextFloat() < var4 / 80.0F) {
            Item var7 = Items.BOSS_SUMMONER.get();
            var1.getDrops().add(new ItemEntity(var5.level(), var5.getX(), var5.getY(), var5.getZ(), new ItemStack(var7, 1)));
         }

         if (var5.level().random.nextFloat() < var4 / 40.0F) {
            SpawnEggItem var9 = ForgeSpawnEggItem.fromEntityType(var5.getType());
            if (var9 == null) {
               return;
            }

            ItemStack var8 = new ItemStack(var9);
            var1.getDrops().add(new ItemEntity(var5.level(), var5.getX(), var5.getY(), var5.getZ(), var8));
         }
      }
   }
}
