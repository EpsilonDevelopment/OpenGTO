package com.gtolib.api.annotation.dynamic;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import com.gtolib.GTOCore;
import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.lang.CNEN;
import com.gtolib.api.registries.ScanningClass;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

@DataGeneratorScanned
public final class DynamicInitialData {
   public static final Map<String, CNEN> LANG = GTCEu.isDataGen() ? new O2OOpenCacheHashMap<>() : null;
   public static final String PREFIX_DYNAMIC_VALUE = "gtocore.difficulty_config";
   private static final String PREFIX_DYNAMIC_VALUE_WITH_END = "gtocore.difficulty_config.";
   private static final String PREFIX_NAME = "gtocore.difficulty_config.name.";
   private static final String PREFIX_COMMENT = "gtocore.difficulty_config.comment.";
   public static final DynamicInitialData DEFAULT = new DynamicInitialData();
   private final List<DynamicInitialData.Value> list = new ArrayList<>();
   @RegisterLanguage(en = "(Easy) ", cn = "(简单) ")
   public static final String IS_EASY = "gtocore.difficulty_config.is_easy";
   @RegisterLanguage(en = "(Normal) ", cn = "(普通) ")
   public static final String IS_NORMAL = "gtocore.difficulty_config.is_normal";
   @RegisterLanguage(en = "(Expert) ", cn = "(专家) ")
   public static final String IS_EXPERT = "gtocore.difficulty_config.is_expert";
   @RegisterLanguage(en = "(Common) ", cn = "(通用) ")
   public static final String COMMON = "gtocore.difficulty_config.common";
   public void update(Object object) {
      for (DynamicInitialData.Value value : this.list) {
         try {
            value.field.set(object, value.value);
         } catch (IllegalAccessException e) {
            GTOCore.LOGGER.error("Failed to set difficulty config value for field {}", value.field.getName(), e);
         }
      }
   }

   public void add(Object value) {
      this.list.add((DynamicInitialData.Value)value);
   }

   public Object add(Field field) {
      field.setAccessible(true);
      DynamicInitialValue config = field.getAnnotation(DynamicInitialValue.class);
      assert config != null;
      if (ScanningClass.LANG != null) {
         ScanningClass.LANG.put("gtocore.difficulty_config.name." + config.key(), new CNEN(config.cn(), config.en()));
      }

      List<String> commentTranKeys = Collections.emptyList();
      if (!config.enComment().isEmpty() && !config.cnComment().isEmpty()) {
         commentTranKeys = new ArrayList<>();
         String[] split_en = config.enComment().lines().toArray(String[]::new);
         String[] split_cn = config.cnComment().lines().toArray(String[]::new);
         if (split_en.length != split_cn.length) {
            throw new IllegalArgumentException("enComment line number is not equal cnComment");
         }

         for (int i = 0; i < split_en.length; i++) {
            String key = "gtocore.difficulty_config.comment." + config.key() + "." + i;
            if (ScanningClass.LANG != null) {
               ScanningClass.LANG.put(key, new CNEN(split_cn[i], split_en[i]));
            }

            commentTranKeys.add(key);
         }
      }

      boolean isDifficultyConfig = !config.easyValue().equals(config.normalValue()) || !config.easyValue().equals(config.expertValue());

      DynamicInitialData.Value value = new DynamicInitialData.Value(field, parse(field.getType(), switch (GTOCore.difficulty) {
         case 1 -> config.easyValue();
         case 3 -> config.expertValue();
         default -> config.normalValue();
      }), config.key(), commentTranKeys, config.en().contains("%s"), isDifficultyConfig);
      this.list.add(value);
      if (Modifier.isStatic(field.getModifiers())) {
         try {
            field.set(null, value.value());
            return null;
         } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
         }
      } else {
         return value;
      }
   }

   public static Component getDifficultyComponent(int difficulty) {
      return switch (difficulty) {
         case 1 -> Component.translatable("gtocore.difficulty_config.is_easy").withStyle(ChatFormatting.GREEN);
         case 2 -> Component.translatable("gtocore.difficulty_config.is_normal").withStyle(ChatFormatting.YELLOW);
         case 3 -> Component.translatable("gtocore.difficulty_config.is_expert").withStyle(ChatFormatting.LIGHT_PURPLE);
         default -> Component.translatable("gtocore.difficulty_config.common");
      };
   }

   public static <T extends MachineBuilder<?>> T addTooltipsText(T builder, Class<?> clazz, Predicate<DynamicInitialData.Value> filter) {
      DynamicInitialData value = ScanningClass.VALUES.get(clazz);
      if (value == null) {
         throw new IllegalArgumentException("Class " + clazz.getName() + " is not annotated with @Scanned");
      }

      List<DynamicInitialData.Value> valueList = value.list;

      for (DynamicInitialData.Value fieldValue : valueList.stream()
         .filter(filter)
         .sorted(Comparator.comparing(value1 -> !value1.isDifficultyConfig()))
         .toList()) {
         String[] args = new String[]{
            fieldValue.value().toString(), fieldValue.value().toString(), fieldValue.value().toString(), fieldValue.value().toString()
         };
         MutableComponent nameComponent = Component.empty();
         if (fieldValue.isDifficultyConfig()) {
            Component difficultyComponent = switch (GTOCore.difficulty) {
               case 1 -> Component.translatable("gtocore.difficulty_config.is_easy").withStyle(ChatFormatting.DARK_AQUA);
               case 3 -> Component.translatable("gtocore.difficulty_config.is_expert").withStyle(ChatFormatting.RED);
               default -> Component.translatable("gtocore.difficulty_config.is_normal").withStyle(ChatFormatting.GREEN);
            };
            nameComponent.append(difficultyComponent);
         }

         if (fieldValue.hasFormatInName()) {
            nameComponent.append(Component.translatable("gtocore.difficulty_config.name." + fieldValue.key(), args).withStyle(ChatFormatting.AQUA));
         } else {
            nameComponent.append(
               Component.translatable("gtocore.difficulty_config.name." + fieldValue.key()).append(" : %s".formatted(args)).withStyle(ChatFormatting.AQUA)
            );
         }

         builder.tooltips(nameComponent);
         if (!fieldValue.commentTranKey().isEmpty()) {
            for (String key : fieldValue.commentTranKey()) {
               builder.tooltips(
                  Component.literal("  ").append(Component.translatable(key, args)).withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC)
               );
            }
         }
      }

      return builder;
   }

   private static Object parse(Class<?> type, String value) {
      if (type == int.class) {
         return Integer.parseInt(value);
      } else if (type == long.class) {
         return Long.parseLong(value);
      } else if (type == byte.class) {
         return Byte.parseByte(value);
      } else if (type == double.class) {
         return Double.parseDouble(value);
      } else if (type == float.class) {
         return Float.parseFloat(value);
      } else if (type == boolean.class) {
         return Boolean.parseBoolean(value);
      } else {
         throw new IllegalArgumentException("Unsupported type: " + type.getName());
      }
   }

   public record Value(Field field, Object value, String key, List<String> commentTranKey, boolean hasFormatInName, boolean isDifficultyConfig) {
   }
}
