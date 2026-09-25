package com.gtolib.mixin.mc;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Set;
import net.minecraft.core.Holder.Reference;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = Reference.class, priority = 2000)
public class ReferenceMixin {
   @Shadow
   public Set tags;

   @Overwrite
   public void bindTags(Collection var1) {
      if (var1 instanceof Set var2) {
         this.tags = var2;
      } else {
         this.tags = ImmutableSet.copyOf(var1);
      }
   }
}
