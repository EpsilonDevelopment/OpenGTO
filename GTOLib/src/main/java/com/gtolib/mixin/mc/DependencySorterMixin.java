package com.gtolib.mixin.mc;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.gto.fastcollection.fastutil.OpenCacheHashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.util.DependencySorter;
import net.minecraft.util.DependencySorter.Entry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = DependencySorter.class, priority = 2000)
public abstract class DependencySorterMixin<K, V extends Entry<K>> {
   @Mutable
   @Shadow
   @Final
   private Map<K, V> contents;

   @Shadow
   private static <K> void addDependencyIfNotCyclic(Multimap<K, K> var0, K var1, K var2) {
   }

   @Shadow
   protected abstract void visitDependenciesAndElement(Multimap<K, K> var1, Set<K> var2, K var3, BiConsumer<K, V> var4);

   @Overwrite
   public void orderByDependencies(BiConsumer<K, V> var1) {
      SetMultimap var2 = Multimaps.newSetMultimap(new HashMap<>(), OpenCacheHashSet::new);
      this.contents.forEach((var1x, var2x) -> var2x.visitRequiredDependencies(var2xx -> addDependencyIfNotCyclic(var2, (K)var1x, var2xx)));
      this.contents.forEach((var1x, var2x) -> var2x.visitOptionalDependencies(var2xx -> addDependencyIfNotCyclic(var2, (K)var1x, var2xx)));
      OpenCacheHashSet var3 = new OpenCacheHashSet();
      this.contents.keySet().forEach(var4 -> this.visitDependenciesAndElement(var2, var3, (K)var4, var1));
   }
}
