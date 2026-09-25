package com.gtolib.cache.resourcestree.node;

import com.gtolib.utils.RLUtils;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.server.packs.resources.IoSupplier;

public class CachedFileNode extends FileNode {
   byte[] data;

   @Override
   public IoSupplier<InputStream> getStream(String[] paths, Function<String[], Path> resolve) {
      return () -> {
         if (this.data == null) {
            this.data = Files.readAllBytes(resolve.apply(paths));
         }

         return new ByteArrayInputStream(this.data);
      };
   }

   @Override
   public void output(String namespace, Path basePath, String path, BiConsumer output) {
      IoSupplier<InputStream> consumer = () -> {
         if (this.data == null) {
            this.data = Files.readAllBytes(basePath.resolve(path));
         }

         return new ByteArrayInputStream(this.data);
      };
      output.accept(RLUtils.fromNamespaceAndPath(namespace, path), consumer);
   }
}
