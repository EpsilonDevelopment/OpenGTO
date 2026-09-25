package com.gtolib.utils;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class NumberUtils {
   private static final DecimalFormat DF = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.ROOT));
   private static final DecimalFormat READABLE_DF = new DecimalFormat("#,##0.#", DecimalFormatSymbols.getInstance(Locale.ROOT));

   private NumberUtils() {
   }

   public static String formatDouble(double number) {
      if (number < 1000.0) {
         return DF.format(number);
      } else if (number < 1000000.0) {
         return DF.format(number / 1000.0) + "K";
      } else if (number < 1.0E9) {
         return DF.format(number / 1000000.0) + "M";
      } else if (number < 1.0E12) {
         return DF.format(number / 1.0E9) + "G";
      } else if (number < 1.0E15) {
         return DF.format(number / 1.0E12) + "T";
      } else if (number < 1.0E18) {
         return DF.format(number / 1.0E15) + "P";
      } else if (number < 1.0E21) {
         return DF.format(number / 1.0E18) + "E";
      } else if (number < 1.0E24) {
         return DF.format(number / 1.0E21) + "Z";
      } else if (number < 1.0E27) {
         return DF.format(number / 1.0E24) + "Y";
      } else if (number < 1.0E30) {
         return DF.format(number / 1.0E27) + "R";
      } else {
         return number < 1.0E33 ? DF.format(number / 1.0E30) + "Q" : "Infinity";
      }
   }

   public static String formatLong(long number) {
      if (number < 1000L) {
         return DF.format(number);
      } else if (number < 1000000L) {
         return DF.format(number / 1000.0) + "K";
      } else if (number < 1000000000L) {
         return DF.format(number / 1000000.0) + "M";
      } else if (number < 1000000000000L) {
         return DF.format(number / 1.0E9) + "G";
      } else if (number < 1000000000000000L) {
         return DF.format(number / 1.0E12) + "T";
      } else {
         return number < 1000000000000000000L ? DF.format(number / 1.0E15) + "P" : DF.format(number / 1.0E18) + "E";
      }
   }

   public static String formatLongToKorM(long number) {
      long absNumber = Math.abs(number);
      String sign = number < 0L ? "-" : "";
      if (absNumber < 1000L) {
         return String.valueOf(number);
      }

      if (absNumber < 1000000L) {
         return String.format(Locale.ROOT, "%s%.2fK", sign, absNumber / 1000.0);
      }

      double millions = absNumber / 1000000.0;
      String formatted = String.format(Locale.ROOT, "%.2f", millions);
      int dotIndex = formatted.indexOf(46);
      String integerPart;
      String decimalPart;
      if (dotIndex != -1) {
         integerPart = formatted.substring(0, dotIndex);
         decimalPart = formatted.substring(dotIndex);
      } else {
         integerPart = formatted;
         decimalPart = ".00";
      }

      StringBuilder sb = new StringBuilder();
      int i = integerPart.length() - 1;

      for (int count = 0; i >= 0; count++) {
         if (count > 0 && count % 3 == 0) {
            sb.insert(0, ',');
         }

         sb.insert(0, integerPart.charAt(i));
         i--;
      }

      return sign + sb + decimalPart + "M" + " (%s)".formatted(FormattingUtil.formatNumberReadable(number, false, READABLE_DF, null));
   }

   public static MutableComponent numberText(double number) {
      return Component.literal(formatDouble(number));
   }

   public static MutableComponent numberText(long number) {
      return Component.literal(formatLong(number));
   }

   public static int chanceOccurrences(int count, int chance, int max) {
      int occurrences = 0;

      for (int i = 0; i < count; i++) {
         if (occurrences == max) {
            return max;
         }

         if (GTValues.RNG.nextInt(chance) == 0) {
            occurrences++;
         }
      }

      return occurrences;
   }
}
