package com.gtolib.api.adastra;

import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

public interface IAdDisplayTagName {
   Map<ResourceKey<Level>, List<IAdDisplayTagName.CountIngredient>> gtocore$getAdastraDisplayTagNames();

   record CountIngredient(Ingredient ingredient, int holderCount, int count) {
   }
}
