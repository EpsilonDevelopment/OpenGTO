package com.gtolib.mixin.mc.pack;

import com.gtolib.cache.resourcestree.CachedResourcesTree;
import com.gtolib.utils.PathUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PackResources.ResourceOutput;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PathPackResources.class, priority = 0)
public abstract class PathPackResourcesMixin extends AbstractPackResources {
   @Shadow
   @Final
   private Path root;
   @Unique
   private CachedResourcesTree gtolib$tree;
   @Unique
   private byte[] gtolib$metadata;

   protected PathPackResourcesMixin(String var1, boolean var2) {
      super(var1, var2);
   }

   @Inject(
      method = "getResource(Lnet/minecraft/server/packs/PackType;Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/server/packs/resources/IoSupplier;",
      at = @At("HEAD"),
      cancellable = true
   )
   public void getResource(PackType var1, ResourceLocation var2, CallbackInfoReturnable<IoSupplier<InputStream>> var3) {
      var3.setReturnValue(this.gtolib$getTree().getResource(var1, var2, this::gtolib$resolve));
   }

   @Inject(method = "listResources", at = @At("HEAD"), cancellable = true)
   public void listResources(PackType var1, String var2, String var3, ResourceOutput var4, CallbackInfo var5) {
      this.gtolib$getTree().listResources(var1, var2, var3, var4);
      var5.cancel();
   }

   @Nullable
   @Override
   public <T> T getMetadataSection(MetadataSectionSerializer<T> var1) throws IOException {
      if (this.gtolib$metadata == null) {
         IoSupplier var2 = this.gtolib$getMetadataResource();
         if (var2 == null) {
            this.gtolib$metadata = ArrayUtils.EMPTY_BYTE_ARRAY;
            return null;
         }

         this.gtolib$metadata = ((InputStream)var2.get()).readAllBytes();
      } else if (this.gtolib$metadata == ArrayUtils.EMPTY_BYTE_ARRAY) {
         return null;
      }

      try (ByteArrayInputStream var7 = new ByteArrayInputStream(this.gtolib$metadata)) {
         return getMetadataFromStream(var1, var7);
      }
   }

   @Inject(method = "getNamespaces", at = @At("HEAD"), cancellable = true)
   public void getNamespaces(PackType var1, CallbackInfoReturnable<Set<String>> var2) {
      var2.setReturnValue(this.gtolib$getTree().getNamespaces(var1));
   }

   @Inject(method = "getRootResource", at = @At("HEAD"), cancellable = true)
   public void getRootResource(String[] var1, CallbackInfoReturnable<IoSupplier<InputStream>> var2) {
      var2.setReturnValue(this.gtolib$getTree().getRootResource(var1, this::gtolib$resolve));
   }

   @Unique
   private CachedResourcesTree gtolib$getTree() {
      if (this.gtolib$tree != null) {
         return this.gtolib$tree;
      }

      synchronized (this) {
         return this.gtolib$tree != null
            ? this.gtolib$tree
            : (this.gtolib$tree = CachedResourcesTree.get("p" + this.packId(), var1 -> this.root.resolve(var1.getDirectory())));
      }
   }

   @Unique
   @Nullable
   protected IoSupplier<InputStream> gtolib$getMetadataResource() {
      Path var1 = this.root.resolve("pack.mcmeta");
      return Files.exists(var1) ? IoSupplier.create(var1) : null;
   }

   @Unique
   private Path gtolib$resolve(String... var1) {
      return PathUtils.resolve(this.root, var1);
   }
}
