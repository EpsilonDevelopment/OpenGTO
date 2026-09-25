package com.gtolib.mixin.adastra;

import com.gregtechceu.gtceu.GTCEu;
import com.gtolib.api.misc.PlanetManagement;
import earth.terrarium.adastra.api.planets.Planet;
import earth.terrarium.adastra.common.config.AdAstraConfig;
import earth.terrarium.adastra.common.entities.vehicles.Lander;
import earth.terrarium.adastra.common.entities.vehicles.Rocket;
import earth.terrarium.adastra.common.menus.PlanetsMenu;
import earth.terrarium.adastra.common.registry.ModEntityTypes;
import earth.terrarium.adastra.common.utils.ModUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModUtils.class)
public final class ModUtilsMixin {
   @Overwrite(remap = false)
   public static boolean canTeleportToPlanet(Player var0, Planet var1) {
      if (GTCEu.isDev() || var0.removeTag("spaceelevatorst")) {
         var0.addTag("canTeleportToPlanet");
         return true;
      }

      if (!(var0.containerMenu instanceof PlanetsMenu)) {
         return false;
      }

      if (!var0.isCreative() && !var0.isSpectator()) {
         String[] var2 = AdAstraConfig.disabledPlanets.split(",");

         for (String var6 : var2) {
            if (var6.equals(var1.dimension().location().toString())) {
               return false;
            }
         }

         if (var0.getVehicle() instanceof Rocket var8) {
            return var8.getY() < AdAstraConfig.atmosphereLeave ? false : PlanetManagement.calculateTier(var1, var8.level().dimension()) <= var8.tier();
         } else {
            return false;
         }
      } else {
         return true;
      }
   }

   @Inject(method = "land", at = @At("HEAD"), remap = false, cancellable = true)
   private static void land(ServerPlayer var0, ServerLevel var1, Vec3 var2, CallbackInfo var3) {
      if (var0.removeTag("canTeleportToPlanet")) {
         var3.cancel();
         var0.moveTo(var2);
         Lander var4 = ModEntityTypes.LANDER.get().create(var1);
         if (var4 == null) {
            return;
         }

         var4.setPos(var2);
         var1.addFreshEntity(var4);
         ModUtils.teleportToDimension(var0, var1).startRiding(var4);
      }
   }
}
