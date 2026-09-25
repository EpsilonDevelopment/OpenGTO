package com.gtolib.mixin.mc.entity;

import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = Monster.class, priority = 0)
public class MonsterMixin {
   @Redirect(
      method = "checkMonsterSpawnRules",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ServerLevelAccessor;getDifficulty()Lnet/minecraft/world/Difficulty;")
   )
   private static Difficulty checkMonsterSpawnRules(ServerLevelAccessor var0) {
      return Difficulty.HARD;
   }

   @Redirect(
      method = "checkAnyLightMonsterSpawnRules",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelAccessor;getDifficulty()Lnet/minecraft/world/Difficulty;")
   )
   private static Difficulty checkAnyLightMonsterSpawnRules(LevelAccessor var0) {
      return Difficulty.HARD;
   }
}
