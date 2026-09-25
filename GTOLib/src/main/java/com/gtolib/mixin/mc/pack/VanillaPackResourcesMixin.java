package com.gtolib.mixin.mc.pack;

import com.gtolib.cache.resourcestree.SaveableResourcesTree;
import com.gtolib.utils.PathUtils;
import it.unimi.dsi.fastutil.Pair;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.PackResources.ResourceOutput;
import net.minecraft.server.packs.resources.IoSupplier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = VanillaPackResources.class, priority = 0)
public class VanillaPackResourcesMixin {
   @Shadow
   @Final
   private List<Path> rootPaths;
   @Shadow
   @Final
   private Map<PackType, List<Path>> pathsForType;
   @Unique
   private List<Pair<Path, SaveableResourcesTree>> gtolib$rootTrees;
   @Unique
   private Map<PackType, List<Pair<Path, SaveableResourcesTree>>> gtolib$treeForType;

   @Unique
   private List<Pair<Path, SaveableResourcesTree>> gtolib$getRootTrees() {
      if (this.gtolib$rootTrees != null) {
         return this.gtolib$rootTrees;
      }

      synchronized (this) {
         return this.gtolib$rootTrees != null
            ? this.gtolib$rootTrees
            : (
               this.gtolib$rootTrees = this.rootPaths
                  .stream()
                  .map(var0 -> Pair.of(var0, SaveableResourcesTree.get("vanilla" + var0.toString(), true, var1 -> var0.resolve(var1.getDirectory()))))
                  .toList()
            );
      }
   }

   @Unique
   private Map<PackType, List<Pair<Path, SaveableResourcesTree>>> gtolib$getTreeForType() {
      if (this.gtolib$treeForType != null) {
         return this.gtolib$treeForType;
      }

      synchronized (this) {
         if (this.gtolib$treeForType != null) {
            return this.gtolib$treeForType;
         }

         this.gtolib$treeForType = new EnumMap<>(PackType.class);

         for (Entry var3 : this.pathsForType.entrySet()) {
            this.gtolib$treeForType
               .put(
                  (PackType)var3.getKey(),
                  ((List<Path>)var3.getValue())
                     .stream()
                     .map(
                        var1 -> Pair.of(
                           var1, SaveableResourcesTree.get("vanilla_" + ((PackType)var3.getKey()).getDirectory() + var1.toString(), true, var1x -> var1)
                        )
                     )
                     .toList()
               );
         }

         return this.gtolib$treeForType;
      }
   }

   @Redirect(
      method = "getMetadataSection",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/server/packs/VanillaPackResources;getRootResource([Ljava/lang/String;)Lnet/minecraft/server/packs/resources/IoSupplier;"
      )
   )
   private IoSupplier<InputStream> getMetadataResource(VanillaPackResources var1, String[] var2) {
      for (Path var4 : this.rootPaths) {
         Path var5 = var4.resolve("pack.mcmeta");
         if (Files.exists(var5)) {
            return IoSupplier.create(var5);
         }
      }

      return null;
   }

   @Inject(method = "getRootResource", at = @At("HEAD"), cancellable = true)
   private void getRootResource(String[] var1, CallbackInfoReturnable<IoSupplier<InputStream>> var2) {
      for (Pair var4 : this.gtolib$getRootTrees()) {
         IoSupplier var5 = ((SaveableResourcesTree)var4.right()).getRootResource(var1, var1x -> PathUtils.resolve((Path)var4.left(), var1x));
         if (var5 != null) {
            var2.setReturnValue(var5);
            return;
         }
      }

      var2.setReturnValue(null);
   }

   @Inject(method = "listResources", at = @At("HEAD"), cancellable = true)
   public void listResources(PackType var1, String var2, String var3, ResourceOutput var4, CallbackInfo var5) {
      var5.cancel();
      List var6 = this.gtolib$getTreeForType().get(var1);
      int var7 = var6.size();
      if (var7 == 1) {
         ((SaveableResourcesTree)((Pair)var6.get(0)).right()).listResources(var1, var2, var3, var4);
      } else if (var7 > 1) {
         HashMap var8 = new HashMap();

         for (int var9 = 0; var9 < var7 - 1; var9++) {
            ((SaveableResourcesTree)((Pair)var6.get(var9)).right()).listResources(var1, var2, var3, var8::putIfAbsent);
         }

         if (var8.isEmpty()) {
            ((SaveableResourcesTree)((Pair)var6.get(var7 - 1)).right()).listResources(var1, var2, var3, var4);
         } else {
            ((SaveableResourcesTree)((Pair)var6.get(var7 - 1)).right()).listResources(var1, var2, var3, var8::putIfAbsent);
            var8.forEach(var4);
         }
      }
   }

   @Inject(method = "getResource", at = @At("HEAD"), cancellable = true)
   public void getResource(PackType var1, ResourceLocation var2, CallbackInfoReturnable<IoSupplier<InputStream>> var3) {
      for (Pair var5 : this.gtolib$getTreeForType().get(var1)) {
         IoSupplier var6 = ((SaveableResourcesTree)var5.right()).getResource(var1, var2, var1x -> PathUtils.resolve((Path)var5.left(), 1, var1x));
         if (var6 != null) {
            var3.setReturnValue(var6);
            return;
         }
      }

      var3.setReturnValue(null);
   }
}
