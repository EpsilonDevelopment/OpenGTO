package com.gtolib.cache.resourcestree;

import com.gtolib.GTOCore;
import com.gtolib.cache.CacheManager;
import com.gtolib.cache.resourcestree.node.DirectoryNode;
import com.gtolib.cache.resourcestree.node.ResourcesNode;
import com.gtolib.utils.PathUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;

public class ZIPResourcesTree extends AbstractResourcesTree {
   private static Map<String, ZIPResourcesTree> CACHE = CacheManager.enableCache ? new ConcurrentHashMap<>() : null;
   private final Map<String, byte[]> resources = new HashMap<>();

   public static ZIPResourcesTree get(String name, ZipFile file) {
      return CACHE != null ? CACHE.computeIfAbsent(name, k -> create(name, file)) : create(name, file);
   }

   private static ZIPResourcesTree create(String name, ZipFile file) {
      long time = System.currentTimeMillis();
      ZIPResourcesTree tree = new ZIPResourcesTree();
      Enumeration<? extends ZipEntry> entries = file.entries();

      while (entries.hasMoreElements()) {
         ZipEntry entry = entries.nextElement();
         if (!entry.isDirectory()) {
            String path = entry.getName();

            try (InputStream in = file.getInputStream(entry)) {
               byte[] data = in.readAllBytes();
               String[] paths = PathUtils.decompose(path);
               if (paths.length > 2) {
                  String type = paths[0];
                  boolean assets = "assets".equals(type);
                  if (assets || "data".equals(type)) {
                     tree.addResources(assets, paths, data);
                     continue;
                  }
               }

               tree.resources.put(path, data);
            } catch (IOException e) {
               GTOCore.LOGGER.error("Failed to load resource {}", path, e);
            }
         }
      }

      GTOCore.LOGGER.info("Loaded {} zip resources tree took {}ms", name, System.currentTimeMillis() - time);
      return tree;
   }

   private ZIPResourcesTree() {
   }

   private void addResources(boolean assets, String[] paths, byte[] data) {
      DirectoryNode node = this.rootNodeByType.computeIfAbsent(assets ? PackType.CLIENT_RESOURCES : PackType.SERVER_DATA, k -> new DirectoryNode());
      int i = 1;
      int len = paths.length;

      while (i < len) {
         if (node.path == null) {
            node.path = new HashMap<>();
         }

         String path = paths[i++];
         if (i == len) {
            node.path.put(path, new ResourcesNode(data));
         } else {
            node = (DirectoryNode)node.path.computeIfAbsent(path, k -> new DirectoryNode());
         }
      }
   }

   @Override
   protected Path getBasePath(PackType type, String namespace) {
      return null;
   }

   @Override
   protected IoSupplier<InputStream> getFileResource(String[] paths, Function<String[], Path> resolve) {
      byte[] data = this.resources.get(String.join("/", paths));
      return data == null ? null : () -> new ByteArrayInputStream(data);
   }

   static {
      CacheManager.addCache(() -> CACHE = null);
   }
}
