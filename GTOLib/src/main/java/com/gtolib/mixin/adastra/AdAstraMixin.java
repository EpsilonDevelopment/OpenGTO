package com.gtolib.mixin.adastra;

import earth.terrarium.adastra.AdAstra;
import earth.terrarium.adastra.api.systems.GravityApi;
import earth.terrarium.adastra.api.systems.OxygenApi;
import earth.terrarium.adastra.api.systems.PlanetData;
import earth.terrarium.adastra.api.systems.TemperatureApi;
import earth.terrarium.adastra.common.network.NetworkHandler;
import earth.terrarium.adastra.common.network.messages.ClientboundSyncLocalPlanetDataPacket;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AdAstra.class)
public final class AdAstraMixin {
   @Redirect(method = "init", at = @At(value = "INVOKE", target = "Learth/terrarium/adastra/common/utils/radio/StationLoader;init()V"), remap = false)
   private static void stationLoader() {
   }

   @Overwrite(remap = false)
   public static void onServerTick(MinecraftServer var0) {
      if (var0.getTickCount() % 40 == 0) {
         var0.getPlayerList()
            .getPlayers()
            .forEach(
               var0x -> NetworkHandler.CHANNEL
                  .sendToPlayer(
                     new ClientboundSyncLocalPlanetDataPacket(
                        new PlanetData(OxygenApi.API.hasOxygen(var0x), TemperatureApi.API.getTemperature(var0x), GravityApi.API.getGravity(var0x))
                     ),
                     var0x
                  )
            );
      }
   }
}
