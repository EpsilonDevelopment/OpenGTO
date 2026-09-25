package com.gtolib.utils.register;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import com.gtolib.GTOCore;
import com.gtolib.api.data.chemical.material.GTOMaterialBuilder;
import com.gtolib.api.lang.CNEN;
import java.util.Map;
import java.util.function.Supplier;

public final class MaterialsRegisterUtils {
   public static final Map<String, CNEN> LANG = GTCEu.isDataGen() ? new O2OOpenCacheHashMap<>() : null;

   private MaterialsRegisterUtils() {
   }

   private static void addLang(String name, Supplier<CNEN> supplier) {
      if (LANG != null) {
         CNEN lang = supplier.get();
         if (LANG.containsKey(name)) {
            GTOCore.LOGGER.error("Repetitive Key: {}", name);
            throw new IllegalStateException();
         }

         if (LANG.containsValue(lang)) {
            GTOCore.LOGGER.error("Repetitive Value: {}", lang);
            throw new IllegalStateException();
         }

         LANG.put(name, lang);
      }
   }

   public static GTOMaterialBuilder material(String name, String en, String cn) {
      addLang(name, () -> new CNEN(cn, en));
      return new GTOMaterialBuilder(name);
   }

   public static GTOMaterialBuilder material(String name, String cn) {
      addLang(name, () -> new CNEN(cn, FormattingUtil.toEnglishName(name)));
      return new GTOMaterialBuilder(name);
   }
}
