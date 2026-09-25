package com.gtolib.api.misc;

import com.gto.fastcollection.fastutil.OpenCacheHashSet;
import com.gtolib.Client;
import com.gtolib.api.data.GTODimensions;
import com.gtolib.api.network.NetworkPack;
import com.gtolib.data.CommonSavaedData;
import com.hepdd.gtmthings.utils.TeamUtil;
import earth.terrarium.adastra.api.planets.Planet;
import earth.terrarium.adastra.api.planets.PlanetApi;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public final class PlanetManagement {
   private static final NetworkPack CLIENT = NetworkPack.registerC2S(
      "planetC2S", (p, buf) -> checkIsUnlocked(p, GTODimensions.getDimensionKey(buf.readResourceLocation()))
   );
   private static final NetworkPack SERVER = NetworkPack.registerS2C(
      "planetS2C", (p, buf) -> clientUnlock(GTODimensions.getDimensionKey(buf.readResourceLocation()))
   );

   public static void checkPlanetIsUnlocked(ResourceKey<Level> planet) {
      if (!isClientUnlocked(planet)) {
         CLIENT.send(buf -> buf.writeResourceLocation(planet.location()));
      }
   }

   public static void planetUnlock(ServerPlayer serverPlayer, ResourceKey<Level> planet) {
      SERVER.send(buf -> buf.writeResourceLocation(planet.location()), serverPlayer);
   }

   public static boolean isClientUnlocked(ResourceKey<Level> planet) {
      if (planet == null) {
         return false;
      } else {
         return planet == GTODimensions.OVERWORLD ? true : Client.UNLOCKED_PLANET.contains(planet);
      }
   }

   public static void clientUnlock(ResourceKey<Level> planet) {
      if (planet != null) {
         Client.UNLOCKED_PLANET.add(planet);
      }
   }

   public static void checkIsUnlocked(ServerPlayer serverPlayer, ResourceKey<Level> planet) {
      if (planet != GTODimensions.OVERWORLD) {
         boolean value = isUnlocked(serverPlayer, planet);
         if (value) {
            planetUnlock(serverPlayer, planet);
         }
      }
   }

   public static boolean isUnlocked(ServerPlayer serverPlayer, ResourceKey<Level> planet) {
      if (planet == null) {
         return false;
      } else {
         return planet == GTODimensions.OVERWORLD
            ? true
            : CommonSavaedData.INSTANCE.getPlanetUnlocked().getOrDefault(TeamUtil.getTeamUUID(serverPlayer.getUUID()), Collections.emptySet()).contains(planet);
      }
   }

   public static boolean isUnlocked(UUID serverPlayer, ResourceKey<Level> planet) {
      if (planet == null) {
         return false;
      } else {
         return planet == GTODimensions.OVERWORLD
            ? true
            : CommonSavaedData.INSTANCE.getPlanetUnlocked().getOrDefault(TeamUtil.getTeamUUID(serverPlayer), Collections.emptySet()).contains(planet);
      }
   }

   public static void unlock(UUID uuid, ResourceKey<Level> planet) {
      if (planet != null) {
         CommonSavaedData.INSTANCE.getPlanetUnlocked().computeIfAbsent(TeamUtil.getTeamUUID(uuid), k -> new OpenCacheHashSet<>()).add(planet);
         CommonSavaedData.INSTANCE.setDirty();
      }
   }

   public static int calculateTier(Planet targetPlanet, ResourceKey<Level> current) {
      if (targetPlanet.tier() < 10) {
         if (targetPlanet.dimension() == GTODimensions.BARNARDA_C) {
            return 8;
         }

         Planet currentPlanet = PlanetApi.API.getPlanet(current);
         if (currentPlanet == null) {
            return 7;
         }

         ResourceKey<Level> target = targetPlanet.dimension();
         Optional<ResourceKey<Level>> currentOrbit = currentPlanet.orbit();
         Optional<ResourceKey<Level>> targetOrbit = targetPlanet.orbit();
         if (currentOrbit.isPresent() && currentOrbit.get() == target) {
            return 1;
         }

         if (targetOrbit.isPresent() && targetOrbit.get() == current) {
            return 1;
         }

         if (!Objects.equals(GTODimensions.getGalaxy(targetPlanet.dimension()), GTODimensions.getGalaxy(current))) {
            return 7;
         }

         int distanceFromEarth1 = GTODimensions.getPlanetDistances(target);
         if (distanceFromEarth1 > 0) {
            int distanceFromEarth2 = GTODimensions.getPlanetDistances(current);
            if (distanceFromEarth2 == 0) {
               return 7;
            }

            return Math.max(1, Math.min(6, Math.abs(distanceFromEarth1 - distanceFromEarth2)));
         }
      }

      return targetPlanet.tier();
   }
}
