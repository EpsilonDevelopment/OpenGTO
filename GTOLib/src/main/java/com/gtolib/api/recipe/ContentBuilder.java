package com.gtolib.api.recipe;

import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentInner;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.ItemIngredient;
import it.unimi.dsi.fastutil.Hash.Strategy;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public final class ContentBuilder {
   public static final int maxChance = 10000;
   private final boolean list;
   private Object content;
   private int chance = 10000;
   private int tierChanceBoost = 0;
   public static final Strategy<Content> HASH_STRATEGY = new ContentBuilder.ContentHashStrategy();
   public static final Strategy<Content> IDENTITY_HASH_STRATEGY = new ContentBuilder.ContentIdentityHashStrategy();

   private ContentBuilder(boolean list) {
      this.list = list;
      if (list) {
         this.content = new ArrayList();
      }
   }

   public static ContentBuilder create() {
      return new ContentBuilder(false);
   }

   public static ContentBuilder createList() {
      return new ContentBuilder(true);
   }

   public ContentBuilder items(ItemStack... item) {
      for (ItemStack i : item) {
         ((List)this.content).add(ItemIngredient.of(i));
      }

      return this;
   }

   public ContentBuilder item(ItemStack item) {
      Object object = ItemIngredient.of(item);
      if (this.list) {
         ((List)this.content).add(object);
      } else {
         this.content = object;
      }

      return this;
   }

   public ContentBuilder fluids(FluidStack... fluid) {
      for (FluidStack f : fluid) {
         ((List)this.content).add(FluidIngredient.of(f));
      }

      return this;
   }

   public ContentBuilder fluid(FluidStack fluid) {
      Object object = FluidIngredient.of(fluid);
      if (this.list) {
         ((List)this.content).add(object);
      } else {
         this.content = object;
      }

      return this;
   }

   public Content<ItemIngredient> builderItem() {
      return new Content<ItemIngredient>((ItemIngredient)this.content, this.chance, this.tierChanceBoost);
   }

   public List<Content<ItemIngredient>> buildItemList() {
      return ((List)this.content).stream().map(c -> new Content<>((ContentInner)c, this.chance, this.tierChanceBoost)).toList();
   }

   public Content<FluidIngredient> builderFluid() {
      return new Content<FluidIngredient>((FluidIngredient)this.content, this.chance, this.tierChanceBoost);
   }

   public List<Content<FluidIngredient>> buildFluidList() {
      return ((List)this.content).stream().map(c -> new Content<>((ContentInner)c, this.chance, this.tierChanceBoost)).toList();
   }

   public ContentBuilder chance(int chance) {
      this.chance = chance;
      return this;
   }

   public ContentBuilder tierChanceBoost(int tierChanceBoost) {
      this.tierChanceBoost = tierChanceBoost;
      return this;
   }

   private static class ContentHashStrategy implements Strategy<Content> {
      public int hashCode(Content content) {
         int result = content.chance + 31 * content.tierChanceBoost;
         return 31 * result + content.inner.hashCode();
      }

      public boolean equals(Content a, Content b) {
         return a != null && b != null && a.chance == b.chance && a.tierChanceBoost == b.tierChanceBoost && a.inner.equals(b.inner);
      }
   }

   private static class ContentIdentityHashStrategy implements Strategy<Content> {
      public int hashCode(Content content) {
         int result = content.chance + 31 * content.tierChanceBoost;
         return 31 * result + System.identityHashCode(content.inner);
      }

      public boolean equals(Content a, Content b) {
         return a != null && b != null && a.chance == b.chance && a.tierChanceBoost == b.tierChanceBoost && a.inner == b.inner;
      }
   }
}
