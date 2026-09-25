package com.gtolib.mixin.mc;

import com.gtolib.mc.IIngredient;
import com.gtolib.mc.ITagKey;
import com.gtolib.utils.EmptyStream;
import com.gtolib.utils.GTOUtils;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Ingredient.TagValue;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = TagKey.class, priority = Integer.MAX_VALUE)
public class TagKeyMixin implements ITagKey {
   @Unique
   private Ingredient gtolib$ingredient;

   @NotNull
   @Override
   public Ingredient gtolib$getIngredient() {
      if (this.gtolib$ingredient == null) {
         this.gtolib$ingredient = new Ingredient(EmptyStream.create(GTOUtils.array(new TagValue((TagKey<Item>)(Object)this))));
         ((IIngredient)this.gtolib$ingredient).gtolib$setCache();
      }

      return this.gtolib$ingredient;
   }
}
