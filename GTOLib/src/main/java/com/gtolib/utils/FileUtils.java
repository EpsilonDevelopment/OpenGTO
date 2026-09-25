package com.gtolib.utils;

import com.gtolib.utils.iostream.DataIOStream;
import com.gtolib.utils.iostream.DataInputStreamWrapper;
import com.gtolib.utils.iostream.DataOutputStreamWrapper;
import com.gtolib.utils.iostream.IOStreamCodec;
import com.gtolib.utils.iostream.IOStreamDecoder;
import com.gtolib.utils.iostream.IOStreamEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class FileUtils {
   public static final IOStreamCodec<BigInteger> OLD_BIG_INTEGER = new IOStreamCodec<BigInteger>() {
      public void encode(DataIOStream stream, BigInteger obj) throws IOException {
         byte[] bytes = obj.toByteArray();
         stream.writeInt(bytes.length);
         stream.write(bytes);
      }

      public BigInteger decode(DataIOStream stream) throws IOException {
         byte[] bytes = new byte[stream.readInt()];
         stream.readFully(bytes);
         return new BigInteger(bytes);
      }
   };
   public static final IOStreamCodec<UUID> OLD_UUID = new IOStreamCodec<UUID>() {
      public void encode(DataIOStream stream, UUID obj) throws IOException {
         stream.writeUTF(obj.toString());
      }

      public UUID decode(DataIOStream stream) throws IOException {
         return UUID.fromString(stream.readUTF());
      }
   };
   private static final Map<String, ReentrantLock> FILE_LOCKS = new ConcurrentHashMap<>();

   private FileUtils() {
   }

   public static Stream<File> getCacheFiles(File dir) {
      File[] caches = dir.listFiles((dir1, name) -> name.toLowerCase().endsWith(".ser"));
      return caches == null ? Stream.empty() : Arrays.stream(caches).filter(file -> !file.isDirectory());
   }

   public static void deleteDirectory(File directory) {
      if (directory.exists() && directory.isDirectory()) {
         File[] files = directory.listFiles();
         if (files != null) {
            for (File file : files) {
               if (file.isDirectory()) {
                  deleteDirectory(file);
               } else {
                  file.delete();
               }
            }
         }

         directory.delete();
      }
   }

   public static String calculateFileHash(File file, String algorithm) {
      try (InputStream inputStream = new FileInputStream(file)) {
         MessageDigest digest = MessageDigest.getInstance(algorithm);
         byte[] buffer = new byte[8192];

         int bytesRead;
         while ((bytesRead = inputStream.read(buffer)) != -1) {
            digest.update(buffer, 0, bytesRead);
         }

         byte[] hashBytes = digest.digest();
         return HexFormat.of().formatHex(hashBytes);
      } catch (NoSuchAlgorithmException | IOException e) {
         throw new RuntimeException(e);
      }
   }

   public static void saveFile(File file, Consumer<DataIOStream> consumer) {
      Path parentPath = Paths.get(file.getParent());
      if (!Files.exists(parentPath)) {
         try {
            Files.createDirectories(parentPath);
         } catch (IOException var8) {
         }
      }

      try {
         try (DataOutputStreamWrapper dos = DataIOStream.of(new BufferedOutputStream(new FileOutputStream(file)))) {
            consumer.accept(dos);
            dos.flush();
         }
      } catch (Exception e) {
         try {
            Files.deleteIfExists(file.toPath());
         } catch (IOException var6) {
         }

         throw new RuntimeException(e);
      }
   }

   public static void loadFile(File file, Consumer<DataIOStream> consumer) {
      if (file.exists()) {
         try (DataInputStreamWrapper dis = DataIOStream.of(new BufferedInputStream(new FileInputStream(file)))) {
            consumer.accept(dis);
         } catch (Exception e) {
            try {
               Files.deleteIfExists(file.toPath());
            } catch (IOException var5) {
            }

            throw new RuntimeException(e);
         }
      }
   }

   public static <T> void saveToStream(T obj, OutputStream out, IOStreamEncoder<? super T> encoder) {
      try {
         DataOutputStreamWrapper dos = DataIOStream.of(new BufferedOutputStream(out));
         encoder.encode(dos, obj);
         dos.flush();
         dos.close();
         out.close();
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   public static <T> void saveToFile(T obj, File file, IOStreamEncoder<? super T> encoder) {
      try {
         Path parentPath = Paths.get(file.getParent());
         if (!Files.exists(parentPath)) {
            Files.createDirectories(parentPath);
         }

         Path tempPath = Files.createTempFile(parentPath, "temp", ".tmp");

         try {
            FileOutputStream fos = new FileOutputStream(tempPath.toFile());
            DataOutputStreamWrapper dos = DataIOStream.of(new BufferedOutputStream(fos));
            encoder.encode(dos, obj);
            dos.flush();
            dos.close();
            fos.close();
            Files.move(tempPath, file.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
         } catch (IOException e) {
            Files.deleteIfExists(tempPath);
            throw e;
         }
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   public static <T> T loadFromStream(InputStream in, IOStreamDecoder<? extends T> decoder) {
      try {
         DataInputStreamWrapper dis = DataIOStream.of(new BufferedInputStream(in));
         T obj = (T)decoder.decode(dis);
         dis.close();
         in.close();
         return obj;
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   public static <T> T loadFromFile(File file, IOStreamDecoder<? extends T> decoder) {
      try (DataInputStreamWrapper dis = DataIOStream.of(new BufferedInputStream(new FileInputStream(file)))) {
         return (T)decoder.decode(dis);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   public static <T> void saveToCompressFile(T obj, File file, IOStreamEncoder<? super T> encoder) {
      try {
         Path parentPath = Paths.get(file.getParent());
         if (!Files.exists(parentPath)) {
            Files.createDirectories(parentPath);
         }

         Path tempPath = Files.createTempFile(parentPath, "temp", ".tmp");

         try {
            FileOutputStream fos = new FileOutputStream(tempPath.toFile());
            DataOutputStreamWrapper dos = DataIOStream.of(new BufferedOutputStream(new GZIPOutputStream(fos)));
            encoder.encode(dos, obj);
            dos.flush();
            dos.close();
            fos.close();
            Files.move(tempPath, file.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
         } catch (IOException e) {
            Files.deleteIfExists(tempPath);
            throw e;
         }
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   public static <T> T loadFromCompressFile(File file, IOStreamDecoder<? extends T> decoder) {
      try (DataInputStreamWrapper dis = DataIOStream.of(new BufferedInputStream(new GZIPInputStream(new FileInputStream(file))))) {
         return (T)decoder.decode(dis);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   public static ReentrantLock getFileLock(File file) {
      return FILE_LOCKS.computeIfAbsent(file.getAbsolutePath(), k -> new ReentrantLock());
   }
}
