package com.gtolib.mixin.mc;

import com.kyanite.deeperdarker.content.DDBlocks;
import earth.terrarium.adastra.common.registry.ModBlocks;
import java.util.List;
import java.util.Set;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration.TargetBlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = OreConfiguration.class, priority = 0)
public class OreConfigurationMixin {
   @Unique
   private static Set<Block> EXCLUDED_ORE;

   @ModifyVariable(method = "<init>(Ljava/util/List;IF)V", at = @At("HEAD"), index = 1, argsOnly = true)
   private static List<TargetBlockState> gtolib$init(List<TargetBlockState> var0) {
      if (EXCLUDED_ORE == null) {
         EXCLUDED_ORE = Set.of(
            Blocks.COAL_ORE,
            Blocks.DEEPSLATE_COAL_ORE,
            Blocks.COPPER_ORE,
            Blocks.DEEPSLATE_COPPER_ORE,
            Blocks.IRON_ORE,
            Blocks.DEEPSLATE_IRON_ORE,
            Blocks.GOLD_ORE,
            Blocks.DEEPSLATE_GOLD_ORE,
            Blocks.NETHER_GOLD_ORE,
            Blocks.DIAMOND_ORE,
            Blocks.DEEPSLATE_DIAMOND_ORE,
            Blocks.EMERALD_ORE,
            Blocks.DEEPSLATE_EMERALD_ORE,
            Blocks.LAPIS_ORE,
            Blocks.DEEPSLATE_LAPIS_ORE,
            Blocks.REDSTONE_ORE,
            Blocks.DEEPSLATE_REDSTONE_ORE,
            Blocks.NETHER_QUARTZ_ORE,
            ModBlocks.MOON_DESH_ORE.get(),
            ModBlocks.DEEPSLATE_DESH_ORE.get(),
            ModBlocks.MOON_IRON_ORE.get(),
            ModBlocks.MOON_ICE_SHARD_ORE.get(),
            ModBlocks.DEEPSLATE_ICE_SHARD_ORE.get(),
            ModBlocks.MARS_IRON_ORE.get(),
            ModBlocks.MARS_DIAMOND_ORE.get(),
            ModBlocks.MARS_OSTRUM_ORE.get(),
            ModBlocks.DEEPSLATE_OSTRUM_ORE.get(),
            ModBlocks.MARS_ICE_SHARD_ORE.get(),
            ModBlocks.MERCURY_IRON_ORE.get(),
            ModBlocks.VENUS_COAL_ORE.get(),
            ModBlocks.VENUS_GOLD_ORE.get(),
            ModBlocks.VENUS_DIAMOND_ORE.get(),
            ModBlocks.VENUS_CALORITE_ORE.get(),
            ModBlocks.DEEPSLATE_CALORITE_ORE.get(),
            ModBlocks.GLACIO_ICE_SHARD_ORE.get(),
            ModBlocks.GLACIO_COAL_ORE.get(),
            ModBlocks.GLACIO_COPPER_ORE.get(),
            ModBlocks.GLACIO_IRON_ORE.get(),
            ModBlocks.GLACIO_LAPIS_ORE.get(),
            DDBlocks.SCULK_STONE_COAL_ORE.get(),
            DDBlocks.SCULK_STONE_IRON_ORE.get(),
            DDBlocks.SCULK_STONE_COPPER_ORE.get(),
            DDBlocks.SCULK_STONE_GOLD_ORE.get(),
            DDBlocks.SCULK_STONE_REDSTONE_ORE.get(),
            DDBlocks.SCULK_STONE_EMERALD_ORE.get(),
            DDBlocks.SCULK_STONE_LAPIS_ORE.get(),
            DDBlocks.SCULK_STONE_DIAMOND_ORE.get(),
            DDBlocks.GLOOMSLATE_COAL_ORE.get(),
            DDBlocks.GLOOMSLATE_IRON_ORE.get(),
            DDBlocks.GLOOMSLATE_COPPER_ORE.get(),
            DDBlocks.GLOOMSLATE_GOLD_ORE.get(),
            DDBlocks.GLOOMSLATE_REDSTONE_ORE.get(),
            DDBlocks.GLOOMSLATE_EMERALD_ORE.get(),
            DDBlocks.GLOOMSLATE_LAPIS_ORE.get(),
            DDBlocks.GLOOMSLATE_DIAMOND_ORE.get(),
            mythicbotany.register.ModBlocks.elementiumOre,
            mythicbotany.register.ModBlocks.dragonstoneOre,
            mythicbotany.register.ModBlocks.goldOre
         );
      }

      return var0.stream().filter(var0x -> !EXCLUDED_ORE.contains(var0x.state.getBlock())).toList();
   }
}
