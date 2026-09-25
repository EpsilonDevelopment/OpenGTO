package com.gtolib.utils.iostream;

import java.io.DataInputStream;
import java.io.InputStream;
import org.jetbrains.annotations.NotNull;

public final class DataInputStreamWrapper extends DataInputStream implements DataIOStream {
   private final int dataVersion;

   public DataInputStreamWrapper(InputStream in, int dataVersion) {
      super(in);
      this.dataVersion = dataVersion;
   }

   @Override
   public int dataVersion() {
      return this.dataVersion;
   }

   @Override
   public void write(int b) {
      throw new UnsupportedOperationException("Cannot write from input stream");
   }

   @Override
   public void write(byte @NotNull [] b) {
      throw new UnsupportedOperationException("Cannot write from input stream");
   }

   @Override
   public void write(byte @NotNull [] b, int off, int len) {
      throw new UnsupportedOperationException("Cannot write from input stream");
   }

   @Override
   public void writeBoolean(boolean b) {
      throw new UnsupportedOperationException("Cannot write from input stream");
   }

   @Override
   public void writeByte(int b) {
      throw new UnsupportedOperationException("Cannot write from input stream");
   }

   @Override
   public void writeShort(int s) {
      throw new UnsupportedOperationException("Cannot write from input stream");
   }

   @Override
   public void writeChar(int c) {
      throw new UnsupportedOperationException("Cannot write from input stream");
   }

   @Override
   public void writeInt(int i) {
      throw new UnsupportedOperationException("Cannot write from input stream");
   }

   @Override
   public void writeLong(long l) {
      throw new UnsupportedOperationException("Cannot write from input stream");
   }

   @Override
   public void writeFloat(float f) {
      throw new UnsupportedOperationException("Cannot write from input stream");
   }

   @Override
   public void writeDouble(double d) {
      throw new UnsupportedOperationException("Cannot write from input stream");
   }

   @Override
   public void writeBytes(@NotNull String s) {
      throw new UnsupportedOperationException("Cannot write from input stream");
   }

   @Override
   public void writeChars(@NotNull String s) {
      throw new UnsupportedOperationException("Cannot write from input stream");
   }

   @Override
   public void writeUTF(@NotNull String s) {
      throw new UnsupportedOperationException("Cannot write from input stream");
   }
}
