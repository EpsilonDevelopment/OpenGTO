package com.gtolib.utils;

import com.google.common.util.concurrent.AtomicDouble;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.internal.LazilyParsedNumber;
import com.gtolib.utils.iostream.DataIOStream;
import com.gtolib.utils.iostream.IOStreamCodec;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Generated;

public final class JsonUtils {
   private static final VarHandle VALUE;
   private static final byte TYPE_NULL = 0;
   private static final byte TYPE_BOOLEAN = 1;
   private static final byte TYPE_BYTE = 2;
   private static final byte TYPE_SHORT = 3;
   private static final byte TYPE_INT = 4;
   private static final byte TYPE_LONG = 5;
   private static final byte TYPE_FLOAT = 6;
   private static final byte TYPE_DOUBLE = 7;
   private static final byte TYPE_BIG_INTEGER = 8;
   private static final byte TYPE_BIG_DECIMAL = 9;
   private static final byte TYPE_STRING = 10;
   private static final byte TYPE_CHAR = 11;
   private static final byte TYPE_LAZILY_PARSED = 12;
   public static final IOStreamCodec<JsonElement> JSON_ELEMENT_CODEC;
   public static final IOStreamCodec<JsonPrimitive> JSON_PRIMITIVE_CODEC;
   public static final IOStreamCodec<JsonArray> JSON_ARRAY_CODEC;
   public static final IOStreamCodec<JsonObject> JSON_OBJECT_CODEC;

   public static Object getValue(JsonPrimitive jsonPrimitive) {
      return (Object)VALUE.get((JsonPrimitive)jsonPrimitive);
   }

