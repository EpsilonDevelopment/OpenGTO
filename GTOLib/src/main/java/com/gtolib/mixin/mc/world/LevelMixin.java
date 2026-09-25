package com.gtolib.mixin.mc.world;

import com.gtolib.api.beam.BeamManager;
import com.gtolib.api.data.GTODimensions;
import com.gtolib.mc.ILevel;
import com.llamalad7.mixinextras.lib.apache.commons.ObjectUtils;
import earth.terrarium.adastra.api.planets.Planet;
import earth.terrarium.adastra.api.planets.PlanetApi;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.WritableLevelData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Level.class, priority = 0)
public abstract class LevelMixin implements ILevel {
   @Shadow
   @Final
   private ResourceKey<Level> dimension;
   @Unique
   private Reference2ReferenceOpenHashMap<Class, ReferenceSet> machineNet;
   @Unique
   private Object gtolib$planet;
   @Unique
   private boolean gtolib$isVoid;

   @Inject(method = "<init>", at = @At("TAIL"))
   private void init(
      WritableLevelData var1,
      ResourceKey var2,
      RegistryAccess var3,
      Holder var4,
      Supplier var5,
      boolean var6,
      boolean var7,
      long var8,
      int var10,
      CallbackInfo var11
   ) {
      this.gtolib$isVoid = GTODimensions.isVoid(var2) || PlanetApi.API.isSpace(var2);
   }

   @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", at = @At("RETURN"))
   private void gtolib$invalidateRayBeams(BlockPos var1, BlockState var2, int var3, int var4, CallbackInfoReturnable<Boolean> var5) {
      if (var5.getReturnValueZ()) {
         BeamManager.onBlockChanged((Level)(Object)this, var1);
      }
   }

   @Override
   public Reference2ReferenceOpenHashMap<Class, ReferenceSet> gtolib$getMachineNet() {
      if (this.machineNet == null) {
         this.machineNet = new Reference2ReferenceOpenHashMap<>();
      }

      return this.machineNet;
   }

   @Nullable
   @Override
   public Planet gtolib$getPlanet() {
      if (this.gtolib$planet == null) {
         this.gtolib$planet = PlanetApi.API.getPlanet(this.dimension);
         if (this.gtolib$planet == null) {
            this.gtolib$planet = ObjectUtils.NULL;
         }
      }

      return this.gtolib$planet == ObjectUtils.NULL ? null : (Planet)this.gtolib$planet;
   }

   @Override
   public boolean gtolib$isVoid() {
      return this.gtolib$isVoid;
   }
}
