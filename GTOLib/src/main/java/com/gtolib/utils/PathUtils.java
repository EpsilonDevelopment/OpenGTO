package com.gtolib.utils;

import com.google.common.base.Joiner;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

public final class PathUtils {
   private static final Joiner SLASH_JOINER = Joiner.on('/');
   private static final ConcurrentHashMap<String, String[]> SPLIT_PATHS = new ConcurrentHashMap<>();

   private PathUtils() {
   }

   public static Path resolve(Path p_path, int offset, String... paths) {
      int i = paths.length - offset;

      return switch (i) {
         case 0 -> p_path;
         case 1 -> p_path.resolve(paths[offset]);
         default -> {
            int j = i - 1;
            String[] astring = new String[j];
            System.arraycopy(paths, offset + 1, astring, 0, j);
            yield p_path.resolve(p_path.getFileSystem().getPath(paths[offset], astring));
         }
      };
   }

   public static Path resolve(Path p_path, String... paths) {
      int i = paths.length;

      return switch (i) {
         case 0 -> p_path;
         case 1 -> p_path.resolve(paths[0]);
         default -> {
            int j = i - 1;
            String[] astring = new String[j];
            System.arraycopy(paths, 1, astring, 0, j);
            yield p_path.resolve(p_path.getFileSystem().getPath(paths[0], astring));
         }
      };
   }

   public static String[] decompose(String path) {
      return SPLIT_PATHS.computeIfAbsent(path, k -> {
         String[] components = path.split("/");

         for (int i = 0; i < components.length; i++) {
            components[i] = components[i].intern();
         }

         return components;
      });
   }

   public static boolean isValidCachedResourcePath(Path path) {
      if (path.getFileName() != null && path.getNameCount() != 0) {
         String str = SLASH_JOINER.join(path);
         if (str.isEmpty()) {
            return false;
         }

         for (int i = 0; i < str.length(); i++) {
            if (!ResourceLocation.validPathChar(str.charAt(i))) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public static String[] getPathFromLocation(PackType type, ResourceLocation location) {
      String[] parts = location.getPath().split("/");
      int len = parts.length;
      String[] result = new String[len + 2];
      result[0] = type.getDirectory();
      result[1] = location.getNamespace();
      System.arraycopy(parts, 0, result, 2, len);
      return result;
   }

   public static String[] getPathFromLocation(ResourceLocation location) {
      String[] parts = location.getPath().split("/");
      int len = parts.length;
      String[] result = new String[len + 1];
      result[0] = location.getNamespace();
      System.arraycopy(parts, 0, result, 1, len);
      return result;
   }
}
