package com.gtolib.api.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.client.model.geometry.IGeometryLoader;

public final class YLayeredModelLoader implements IGeometryLoader<YLayeredGeometry> {
   public static final YLayeredModelLoader INSTANCE = new YLayeredModelLoader();

   private YLayeredModelLoader() {
   }

   public YLayeredGeometry read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) throws JsonParseException {
      JsonObject config = GsonHelper.getAsJsonObject(jsonObject, "y_layered");
      JsonArray layersJson = GsonHelper.getAsJsonArray(config, "layers");
      if (layersJson.isEmpty()) {
         throw new JsonParseException("gtocore:y_layered requires at least one dynamic layer");
      }

      List<YLayeredGeometry.LayerSpec> layers = new ArrayList<>(layersJson.size());

      for (int i = 0; i < layersJson.size(); i++) {
         JsonObject layerJson = GsonHelper.convertToJsonObject(layersJson.get(i), "y_layered.layers[" + i + "]");
         String source = GsonHelper.getAsString(layerJson, "source");
         String texture = GsonHelper.getAsString(layerJson, "texture", defaultTextureReference(source));
         Integer tintIndex = layerJson.has("tint_index") ? GsonHelper.getAsInt(layerJson, "tint_index") : null;
         int columns = GsonHelper.getAsInt(layerJson, "columns", 1);
         int rows = GsonHelper.getAsInt(layerJson, "rows", 1);
         boolean wrap = GsonHelper.getAsBoolean(layerJson, "wrap", true);
         boolean replace = GsonHelper.getAsBoolean(layerJson, "replace", true);
         float depthOffset = GsonHelper.getAsFloat(layerJson, "depth_offset", replace ? 0.0F : 0.001F * (i + 1));
         if (columns < 1 || rows < 1) {
            throw new JsonParseException("gtocore:y_layered layer grid must be at least 1x1");
         }

         layers.add(new YLayeredGeometry.LayerSpec(source, texture, tintIndex, columns, rows, wrap, replace, depthOffset));
      }

      return new YLayeredGeometry(List.copyOf(layers));
   }

   private static String defaultTextureReference(String source) {
      return !source.startsWith("#") && !source.contains(":") && !source.contains("/") ? "#" + source : source;
   }
}
