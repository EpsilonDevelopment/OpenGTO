package com.gtolib.cache.pack;

import com.gtolib.GTOCore;
import com.gtolib.cache.resourcestree.ZIPResourcesTree;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PackResources.ResourceOutput;
import net.minecraft.server.packs.resources.IoSupplier;
import org.apache.commons.io.IOUtils;

public class FastFilePackResources extends AbstractFastPackResources {
   private final File file;
   private ZipFile zipFile;
   private ZIPResourcesTree tree;
   private boolean failedToLoad;

   public FastFilePackResources(String name, File file, boolean isBuiltin) {
      super(name, isBuiltin);
      this.file = file;
   }

   @Nullable
   @Override
   public IoSupplier<InputStream> getResource(PackType packType, ResourceLocation resourceLocation) {
      ZipFile zipFile = this.getOrCreateZipFile();
      return zipFile == null ? null : this.getTree(zipFile).getResource(packType, resourceLocation, null);
   }

   @Override
   public void close() {
      if (this.zipFile != null) {
         IOUtils.closeQuietly(this.zipFile);
         this.zipFile = null;
      }
   }

   private ZIPResourcesTree getTree(ZipFile zipFile) {
      if (this.tree != null) {
         return this.tree;
      }

      synchronized (this) {
         return this.tree != null ? this.tree : (this.tree = ZIPResourcesTree.get(this.packId(), zipFile));
      }
   }

   @Override
   public void listResources(PackType packType, String namespace, String path, ResourceOutput resourceOutput) {
      ZipFile zipFile = this.getOrCreateZipFile();
      if (zipFile != null) {
         this.getTree(zipFile).listResources(packType, namespace, path, resourceOutput);
      }
   }

   @Nullable
   @Override
   protected IoSupplier<InputStream> getMetadataResource() {
      ZipFile zipFile = this.getOrCreateZipFile();
      if (zipFile == null) {
         return null;
      }

      ZipEntry zipentry = zipFile.getEntry("pack.mcmeta");
      return zipentry == null ? null : IoSupplier.create(zipFile, zipentry);
   }

   @Override
   public Set<String> getNamespaces(PackType packType) {
      ZipFile zipFile = this.getOrCreateZipFile();
      return zipFile == null ? Collections.emptySet() : this.getTree(zipFile).getNamespaces(packType);
   }

   @Nullable
   @Override
   public IoSupplier<InputStream> getRootResource(String... parts) {
      ZipFile zipFile = this.getOrCreateZipFile();
      return zipFile == null ? null : this.getTree(zipFile).getRootResource(parts, null);
   }

   private ZipFile getOrCreateZipFile() {
      if (this.zipFile != null) {
         return this.zipFile;
      }

      if (this.failedToLoad) {
         return null;
      }

      try {
         this.zipFile = new ZipFile(this.file);
      } catch (IOException ioexception) {
         GTOCore.LOGGER.error("Failed to open pack {}", this.file, ioexception);
         this.failedToLoad = true;
         return null;
      }

      return this.zipFile;
   }
}
