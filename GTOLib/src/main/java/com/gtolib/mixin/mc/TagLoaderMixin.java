package com.gtolib.mixin.mc;

import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import com.gtocore.common.data.GTOTags;
import com.gtolib.cache.CacheManager;
import com.gtolib.utils.FileUtils;
import com.gtolib.utils.GTOUtils;
import com.gtolib.utils.RLUtils;
import com.gtolib.utils.TagUtils;
import com.gtolib.utils.iostream.IOStreamDecoder;
import com.gtolib.utils.iostream.IOStreamEncoder;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagLoader;
import net.minecraft.tags.TagLoader.EntryWithSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = TagLoader.class, priority = 2000)
public abstract class TagLoaderMixin {
   @Shadow
   @Final
   private String directory;

   @Overwrite
   public Map<ResourceLocation, List<EntryWithSource>> load(ResourceManager var1) {
      File var3 = CacheManager.getCacheFile(this.directory);
      ReentrantLock var4 = FileUtils.getFileLock(var3);
      var4.lock();

      Map<ResourceLocation, List<EntryWithSource>> var2;
      try {
         if (CacheManager.shouldCache() && var3.exists()) {
            var2 = FileUtils.loadFromFile(
               var3, IOStreamDecoder.map(HashMap::new, RLUtils.IO_CODEC, IOStreamDecoder.collection(ArrayList::new, TagUtils.SOURCE_IO_CODEC))
            );
         } else {
            O2OOpenCacheHashMap<ResourceLocation, List<EntryWithSource>> var5 = GTOTags.load(var1, this.directory);
            var2 = var5;
            if (CacheManager.shouldCache()) {
               O2OOpenCacheHashMap<ResourceLocation, List<EntryWithSource>> var6 = var5.clone();
               GTOUtils.asyncExecute(
                  var4, () -> FileUtils.saveToFile(var6, var3, IOStreamEncoder.map(RLUtils.IO_CODEC, IOStreamEncoder.collection(TagUtils.SOURCE_IO_CODEC)))
               );
            }
         }
      } finally {
         var4.unlock();
      }

      return var2;
   }
}
