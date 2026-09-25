package com.gtolib.utils.iostream;

import io.netty.buffer.ByteBuf;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.UUID;

public interface DataIOStream extends DataInput, DataOutput {
   static DataInputStreamWrapper of(byte[] bytes) {
      return new DataInputStreamWrapper(new ByteArrayInputStream(bytes), 0);
   }

   static DataInputStreamWrapper of(InputStream stream) {
      return new DataInputStreamWrapper(stream, 0);
   }

   static DataOutputStreamWrapper of(OutputStream stream) {
      return new DataOutputStreamWrapper(stream, 0);
   }

   static ByteBufWrapper of(ByteBuf buf) {
      return new ByteBufWrapper(buf, 0);
   }

   static DataInputStreamWrapper of(byte[] bytes, int dataVersion) {
      return new DataInputStreamWrapper(new ByteArrayInputStream(bytes), dataVersion);
   }

   static DataInputStreamWrapper of(InputStream stream, int dataVersion) {
      return new DataInputStreamWrapper(stream, dataVersion);
   }

   static DataOutputStreamWrapper of(OutputStream stream, int dataVersion) {
      return new DataOutputStreamWrapper(stream, dataVersion);
   }

   static ByteBufWrapper of(ByteBuf buf, int dataVersion) {
      return new ByteBufWrapper(buf, dataVersion);
   }

   @Override
   void write(byte[] var1) throws IOException;

   @Override
   void writeInt(int var1) throws IOException;

   @Override
   int readInt() throws IOException;

   @Override
   void writeUTF(String var1) throws IOException;

   @Override
   String readUTF() throws IOException;

   @Override
   long readLong() throws IOException;

   @Override
   byte readByte() throws IOException;

   @Override
   short readShort() throws IOException;

   @Override
   void writeLong(long var1) throws IOException;

   @Override
   void writeByte(int var1) throws IOException;

   @Override
   void writeShort(int var1) throws IOException;

   @Override
   void writeChar(int var1) throws IOException;

   @Override
   char readChar() throws IOException;

   @Override
   void writeFloat(float var1) throws IOException;

   @Override
   float readFloat() throws IOException;

   @Override
   void writeDouble(double var1) throws IOException;

   @Override
   double readDouble() throws IOException;

   @Override
   void readFully(byte[] var1) throws IOException;

   @Override
   boolean readBoolean() throws IOException;

   @Override
   void writeBoolean(boolean var1) throws IOException;

   default <T extends Enum<T>> T readEnum(Class<T> enumClass) throws IOException {
      return enumClass.getEnumConstants()[this.readVarInt()];
   }

   default void writeUUID(UUID uuid) throws IOException {
      this.writeLong(uuid.getMostSignificantBits());
      this.writeLong(uuid.getLeastSignificantBits());
   }

   default UUID readUUID() throws IOException {
      return new UUID(this.readLong(), this.readLong());
   }

   default void writeEnum(Enum<?> value) throws IOException {
      this.writeVarInt(value.ordinal());
   }

   default int readVarInt() throws IOException {
      int i = 0;
      int j = 0;

      byte b0;
      do {
         b0 = this.readByte();
         i |= (b0 & 127) << j++ * 7;
         if (j > 5) {
            throw new RuntimeException("VarInt too big");
         }
      } while ((b0 & 128) == 128);

      return i;
   }

   default void writeVarInt(int input) throws IOException {
      while ((input & -128) != 0) {
         this.writeByte(input & 127 | 128);
         input >>>= 7;
      }

      this.writeByte(input);
   }

   default void writeLongArray(long[] array) throws IOException {
      this.writeVarInt(array.length);

      for (long i : array) {
         this.writeLong(i);
      }
   }

   default long[] readVarLongArray() throws IOException {
      int i = this.readVarInt();
      long[] array = new long[i];

      for (int j = 0; j < i; j++) {
         array[j] = this.readVarLong();
      }

      return array;
   }

   default float[] readFloatArray() throws IOException {
      int length = this.readVarInt();
      float[] floats = new float[length];

      for (int i = 0; i < length; i++) {
         floats[i] = this.readFloat();
      }

      return floats;
   }

   default void writeIntArray(int[] array) throws IOException {
      this.writeVarInt(array.length);

      for (int i : array) {
         this.writeInt(i);
      }
   }

