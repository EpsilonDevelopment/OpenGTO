package com.gtolib.mixin.lowdraglib;

import com.lowdragmc.lowdraglib.client.model.custommodel.LDLMetadataSection.Serializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Serializer.class)
public interface LDLMetadataSectionAccessor {
   @Accessor(value = "INSTANCE", remap = false)
   static Serializer instance() {
      return null;
   }
}
