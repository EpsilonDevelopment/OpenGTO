package com.gtolib.utils;

import com.gtolib.api.player.IEnhancedPlayer;
import com.gtolib.api.player.PlayerData;
import com.gtolib.api.player.attribute.PlayerAttributes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class PlayerUtils {
   public static void refreshPlayerAttributes(Player player) {
      if (player instanceof ServerPlayer serverPlayer) {
         PlayerData pData = IEnhancedPlayer.of(player).getPlayerData();
         PlayerAttributes attributes = pData.getPlayerAttributes();
         PlayerAttributes.REGISTRY.forEach(attributes::markDirty);
         serverPlayer.onUpdateAbilities();
      }
   }
}
