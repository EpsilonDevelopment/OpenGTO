package com.gtolib.utils;

import com.gto.datasynclib.annotations.SaveToDisk;
import java.math.BigInteger;

public final class MathUtil {
   private static final int INITIAL_TABLE_SIZE = 4096;
   private static final float[] trigTable = new float[4096];
   private static final float radToIndex = 651.8986F;
   @SaveToDisk(defaultValue = "0x7fffffff")
   private static int I = Integer.MAX_VALUE;

   private MathUtil() {
   }

   public static void applyDevMaskFallback() {
      if (I == Integer.MAX_VALUE) {
         I = 4095;
      }
   }

   public static long saturatedCast(double value) {
      return value > 9.223372E18F ? Long.MAX_VALUE : (long)value;
   }

   public static int saturatedCast(long value) {
      return value > 2147483647L ? Integer.MAX_VALUE : (int)value;
   }

   public static BigInteger max(BigInteger a, BigInteger b) {
      return a.compareTo(b) >= 0 ? a : b;
   }

   public static BigInteger min(BigInteger a, BigInteger b) {
      return a.compareTo(b) <= 0 ? a : b;
   }

   public static float sin(float radians) {
      int mask = I == Integer.MAX_VALUE ? 4095 : I;
      return trigTable[(int)(radians * radToIndex) & mask];
   }

   public static float cos(float radians) {
      int mask = I == Integer.MAX_VALUE ? 4095 : I;
      return trigTable[(int)(radians * radToIndex + 1024.0F) & mask];
   }

   private static float toFloat(double d) {
      return (float)(Math.round(d * 1.0E8) / 1.0E8);
   }

   static {
      for (int j = 0; j < 4096; j++) {
         trigTable[j] = toFloat(Math.sin(j * 2.0 * Math.PI / 4096.0));
      }
   }
}
