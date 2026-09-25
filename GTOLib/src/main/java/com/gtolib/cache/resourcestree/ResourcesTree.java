package com.gtolib.cache.resourcestree;

import com.gtolib.cache.resourcestree.node.DirectoryNode;
import com.gtolib.cache.resourcestree.node.FileNode;
import com.gtolib.utils.PathUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.server.packs.PackType;

public class ResourcesTree extends AbstractResourcesTree {
   protected final Map<PackType, Path> rootPathsByType = new EnumMap<>(PackType.class);

   protected ResourcesTree() {
   }

   protected void load(Function<PackType, Path> basePathRetriever) {
      for (PackType type : PackType.values()) {
         DirectoryNode typeRoot = this.createDirectoryNode();
         this.rootNodeByType.put(type, typeRoot);
         Path root = basePathRetriever.apply(type);
         this.rootPathsByType.put(type, root);

         try (Stream<Path> stream = Files.find(root, Integer.MAX_VALUE, (p, a) -> a.isRegularFile())) {
            stream.<Path>map(path -> root.relativize(path.toAbsolutePath())).filter(PathUtils::isValidCachedResourcePath).forEach(path -> {
               DirectoryNode node = typeRoot;
               int nameCount = path.getNameCount();
               int i = 0;

               while (i < nameCount) {
                  if (node.path == null) {
                     node.path = new HashMap<>();
                  }

                  String key = path.getName(i).toString();
                  i++;
                  node = (DirectoryNode)node.path.computeIfAbsent(i < nameCount ? key.intern() : key, $ -> this.createDirectoryNode());
               }
            });
         } catch (IOException var13) {
         }

         if (typeRoot.isEmpty()) {
            this.rootNodeByType.put(type, null);
         } else {
            typeRoot.build(this::createFileNode);
         }
      }
   }

   @Override
   protected Path getBasePath(PackType type, String namespace) {
      return this.rootPathsByType.get(type).resolve(namespace);
   }

   protected DirectoryNode createDirectoryNode() {
      return new DirectoryNode();
   }

   protected FileNode createFileNode() {
      return FileNode.INSTANCE;
   }
}
