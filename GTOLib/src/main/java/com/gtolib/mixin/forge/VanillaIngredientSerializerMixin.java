package com.gtolib.mixin.forge;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.gtolib.api.item.IItem;
import com.gtolib.mc.ITagKey;
import com.gtolib.utils.RLUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.VanillaIngredientSerializer;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(VanillaIngredientSerializer.class)
public class VanillaIngredientSerializerMixin {
   @Overwrite(remap = false)
   public Ingredient parse(JsonObject var1) {
      JsonElement var2 = var1.get("item");
      if (var2 != null) {
         Object var6 = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(var2.getAsString()));
         if (var6 == null) {
            throw new JsonSyntaxException("Unknown item '" + var2 + "'");
         } else {
            return ((IItem)var6).gtolib$getIngredient();
         }
      } else {
         JsonElement var3 = var1.get("tag");
         if (var3 != null) {
            ResourceLocation var4 = RLUtils.parse(var3.getAsString());
            TagKey var5 = TagKey.create(Registries.ITEM, var4);
            return ((ITagKey)(Object)var5).gtolib$getIngredient();
         } else {
            throw new JsonParseException("An ingredient entry needs either a tag or an item");
         }
      }
   }
}
