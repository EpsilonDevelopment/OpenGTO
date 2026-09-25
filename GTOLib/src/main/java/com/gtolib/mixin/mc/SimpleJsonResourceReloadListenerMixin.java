package com.gtolib.mixin.mc;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.gtocore.common.data.GTOLoots;
import com.gtocore.common.data.GTORecipes;
import com.gtocore.data.recipe.RecipeFilter;
import com.gtolib.cache.CacheManager;
import com.gtolib.utils.FileUtils;
import com.gtolib.utils.GTOUtils;
import com.gtolib.utils.JsonUtils;
import com.gtolib.utils.RLUtils;
import com.gtolib.utils.iostream.IOStreamDecoder;
import com.gtolib.utils.iostream.IOStreamEncoder;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = SimpleJsonResourceReloadListener.class, priority = 2000)
public class SimpleJsonResourceReloadListenerMixin {
   @Overwrite
   public static void scanDirectory(ResourceManager var0, String var1, Gson var2, Map<ResourceLocation, JsonElement> var3) {
      File var5 = CacheManager.getCacheFile("json/" + var1);
      ReentrantLock var6 = FileUtils.getFileLock(var5);
      var6.lock();

      Map<ResourceLocation, JsonElement> var4;
      try {
         if (CacheManager.shouldCache() && var5.exists()) {
            var4 = FileUtils.loadFromFile(var5, IOStreamDecoder.map(RLUtils.IO_CODEC, JsonUtils.JSON_ELEMENT_CODEC));
         } else {
            Predicate var7;
            switch (var1) {
               case "advancements":
                  return;
               case "recipes":
                  if (GTORecipes.cache) {
                     return;
                  }

                  var7 = RecipeFilter.getJsonFilter();
                  break;
               case "loot_tables":
                  if (GTOLoots.cache) {
                     return;
                  }

                  var7 = GTOLoots.getFilter();
                  break;
               default:
                  var7 = null;
            }

            var4 = new HashMap<>();
            FileToIdConverter var13 = FileToIdConverter.json(var1);
            var var14 = var13.listMatchingResources(var0);
            var14.forEach((var4x, var5x) -> {
               ResourceLocation var6x = var13.fileToId(var4x);
               if (var7 == null || !var7.test(var6x)) {
                  try (BufferedReader var7x = var5x.openAsReader()) {
                     JsonElement var8 = GsonHelper.fromJson(var2, var7x, JsonElement.class);
                     var4.put(var6x, var8);
                  } catch (IOException var12x) {
                  }
               }
            });
            if (CacheManager.shouldCache()) {
               GTOUtils.asyncExecute(var6, () -> FileUtils.saveToFile(var4, var5, IOStreamEncoder.map(RLUtils.IO_CODEC, JsonUtils.JSON_ELEMENT_CODEC)));
            }
         }
      } finally {
         var6.unlock();
      }

      var3.putAll(var4);
   }
}
