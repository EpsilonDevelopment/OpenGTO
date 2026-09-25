package com.gtolib.cache.pack;

import com.gtolib.cache.CacheManager;
import com.gtolib.cache.resourcestree.CachedResourcesTree;
import com.gtolib.cache.resourcestree.SaveableResourcesTree;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PackResources.ResourceOutput;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.resource.PathPackResources;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FastModPackResources extends PathPackResources {
   private final IModFile modFile;
   private CachedResourcesTree tree;

   public FastModPackResources(IModFile modFile) {
      super(modFile.getFileName(), true, modFile.getFilePath());
      this.modFile = modFile;
   }

   @NotNull
   @Override
   protected Path resolve(@NotNull String... paths) {
      return this.modFile.findResource(paths);
   }

   @NotNull
   private CachedResourcesTree getTree() {
      if (this.tree != null) {
         return this.tree;
      }

      synchronized (this) {
         if (this.tree != null) {
            return this.tree;
         }

         String id = this.packId();
         Function<PackType, Path> basePathRetriever = type -> this.resolve(type.getDirectory());
         this.tree = CacheManager.shouldCache() ? SaveableResourcesTree.get(id, true, basePathRetriever) : CachedResourcesTree.get(id, basePathRetriever);
         this.tree.setCacheExternal(true);
         return this.tree;
      }
   }

   @Override
   public void listResources(@NotNull PackType type, @NotNull String namespace, @NotNull String path, @NotNull ResourceOutput resourceOutput) {
      this.getTree().listResources(type, namespace, path, resourceOutput);
   }

   @Nullable
   @Override
   public IoSupplier<InputStream> getRootResource(String @NotNull ... paths) {
      return this.getTree().getRootResource(paths, this::resolve);
   }

   @NotNull
   @Override
   public Set<String> getNamespaces(@NotNull PackType type) {
      return this.getTree().getNamespaces(type);
   }
}
