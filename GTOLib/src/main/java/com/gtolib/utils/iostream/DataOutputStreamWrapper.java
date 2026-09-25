package com.gtolib.utils.iostream;

import java.io.DataOutputStream;
import java.io.OutputStream;
import org.jetbrains.annotations.NotNull;

public final class DataOutputStreamWrapper extends DataOutputStream implements DataIOStream {
   private final int dataVersion;

   public DataOutputStreamWrapper(OutputStream out, int dataVersion) {
      super(out);
      this.dataVersion = dataVersion;
   }

   @Override
   public int dataVersion() {
      return this.dataVersion;
   }

   @Override
   public void readFully(byte @NotNull [] b) {
      throw new UnsupportedOperationException("Cannot read from output stream");
   }

   @Override
   public void readFully(@NotNull byte[] b, int off, int len) {
      throw new UnsupportedOperationException("Cannot read from output stream");
   }

   @Override
   public int skipBytes(int n) {
      throw new UnsupportedOperationException("Cannot read from output stream");
   }

   @Override
   public boolean readBoolean() {
      throw new UnsupportedOperationException("Cannot read from output stream");
   }

   @Override
   public byte readByte() {
      throw new UnsupportedOperationException("Cannot read from output stream");
   }

   @Override
   public int readUnsignedByte() {
      throw new UnsupportedOperationException("Cannot read from output stream");
   }

   @Override
   public short readShort() {
      throw new UnsupportedOperationException("Cannot read from output stream");
   }

   @Override
   public int readUnsignedShort() {
      throw new UnsupportedOperationException("Cannot read from output stream");
   }

   @Override
   public char readChar() {
      throw new UnsupportedOperationException("Cannot read from output stream");
   }

   @Override
   public int readInt() {
      throw new UnsupportedOperationException("Cannot read from output stream");
   }

   @Override
   public long readLong() {
      throw new UnsupportedOperationException("Cannot read from output stream");
   }

   @Override
   public float readFloat() {
      throw new UnsupportedOperationException("Cannot read from output stream");
   }

   @Override
   public double readDouble() {
      throw new UnsupportedOperationException("Cannot read from output stream");
   }

   @Override
   public String readLine() {
      throw new UnsupportedOperationException("Cannot read from output stream");
   }

   @NotNull
   @Override
   public String readUTF() {
      throw new UnsupportedOperationException("Cannot read from output stream");
   }
}
