package com.gtolib.mixin.mc.world;

import com.gtolib.mc.ILevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ServerLevel.class, priority = 0)
public abstract class ServerLevelMixin implements ILevel {
   @Shadow
   @Final
   public PersistentEntitySectionManager<Entity> entityManager;

   @Overwrite
   public boolean isNaturalSpawningAllowed(BlockPos var1) {
      return this.gtolib$isVoid() ? false : this.entityManager.canPositionTick(var1);
   }

   @Overwrite
   public boolean isNaturalSpawningAllowed(ChunkPos var1) {
      return this.gtolib$isVoid() ? false : this.entityManager.canPositionTick(var1);
   }
}
