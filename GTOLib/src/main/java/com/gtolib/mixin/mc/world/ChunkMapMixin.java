package com.gtolib.mixin.mc.world;

import com.gtolib.mc.IChunkMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.util.function.BooleanSupplier;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ChunkMap.class, priority = 0)
public abstract class ChunkMapMixin implements IChunkMap {
   @Shadow
   public volatile Long2ObjectLinkedOpenHashMap<ChunkHolder> visibleChunkMap;
   @Unique
   private boolean gtolib$skip;

   @Inject(
      method = "tick(Ljava/util/function/BooleanSupplier;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkMap;processUnloads(Ljava/util/function/BooleanSupplier;)V"),
      cancellable = true
   )
   private void tick(BooleanSupplier var1, CallbackInfo var2) {
      if (this.gtolib$skip) {
         this.gtolib$skip = false;
         var2.cancel();
      }
   }

   @Override
   public void gtolib$setSkip(boolean var1) {
      this.gtolib$skip = var1;
   }

   @Overwrite
   public Iterable<ChunkHolder> getChunks() {
      return this.visibleChunkMap.values();
   }
}
