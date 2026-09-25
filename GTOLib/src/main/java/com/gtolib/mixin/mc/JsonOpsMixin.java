package com.gtolib.mixin.mc;

import com.google.common.util.concurrent.AtomicDouble;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.internal.LazilyParsedNumber;
import com.gtolib.utils.JsonUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@Mixin(JsonOps.class)
public class JsonOpsMixin {
   @Overwrite(remap = false)
   public <U> U convertTo(DynamicOps<U> var1, JsonElement var2) {
      return (U)(switch (var2) {
         case JsonNull var5 -> (Object)var1.empty();
         case JsonObject var6 -> (Object)JsonOps.INSTANCE.convertMap(var1, var6);
         case JsonArray var7 -> (Object)JsonOps.INSTANCE.convertList(var1, var7);
         case JsonPrimitive var8 -> {
            yield switch (JsonUtils.getValue(var8)) {
               case Boolean var9 -> (Object)var1.createBoolean(var9);
               case String var10 -> (Object)var1.createString(var10);
               case Byte var11 -> (Object)var1.createByte(var11);
               case Short var12 -> (Object)var1.createShort(var12);
               case Integer var13 -> (Object)var1.createInt(var13);
               case Long var14 -> (Object)var1.createLong(var14);
               case Float var15 -> (Object)var1.createFloat(var15);
               case Double var16 -> (Object)var1.createDouble(var16);
               case AtomicInteger var17 -> (Object)var1.createInt(var17.get());
               case AtomicLong var18 -> (Object)var1.createLong(var18.get());
               case AtomicDouble var19 -> (Object)var1.createDouble(var19.get());
               case LazilyParsedNumber var20 -> (Object)gtolib$convertTo(var1, var20);
               case BigInteger var21 -> (Object)gtolib$convertTo(var1, var21);
               case BigDecimal var22 -> (Object)gtolib$convertTo(var1, var22);
               case Number var23 -> (Object)gtolib$convertTo(var1, new BigDecimal(var23.toString()));
               default -> throw new IllegalStateException("Unsupported JsonPrimitive value type: " + var8);
            };
         }
         default -> null;
      });
   }

   @Unique
   private static <U> U gtolib$convertTo(DynamicOps<U> var0, LazilyParsedNumber var1) {
      try {
         long var2 = Long.parseLong(var1.toString());
         if ((byte)var2 == var2) {
            return (U)var0.createByte((byte)var2);
         } else if ((short)var2 == var2) {
            return (U)var0.createShort((short)var2);
         } else {
            return (U)((int)var2 == var2 ? var0.createInt((int)var2) : var0.createLong(var2));
         }
      } catch (NumberFormatException var5) {
         double var3 = var1.doubleValue();
         return (U)((float)var3 == var3 ? var0.createFloat((float)var3) : var0.createDouble(var3));
      }
   }

   @Unique
   private static <U> U gtolib$convertTo(DynamicOps<U> var0, BigInteger var1) {
      try {
         long var2 = var1.longValueExact();
         if ((byte)var2 == var2) {
            return (U)var0.createByte((byte)var2);
         } else if ((short)var2 == var2) {
            return (U)var0.createShort((short)var2);
         } else {
            return (U)((int)var2 == var2 ? var0.createInt((int)var2) : var0.createLong(var2));
         }
      } catch (ArithmeticException var5) {
         double var3 = var1.doubleValue();
         return (U)((float)var3 == var3 ? var0.createFloat((float)var3) : var0.createDouble(var3));
      }
   }

   @Unique
   private static <U> U gtolib$convertTo(DynamicOps<U> var0, BigDecimal var1) {
      try {
         long var2 = var1.longValueExact();
         if ((byte)var2 == var2) {
            return (U)var0.createByte((byte)var2);
         } else if ((short)var2 == var2) {
            return (U)var0.createShort((short)var2);
         } else {
            return (U)((int)var2 == var2 ? var0.createInt((int)var2) : var0.createLong(var2));
         }
      } catch (ArithmeticException var5) {
         double var3 = var1.doubleValue();
         return (U)((float)var3 == var3 ? var0.createFloat((float)var3) : var0.createDouble(var3));
      }
   }
}
