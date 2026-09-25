package com.gtolib.api.annotation.component_builder;

import com.gregtechceu.gtceu.GTCEu;
import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import com.gtolib.api.lang.CNEN;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public final class TranslationKeyProvider {
   public static final Object2ObjectOpenHashMap<String, CNEN> LANG = GTCEu.isDataGen() ? new O2OOpenCacheHashMap<>() : null;

   public static String getTranslationKey(String cn, String en) {
      return getTranslationKey(cn, en, "gtocore.lang");
   }

   public static String getTranslationKey(String cn, String en, String prefix) {
      String translationKey = (prefix != null ? prefix : "") + "." + en.hashCode();
      String var4 = translationKey.replace("..", ".");
      if (LANG != null) {
         LANG.put(var4, new CNEN(cn, en));
      }

      return var4;
   }
}
