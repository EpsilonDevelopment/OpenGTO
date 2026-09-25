package com.gtolib.api.item;

import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IItem {
   @NotNull
   ResourceLocation gtolib$getIdLocation();

   @NotNull
   default String gtolib$getIdString() {
      return this.gtolib$getIdLocation().toString();
   }

   @NotNull
   Ingredient gtolib$getIngredient();

   int @Nullable [] gtolib$getMapItem();

   void gtolib$setMapItem(int @NotNull [] var1);

   @NotNull
   ItemStack gtolib$getReadOnlyStack();

   void gtolib$setToolTips(Supplier<Component>... var1);

   Supplier<Component>[] gtolib$getToolTips();
}
