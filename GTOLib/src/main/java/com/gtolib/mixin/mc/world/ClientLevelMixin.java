package com.gtolib.mixin.mc.world;

import com.gtocore.common.saved.DysonSphereSavaedData;
import com.gtolib.mc.ILevel;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.function.Supplier;
import net.minecraft.client.multiplayer.ClientLevel.ClientLevelData;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientLevel.class, priority = 0)
public abstract class ClientLevelMixin extends Level implements ILevel {
   @Shadow
   @Final
   private ClientLevelData clientLevelData;
   @Unique
   private LongSet gtolib$preventUpdate;

   private ClientLevelMixin(
      WritableLevelData var1,
      ResourceKey<Level> var2,
      RegistryAccess var3,
      Holder<DimensionType> var4,
      Supplier<ProfilerFiller> var5,
      boolean var6,
      boolean var7,
      long var8,
      int var10
   ) {
      super(var1, var2, var3, var4, var5, var6, var7, var8, var10);
   }

   @Override
   public LongSet gtolib$getPreventUpdate() {
      if (this.gtolib$preventUpdate == null) {
         this.gtolib$preventUpdate = new LongOpenHashSet();
      }

      return this.gtolib$preventUpdate;
   }

   @Override
   public boolean setBlock(BlockPos var1, BlockState var2, int var3) {
      return this.gtolib$preventUpdate != null && this.gtolib$preventUpdate.contains(var1.asLong()) ? false : this.setBlock(var1, var2, var3, 512);
   }

   @Inject(method = "setDayTime", at = @At("HEAD"), cancellable = true)
   private void setDayTime(long var1, CallbackInfo var3) {
      if (DysonSphereSavaedData.getDimensionLaunchData(this.dimension()) > 100) {
         long var4 = this.clientLevelData.getDayTime();
         this.clientLevelData.setDayTime(var4 + (24000L - var4 % 24000L + 18000L) % 24000L);
         var3.cancel();
      }
   }
}
