package com.gtolib.mixin.mc;

import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = ResourceKey.class, priority = Integer.MAX_VALUE)
public class ResourceKeyMixin {
   @Overwrite(remap = false)
   @Override
   public boolean equals(Object var1) {
      return this == var1;
   }
}
