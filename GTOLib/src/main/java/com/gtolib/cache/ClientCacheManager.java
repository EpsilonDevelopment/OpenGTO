package com.gtolib.cache;

import com.gtolib.utils.FileUtils;
import com.gtolib.utils.RLUtils;
import com.gtolib.utils.iostream.DataIOStream;
import com.gtolib.utils.iostream.IOStreamCodec;
import com.gtolib.utils.iostream.IOStreamDecoder;
import com.gtolib.utils.iostream.IOStreamEncoder;
import com.gtolib.utils.reflect.FieldReference;
import com.lowdragmc.lowdraglib.client.model.custommodel.LDLMetadataSection;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.ResourceLocation;

public final class ClientCacheManager {
   public static Map<ResourceLocation, Boolean> RESOURCE_EXIST = new ConcurrentHashMap<>();
   private static final IOStreamCodec<LDLMetadataSection> LDLM_IO_CODEC = new IOStreamCodec<LDLMetadataSection>() {
      public void encode(DataIOStream stream, LDLMetadataSection obj) throws IOException {
         if (obj.connection == null) {
            stream.writeUTF(".");
         } else {
            RLUtils.IO_CODEC.encode(stream, obj.connection);
            stream.writeBoolean(obj.emissive);
         }
      }

      public LDLMetadataSection decode(DataIOStream stream) throws IOException {
         String namespace = stream.readUTF();
         if (namespace.equals(".")) {
            return LDLMetadataSection.MISSING;
         }

         ResourceLocation connection = RLUtils.fromNamespaceAndPath(namespace.intern(), stream.readUTF());
         return new LDLMetadataSection(stream.readBoolean(), connection);
      }
   };

   static void init() {
      File resourceExist = CacheManager.getCacheFile("resource_exist");
      if (resourceExist.exists()) {
         RESOURCE_EXIST = FileUtils.loadFromFile(resourceExist, IOStreamDecoder.map(HashMap::new, RLUtils.IO_CODEC, IOStreamCodec.BOOLEAN_CODEC));
      }

      File ctm = CacheManager.getCacheFile("ctm_metadata");
      if (ctm.exists()) {
         try {
            FieldReference<Map<ResourceLocation, LDLMetadataSection>> metadataField = FieldReference.fromClass(LDLMetadataSection.class, "METADATA_CACHE");
            metadataField.get().putAll(FileUtils.loadFromFile(ctm, IOStreamDecoder.map(RLUtils.IO_CODEC, LDLM_IO_CODEC)));
         } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
         }
      }
   }

   static void save() {
      FileUtils.saveToFile(RESOURCE_EXIST, CacheManager.getCacheFile("resource_exist"), IOStreamEncoder.map(RLUtils.IO_CODEC, IOStreamCodec.BOOLEAN_CODEC));
      RESOURCE_EXIST = new ConcurrentHashMap<>();

      try {
         FieldReference<Map<ResourceLocation, LDLMetadataSection>> metadataField = FieldReference.fromClass(LDLMetadataSection.class, "METADATA_CACHE");
         FileUtils.saveToFile(metadataField.get(), CacheManager.getCacheFile("ctm_metadata"), IOStreamEncoder.map(RLUtils.IO_CODEC, LDLM_IO_CODEC));
         metadataField.set(new ConcurrentHashMap<>());
      } catch (IllegalAccessException var1) {
      }
   }
}
