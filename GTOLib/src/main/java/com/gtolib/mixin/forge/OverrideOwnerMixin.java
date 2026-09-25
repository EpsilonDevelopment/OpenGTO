package com.gtolib.mixin.forge;

import com.gtolib.forge.IOverrideOwner;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraftforge.registries.ForgeRegistry$OverrideOwner")
public abstract class OverrideOwnerMixin<V> implements IOverrideOwner<V> {
   @Shadow(remap = false)
   @Final
   private ResourceKey<V> key;

   @Override
   public ResourceKey<V> gtolib$k() {
      return this.key;
   }
}
