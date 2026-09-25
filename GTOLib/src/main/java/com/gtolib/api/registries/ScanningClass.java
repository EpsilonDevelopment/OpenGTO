package com.gtolib.api.registries;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.gregtechceu.gtceu.GTCEu;
import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import com.gtolib.GTOCore;
import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.Scanned;
import com.gtolib.api.annotation.dynamic.DynamicInitialData;
import com.gtolib.api.annotation.dynamic.DynamicInitialValue;
import com.gtolib.api.annotation.language.RegisterEnumLang;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.lang.CNEN;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData;
import net.minecraftforge.forgespi.language.ModFileScanData.AnnotationData;
import org.objectweb.asm.Type;

public final class ScanningClass {
   public static final Map<String, CNEN> LANG = GTCEu.isDataGen() ? new O2OOpenCacheHashMap<>() : null;
   public static ImmutableMap<Class<?>, DynamicInitialData> VALUES;
   public static final ImmutableMap<Class<?>, DynamicInitialData> OBJECT_VALUES;
   public static void init() {
   }

   static {
      long time = System.currentTimeMillis();
      Builder<Class<?>, DynamicInitialData> values = ImmutableMap.builder();
      Builder<Class<?>, DynamicInitialData> objects = ImmutableMap.builder();
      Type annotationType = Type.getType(Scanned.class);
      Type dataGenerator = LANG == null ? null : Type.getType(DataGeneratorScanned.class);

      for (ModFileScanData scanData : ModList.get().getAllScanData()) {
         for (AnnotationData a : scanData.getAnnotations()) {
            Type annotation = a.annotationType();
            if (Objects.equals(annotation, annotationType) || dataGenerator != null && Objects.equals(annotation, dataGenerator)) {
               try {
                  Class<?> clazz = Class.forName(a.memberName());
                  DynamicInitialData allValue = null;
                  DynamicInitialData objectValue = null;

                  for (Field field : clazz.getDeclaredFields()) {
                     if (field.isAnnotationPresent(DynamicInitialValue.class)) {
                        if (allValue == null) {
                           allValue = new DynamicInitialData();
                        }

                        Object value = allValue.add(field);
                        if (value != null) {
                           if (objectValue == null) {
                              objectValue = new DynamicInitialData();
                           }

                           objectValue.add(value);
                        }
                     } else if (LANG != null && field.isAnnotationPresent(RegisterLanguage.class)) {
                        RegisterLanguage config = field.getAnnotation(RegisterLanguage.class);

                        try {
                           assert config != null;
                           String key = config.key();
                           if (key.isEmpty()) {
                              String namePrefix = config.namePrefix();
                              if (!namePrefix.isEmpty()) {
                                 key = namePrefix + "." + field.getName();
                              } else {
                                 field.setAccessible(true);
                                 key = (String)field.get(null);
                                 String valuePrefix = config.valuePrefix();
                                 if (!valuePrefix.isEmpty()) {
                                    key = valuePrefix + "." + key;
                                 }
                              }
                           }

                           LANG.put(key, new CNEN(config.cn(), config.en()));
                        } catch (IllegalAccessException e) {
                           throw new RuntimeException(e);
                        }
                     }
                  }

                  if (allValue != null) {
                     values.put(clazz, allValue);
                     if (objectValue != null) {
                        objects.put(clazz, objectValue);
                     }
                  }

                  if (LANG != null && clazz.isEnum()) {
                     RegisterEnumLang ann = clazz.getAnnotation(RegisterEnumLang.class);
                     if (ann != null) {
                        Map<String, String> enTranslations = new O2OOpenCacheHashMap<>();
                        Map<String, String> cnTranslations = new O2OOpenCacheHashMap<>();
                        String prefix = ann.keyPrefix();
                        Object[] constants = clazz.getEnumConstants();

                        for (Object constant : constants) {
                           String enumName = ((Enum)constant).name();

                           for (Field field : clazz.getDeclaredFields()) {
                              if (field.isAnnotationPresent(RegisterEnumLang.EnValue.class)) {
                                 RegisterEnumLang.EnValue enValue = field.getAnnotation(RegisterEnumLang.EnValue.class);
                                 field.setAccessible(true);
                                 if (enValue != null) {
                                    try {
                                       String key = prefix + "." + enValue.value() + "." + enumName;
                                       String value = (String)field.get(constant);
                                       if (cnTranslations.containsKey(key)) {
                                          LANG.put(key, new CNEN(cnTranslations.get(key), value));
                                          cnTranslations.remove(key);
                                       } else {
                                          enTranslations.put(key, value);
                                       }
                                    } catch (IllegalAccessException e) {
                                       throw new RuntimeException(e);
                                    }
                                 }
                              } else if (field.isAnnotationPresent(RegisterEnumLang.CnValue.class)) {
                                 RegisterEnumLang.CnValue cnValue = field.getAnnotation(RegisterEnumLang.CnValue.class);
                                 field.setAccessible(true);
                                 if (cnValue != null) {
                                    try {
                                       String key = prefix + "." + cnValue.value() + "." + enumName;
                                       String value = (String)field.get(constant);
                                       if (enTranslations.containsKey(key)) {
                                          LANG.put(key, new CNEN(value, enTranslations.get(key)));
                                          enTranslations.remove(key);
                                       } else {
                                          cnTranslations.put(key, value);
                                       }
                                    } catch (IllegalAccessException e) {
                                       throw new RuntimeException(e);
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               } catch (ClassNotFoundException | NoClassDefFoundError var35) {
               }
            }
         }
      }

      VALUES = values.build();
      OBJECT_VALUES = objects.build();
      GTOCore.LOGGER.info("ScanningClass init time: {}ms", System.currentTimeMillis() - time);
   }
}
