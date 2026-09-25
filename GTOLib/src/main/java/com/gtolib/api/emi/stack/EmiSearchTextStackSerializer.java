package com.gtolib.api.emi.stack;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.serializer.EmiStackSerializer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class EmiSearchTextStackSerializer implements EmiStackSerializer<EmiSearchTextStack> {
   @Override
   public String getType() {
      return "search_text";
   }

   @Override
   public EmiStack create(ResourceLocation id, CompoundTag nbt, long amount) {
      return new EmiSearchTextStack(nbt.getString("text"));
   }
}
