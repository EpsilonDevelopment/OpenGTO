package com.gtolib.forge;

import java.util.Map;
import java.util.Set;
import net.minecraft.core.HolderSet.Named;
import net.minecraft.tags.TagKey;

public interface IForgeRegistry<V> {
   void gtolib$onBindTags(Map<TagKey<V>, Named<V>> var1, Set<TagKey<V>> var2);
}
