package com.gtolib.mixin.mc;

import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = ResourceLocation.class, priority = 2000)
public class ResourceLocationMixn {
   @Shadow
   @Final
   private String namespace;
   @Shadow
   @Final
   private String path;
   @Unique
   private boolean gtolib$h;
   @Unique
   private int gtolib$hashCode;
   @Unique
   private String gtolib$string;

   @Overwrite
   @Override
   public String toString() {
      if (this.gtolib$string == null) {
         this.gtolib$string = this.namespace + ":" + this.path;
      }

      return this.gtolib$string;
   }

   @Overwrite(remap = false)
   @Override
   public int hashCode() {
      if (this.gtolib$h) {
         return this.gtolib$hashCode;
      }

      this.gtolib$h = true;
      return this.gtolib$hashCode = 31 * this.namespace.hashCode() + this.path.hashCode();
   }

   @Overwrite(remap = false)
   @Override
   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      }

      if (!(var1 instanceof ResourceLocation)) {
         return false;
      }

      ResourceLocationMixn var2 = (ResourceLocationMixn)var1;
      return this.gtolib$h && var2.gtolib$h && this.gtolib$hashCode != var2.gtolib$hashCode
         ? false
         : this.namespace.equals(var2.namespace) && this.path.equals(var2.path);
   }

   @Overwrite
   private static String assertValidNamespace(String var0, String var1) {
      return var0;
   }

   @Overwrite
   private static String assertValidPath(String var0, String var1) {
      return var1;
   }

   @Overwrite
   public ResourceLocation withPath(String var1) {
      return new ResourceLocation(this.namespace, var1, null);
   }
}
