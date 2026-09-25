package com.gtolib.mc;

import com.gtolib.GTOCore;
import net.minecraft.world.level.LevelSettings;

public interface IExtendedLevelSetting {
   int gto$getGTODifficulty();

   void gto$setGTODifficulty(int var1);

   boolean gto$isSrm();

   void gto$setSrm(boolean var1);

   boolean gto$isDevMode();

   void gto$setDevMode(boolean var1);

   static boolean matchDifficulty(LevelSettings settings) {
      return ((Object)settings) instanceof IExtendedLevelSetting extendedSettings ? GTOCore.difficulty == extendedSettings.gto$getGTODifficulty() : false;
   }
}
