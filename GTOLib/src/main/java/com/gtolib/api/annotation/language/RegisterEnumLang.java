package com.gtolib.api.annotation.language;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RegisterEnumLang {
   String keyPrefix();

   @Target(ElementType.FIELD)
   @Retention(RetentionPolicy.RUNTIME)
   @interface CnValue {
      String value();
   }

   @Target(ElementType.FIELD)
   @Retention(RetentionPolicy.RUNTIME)
   @interface EnValue {
      String value();
   }
}
