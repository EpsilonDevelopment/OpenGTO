package com.gtolib.api.emi.stack;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKey;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.serializer.EmiStackSerializer;
import java.util.Map.Entry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class EmiTagprefixStackSerializer implements EmiStackSerializer<EmiTagprefixStack> {
   @Override
   public String getType() {
      return "tag_prefix";
   }

   @Override
   public EmiStack create(ResourceLocation id, CompoundTag nbt, long amount) {
      String name = id.getPath();
      if (name.startsWith("item/")) {
         String lowerCaseName = name.substring("item/".length());
         TagPrefix prefix = TagPrefix.PREFIXES
            .entrySet()
            .stream()
            .filter(e -> e.getKey().toLowerCase().equals(lowerCaseName))
            .map(Entry::getValue)
            .findFirst()
            .orElse(null);
         if (prefix != null) {
            return new EmiTagprefixStack(prefix);
         }
      }

      if (FluidStorageKey.getByName(id) != null) {
         FluidStorageKey storageKey = FluidStorageKey.getByName(id);
         return new EmiTagprefixStack(storageKey);
      } else {
         return EmiStack.EMPTY;
      }
   }
}
