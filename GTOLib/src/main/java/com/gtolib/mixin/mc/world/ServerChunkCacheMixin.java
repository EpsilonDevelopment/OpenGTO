package com.gtolib.mixin.mc.world;

import com.gregtechceu.gtceu.api.GTValues;
import com.gtolib.mc.IChunkMap;
import com.gtolib.mc.ILevel;
import java.util.function.BooleanSupplier;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LocalMobCapCalculator;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.GameRules.BooleanValue;
import net.minecraft.world.level.GameRules.IntegerValue;
import net.minecraft.world.level.GameRules.Key;
import net.minecraft.world.level.NaturalSpawner.ChunkGetter;
import net.minecraft.world.level.NaturalSpawner.SpawnState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerChunkCache.class, priority = 0)
public abstract class ServerChunkCacheMixin {
   @Shadow
   @Final
   public ChunkMap chunkMap;
   @Shadow
   @Final
   public ServerLevel level;
   @Unique
   private final int gtolib$offset = GTValues.RNG.nextInt(200);

   @Redirect(
      method = "tickChunks",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/level/NaturalSpawner;createState(ILjava/lang/Iterable;Lnet/minecraft/world/level/NaturalSpawner$ChunkGetter;Lnet/minecraft/world/level/LocalMobCapCalculator;)Lnet/minecraft/world/level/NaturalSpawner$SpawnState;"
      )
   )
   private SpawnState createSpawnerState(int var1, Iterable<Entity> var2, ChunkGetter var3, LocalMobCapCalculator var4) {
      return ((ILevel)this.level).gtolib$isVoid() ? null : NaturalSpawner.createState(var1, var2, var3, var4);
   }

   @Redirect(
      method = "tickChunks",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z")
   )
   private boolean tickChunks(GameRules var1, Key<BooleanValue> var2) {
      return ((ILevel)this.level).gtolib$isVoid() ? false : var1.getBoolean(var2);
   }

   @Redirect(
      method = "tickChunks",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules;getInt(Lnet/minecraft/world/level/GameRules$Key;)I")
   )
   private int getInt(GameRules var1, Key<IntegerValue> var2) {
      return ((ILevel)this.level).gtolib$isVoid() ? 600 : 30;
   }

   @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkMap;tick(Ljava/util/function/BooleanSupplier;)V"))
   private void tick(BooleanSupplier var1, boolean var2, CallbackInfo var3) {
      ((IChunkMap)this.chunkMap).gtolib$setSkip(false);
      if (var2 && this.level.getServer().getTickCount() % 80 != 0) {
         ((IChunkMap)this.chunkMap).gtolib$setSkip(true);
      }
   }

   @Inject(method = "tickChunks", at = @At("HEAD"), cancellable = true)
   private void tickChunks(CallbackInfo var1) {
      int var2 = this.level.getServer().getTickCount() + this.gtolib$offset;
      boolean var3 = ((ILevel)this.level).gtolib$isVoid();
      if (var2 % (var3 ? 200 : 10) != 0) {
         var1.cancel();
         if (!this.level.players().isEmpty()) {
            this.chunkMap.visibleChunkMap.values().forEach(var0 -> {
               LevelChunk var1x = var0.getTickingChunk();
               if (var1x != null) {
                  var0.broadcastChanges(var1x);
               }
            });
            this.chunkMap.tick();
         }
      }
   }
}
