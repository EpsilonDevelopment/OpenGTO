package com.gtolib.api.data.chemical.material;

import com.gregtechceu.gtceu.api.data.chemical.material.properties.MaterialProperties;
import net.minecraft.world.item.Rarity;

public interface GTOMaterial {
   MaterialProperties gtolib$getProperties();

   Rarity gtolib$rarity();

   void gtolib$setRarity(Rarity var1);

   boolean gtolib$glow();

   void gtolib$setGlow();

   int gtolib$temp();

   void gtolib$setTemp(int var1);
}
