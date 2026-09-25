package com.gtolib.cache.resourcestree;

import com.gtolib.GTOCore;
import com.gtolib.cache.CacheManager;
import com.gtolib.cache.ICache;
import com.gtolib.cache.resourcestree.node.DirectoryNode;
import com.gtolib.cache.resourcestree.node.FileNode;
import com.gtolib.cache.resourcestree.node.SaveableDirectoryNode;
import com.gtolib.cache.resourcestree.node.SaveableFileNode;
import com.gtolib.cache.resourcestree.node.SaveableNode;
import com.gtolib.utils.FileUtils;
import com.gtolib.utils.GTOUtils;
import com.gtolib.utils.iostream.DataIOStream;
import it.unimi.dsi.fastutil.Pair;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import net.minecraft.server.packs.PackType;

public class SaveableResourcesTree extends CachedResourcesTree {
   private static Map<String, SaveableResourcesTree> SAVEABLE_CACHE = CacheManager.enableCache ? new ConcurrentHashMap<>() : null;
   private static Map<String, SaveableResourcesTree> CACHE = CacheManager.enableCache ? new ConcurrentHashMap<>() : null;
   private static boolean save;

   public static SaveableResourcesTree get(String name, boolean saveResources, Function<PackType, Path> basePathRetriever) {
      Map<String, SaveableResourcesTree> map = saveResources ? SAVEABLE_CACHE : CACHE;
      return map != null ? map.computeIfAbsent(name, k -> create(name, saveResources, basePathRetriever)) : create(name, saveResources, basePathRetriever);
   }

   private static SaveableResourcesTree create(String name, boolean saveResources, Function<PackType, Path> basePathRetriever) {
      long time = System.currentTimeMillis();
      SaveableResourcesTree tree = new SaveableResourcesTree();
      if (saveResources && CacheManager.cachedResources != null) {
         Pair<Map<String, byte[]>, Map<PackType, DirectoryNode>> pair = CacheManager.cachedResources.get(name);
         if (pair != null) {
            tree.rootNodeByType.put(PackType.CLIENT_RESOURCES, pair.right().get(PackType.CLIENT_RESOURCES));
            tree.rootNodeByType.put(PackType.SERVER_DATA, pair.right().get(PackType.SERVER_DATA));
            tree.resources = pair.left();
         }
      }

      if (tree.rootNodeByType.isEmpty()) {
         FileUtils.loadFile(CacheManager.getCacheFile(name + ".bin"), dis -> {
            SaveableNode a = SaveableNode.read(dis, tree::createFileNode);
            if (a != null) {
               SaveableNode b = SaveableNode.read(dis, tree::createFileNode);
               if (b != null) {
                  tree.rootNodeByType.put(PackType.CLIENT_RESOURCES, a instanceof FileNode ? null : (DirectoryNode)a);
                  tree.rootNodeByType.put(PackType.SERVER_DATA, b instanceof FileNode ? null : (DirectoryNode)b);
               }
            }
         });
      }

      if (tree.rootNodeByType.isEmpty()) {
         tree.load(basePathRetriever);
         GTOUtils.asyncExecute(() -> FileUtils.saveFile(CacheManager.getCacheFile(name + ".bin"), dos -> {
            try {
               for (DirectoryNode node : tree.rootNodeByType.values()) {
                  if (node instanceof SaveableDirectoryNode directoryNode) {
                     directoryNode.write(dos, false);
                  } else {
                     dos.writeByte(0);
                  }
               }
            } catch (IOException e) {
               throw new RuntimeException(e);
            }
         }));
      } else {
         for (PackType type : PackType.values()) {
            tree.rootPathsByType.put(type, basePathRetriever.apply(type));
         }
      }

      GTOCore.LOGGER.info("Loaded {} saveable resources tree took {} ms", name, System.currentTimeMillis() - time);
      return tree;
   }

   @Override
   protected DirectoryNode createDirectoryNode() {
      return new SaveableDirectoryNode();
   }

   @Override
   protected FileNode createFileNode() {
      return this.resources == null ? FileNode.INSTANCE : new SaveableFileNode();
   }

   public static synchronized void save() {
      if (!save) {
         save = true;
         CacheManager.cachedResources = null;
         FileUtils.saveFile(CacheManager.getCacheFile("resources"), dos -> {
            try {
               long time = System.currentTimeMillis();
               dos.writeVarInt(SAVEABLE_CACHE.size());
               SAVEABLE_CACHE.forEach((k, v) -> {
                  try {
                     dos.writeUTF(k);
                     v.writeCached(dos);
                  } catch (IOException e) {
                     throw new RuntimeException(e);
                  }
               });
               GTOCore.LOGGER.info("Saved cached resources took {} ms", System.currentTimeMillis() - time);
            } catch (IOException e) {
               GTOCore.LOGGER.error("Failed to save cached resources", e);
            }
         });
      }
   }

   private void writeCached(DataIOStream dos) throws IOException {
      dos.writeVarInt(this.resources.size());
      this.resources.forEach((k, v) -> {
         try {
            dos.writeUTF(k);
            if (v.length == 0) {
               dos.writeByte(0);
            } else {
               dos.writeVarInt(v.length);
               dos.write(v);
            }
         } catch (IOException e) {
            throw new RuntimeException(e);
         }
      });

      for (DirectoryNode node : this.rootNodeByType.values()) {
         if (node instanceof SaveableDirectoryNode directoryNode) {
            directoryNode.write(dos, true);
         } else {
            dos.writeByte(0);
         }
      }
   }

   static {
      CacheManager.addCache(() -> {
         CACHE.values().forEach(ICache::clearCache);
         CACHE = null;
      });
      CacheManager.addCache(() -> {
         SAVEABLE_CACHE.values().forEach(ICache::clearCache);
         SAVEABLE_CACHE = null;
      });
   }
}
