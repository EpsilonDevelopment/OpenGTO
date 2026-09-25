package com.gtolib.cache.pack;

import com.gtolib.cache.resourcestree.CachedResourcesTree;
import com.gtolib.utils.PathUtils;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PackResources.ResourceOutput;
import net.minecraft.server.packs.resources.IoSupplier;

public class FastPathPackResources extends AbstractFastPackResources {
   private final Path root;
   private CachedResourcesTree tree;

   public FastPathPackResources(String name, Path root, boolean isBuiltin) {
      super(name, isBuiltin);
      this.root = root;
   }

   @Nullable
   @Override
   public IoSupplier<InputStream> getResource(PackType type, ResourceLocation location) {
      return this.getTree().getResource(type, location, this::resolve);
   }

   private Path resolve(String... paths) {
      return PathUtils.resolve(this.root, paths);
   }

   @Override
   public void close() {
   }

   private CachedResourcesTree getTree() {
      if (this.tree != null) {
         return this.tree;
      }

      synchronized (this) {
         return this.tree != null ? this.tree : (this.tree = CachedResourcesTree.get(this.packId(), type -> this.root.resolve(type.getDirectory())));
      }
   }

   @Override
   public void listResources(PackType packType, String namespace, String p_path, ResourceOutput resourceOutput) {
      this.getTree().listResources(packType, namespace, p_path, resourceOutput);
   }

   @Nullable
   @Override
   protected IoSupplier<InputStream> getMetadataResource() {
      Path path = this.root.resolve("pack.mcmeta");
      return Files.exists(path) ? IoSupplier.create(path) : null;
   }

   @Override
   public Set<String> getNamespaces(PackType type) {
      return this.getTree().getNamespaces(type);
   }

   @Nullable
   @Override
   public IoSupplier<InputStream> getRootResource(String... paths) {
      return this.getTree().getRootResource(paths, this::resolve);
   }
}
