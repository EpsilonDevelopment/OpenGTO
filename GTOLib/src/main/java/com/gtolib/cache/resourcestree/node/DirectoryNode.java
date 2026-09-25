package com.gtolib.cache.resourcestree.node;

import com.gtolib.cache.ICache;
import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class DirectoryNode implements Node, ICache {
   public Map<String, Node> path;

   public void build(Supplier<FileNode> fileNodeSupplier) {
      for (Entry<String, Node> entry : this.path.entrySet()) {
         if (entry.getValue() instanceof DirectoryNode node) {
            if (node.isEmpty()) {
               entry.setValue(fileNodeSupplier.get());
            } else {
               node.build(fileNodeSupplier);
            }
         }
      }
   }

   public boolean isEmpty() {
      return this.path == null || this.path.isEmpty();
   }

   @Override
   public void clearCache() {
      if (!this.isEmpty()) {
         for (Entry<String, Node> entry : this.path.entrySet()) {
            Node node = entry.getValue();
            if (node instanceof CachedFileNode) {
               entry.setValue(FileNode.INSTANCE);
            } else if (node instanceof ICache cache) {
               cache.clearCache();
            }
         }
      }
   }

   @Override
   public void output(String namespace, Path basePath, String path, BiConsumer output) {
      this.path.forEach((k, v) -> v.output(namespace, basePath, path + "/" + k, output));
   }
}
