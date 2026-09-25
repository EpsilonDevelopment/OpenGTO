package com.gtolib.cache.resourcestree;

import com.gtolib.GTOCore;
import com.gtolib.cache.CacheManager;
import com.gtolib.cache.ICache;
import com.gtolib.cache.resourcestree.node.CachedFileNode;
import com.gtolib.cache.resourcestree.node.FileNode;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import lombok.Generated;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import org.apache.commons.lang3.ArrayUtils;

public class CachedResourcesTree extends ResourcesTree implements ICache {
   private static Map<String, CachedResourcesTree> CACHE = CacheManager.enableCache ? new ConcurrentHashMap<>() : null;
   protected Map<String, byte[]> resources = CacheManager.enableCache ? new ConcurrentHashMap<>() : null;
   private boolean cacheExternal = false;

   public static CachedResourcesTree get(String name, Function<PackType, Path> basePathRetriever) {
      return CACHE != null ? CACHE.computeIfAbsent(name, k -> create(name, basePathRetriever)) : create(name, basePathRetriever);
   }

   private static CachedResourcesTree create(String name, Function<PackType, Path> basePathRetriever) {
      long time = System.currentTimeMillis();
      CachedResourcesTree tree = new CachedResourcesTree();
      tree.load(basePathRetriever);
      GTOCore.LOGGER.info("Loaded {} cached resources tree took {} ms", name, System.currentTimeMillis() - time);
      return tree;
   }

   @Override
   protected IoSupplier<InputStream> getFileResource(String[] paths, Function<String[], Path> resolve) {
      if (this.cacheExternal && this.resources != null) {
         Path path = resolve.apply(paths);
         byte[] data = this.resources.computeIfAbsent(path.toString(), k -> {
            if (Files.exists(path)) {
               try {
                  return Files.readAllBytes(path);
               } catch (Exception var3x) {
               }
            }

            return ArrayUtils.EMPTY_BYTE_ARRAY;
         });
         return data.length > 0 ? () -> new ByteArrayInputStream(data) : null;
      } else {
         return super.getFileResource(paths, resolve);
      }
   }

   @Override
   protected FileNode createFileNode() {
      return this.resources == null ? FileNode.INSTANCE : new CachedFileNode();
   }

   @Override
   public void clearCache() {
      this.resources = null;
      this.rootNodeByType.values().forEach(n -> {
         if (n != null) {
            n.clearCache();
         }
      });
   }

   @Generated
   public void setCacheExternal(boolean cacheExternal) {
      this.cacheExternal = cacheExternal;
   }

   static {
      CacheManager.addCache(() -> {
         CACHE.values().forEach(ICache::clearCache);
         CACHE = null;
      });
   }
}
