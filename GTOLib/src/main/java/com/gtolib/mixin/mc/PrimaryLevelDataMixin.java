package com.gtolib.mixin.mc;

import com.gtocore.config.GTOConfig;
import com.gtolib.GTOCore;
import com.gtolib.utils.ServerUtils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PrimaryLevelData.class)
public class PrimaryLevelDataMixin {
   @Inject(method = "setTagData", at = @At("TAIL"))
   private void onSetTagData(RegistryAccess var1, CompoundTag var2, CompoundTag var3, CallbackInfo var4) {
      var2.putInt("gto$Difficulty", Math.max(GTOCore.difficulty, ServerUtils.getPersistentData().getInt("difficulty")));
      var2.putBoolean("gto$SelfRestraint", ServerUtils.getPersistentData().getBoolean("srm") || GTOConfig.INSTANCE.gamePlay.selfRestraint);
      var2.putBoolean("gto$Dev", ServerUtils.getPersistentData().getBoolean("dev") || GTOConfig.INSTANCE.devMode.dev);
   }
}