   @Generated
   private JsonUtils() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   static {
      try {
         VALUE = MethodHandles.privateLookupIn(JsonPrimitive.class, MethodHandles.lookup()).findVarHandle(JsonPrimitive.class, "value", Object.class);
      } catch (Exception e) {
         throw new ExceptionInInitializerError(e);
      }

      JSON_ELEMENT_CODEC = new IOStreamCodec<JsonElement>() {
         public void encode(DataIOStream stream, JsonElement obj) throws IOException {
            switch (obj) {
               case JsonNull ignored:
                  stream.writeByte(0);
                  break;
               case JsonPrimitive p:
                  stream.writeByte(1);
                  JsonUtils.JSON_PRIMITIVE_CODEC.encode(stream, p);
                  break;
               case JsonArray a:
                  stream.writeByte(2);
                  JsonUtils.JSON_ARRAY_CODEC.encode(stream, a);
                  break;
               case JsonObject o:
                  stream.writeByte(3);
                  JsonUtils.JSON_OBJECT_CODEC.encode(stream, o);
                  break;
               default:
                  throw new IllegalStateException("Unexpected value: " + obj);
            }
         }

         public JsonElement decode(DataIOStream stream) throws IOException {
            byte type = stream.readByte();

            return switch (type) {
               case 0 -> JsonNull.INSTANCE;
               case 1 -> (JsonPrimitive)JsonUtils.JSON_PRIMITIVE_CODEC.decode(stream);
               case 2 -> (JsonArray)JsonUtils.JSON_ARRAY_CODEC.decode(stream);
               case 3 -> (JsonObject)JsonUtils.JSON_OBJECT_CODEC.decode(stream);
               default -> throw new IllegalStateException("Unexpected value: " + type);
            };
         }
      };
      JSON_PRIMITIVE_CODEC = new IOStreamCodec<JsonPrimitive>() {
         public void encode(DataIOStream stream, JsonPrimitive obj) throws IOException {
            Object value = (Object)JsonUtils.VALUE.get((JsonPrimitive)obj);
            switch (value) {
               case null:
                  stream.writeByte(0);
                  break;
               case Boolean b:
                  stream.writeByte(1);
                  stream.writeBoolean(b);
                  break;
               case String s:
                  stream.writeByte(10);
                  stream.writeUTF(s);
                  break;
               case Byte b:
                  stream.writeByte(2);
                  stream.writeByte(b);
                  break;
               case Short s:
                  stream.writeByte(3);
                  stream.writeShort(s);
                  break;
               case Integer i:
                  stream.writeByte(4);
                  stream.writeInt(i);
                  break;
               case Long l:
                  stream.writeByte(5);
                  stream.writeLong(l);
                  break;
               case Float f:
                  stream.writeByte(6);
                  stream.writeFloat(f);
                  break;
               case Double d:
                  stream.writeByte(7);
                  stream.writeDouble(d);
                  break;
               case Character c:
                  stream.writeByte(11);
                  stream.writeChar(c);
                  break;
               case LazilyParsedNumber p:
                  String numStr = p.toString();
                  if (isInteger(numStr)) {
                     try {
                        long l = Long.parseLong(numStr);
                        byte b = (byte)l;
                        if (b == l) {
                           stream.writeByte(2);
                           stream.writeByte(b);
                           return;
                        }

                        short s = (short)l;
                        if (s == l) {
                           stream.writeByte(3);
                           stream.writeShort(s);
                           return;
                        }

                        int i = (int)l;
                        if (i == l) {
                           stream.writeByte(4);
                           stream.writeInt(i);
                           return;
                        }

                        stream.writeByte(5);
                        stream.writeLong(l);
                     } catch (NumberFormatException e) {
                        stream.writeByte(8);
                        IOStreamCodec.BIG_INTEGER_CODEC.encode(stream, new BigInteger(numStr));
                     }
                  } else {
                     double d = p.doubleValue();
                     float f = (float)d;
                     if (f == d) {
                        stream.writeByte(6);
                        stream.writeFloat(f);
                        return;
                     }

                     stream.writeByte(7);
                     stream.writeDouble(d);
                  }
                  break;
               case BigInteger bi:
                  stream.writeByte(8);
                  IOStreamCodec.BIG_INTEGER_CODEC.encode(stream, bi);
                  break;
               case BigDecimal bd:
                  stream.writeByte(9);
                  IOStreamCodec.BIG_DECIMAL_CODEC.encode(stream, bd);
                  break;
               case AtomicInteger i:
                  stream.writeByte(4);
                  stream.writeInt(i.intValue());
                  break;
               case AtomicLong l:
                  stream.writeByte(5);
                  stream.writeLong(l.longValue());
                  break;
               case AtomicDouble d:
                  stream.writeByte(7);
                  stream.writeDouble(d.doubleValue());
                  break;
               case Number n:
                  stream.writeByte(12);
                  stream.writeUTF(n.toString());
                  break;
               default:
                  throw new IOException("Unsupported JsonPrimitive value type: " + value.getClass());
            }
         }

         private static boolean isInteger(String str) {
            int length = str.length();

            for (int i = 0; i < length; i++) {
               char c = str.charAt(i);
               if (c == '.' || c == 'e' || c == 'E') {
                  return false;
               }
            }

            return true;
         }

         public JsonPrimitive decode(DataIOStream stream) throws IOException {
            byte type = stream.readByte();

            return switch (type) {
               case 0 -> new JsonPrimitive((String)null);
               case 1 -> new JsonPrimitive(stream.readBoolean());
               case 2 -> new JsonPrimitive(stream.readByte());
               case 3 -> new JsonPrimitive(stream.readShort());
               case 4 -> new JsonPrimitive(stream.readInt());
               case 5 -> new JsonPrimitive(stream.readLong());
               case 6 -> new JsonPrimitive(stream.readFloat());
               case 7 -> new JsonPrimitive(stream.readDouble());
               case 8 -> new JsonPrimitive(IOStreamCodec.BIG_INTEGER_CODEC.decode(stream));
               case 9 -> new JsonPrimitive(IOStreamCodec.BIG_DECIMAL_CODEC.decode(stream));
               case 10 -> new JsonPrimitive(stream.readUTF());
               case 11 -> new JsonPrimitive(stream.readChar());
               case 12 -> new JsonPrimitive(new LazilyParsedNumber(stream.readUTF()));
               default -> throw new IOException("Unknown JsonPrimitive type marker: " + type);
            };
         }

         static {
            IOStreamCodec.registerCodec(JsonPrimitive.class, JsonUtils.JSON_PRIMITIVE_CODEC);
         }
      };
      JSON_ARRAY_CODEC = new IOStreamCodec<JsonArray>() {
         public void encode(DataIOStream stream, JsonArray obj) throws IOException {
            stream.writeVarInt(obj.size());

            for (JsonElement jsonObject : obj) {
               JsonUtils.JSON_ELEMENT_CODEC.encode(stream, jsonObject);
            }
         }

         public JsonArray decode(DataIOStream stream) throws IOException {
            int capacity = stream.readVarInt();
            JsonArray array = new JsonArray(capacity);

            for (int i = 0; i < capacity; i++) {
               array.add(JsonUtils.JSON_ELEMENT_CODEC.decode(stream));
            }

            return array;
         }
      };
      JSON_OBJECT_CODEC = new IOStreamCodec<JsonObject>() {
         public void encode(DataIOStream stream, JsonObject obj) throws IOException {
            stream.writeVarInt(obj.size());

            for (Entry<String, JsonElement> entry : obj.entrySet()) {
               stream.writeUTF(entry.getKey());
               JsonUtils.JSON_ELEMENT_CODEC.encode(stream, entry.getValue());
            }
         }

         public JsonObject decode(DataIOStream stream) throws IOException {
            int capacity = stream.readVarInt();
            JsonObject obj = new JsonObject();

            for (int i = 0; i < capacity; i++) {
               obj.add(stream.readUTF(), JsonUtils.JSON_ELEMENT_CODEC.decode(stream));
            }

            return obj;
         }
      };
   }
}
