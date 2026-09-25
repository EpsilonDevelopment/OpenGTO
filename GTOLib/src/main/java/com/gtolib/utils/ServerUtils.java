package com.gtolib.utils;

import com.gtolib.data.CommonSavaedData;
import earth.terrarium.adastra.common.utils.ModUtils;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

public final class ServerUtils {
   public static final String IDENTIFIER_KEY = "server_id";

   private ServerUtils() {
   }

   @NotNull
   public static MinecraftServer getServer() {
      return ServerLifecycleHooks.getCurrentServer();
   }

   @NotNull
   public static CompoundTag getPersistentData() {
      return CommonSavaedData.getData();
   }

   public static void setPersistentDataDirty() {
      CommonSavaedData.INSTANCE.setDirty();
   }

   public static UUID getServerIdentifier() {
      CompoundTag data = getPersistentData();
      if (!data.contains("server_id")) {
         data.putUUID("server_id", UUID.randomUUID());
         CommonSavaedData.INSTANCE.setDirty();
      }

      return data.getUUID("server_id");
   }

   public static void runCommandSilent(MinecraftServer server, String command) {
      server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(), command);
   }

   public static void teleportToDimension(ServerLevel serverLevel, Entity entity, Vec3 vec3) {
      entity.moveTo(vec3);
      ModUtils.teleportToDimension(entity, serverLevel);
   }

   public static void teleportToDimension(MinecraftServer server, Entity entity, ResourceKey<Level> dim, Vec3 vec3) {
      ServerLevel serverLevel = server.getLevel(dim);
      if (serverLevel != null) {
         teleportToDimension(serverLevel, entity, vec3);
      }
   }

   public static void markServerLangInitialized() {
      CommonSavaedData.INSTANCE.markServerLangInitialized();
   }

   public static boolean isServerLangInitialized() {
      return CommonSavaedData.INSTANCE.isServerLangInitialized();
   }
}