   default void writeFloatArray(float[] array) throws IOException {
      this.writeVarInt(array.length);

      for (float b : array) {
         this.writeFloat(b);
      }
   }

   default int[] readIntArray() throws IOException {
      int i = this.readVarInt();
      int[] aint = new int[i];

      for (int j = 0; j < i; j++) {
         aint[j] = this.readInt();
      }

      return aint;
   }

   default long[] readLongArray() throws IOException {
      int i = this.readVarInt();
      long[] array = new long[i];

      for (int j = 0; j < i; j++) {
         array[j] = this.readLong();
      }

      return array;
   }

   default boolean[] readBooleanArray() throws IOException {
      int length = this.readVarInt();
      boolean[] booleans = new boolean[length];

      for (int i = 0; i < length; i++) {
         booleans[i] = this.readBoolean();
      }

      return booleans;
   }

   default void writeCharArray(char[] array) throws IOException {
      this.writeVarInt(array.length);

      for (char b : array) {
         this.writeChar(b);
      }
   }

   default void writeVarLongArray(long[] array) throws IOException {
      this.writeVarInt(array.length);

      for (long i : array) {
         this.writeVarLong(i);
      }
   }

   default BigInteger readBigInteger() throws IOException {
      return new BigInteger(this.readByteArray());
   }

   default void writeDoubleArray(double[] array) throws IOException {
      this.writeVarInt(array.length);

      for (double b : array) {
         this.writeDouble(b);
      }
   }

   default void writeBooleanArray(boolean[] array) throws IOException {
      this.writeVarInt(array.length);

      for (boolean b : array) {
         this.writeBoolean(b);
      }
   }

   default void writeByteArray(byte[] array) throws IOException {
      this.writeVarInt(array.length);
      this.write(array);
   }

   default void writeVarLong(long value) throws IOException {
      while ((value & -128L) != 0L) {
         this.writeByte((int)(value & 127L) | 128);
         value >>>= 7;
      }

      this.writeByte((int)value);
   }

   default short[] readShortArray() throws IOException {
      int length = this.readVarInt();
      short[] shorts = new short[length];

      for (int i = 0; i < length; i++) {
         shorts[i] = this.readShort();
      }

      return shorts;
   }

   default byte[] readByteArray() throws IOException {
      byte[] array = new byte[this.readVarInt()];
      this.readFully(array);
      return array;
   }

   default long readVarLong() throws IOException {
      long i = 0L;
      int j = 0;

      byte b0;
      do {
         b0 = this.readByte();
         i |= (long)(b0 & 127) << j++ * 7;
         if (j > 10) {
            throw new RuntimeException("VarLong too big");
         }
      } while ((b0 & 128) == 128);

      return i;
   }

   default void writeBigDecimal(BigDecimal bigDecimal) throws IOException {
      this.writeByteArray(bigDecimal.unscaledValue().toByteArray());
      this.writeVarInt(bigDecimal.scale());
   }

   default char[] readCharArray() throws IOException {
      int length = this.readVarInt();
      char[] chars = new char[length];

      for (int i = 0; i < length; i++) {
         chars[i] = this.readChar();
      }

      return chars;
   }

   default BigDecimal readBigDecimal() throws IOException {
      return new BigDecimal(new BigInteger(this.readByteArray()), this.readVarInt());
   }

   default void writeVarIntArray(int[] array) throws IOException {
      this.writeVarInt(array.length);

      for (int i : array) {
         this.writeVarInt(i);
      }
   }

   default void writeShortArray(short[] array) throws IOException {
      this.writeVarInt(array.length);

      for (short b : array) {
         this.writeShort(b);
      }
   }

   default int[] readVarIntArray() throws IOException {
      int i = this.readVarInt();
      int[] aint = new int[i];

      for (int j = 0; j < i; j++) {
         aint[j] = this.readVarInt();
      }

      return aint;
   }

   default double[] readDoubleArray() throws IOException {
      int length = this.readVarInt();
      double[] doubles = new double[length];

      for (int i = 0; i < length; i++) {
         doubles[i] = this.readDouble();
      }

      return doubles;
   }

   default void writeBigInteger(BigInteger bigInteger) throws IOException {
      this.writeByteArray(bigInteger.toByteArray());
   }

   int dataVersion();
}
