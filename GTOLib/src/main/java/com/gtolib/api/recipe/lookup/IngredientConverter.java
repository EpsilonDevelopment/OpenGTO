package com.gtolib.api.recipe.lookup;

import com.gto.recipesearch.IntLongMap;

public interface IngredientConverter<T> {
   void convert(T var1, long var2, IntLongMap var4);
}
