package com.gtolib.ae2.crafting.models;

import appeng.api.stacks.AEKey;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap.Entry;
import java.util.Iterator;
import org.jetbrains.annotations.NotNull;

public class DependencyGraph implements Iterable<Entry<AEKey, DependencyNode>> {
   private final Reference2ObjectOpenHashMap<AEKey, DependencyNode> nodes = new Reference2ObjectOpenHashMap<>();

   public DependencyNode getOrCreate(AEKey item) {
      return this.nodes.computeIfAbsent(item, k -> new DependencyNode());
   }

   public int size() {
      return this.nodes.size();
   }

   public DependencyNode getNode(AEKey item) {
      return this.nodes.get(item);
   }

   @NotNull
   @Override
   public Iterator<Entry<AEKey, DependencyNode>> iterator() {
      return this.nodes.reference2ObjectEntrySet().fastIterator();
   }

   @Override
   public final boolean equals(Object object) {
      return object instanceof DependencyGraph entries ? this.nodes.equals(entries.nodes) : false;
   }

   @Override
   public int hashCode() {
      return this.nodes.hashCode();
   }
}
