package com.gtolib.cache.resourcestree;

import com.gto.fastcollection.fastutil.OpenCacheHashSet;
import com.gtolib.cache.resourcestree.node.DirectoryNode;
import com.gtolib.cache.resourcestree.node.Node;
import com.gtolib.utils.PathUtils;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PackResources.ResourceOutput;
import net.minecraft.server.packs.resources.IoSupplier;

public abstract class AbstractResourcesTree {
   protected final Map<PackType, DirectoryNode> rootNodeByType = new EnumMap<>(PackType.class);

   protected AbstractResourcesTree() {
   }

   protected abstract Path getBasePath(PackType var1, String var2);

   protected Node getResourceNode(boolean assets, String[] paths) {
      DirectoryNode node = assets ? this.rootNodeByType.get(PackType.CLIENT_RESOURCES) : this.rootNodeByType.get(PackType.SERVER_DATA);
      if (node == null) {
         return null;
      }

      int i = 1;
      int len = paths.length;

      while (i < len) {
         Node n = node.path.get(paths[i++]);
         if (n == null) {
            return null;
         }

         if (!(n instanceof DirectoryNode directoryNode)) {
            return i == len ? n : null;
         }

         node = directoryNode;
      }

      return null;
   }

   public IoSupplier<InputStream> getResource(PackType type, ResourceLocation location, Function<String[], Path> resolve) {
      return this.getRootResource(PathUtils.getPathFromLocation(type, location), resolve);
   }

   public IoSupplier<InputStream> getRootResource(String[] paths, Function<String[], Path> resolve) {
      if (paths.length > 2) {
         String type = paths[0];
         boolean assets = "assets".equals(type);
         if (assets || "data".equals(type)) {
            Node node;
            if ((node = this.getResourceNode(assets, paths)) != null) {
               return node.getStream(paths, resolve);
            }

            return null;
         }
      }

      return this.getFileResource(paths, resolve);
   }

   protected IoSupplier<InputStream> getFileResource(String[] paths, Function<String[], Path> resolve) {
      Path path = resolve.apply(paths);
      return Files.exists(path) ? IoSupplier.create(path) : null;
   }

   public void listResources(PackType type, String namespace, String path, ResourceOutput output) {
      DirectoryNode typeNode = this.rootNodeByType.get(type);
      if (typeNode != null) {
         if (typeNode.path.get(namespace) instanceof DirectoryNode node) {
            String[] pathComponents = PathUtils.decompose(path);
            int size = pathComponents.length;
            if (size == 0) {
               node.output(namespace, this.getBasePath(type, namespace), path, output);
            } else {
               int i = 0;

               while (i < size) {
                  String component = pathComponents[i++];
                  if (!component.isEmpty()) {
                     Node nextNode = node.path.get(component);
                     if (nextNode == null) {
                        return;
                     }

                     if (i == size) {
                        nextNode.output(namespace, this.getBasePath(type, namespace), path, output);
                        return;
                     }

                     if (!(nextNode instanceof DirectoryNode directoryNode)) {
                        return;
                     }

                     node = directoryNode;
                  }
               }
            }
         }
      }
   }

   public Set<String> getNamespaces(PackType type) {
      DirectoryNode directoryNode;
      if ((directoryNode = this.rootNodeByType.get(type)) != null) {
         Set<String> results = new OpenCacheHashSet<>();
         directoryNode.path.forEach((k, v) -> {
            if (v instanceof DirectoryNode) {
               results.add(k);
            }
         });
         return results;
      } else {
         return Collections.emptySet();
      }
   }
}
