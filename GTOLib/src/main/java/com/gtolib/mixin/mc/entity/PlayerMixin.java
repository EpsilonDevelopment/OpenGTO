package com.gtolib.mixin.mc.entity;

import com.gtolib.api.player.IEnhancedPlayer;
import com.gtolib.api.player.PlayerData;
import com.gtolib.api.player.attribute.PlayerAttributes;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Player.class, priority = 0)
public abstract class PlayerMixin extends LivingEntity implements IEnhancedPlayer {
   @Shadow
   @Final
   private Abilities abilities;
   @Unique
   private PlayerData gtolib$playerData;

   @Shadow(remap = false)
   public abstract void setForcedPose(@Nullable Pose var1);

   @Inject(method = "<init>", at = @At("TAIL"), remap = false)
   private void gtolib$init(Level var1, BlockPos var2, float var3, GameProfile var4, CallbackInfo var5) {
      this.gtolib$playerData = new PlayerData((Player)(Object)this);
   }

   private PlayerMixin(EntityType<? extends LivingEntity> var1, Level var2) {
      super(var1, var2);
   }

   @Override
   protected int decreaseAirSupply(int var1) {
      int var2 = EnchantmentHelper.getRespiration(this);
      return var2 > 0 && this.random.nextInt(var2 + 1) > 0 ? var1 : var1 - 1;
   }

   @Inject(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;setSharedFlag(IZ)V"))
   private void travel(Vec3 var1, CallbackInfo var2) {
      if (this.xxa == 0.0F && this.zza == 0.0F && this.getPlayerData().getPlayerAttributes().getBooleanCurrent(PlayerAttributes.DISABLE_DRIFT)) {
         this.setDeltaMovement(this.getDeltaMovement().multiply(0.5, 1.0, 0.5));
      }
   }

   @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
   private void readAdditionalSaveData(CompoundTag var1, CallbackInfo var2) {
      this.gtolib$playerData.readAdditionalSaveData(var1);
   }

   @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
   private void addAdditionalSaveData(CompoundTag var1, CallbackInfo var2) {
      this.gtolib$playerData.addAdditionalSaveData(var1);
   }

   @Inject(method = "tick", at = @At("TAIL"))
   private void tickTail(CallbackInfo var1) {
      this.gtolib$playerData.onPlayerTick();
      this.setForcedPose(null);
   }

   @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isSpectator()Z", ordinal = 1))
   private void tickHEAD(CallbackInfo var1) {
      if (this.getPlayerData().getPlayerAttributes().getBooleanCurrent(PlayerAttributes.FREE_MOV_STATE) && this.abilities.flying) {
         this.noPhysics = true;
         this.setForcedPose(Pose.STANDING);
      }
   }

   @Redirect(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;setFoodLevel(I)V"))
   private void gtolib$setFoodLevel(FoodData var1, int var2) {
   }

   @Redirect(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/DamageSource;scalesWithDifficulty()Z"))
   private boolean scalesWithDifficulty(DamageSource var1) {
      return false;
   }

   @Override
   public PlayerData getPlayerData() {
      return this.gtolib$playerData;
   }

   @Override
   public boolean isUnderWater() {
      return this.isInWater() || super.isUnderWater();
   }

   @Override
   public boolean isInWater() {
      return this.getPlayerData().isNoGravity() || super.isInWater();
   }

   @Override
   public boolean canStartSwimming() {
      return this.getPlayerData().isNoGravity() || super.canStartSwimming();
   }

   @Inject(at = @At("TAIL"), method = "tick")
   public void jump(CallbackInfo var1) {
      if (this.getPlayerData().isNoGravity() && this.jumping) {
         this.jumpInFluid(Fluids.WATER.getFluidType());
      }
   }

   @Override
   protected void checkFallDamage(double var1, boolean var3, @NotNull BlockState var4, @NotNull BlockPos var5) {
      super.checkFallDamage(var1, var3, var4, var5);
      if (this.getPlayerData().isNoGravity()) {
         this.resetFallDistance();
      }
   }

   @Redirect(
      method = "playSound",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"
      )
   )
   private void gtolib$redirectPlaySound(
      Level var1, Player var2, double var3, double var5, double var7, SoundEvent var9, SoundSource var10, float var11, float var12
   ) {
      boolean var13 = var9 == SoundEvents.PLAYER_SWIM || var9 == SoundEvents.PLAYER_SPLASH || var9 == SoundEvents.PLAYER_SPLASH_HIGH_SPEED;
      if (!this.getPlayerData().isNoGravity() || !var13) {
         var1.playSound(var2, var3, var5, var7, var9, var10, var11, var12);
      }
   }
}
