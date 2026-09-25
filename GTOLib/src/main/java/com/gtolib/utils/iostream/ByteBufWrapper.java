package com.gtolib.utils.iostream;

import com.gto.datasynclib.datastream.data.Data;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.util.CharsetUtil;
import java.nio.charset.StandardCharsets;
import org.jetbrains.annotations.NotNull;

public final class ByteBufWrapper implements DataIOStream {
   private static final int MAX_UTF_BYTES = 1048576;
   private final ByteBuf buf;
   private final int readerEndIndex;
   private final int dataVersion;
   private StringBuilder lineBuf;

   public ByteBufWrapper(ByteBuf buf, int dataVersion) {
      this.buf = buf;
      this.readerEndIndex = buf.readerIndex() + buf.readableBytes();
      this.dataVersion = dataVersion;
   }

   @Override
   public int dataVersion() {
      return this.dataVersion;
   }

   @Override
   public void write(int b) {
      this.buf.writeByte(b);
   }

   @Override
   public void write(byte @NotNull [] b) {
      this.buf.writeBytes(b);
   }

   @Override
   public void write(byte @NotNull [] b, int off, int len) {
      this.buf.writeBytes(b, off, len);
   }

   @Override
   public void writeBoolean(boolean b) {
      this.buf.writeBoolean(b);
   }

   @Override
   public void writeByte(int b) {
      this.buf.writeByte(b);
   }

   @Override
   public void writeShort(int s) {
      this.buf.writeShort(s);
   }

   @Override
   public void writeChar(int c) {
      this.buf.writeChar(c);
   }

   @Override
   public void writeInt(int i) {
      this.buf.writeInt(i);
   }

   @Override
   public void writeLong(long l) {
      this.buf.writeLong(l);
   }

   @Override
   public void writeFloat(float f) {
      this.buf.writeFloat(f);
   }

   @Override
   public void writeDouble(double d) {
      this.buf.writeDouble(d);
   }

   @Override
   public void writeBytes(@NotNull String s) {
      this.buf.writeCharSequence(s, CharsetUtil.US_ASCII);
   }

   @Override
   public void writeChars(@NotNull String s) {
      int len = s.length();

      for (int i = 0; i < len; i++) {
         this.buf.writeChar(s.charAt(i));
      }
   }

   @Override
   public void writeUTF(@NotNull String s) {
      int length = ByteBufUtil.utf8Bytes(s);
      if (length > 1048576) {
         throw new IllegalArgumentException("UTF string is too large: " + length);
      }

      Data.writeVarInt(this.buf, length);
      ByteBufUtil.writeUtf8(this.buf, s);
   }

   @Override
   public void readFully(byte @NotNull [] b) {
      this.buf.readBytes(b);
   }

   @Override
   public void readFully(byte @NotNull [] b, int off, int len) {
      this.buf.readBytes(b, off, len);
   }

   @Override
   public int skipBytes(int n) {
      int nBytes = Math.min(this.available(), n);
      this.buf.skipBytes(nBytes);
      return nBytes;
   }

   @Override
   public boolean readBoolean() {
      return this.buf.readBoolean();
   }

   @Override
   public byte readByte() {
      return this.buf.readByte();
   }

   @Override
   public int readUnsignedByte() {
      return this.buf.readUnsignedByte();
   }

   @Override
   public short readShort() {
      return this.buf.readShort();
   }

   @Override
   public int readUnsignedShort() {
      return this.buf.readUnsignedShort();
   }

   @Override
   public char readChar() {
      return this.buf.readChar();
   }

   @Override
   public int readInt() {
      return this.buf.readInt();
   }

   @Override
   public long readLong() {
      return this.buf.readLong();
   }

   @Override
   public float readFloat() {
      return this.buf.readFloat();
   }

   @Override
   public double readDouble() {
      return this.buf.readDouble();
   }

   @Override
   public String readLine() {
      int available = this.available();
      if (available == 0) {
         return null;
      }

      if (this.lineBuf != null) {
         this.lineBuf.setLength(0);
      }

      while (true) {
         int c = this.buf.readUnsignedByte();
         available--;
         switch (c) {
            case 10:
               return this.lineBuf != null && !this.lineBuf.isEmpty() ? this.lineBuf.toString() : "";
            case 13:
               if (available > 0 && (char)this.buf.getUnsignedByte(this.buf.readerIndex()) == '\n') {
                  this.buf.skipBytes(1);
                  available--;
               }

               return this.lineBuf != null && !this.lineBuf.isEmpty() ? this.lineBuf.toString() : "";
            default:
               if (this.lineBuf == null) {
                  this.lineBuf = new StringBuilder();
               }

               this.lineBuf.append((char)c);
               if (available <= 0) {
                  return this.lineBuf != null && !this.lineBuf.isEmpty() ? this.lineBuf.toString() : "";
               }
         }
      }
   }

   @NotNull
   @Override
   public String readUTF() {
      int length = Data.readVarInt(this.buf);
      if (length >= 0 && length <= 1048576 && length <= this.available()) {
         String value = this.buf.toString(this.buf.readerIndex(), length, StandardCharsets.UTF_8);
         this.buf.skipBytes(length);
         return value;
      } else {
         throw new IllegalArgumentException("Invalid UTF string length: " + length);
      }
   }

   public int available() {
      return this.readerEndIndex - this.buf.readerIndex();
   }
}
