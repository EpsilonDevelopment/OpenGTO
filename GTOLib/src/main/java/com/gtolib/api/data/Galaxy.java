package com.gtolib.api.data;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterEnumLang;

@RegisterEnumLang(keyPrefix = "gtolib.galaxy")
@DataGeneratorScanned
public enum Galaxy {
   NONE("无", "None"),
   SOLAR("太阳系", "Solar System"),
   PROXIMA_CENTAURI("比邻星系", "Proxima Centauri"),
   BARNARDA("巴纳德星系", "Barnard's Star");

   @RegisterEnumLang.CnValue("name")
   private final String cn;
   @RegisterEnumLang.EnValue("name")
   private final String en;

   Galaxy(String cn, String en) {
      this.cn = cn;
      this.en = en;
   }

   public static Galaxy get(String name) {
      return GTODimensions.GALAXY_MAP.get(name);
   }

   // $VF: synthetic method
   private static Galaxy[] $values() {
      return new Galaxy[]{NONE, SOLAR, PROXIMA_CENTAURI, BARNARDA};
   }

   static {
      for (Galaxy galaxy : values()) {
         GTODimensions.GALAXY_MAP.put(galaxy.name(), galaxy);
      }
   }
}
