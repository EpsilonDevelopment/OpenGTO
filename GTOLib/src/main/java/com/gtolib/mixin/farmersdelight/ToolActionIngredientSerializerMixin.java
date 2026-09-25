package com.gtolib.mixin.farmersdelight;

import com.google.gson.JsonObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.ToolAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import vectorwing.farmersdelight.common.crafting.ingredient.ToolActionIngredient;
import vectorwing.farmersdelight.common.crafting.ingredient.ToolActionIngredient.Serializer;

@Mixin(value = Serializer.class, remap = false)
public class ToolActionIngredientSerializerMixin {
   @Unique
   private static final Map<String, ToolActionIngredient> gtolib$CACHE = new ConcurrentHashMap<>();

   @Overwrite
   public ToolActionIngredient parse(JsonObject var1) {
      return gtolib$CACHE.computeIfAbsent(var1.get("action").getAsString(), var0 -> new ToolActionIngredient(ToolAction.get(var0)));
   }

   @Overwrite
   public ToolActionIngredient parse(FriendlyByteBuf var1) {
      return gtolib$CACHE.computeIfAbsent(var1.readUtf(), var0 -> new ToolActionIngredient(ToolAction.get(var0)));
   }
}
