package com.gtolib.utils.iostream;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.UUID;

public interface IOStreamCodec<T> extends IOStreamEncoder<T>, IOStreamDecoder<T> {
   IOStreamCodec<Boolean> BOOLEAN_CODEC = new IOStreamCodec<Boolean>() {
      public void encode(DataIOStream stream, Boolean obj) throws IOException {
         stream.writeBoolean(obj);
      }

      public Boolean decode(DataIOStream stream) throws IOException {
         return stream.readBoolean();
      }

      static {
         IOStreamCodec.registerCodec(Boolean.class, BOOLEAN_CODEC);
         IOStreamCodec.registerCodec(boolean.class, BOOLEAN_CODEC);
      }
   };
   IOStreamCodec<Byte> BYTE_CODEC = new IOStreamCodec<Byte>() {
      public void encode(DataIOStream stream, Byte obj) throws IOException {
         stream.writeByte(obj);
      }

      public Byte decode(DataIOStream stream) throws IOException {
         return stream.readByte();
      }

      static {
         IOStreamCodec.registerCodec(Byte.class, BYTE_CODEC);
         IOStreamCodec.registerCodec(byte.class, BYTE_CODEC);
      }
   };
   IOStreamCodec<Short> SHORT_CODEC = new IOStreamCodec<Short>() {
      public void encode(DataIOStream stream, Short obj) throws IOException {
         stream.writeShort(obj);
      }

      public Short decode(DataIOStream stream) throws IOException {
         return stream.readShort();
      }

      static {
         IOStreamCodec.registerCodec(Short.class, SHORT_CODEC);
         IOStreamCodec.registerCodec(short.class, SHORT_CODEC);
      }
   };
   IOStreamCodec<Character> CHAR_CODEC = new IOStreamCodec<Character>() {
      public void encode(DataIOStream stream, Character obj) throws IOException {
         stream.writeChar(obj);
      }

      public Character decode(DataIOStream stream) throws IOException {
         return stream.readChar();
      }

      static {
         IOStreamCodec.registerCodec(Character.class, CHAR_CODEC);
         IOStreamCodec.registerCodec(char.class, CHAR_CODEC);
      }
   };
   IOStreamCodec<Integer> INT_CODEC = new IOStreamCodec<Integer>() {
      public void encode(DataIOStream stream, Integer obj) throws IOException {
         stream.writeInt(obj);
      }

      public Integer decode(DataIOStream stream) throws IOException {
         return stream.readInt();
      }

      static {
         IOStreamCodec.registerCodec(Integer.class, INT_CODEC);
         IOStreamCodec.registerCodec(int.class, INT_CODEC);
      }
   };
   IOStreamCodec<Long> LONG_CODEC = new IOStreamCodec<Long>() {
      public void encode(DataIOStream stream, Long obj) throws IOException {
         stream.writeLong(obj);
      }

      public Long decode(DataIOStream stream) throws IOException {
         return stream.readLong();
      }

      static {
         IOStreamCodec.registerCodec(Long.class, LONG_CODEC);
         IOStreamCodec.registerCodec(long.class, LONG_CODEC);
      }
   };
   IOStreamCodec<Float> FLOAT_CODEC = new IOStreamCodec<Float>() {
      public void encode(DataIOStream stream, Float obj) throws IOException {
         stream.writeFloat(obj);
      }

      public Float decode(DataIOStream stream) throws IOException {
         return stream.readFloat();
      }

      static {
         IOStreamCodec.registerCodec(Float.class, FLOAT_CODEC);
         IOStreamCodec.registerCodec(float.class, FLOAT_CODEC);
      }
   };
   IOStreamCodec<Double> DOUBLE_CODEC = new IOStreamCodec<Double>() {
      public void encode(DataIOStream stream, Double obj) throws IOException {
         stream.writeDouble(obj);
      }

      public Double decode(DataIOStream stream) throws IOException {
         return stream.readDouble();
      }

      static {
         IOStreamCodec.registerCodec(Double.class, DOUBLE_CODEC);
         IOStreamCodec.registerCodec(double.class, DOUBLE_CODEC);
      }
   };
   IOStreamCodec<String> STRING_CODEC = new IOStreamCodec<String>() {
      public void encode(DataIOStream stream, String obj) throws IOException {
         stream.writeUTF(obj);
      }

      public String decode(DataIOStream stream) throws IOException {
         return stream.readUTF();
      }

      static {
         IOStreamCodec.registerCodec(String.class, STRING_CODEC);
      }
   };
   IOStreamCodec<BigInteger> BIG_INTEGER_CODEC = new IOStreamCodec<BigInteger>() {
      public void encode(DataIOStream stream, BigInteger obj) throws IOException {
         stream.writeByteArray(obj.toByteArray());
      }

      public BigInteger decode(DataIOStream stream) throws IOException {
         return new BigInteger(stream.readByteArray());
      }

      static {
         IOStreamCodec.registerCodec(BigInteger.class, BIG_INTEGER_CODEC);
      }
   };
   IOStreamCodec<BigDecimal> BIG_DECIMAL_CODEC = new IOStreamCodec<BigDecimal>() {
      public void encode(DataIOStream stream, BigDecimal obj) throws IOException {
         stream.writeByteArray(obj.unscaledValue().toByteArray());
         stream.writeVarInt(obj.scale());
      }

      public BigDecimal decode(DataIOStream stream) throws IOException {
         return new BigDecimal(new BigInteger(stream.readByteArray()), stream.readVarInt());
      }

      static {
         IOStreamCodec.registerCodec(BigDecimal.class, BIG_DECIMAL_CODEC);
      }
   };
   IOStreamCodec<boolean[]> BOOLEANS_CODEC = new IOStreamCodec<boolean[]>() {
      public void encode(DataIOStream stream, boolean[] obj) throws IOException {
         stream.writeBooleanArray(obj);
      }

      public boolean[] decode(DataIOStream stream) throws IOException {
         return stream.readBooleanArray();
      }

      static {
         IOStreamCodec.registerCodec(boolean[].class, BOOLEANS_CODEC);
      }
   };
   IOStreamCodec<byte[]> BYTES_CODEC = new IOStreamCodec<byte[]>() {
      public void encode(DataIOStream stream, byte[] obj) throws IOException {
         stream.writeByteArray(obj);
      }

      public byte[] decode(DataIOStream stream) throws IOException {
         return stream.readByteArray();
      }

      static {
         IOStreamCodec.registerCodec(byte[].class, BYTES_CODEC);
      }
   };
   IOStreamCodec<short[]> SHORTS_CODEC = new IOStreamCodec<short[]>() {
      public void encode(DataIOStream stream, short[] obj) throws IOException {
         stream.writeShortArray(obj);
      }

      public short[] decode(DataIOStream stream) throws IOException {
         return stream.readShortArray();
      }

      static {
         IOStreamCodec.registerCodec(short[].class, SHORTS_CODEC);
      }
   };
   IOStreamCodec<char[]> CHARS_CODEC = new IOStreamCodec<char[]>() {
      public void encode(DataIOStream stream, char[] obj) throws IOException {
         stream.writeCharArray(obj);
      }

      public char[] decode(DataIOStream stream) throws IOException {
         return stream.readCharArray();
      }

      static {
         IOStreamCodec.registerCodec(char[].class, CHARS_CODEC);
      }
   };
   IOStreamCodec<int[]> INTS_CODEC = new IOStreamCodec<int[]>() {
      public void encode(DataIOStream stream, int[] obj) throws IOException {
         stream.writeIntArray(obj);
      }

      public int[] decode(DataIOStream stream) throws IOException {
         return stream.readIntArray();
      }

      static {
         IOStreamCodec.registerCodec(int[].class, INTS_CODEC);
      }
   };
   IOStreamCodec<long[]> LONGS_CODEC = new IOStreamCodec<long[]>() {
      public void encode(DataIOStream stream, long[] obj) throws IOException {
         stream.writeLongArray(obj);
      }

      public long[] decode(DataIOStream stream) throws IOException {
         return stream.readLongArray();
      }

      static {
         IOStreamCodec.registerCodec(long[].class, LONGS_CODEC);
      }
   };
   IOStreamCodec<float[]> FLOATS_CODEC = new IOStreamCodec<float[]>() {
      public void encode(DataIOStream stream, float[] obj) throws IOException {
         stream.writeFloatArray(obj);
      }

      public float[] decode(DataIOStream stream) throws IOException {
         return stream.readFloatArray();
      }

      static {
         IOStreamCodec.registerCodec(float[].class, FLOATS_CODEC);
      }
   };
   IOStreamCodec<double[]> DOUBLES_CODEC = new IOStreamCodec<double[]>() {
      public void encode(DataIOStream stream, double[] obj) throws IOException {
         stream.writeDoubleArray(obj);
      }

      public double[] decode(DataIOStream stream) throws IOException {
         return stream.readDoubleArray();
      }

      static {
         IOStreamCodec.registerCodec(double[].class, DOUBLES_CODEC);
      }
   };
   IOStreamCodec<UUID> UUID_CODEC = new IOStreamCodec<UUID>() {
      public void encode(DataIOStream stream, UUID obj) throws IOException {
         stream.writeUUID(obj);
      }

      public UUID decode(DataIOStream stream) throws IOException {
         return stream.readUUID();
      }

      static {
         IOStreamCodec.registerCodec(UUID.class, UUID_CODEC);
      }
   };

   static <T> IOStreamCodec<T> of(final IOStreamEncoder<? super T> encoder, final IOStreamDecoder<? extends T> decoder) {
      return new IOStreamCodec<T>() {
         @Override
         public void encode(DataIOStream stream, T obj) throws IOException {
            encoder.encode(stream, obj);
         }

         @Override
         public T decode(DataIOStream stream) throws IOException {
            return (T)decoder.decode(stream);
         }
      };
   }

   static <T> void registerCodec(Class<T> type, IOStreamCodec<T> codec) {
      synchronized (IOStreamCodec.Codecs.CODECS) {
         IOStreamCodec.Codecs.CODECS.put(type, codec);
      }
   }

   static <T> IOStreamCodec<T> getCodec(Class<T> type) {
      return (IOStreamCodec<T>)IOStreamCodec.Codecs.CODECS.get(type);
   }

   final class Codecs {
      private static final Map<Class<?>, IOStreamCodec<?>> CODECS = new Reference2ReferenceOpenHashMap<>();
   }
}
