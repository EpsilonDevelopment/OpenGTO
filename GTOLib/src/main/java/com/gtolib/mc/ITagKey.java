package com.gtolib.mc;

import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

public interface ITagKey {
   @NotNull
   Ingredient gtolib$getIngredient();
}
