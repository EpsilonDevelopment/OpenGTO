package com.gtolib.cache.resourcestree.node;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.server.packs.resources.IoSupplier;

public interface Node {
   default IoSupplier<InputStream> getStream(String[] paths, Function<String[], Path> resolve) {
      return () -> Files.newInputStream(resolve.apply(paths));
   }

   void output(String var1, Path var2, String var3, BiConsumer var4);
}
