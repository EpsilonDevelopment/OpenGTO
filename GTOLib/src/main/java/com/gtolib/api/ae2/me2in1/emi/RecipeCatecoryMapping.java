package com.gtolib.api.ae2.me2in1.emi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import java.io.File;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLPaths;

@OnlyIn(Dist.CLIENT)
public class RecipeCatecoryMapping {
   private static Map<ResourceLocation, String> category2Name;
   private static final Gson gson = new GsonBuilder()
      .registerTypeAdapter(ResourceLocation.class, new RecipeCatecoryMapping.ResourceLocationAdapter())
      .setPrettyPrinting()
      .create();
   public static File configFile = new File(FMLPaths.CONFIGDIR.get().toFile(), "me2in1category.json");

   public static void initialize() {
      if (configFile.exists()) {
         try {
            Map<?, ?> map = gson.fromJson(Files.newBufferedReader(configFile.toPath()), Map.class);
            category2Name = map.entrySet()
               .stream()
               .collect(
                  Collectors.toMap(
                     ex -> ResourceLocation.tryParse(ex.getKey().toString()), ex -> ex.getValue().toString(), (a, b) -> b, O2OOpenCacheHashMap::new
                  )
               );
            return;
         } catch (Exception e) {
            e.printStackTrace();
         }
      }

      category2Name = new O2OOpenCacheHashMap<>();

      try {
         if (!configFile.createNewFile()) {
            throw new Exception("Failed to create config file");
         }

         Files.writeString(configFile.toPath(), gson.toJson(category2Name));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public static void saveConfig() {
      try {
         Files.writeString(configFile.toPath(), gson.toJson(category2Name));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public static Map<ResourceLocation, String> getCategory2NameMap() {
      return category2Name;
   }

   public static void addMapping(ResourceLocation category, String name) {
      category2Name.put(category, name);
      saveConfig();
   }

   public static void removeMapping(ResourceLocation category) {
      category2Name.remove(category);
      saveConfig();
   }

   static {
      initialize();
   }

   private static class ResourceLocationAdapter implements JsonSerializer<ResourceLocation>, JsonDeserializer<ResourceLocation> {
      public JsonElement serialize(ResourceLocation src, Type typeOfSrc, JsonSerializationContext context) {
         return src == null ? JsonNull.INSTANCE : new JsonPrimitive(src.toString());
      }

      public ResourceLocation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
         if (json != null && !json.isJsonNull()) {
            String s = json.getAsString();
            ResourceLocation rl = ResourceLocation.tryParse(s);
            if (rl == null) {
               throw new JsonParseException("Invalid ResourceLocation: " + s);
            } else {
               return rl;
            }
         } else {
            return null;
         }
      }
   }
}
