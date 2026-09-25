package com.gtolib.api.emi.stack;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKey;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;

@DataGeneratorScanned
public class TagPrefixNameUtil {
   @RegisterLanguage(cn = "通配符", en = "Wildcard")
   public static final String TRANSLATE = "gtocore.aekey.tag_prefix";

   public static Component getName(boolean isFluidKey, TagPrefix tagPrefix, FluidStorageKey storageKey) {
      Component base;
      if (isFluidKey) {
         String specificKey = String.format("gtocore.any.%s", storageKey.getResourceLocation().getPath());
         if (Language.getInstance().has(specificKey)) {
            base = Component.translatable(specificKey);
         } else {
            base = Component.translatable(storageKey.getTranslationKeyFor(GTMaterials.NULL), Component.translatable("text.apotheosis.anything"));
         }
      } else {
         String specificKey = String.format("gtocore.any.%s", tagPrefix.getLowerCaseName());
         if (Language.getInstance().has(specificKey)) {
            base = Component.translatable(specificKey);
         } else {
            base = Component.translatable(tagPrefix.getUnlocalizedName(), Component.translatable("text.apotheosis.anything"));
         }
      }

      return Component.translatable("gtocore.aekey.tag_prefix").append("-").append(base);
   }
}
