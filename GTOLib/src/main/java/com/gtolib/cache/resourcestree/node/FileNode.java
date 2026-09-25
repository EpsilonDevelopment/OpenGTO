package com.gtolib.cache.resourcestree.node;

import com.gtolib.utils.RLUtils;
import com.gtolib.utils.iostream.DataIOStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.BiConsumer;
import net.minecraft.server.packs.resources.IoSupplier;

public class FileNode implements SaveableNode {
   public static final FileNode INSTANCE = new FileNode();

   protected FileNode() {
   }

   @Override
   public void write(DataIOStream dos, boolean cached) throws IOException {
      dos.writeByte(0);
   }

   @Override
   public void output(String namespace, Path basePath, String path, BiConsumer output) {
      output.accept(RLUtils.fromNamespaceAndPath(namespace, path), IoSupplier.create(basePath.resolve(path)));
   }
}
