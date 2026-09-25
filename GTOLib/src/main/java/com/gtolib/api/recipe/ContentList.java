package com.gtolib.api.recipe;

import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentInner;
import com.gto.fastcollection.fastutil.O2OOpenCustomCacheHashMap;
import it.unimi.dsi.fastutil.Hash.Strategy;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class ContentList<T extends ContentInner> extends ArrayList<Content<T>> {
   private final Object2ObjectOpenCustomHashMap<Content<T>, Content<T>> map;

   public ContentList(List<Content<T>> list, Strategy<Content> strategy) {
      super(list.size());
      this.map = new O2OOpenCustomCacheHashMap<>(strategy);
      this.addAll(list);
   }

   public ContentList(Strategy<Content> strategy) {
      this.map = new O2OOpenCustomCacheHashMap<>(strategy);
   }

   @Override
   public void clear() {
      this.map.clear();
      super.clear();
   }

   @Override
   public boolean addAll(Collection<? extends Content<T>> c) {
      c.forEach(this::add);
      return !c.isEmpty();
   }

   public boolean add(Content content) {
      this.map.compute(content, (k, v) -> {
         if (v == null) {
            v = k.copy();
            super.add(v);
         } else {
            v.amount = k.amount + v.amount;
         }

         return v;
      });
      return true;
   }
}
