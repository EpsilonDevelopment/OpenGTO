package com.gtolib.utils;

import com.gtolib.GTOCore;
import com.gtolib.utils.iostream.IOStreamCodec;
import com.gtolib.utils.iostream.IOStreamCodecs;
import net.minecraft.resources.ResourceLocation;

public final class RLUtils {
   public static final IOStreamCodec<ResourceLocation> IO_CODEC = IOStreamCodecs.RESOURCE_LOCATION;
   public static final ResourceLocation EMPTY = GTOCore.id("empty");

   private RLUtils() {
   }

   public static ResourceLocation fromNamespaceAndPath(String namespace, String path) {
      return new ResourceLocation(namespace, path, null);
   }

   public static ResourceLocation parse(String location) {
      String namespace = "minecraft";
      String path = location;
      int i = location.indexOf(58);
      if (i >= 0) {
         path = location.substring(i + 1);
         if (i >= 1) {
            namespace = location.substring(0, i);
         }
      }

      return new ResourceLocation(namespace, path, null);
   }

   public static ResourceLocation mc(String path) {
      return fromNamespaceAndPath("minecraft", path);
   }

   public static ResourceLocation forge(String path) {
      return fromNamespaceAndPath("forge", path);
   }

   public static ResourceLocation sp(String path) {
      return fromNamespaceAndPath("sophisticatedbackpacks", path);
   }

   public static ResourceLocation fd(String path) {
      return fromNamespaceAndPath("farmersdelight", path);
   }

   public static ResourceLocation fr(String path) {
      return fromNamespaceAndPath("farmersrespite", path);
   }

   public static ResourceLocation ad(String path) {
      return fromNamespaceAndPath("ad_astra", path);
   }

   public static ResourceLocation bot(String path) {
      return fromNamespaceAndPath("botania", path);
   }

   public static ResourceLocation exbot(String path) {
      return fromNamespaceAndPath("extrabotany", path);
   }

   public static ResourceLocation ars(String path) {
      return fromNamespaceAndPath("ars_nouveau", path);
   }

   public static ResourceLocation ae(String path) {
      return fromNamespaceAndPath("ae2", path);
   }

   public static ResourceLocation mybot(String path) {
      return fromNamespaceAndPath("mythicbotany", path);
   }

   public static ResourceLocation functionalstorage(String path) {
      return fromNamespaceAndPath("functionalstorage", path);
   }
}
