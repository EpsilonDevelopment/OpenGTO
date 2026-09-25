package com.gtolib.cache;

import com.gto.datasynclib.util.holder.IntHolder;
import com.gto.datasynclib.util.holder.ObjHolder;
import com.gtocore.config.GTOConfig;
import com.gtolib.GTOCore;
import com.gtolib.MixinConfigPlugin;
import com.gtolib.cache.resourcestree.SaveableResourcesTree;
import com.gtolib.cache.resourcestree.node.DirectoryNode;
import com.gtolib.cache.resourcestree.node.FileNode;
import com.gtolib.cache.resourcestree.node.SaveableNode;
import com.gtolib.utils.FileUtils;
import com.gtolib.utils.iostream.DataIOStream;
import it.unimi.dsi.fastutil.Pair;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import org.apache.commons.lang3.ArrayUtils;

public final class CacheManager {
   public static Map<String, Pair<Map<String, byte[]>, Map<PackType, DirectoryNode>>> cachedResources = Collections.emptyMap();
   public static boolean enableCache = false;
   private static List<ICache> caches = new ArrayList<>();
   private static boolean load;

   private CacheManager() {
   }

   public static synchronized void addCache(ICache cache) {
      if (caches != null) {
         caches.add(cache);
      }
   }

   public static synchronized void loadCache() {
      if (!load && shouldCache()) {
         load = true;
         FileUtils.loadFile(getCacheFile("resources"), dis -> {
            try {
               long time = System.currentTimeMillis();
               int size = dis.readVarInt();
               cachedResources = new HashMap<>(size);

               for (int i = 0; i < size; i++) {
                  String k = dis.readUTF();
                  Map<String, byte[]> a = readResources(dis);
                  Map<PackType, DirectoryNode> b = readCached(dis);
                  if (b != null) {
                     cachedResources.put(k, Pair.of(a, b));
                  }
               }

               GTOCore.LOGGER.info("Loaded cached resources took {} ms", System.currentTimeMillis() - time);
            } catch (Exception e) {
               GTOCore.LOGGER.error("Failed to load cached resources", e);
            }
         });
         enableCache = true;
      }
   }

   public static synchronized void clearCache() {
      if (enableCache) {
         enableCache = false;
         run(() -> {
            SaveableResourcesTree.save();
            caches.forEach(ICache::clearCache);
            caches = null;
         });
      }
   }

   private static Map<String, byte[]> readResources(DataIOStream dis) throws IOException {
      int size = dis.readVarInt();
      if (size == 0) {
         return new ConcurrentHashMap<>();
      }

      ConcurrentHashMap<String, byte[]> map = new ConcurrentHashMap<>(size);

      for (int i = 0; i < size; i++) {
         String k = dis.readUTF();
         int v = dis.readVarInt();
         if (v == 0) {
            map.put(k, ArrayUtils.EMPTY_BYTE_ARRAY);
         } else {
            byte[] data = new byte[v];
            dis.readFully(data);
            map.put(k, data);
         }
      }

      return map;
   }

   private static Map<PackType, DirectoryNode> readCached(DataIOStream dis) {
      EnumMap<PackType, DirectoryNode> map = new EnumMap<>(PackType.class);
      SaveableNode a = SaveableNode.readSaveable(dis);
      if (a != null) {
         SaveableNode b = SaveableNode.readSaveable(dis);
         if (b != null) {
            map.put(PackType.CLIENT_RESOURCES, a instanceof FileNode ? null : (DirectoryNode)a);
            map.put(PackType.SERVER_DATA, b instanceof FileNode ? null : (DirectoryNode)b);
         }
      }

      return map.isEmpty() ? null : map;
   }

   public static void init() {
      if (FMLLoader.isProduction()) {
         run(
            () -> {
               Set<String> targetModIds = Set.of("ldlib", "gtceu", "gtocore", "ad_astra", "deeperdarker");
               List<ModInfo> foundMods = new ArrayList<>();
               List<ModInfo> remainingMods = FMLLoader.getLoadingModList().getMods().stream().filter(mod -> {
                  if (foundMods.size() < targetModIds.size() && targetModIds.contains(mod.getModId())) {
                     foundMods.add(mod);
                     return false;
                  } else {
                     return true;
                  }
               }).toList();
               FMLLoader.getLoadingModList().getMods().clear();
               FMLLoader.getLoadingModList().getMods().addAll(foundMods);
               FMLLoader.getLoadingModList().getMods().addAll(remainingMods);
               ObjHolder<String> hash = new ObjHolder<>();
               IntHolder mods = new IntHolder(0);
               IntHolder difficulty = new IntHolder(0);
               File file = GTOCore.getFile("hash");
               if (file.exists() && file.canRead()) {
                  try {
                     FileUtils.loadFromFile(file, stream -> {
                        hash.value = stream.readUTF();
                        mods.value = stream.readVarInt();
                        difficulty.value = stream.readVarInt();
                        return null;
                     });
                  } catch (Exception var9) {
                  }
               }

               MixinConfigPlugin.hash = FileUtils.calculateFileHash(
                     FMLLoader.getLoadingModList().getModFileById("gtocore").getFile().getFilePath().toFile(), "MD5"
                  )
                  .substring(0, 6);
               FileUtils.saveToFile(null, file, (dos, obj) -> {
                  dos.writeUTF(MixinConfigPlugin.hash);
                  dos.writeVarInt(FMLLoader.getLoadingModList().getModFiles().size());
                  dos.writeVarInt(GTOCore.difficulty);
               });
               if (mods.value != FMLLoader.getLoadingModList().getModFiles().size() || !MixinConfigPlugin.hash.equals(hash.value)) {
                  File cacheFile = GTOCore.getFile("cache");
                  FileUtils.deleteDirectory(cacheFile);
               }

               if (difficulty.value != GTOCore.difficulty) {
                  try {
                     Files.deleteIfExists(getCacheFile("json/recipes").toPath());
                  } catch (IOException var8) {
                  }
               }

               if (FMLEnvironment.dist.isClient()) {
                  ClientCacheManager.init();
               }

               loadCache();
            }
         );
      }
   }

   public static void save() {
      if (FMLLoader.isProduction() && FMLEnvironment.dist.isClient()) {
         ClientCacheManager.save();
      }
   }

   public static File getCacheFile(String name) {
      return GTOCore.getFile("cache/" + name);
   }

   private static void run(Runnable runnable) {
      Thread t = new Thread(runnable);
      t.setPriority(1);
      t.setDaemon(true);
      t.start();
   }

   public static boolean shouldCache() {
      return GTOConfig.INSTANCE.misc.cacheResources && FMLLoader.isProduction();
   }
}
