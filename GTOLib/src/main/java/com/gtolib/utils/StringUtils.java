package com.gtolib.utils;

import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;

public final class StringUtils {
   public static final String EMPTY = "";
   private static final String[] CHINESE_NUMBERS = new String[]{"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};

   private StringUtils() {
   }

   public static String numberToChinese(int number) {
      if (number >= 0 && number <= 9) {
         return CHINESE_NUMBERS[number];
      } else {
         throw new IllegalArgumentException("Number must be between 0 and 9");
      }
   }

   public static String[] decompose(String location) {
      return decompose(':', location);
   }

   private static String[] decompose(char spot, String string) {
      String[] astring = new String[]{string, ""};
      int i = string.indexOf(spot);
      if (i >= 0) {
         astring[1] = string.substring(i + 1);
         if (i >= 1) {
            astring[0] = string.substring(0, i);
         }
      }

      return astring;
   }

   public static String[] lastDecompose(char spot, String string) {
      String[] result = new String[]{string, ""};
      int lastIndex = string.lastIndexOf(spot);
      if (lastIndex >= 0) {
         result[1] = string.substring(lastIndex + 1);
         result[0] = string.substring(0, lastIndex);
      }

      return result;
   }

   public static boolean containsWithWildcard(String[] array, String target) {
      for (String element : array) {
         if (Pattern.matches(element, target)) {
            return true;
         }
      }

      return false;
   }

   public static String full_color(String input) {
      return formatting(
         input,
         new ChatFormatting[]{
            ChatFormatting.RED,
            ChatFormatting.GOLD,
            ChatFormatting.YELLOW,
            ChatFormatting.GREEN,
            ChatFormatting.AQUA,
            ChatFormatting.BLUE,
            ChatFormatting.LIGHT_PURPLE
         },
         80.0
      );
   }

   public static String dark_purplish_red(String input) {
      return formatting(input, new ChatFormatting[]{ChatFormatting.DARK_PURPLE, ChatFormatting.DARK_RED}, 160.0);
   }

   public static String white_blue(String input) {
      return formatting(
         input,
         new ChatFormatting[]{
            ChatFormatting.BLUE,
            ChatFormatting.BLUE,
            ChatFormatting.BLUE,
            ChatFormatting.BLUE,
            ChatFormatting.WHITE,
            ChatFormatting.BLUE,
            ChatFormatting.WHITE,
            ChatFormatting.WHITE,
            ChatFormatting.BLUE,
            ChatFormatting.WHITE,
            ChatFormatting.WHITE,
            ChatFormatting.BLUE,
            ChatFormatting.RED,
            ChatFormatting.WHITE
         },
         80.0
      );
   }

   public static String purplish_red(String input) {
      return formatting(input, new ChatFormatting[]{ChatFormatting.LIGHT_PURPLE, ChatFormatting.DARK_PURPLE}, 160.0);
   }

   public static String golden(String input) {
      return formatting(input, new ChatFormatting[]{ChatFormatting.YELLOW, ChatFormatting.GOLD}, 160.0);
   }

   public static String dark_green(String input) {
      return formatting(input, new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.DARK_GREEN}, 160.0);
   }

   private static String formatting(String input, ChatFormatting[] colours, double delay) {
      StringBuilder sb = new StringBuilder(input.length() * 3);
      if (delay <= 0.0) {
         delay = 0.001;
      }

      int offset = (int)Math.floor((System.currentTimeMillis() & 16383L) / delay) % colours.length;

      for (int i = 0; i < input.length(); i++) {
         char c = input.charAt(i);
         sb.append(colours[(colours.length + i - offset) % colours.length].toString());
         sb.append(c);
      }

      return sb.toString();
   }
}
