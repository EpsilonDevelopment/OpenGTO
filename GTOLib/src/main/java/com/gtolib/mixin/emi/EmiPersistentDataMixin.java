package com.gtolib.mixin.emi;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.gtocore.config.GTOConfig;
import com.gtolib.Client;
import com.gtolib.GTOCore;
import com.llamalad7.mixinextras.sugar.Local;
import dev.emi.emi.bom.BoM;
import dev.emi.emi.runtime.EmiHidden;
import dev.emi.emi.runtime.EmiPersistentData;
import dev.emi.emi.runtime.EmiSidebars;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.UUID;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EmiPersistentData.class)
public class EmiPersistentDataMixin {
   @Unique
   private static final String FAVORITES_KEY = "favorites";
   @Unique
   private static final String LOOKUP_HISTORY_KEY = "lookup_history";
   @Unique
   private static final String CRAFT_HISTORY_KEY = "craft_history";
   @Unique
   private static final String RECIPE_DEFAULTS_KEY = "recipe_defaults";
   @Unique
   private static final String LEGACY_RECIPE_DEFAULTS_KEY = "recipeDefaults";
   @Unique
   private static final String HIDDEN_STACKS_KEY = "hidden_stacks";
   @Unique
   private static final String BY_ID_KEY = "byId";

   @Unique
   private static JsonObject gtolib$readPersist() throws IOException {
      if (!EmiPersistentData.FILE.exists()) {
         return new JsonObject();
      }

      try (FileReader var0 = new FileReader(EmiPersistentData.FILE)) {
         JsonObject var1 = EmiPersistentData.GSON.fromJson(var0, JsonObject.class);
         return var1 == null ? new JsonObject() : var1;
      }
   }

   @Inject(
      method = "save",
      at = @At(value = "INVOKE", target = "Ldev/emi/emi/runtime/EmiSidebars;save(Lcom/google/gson/JsonObject;)V"),
      remap = false,
      cancellable = true
   )
   private static void save(CallbackInfo var0, @Local(name = "json") JsonObject var1) throws IOException {
      EmiSidebars.save(var1);
      var1.add("recipe_defaults", BoM.saveAdded());
      var1.add("hidden_stacks", EmiHidden.save());
      JsonObject var2 = gtolib$readPersist();
      if (!GTOConfig.INSTANCE.misc.emiGlobalFavorites) {
         UUID var3 = Client.SERVER_IDENTIFIER;
         if (var3 == null) {
            GTOCore.LOGGER.warn("Server ID unavailable, skipping EMI persistent data save");
            var0.cancel();
            return;
         }

         JsonElement var4 = var2.get("byId");
         JsonObject var5 = var4 != null && var4.isJsonObject() ? var4.getAsJsonObject() : new JsonObject();
         var5.add(var3.toString(), var1);
         var2.add("byId", var5);
      } else {
         JsonElement var8 = var2.get("byId");
         var2 = var1;
         if (var8 != null && var8.isJsonObject()) {
            var2.add("byId", var8);
         }
      }

      try (FileWriter var9 = new FileWriter(EmiPersistentData.FILE)) {
         EmiPersistentData.GSON.toJson(var2, var9);
      }

      var0.cancel();
   }

   @Unique
   private static JsonObject gtolib$defaultData() {
      JsonObject var0 = new JsonObject();
      var0.add("favorites", new JsonArray());
      var0.add("lookup_history", new JsonArray());
      var0.add("craft_history", new JsonArray());
      var0.add("recipe_defaults", new JsonObject());
      var0.add("hidden_stacks", new JsonArray());
      return var0;
   }

   @Unique
   private static JsonObject gtolib$normalizeData(JsonObject var0) {
      gtolib$ensureArray(var0, "favorites");
      gtolib$ensureArray(var0, "lookup_history");
      gtolib$ensureArray(var0, "craft_history");
      gtolib$ensureArray(var0, "hidden_stacks");
      JsonElement var1 = var0.get("recipe_defaults");
      if (var1 == null || !var1.isJsonObject()) {
         JsonElement var2 = var0.get("recipeDefaults");
         var0.add("recipe_defaults", var2 != null && var2.isJsonObject() ? var2 : new JsonObject());
      }

      return var0;
   }

   @Unique
   private static void gtolib$ensureArray(JsonObject var0, String var1) {
      JsonElement var2 = var0.get(var1);
      if (var2 == null || !var2.isJsonArray()) {
         var0.add(var1, new JsonArray());
      }
   }

   @Redirect(
      method = "load",
      at = @At(value = "INVOKE", target = "Lcom/google/gson/Gson;fromJson(Ljava/io/Reader;Ljava/lang/Class;)Ljava/lang/Object;"),
      remap = false
   )
   private static Object gtolib$loadPersist(Gson var0, Reader var1, Class<?> var2) throws IOException {
      JsonObject var3;
      try (var1) {
         var3 = var0.fromJson(var1, JsonObject.class);
      }

      if (var3 == null) {
         var3 = new JsonObject();
      }

      JsonElement var9 = var3.get("byId");
      if (!GTOConfig.INSTANCE.misc.emiGlobalFavorites && var9 != null && var9.isJsonObject()) {
         UUID var5 = Client.SERVER_IDENTIFIER;
         if (var5 == null) {
            GTOCore.LOGGER.warn("Server ID unavailable, loading empty EMI persistent data until login completes");
            return gtolib$defaultData();
         } else {
            JsonElement var6 = var9.getAsJsonObject().get(var5.toString());
            return var6 != null && var6.isJsonObject() ? gtolib$normalizeData(var6.getAsJsonObject()) : gtolib$defaultData();
         }
      } else {
         return gtolib$normalizeData(var3);
      }
   }
}
