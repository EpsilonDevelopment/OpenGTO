package com.gtolib.cache.resourcestree.node;

import com.gtolib.utils.RLUtils;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.server.packs.resources.IoSupplier;

public class ResourcesNode implements Node {
   private final byte[] data;

   public ResourcesNode(byte[] data) {
      this.data = data;
   }

   @Override
   public IoSupplier<InputStream> getStream(String[] paths, Function<String[], Path> resolve) {
      return () -> new ByteArrayInputStream(this.data);
   }

   @Override
   public void output(String namespace, Path basePath, String path, BiConsumer output) {
      output.accept(RLUtils.fromNamespaceAndPath(namespace, path), this.getStream(null, null));
   }
}
