package com.gtolib.mixin.forge;

import com.google.common.collect.BiMap;
import com.gtolib.forge.IForgeRegistry;
import com.gtolib.forge.IOverrideOwner;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.HolderSet.Named;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.registries.ForgeRegistry;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ForgeRegistry.class, priority = 1001)
public abstract class ForgeRegistryMixin<V> implements IForgeRegistry<V> {
   @Shadow(remap = false)
   @Final
   private BiMap owners;
   @Shadow(remap = false)
   @Final
   private ResourceLocation defaultKey;

   @Shadow(remap = false)
   abstract void onBindTags(Map<TagKey<V>, Named<V>> var1, Set<TagKey<V>> var2);

   @Override
   public void gtolib$onBindTags(Map<TagKey<V>, Named<V>> var1, Set<TagKey<V>> var2) {
      this.onBindTags(var1, var2);
   }

   @Overwrite(remap = false)
   public ResourceLocation getKey(V var1) {
      Object var2 = this.owners.inverse().get(var1);
      return var2 != null ? ((IOverrideOwner)var2).gtolib$k().location() : this.defaultKey;
   }

   @Overwrite(remap = false)
   @NotNull
   public Optional<ResourceKey<V>> getResourceKey(V var1) {
      Object var2 = this.owners.inverse().get(var1);
      return var2 != null ? Optional.of(((IOverrideOwner)var2).gtolib$k()) : Optional.empty();
   }
}
