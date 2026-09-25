package com.gtolib.mixin.apotheosis;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gtocore.common.data.GTOOres;
import dev.shadowsoffire.apotheosis.ench.enchantments.masterwork.EarthsBoonEnchant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags.Blocks;
import net.minecraftforge.event.level.BlockEvent.BreakEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(EarthsBoonEnchant.class)
public class EarthsBoonEnchantMixin {
   @Overwrite(remap = false)
   public void provideBenefits(BreakEvent var1) {
      Player var2 = var1.getPlayer();
      if (!var2.level().isClientSide) {
         ItemStack var3 = var2.getMainHandItem();
         int var4 = var3.getEnchantmentLevel((EarthsBoonEnchant)(Object)this);
         if (var1.getState().is(Blocks.STONE) && var4 > 0 && var2.getRandom().nextFloat() <= 0.01F * var4) {
            Block.popResource(var2.level(), var1.getPos(), ChemicalHelper.get(TagPrefix.rawOre, GTOOres.selectMaterial(var2.level().dimension())));
         }
      }
   }
}
