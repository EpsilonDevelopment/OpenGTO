package com.gtolib.api.client;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.gtolib.GTOCore;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

public class YLayeredModelBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {
   private final List<YLayeredModelBuilder.LayerData> layers = new ArrayList<>();

   public static <T extends ModelBuilder<T>> YLayeredModelBuilder<T> begin(T parent, ExistingFileHelper existingFileHelper) {
      return new YLayeredModelBuilder<>(parent, existingFileHelper);
   }

   protected YLayeredModelBuilder(T parent, ExistingFileHelper existingFileHelper) {
      super(GTOCore.id("y_layered"), parent, existingFileHelper);
   }

   public YLayeredModelBuilder<T>.LayerBuilder layer(String source) {
      return this.layer(source, defaultTextureReference(source));
   }

   public YLayeredModelBuilder<T>.LayerBuilder layer(ResourceLocation source) {
      return this.layer(source.toString());
   }

   public YLayeredModelBuilder<T>.LayerBuilder layer(String source, ResourceLocation texture) {
      return this.layer(source, texture.toString());
   }

   public YLayeredModelBuilder<T>.LayerBuilder layer(ResourceLocation source, ResourceLocation texture) {
      return this.layer(source.toString(), texture.toString());
   }

   public YLayeredModelBuilder<T>.LayerBuilder layer(String source, String texture) {
      this.validateReference(source, "source");
      this.validateReference(texture, "texture");
      YLayeredModelBuilder.LayerData data = new YLayeredModelBuilder.LayerData(source, texture);
      this.layers.add(data);
      return new YLayeredModelBuilder.LayerBuilder(data);
   }

   @Override
   public JsonObject toJson(JsonObject json) {
      JsonObject var6 = super.toJson(json);
      Preconditions.checkState(!this.layers.isEmpty(), "gtocore:y_layered requires at least one layer");
      JsonArray layersJson = new JsonArray();

      for (YLayeredModelBuilder.LayerData layer : this.layers) {
         JsonObject layerJson = new JsonObject();
         layerJson.addProperty("source", layer.source);
         layerJson.addProperty("texture", layer.texture);
         if (layer.tintIndex != null) {
            layerJson.addProperty("tint_index", layer.tintIndex);
         }

         if (layer.columns != 1) {
            layerJson.addProperty("columns", layer.columns);
         }

         if (layer.rows != 1) {
            layerJson.addProperty("rows", layer.rows);
         }

         if (!layer.wrap) {
            layerJson.addProperty("wrap", false);
         }

         if (!layer.replace) {
            layerJson.addProperty("replace", false);
         }

         if (layer.depthOffset != null) {
            layerJson.addProperty("depth_offset", layer.depthOffset);
         }

         layersJson.add(layerJson);
      }

      JsonObject layeredJson = new JsonObject();
      layeredJson.add("layers", layersJson);
      var6.add("y_layered", layeredJson);
      return var6;
   }

   private void validateReference(String reference, String fieldName) {
      Preconditions.checkNotNull(reference, "%s must not be null", fieldName);
      Preconditions.checkArgument(!reference.isBlank(), "%s must not be blank", fieldName);
      if (!isTextureKey(reference)) {
         ResourceLocation location = ResourceLocation.tryParse(reference);
         Preconditions.checkArgument(location != null, "Invalid %s texture reference: %s", fieldName, reference);
         Preconditions.checkArgument(
            this.existingFileHelper.exists(location, PackType.CLIENT_RESOURCES, ".png", "textures"),
            "Texture %s does not exist in any known resource pack",
            location
         );
      }
   }

   private static boolean isTextureKey(String reference) {
      return reference.startsWith("#") || !reference.contains(":") && !reference.contains("/");
   }

   private static String defaultTextureReference(String source) {
      return !source.startsWith("#") && !source.contains(":") && !source.contains("/") ? "#" + source : source;
   }

   public final class LayerBuilder {
      private final YLayeredModelBuilder.LayerData data;

      private LayerBuilder(YLayeredModelBuilder.LayerData data) {
         this.data = data;
      }

      public YLayeredModelBuilder<T>.LayerBuilder source(String source) {
         YLayeredModelBuilder.this.validateReference(source, "source");
         this.data.source = source;
         return this;
      }

      public YLayeredModelBuilder<T>.LayerBuilder source(ResourceLocation source) {
         return this.source(source.toString());
      }

      public YLayeredModelBuilder<T>.LayerBuilder texture(String texture) {
         YLayeredModelBuilder.this.validateReference(texture, "texture");
         this.data.texture = texture;
         return this;
      }

      public YLayeredModelBuilder<T>.LayerBuilder texture(ResourceLocation texture) {
         return this.texture(texture.toString());
      }

      public YLayeredModelBuilder<T>.LayerBuilder tintIndex(int tintIndex) {
         this.data.tintIndex = tintIndex;
         return this;
      }

      public YLayeredModelBuilder<T>.LayerBuilder clearTintIndex() {
         this.data.tintIndex = null;
         return this;
      }

      public YLayeredModelBuilder<T>.LayerBuilder columns(int columns) {
         Preconditions.checkArgument(columns >= 1, "columns must be at least 1");
         this.data.columns = columns;
         return this;
      }

      public YLayeredModelBuilder<T>.LayerBuilder rows(int rows) {
         Preconditions.checkArgument(rows >= 1, "rows must be at least 1");
         this.data.rows = rows;
         return this;
      }

      public YLayeredModelBuilder<T>.LayerBuilder grid(int columns, int rows) {
         return this.columns(columns).rows(rows);
      }

      public YLayeredModelBuilder<T>.LayerBuilder wrap(boolean wrap) {
         this.data.wrap = wrap;
         return this;
      }

      public YLayeredModelBuilder<T>.LayerBuilder replace(boolean replace) {
         this.data.replace = replace;
         return this;
      }

      public YLayeredModelBuilder<T>.LayerBuilder depthOffset(float depthOffset) {
         this.data.depthOffset = depthOffset;
         return this;
      }

      public YLayeredModelBuilder<T> end() {
         return YLayeredModelBuilder.this;
      }
   }

   private static final class LayerData {
      private String source;
      private String texture;
      private Integer tintIndex;
      private int columns = 1;
      private int rows = 1;
      private boolean wrap = true;
      private boolean replace = true;
      private Float depthOffset;

      private LayerData(String source, String texture) {
         this.source = source;
         this.texture = texture;
      }
   }
}
