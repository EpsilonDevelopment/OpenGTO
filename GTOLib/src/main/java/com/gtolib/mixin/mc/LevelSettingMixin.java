package com.gtolib.mixin.mc;

import com.gtolib.mc.IExtendedLevelSetting;
import com.mojang.serialization.Dynamic;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LevelSettings.class, priority = 0)
public class LevelSettingMixin implements IExtendedLevelSetting {
   @Unique
   private int gto$difficulty = 0;
   @Unique
   private boolean gto$srm = false;
   @Unique
   private boolean gto$dev = false;

   @Inject(method = "parse", at = @At("RETURN"), cancellable = true)
   private static void parse(Dynamic<?> var0, WorldDataConfiguration var1, CallbackInfoReturnable<LevelSettings> var2) {
      LevelSettings var3 = (LevelSettings)var2.getReturnValue();
      IExtendedLevelSetting var4 = (IExtendedLevelSetting)(Object)var3;
      if (var4 != null) {
         var4.gto$setGTODifficulty(var0.get("gto$Difficulty").asInt(0));
         var4.gto$setSrm(var0.get("gto$SelfRestraint").asBoolean(false));
         var4.gto$setDevMode(var0.get("gto$Dev").asBoolean(false));
      }

      var2.setReturnValue(var3);
   }

   @Override
   public int gto$getGTODifficulty() {
      return this.gto$difficulty;
   }

   @Override
   public void gto$setGTODifficulty(int var1) {
      this.gto$difficulty = var1;
   }

   @Override
   public boolean gto$isSrm() {
      return this.gto$srm;
   }

   @Override
   public void gto$setSrm(boolean var1) {
      this.gto$srm = var1;
   }

   @Override
   public boolean gto$isDevMode() {
      return this.gto$dev;
   }

   @Override
   public void gto$setDevMode(boolean var1) {
      this.gto$dev = var1;
   }
}
