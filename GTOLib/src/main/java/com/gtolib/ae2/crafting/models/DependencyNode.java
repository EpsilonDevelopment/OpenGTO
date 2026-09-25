package com.gtolib.ae2.crafting.models;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyMap;
import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;
import java.util.ArrayList;
import lombok.Generated;

public class DependencyNode {
   private final ArrayList<IPatternDetails> patterns = new ArrayList<>();
   private final AEKeyMap dependencies = new AEKeyMap();

   public void addPattern(IPatternDetails pattern) {
      this.patterns.add(pattern);
   }

   public void addDependency(AEKey dependency, long multiplier) {
      this.dependencies.addTo(dependency, multiplier);
   }

   @Override
   public final boolean equals(Object object) {
      return !(object instanceof DependencyNode that) ? false : this.patterns.equals(that.patterns) && this.dependencies.equals(that.dependencies);
   }

   @Override
   public int hashCode() {
      int result = this.patterns.hashCode();
      return 31 * result + this.dependencies.hashCode();
   }

   public Reference2LongOpenHashMap<AEKey> getDependencies() {
      return this.dependencies;
   }

   @Generated
   public ArrayList<IPatternDetails> getPatterns() {
      return this.patterns;
   }
}
