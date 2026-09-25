package com.gtolib.mixin.forge;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.gto.fastcollection.fastutil.OpenCacheHashSet;
import com.gtolib.forge.IForgeRegistry;
import com.gtolib.utils.TagUtils;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderSet.Named;
import net.minecraft.tags.TagKey;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraftforge/registries/NamespacedWrapper", priority = 1001)
public abstract class NamespacedWrapperMixin<T> {
   @Shadow(remap = false)
   private Map<T, Reference<T>> holders;
   @Shadow(remap = false)
   private volatile Map<TagKey<T>, Named<T>> tags;
   @Mutable
   @Shadow(remap = false)
   @Final
   private Multimap<TagKey<T>, Supplier<T>> optionalTags;
   @Shadow(remap = false)
   @Final
   private ForgeRegistry<T> delegate;

   @Shadow
   protected abstract Named<T> createTag(TagKey<T> var1);

   @Inject(method = "<init>", at = @At("TAIL"), remap = false)
   private void init(ForgeRegistry var1, Function var2, RegistryManager var3, CallbackInfo var4) {
      this.optionalTags = Multimaps.newSetMultimap(new Reference2ObjectOpenHashMap<>(), OpenCacheHashSet::new);
   }

   @Redirect(method = "onAdded", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/ObjectList;size(I)V"), remap = false)
   private void onAdded(ObjectList<T> var1, int var2) {
      int var3 = var1.size();
      if (var3 < var2) {
         var1.size(Integer.highestOneBit(var2) << 1);
      }
   }

   @Overwrite
   public void bindTags(Map<TagKey<T>, List<Holder<T>>> var1) {
      Reference2ObjectOpenHashMap<Reference<T>, Collection<TagKey<T>>> var2 = new Reference2ObjectOpenHashMap<>();
      var1.forEach((var1x, var2x) -> var2x.forEach(var2xx -> var2.computeIfAbsent((Reference)var2xx, var0xx -> new ReferenceOpenHashSet()).add(var1x)));
      Reference2ReferenceOpenHashMap<TagKey<T>, Named<T>> var3 = new Reference2ReferenceOpenHashMap<>(this.tags);
      var1.forEach((var2x, var3x) -> var3.computeIfAbsent(var2x, this::createTag).bind((List<Holder<T>>)var3x));
      SetView<TagKey<T>> var4 = Sets.difference(this.optionalTags.keySet(), var1.keySet());
      var4.forEach(var3x -> {
         List<Holder<T>> var4x = new ArrayList<>(this.optionalTags.get(var3x).stream().map(var1xx -> this.holders.get(var1xx.get())).filter(Objects::nonNull).distinct().toList());
         var4x.forEach(var2xx -> var2.computeIfAbsent((Reference)var2xx, var0x -> new ReferenceOpenHashSet()).add(var3x));
         var3.computeIfAbsent(var3x, this::createTag).bind(var4x);
      });
      var2.forEach(Reference::bindTags);
      this.tags = var3;
      ((IForgeRegistry)this.delegate).gtolib$onBindTags(this.tags, var4);
      if (TagUtils.bind) {
         TagUtils.bind = false;
         TagUtils.bindedCallbacks.forEach(Runnable::run);
         TagUtils.bindedCallbacks = null;
      }
   }

   @Overwrite
   public void resetTags() {
   }
}
