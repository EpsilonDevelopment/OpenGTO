package com.gtolib.cache.pack;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.annotation.Nullable;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;
import org.apache.commons.lang3.ArrayUtils;

public abstract class AbstractFastPackResources extends AbstractPackResources {
   private byte[] metadata;

   protected AbstractFastPackResources(String name, boolean isBuiltin) {
      super(name, isBuiltin);
   }

   @Nullable
   @Override
   public <T> T getMetadataSection(MetadataSectionSerializer<T> deserializer) throws IOException {
      if (this.metadata == null) {
         IoSupplier<InputStream> iosupplier = this.getMetadataResource();
         if (iosupplier == null) {
            this.metadata = ArrayUtils.EMPTY_BYTE_ARRAY;
            return null;
         }

         this.metadata = iosupplier.get().readAllBytes();
      } else if (this.metadata == ArrayUtils.EMPTY_BYTE_ARRAY) {
         return null;
      }

      try (ByteArrayInputStream inputstream = new ByteArrayInputStream(this.metadata)) {
         return getMetadataFromStream(deserializer, inputstream);
      }
   }

   @Nullable
   protected abstract IoSupplier<InputStream> getMetadataResource();
}
