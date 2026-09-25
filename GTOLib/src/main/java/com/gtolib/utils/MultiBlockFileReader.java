package com.gtolib.utils;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gtocore.Core;
import com.gtolib.utils.iostream.DataIOStream;
import com.gtolib.utils.iostream.IOStreamCodec;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public final class MultiBlockFileReader {
   public static final int FORMAT_VERSION = 1;
   private static final int VERSIONED_FORMAT_MARKER = -1;
   private static final RelativeDirection DEFAULT_CHAR_DIR = RelativeDirection.LEFT;
   private static final RelativeDirection DEFAULT_STRING_DIR = RelativeDirection.UP;
   private static final RelativeDirection DEFAULT_AISLE_DIR = RelativeDirection.FRONT;

   private MultiBlockFileReader() {
   }

   public static FactoryBlockPattern start(MultiblockMachineDefinition definition) {
      return start(definition, definition.getName());
   }

   public static FactoryBlockPattern start(MultiblockMachineDefinition definition, String name) {
      try (InputStream stream = Core.class.getClassLoader().getResourceAsStream("pattern/" + name + ".mbs")) {
         if (stream == null) {
            throw new RuntimeException("pattern not found: " + name);
         }

         MultiBlockFileReader.MbsData data = load(stream);
         validateDirections(data.charDir(), data.stringDir(), data.aisleDir());
         FactoryBlockPattern pattern = FactoryBlockPattern.start(definition, data.charDir(), data.stringDir(), data.aisleDir());

         for (String[] aisle : data.pattern()) {
            pattern.aisle(aisle);
         }

         return pattern;
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   public static MultiBlockFileReader.MbsData load(File file) {
      return FileUtils.loadFromCompressFile(file, MultiBlockFileReader.MbsData.CODEC);
   }

   public static MultiBlockFileReader.MbsData load(InputStream input) throws IOException {
      return FileUtils.loadFromStream(new GZIPInputStream(input), MultiBlockFileReader.MbsData.CODEC);
   }

   public static void save(File file, String[][] arr) {
      save(file, arr, DEFAULT_CHAR_DIR, DEFAULT_STRING_DIR, DEFAULT_AISLE_DIR);
   }

   public static void save(File file, String[][] arr, RelativeDirection charDir, RelativeDirection stringDir, RelativeDirection aisleDir) {
      validateDirections(charDir, stringDir, aisleDir);
      FileUtils.saveToCompressFile(new MultiBlockFileReader.MbsData(1, charDir, stringDir, aisleDir, arr), file, MultiBlockFileReader.MbsData.CODEC);
   }

   private static void validateDirections(RelativeDirection charDir, RelativeDirection stringDir, RelativeDirection aisleDir) {
      if (charDir.isSameAxis(stringDir) || stringDir.isSameAxis(aisleDir) || aisleDir.isSameAxis(charDir)) {
         throw new IllegalArgumentException("charDir, stringDir and aisleDir must use different axes");
      }
   }

   public record MbsData(int version, RelativeDirection charDir, RelativeDirection stringDir, RelativeDirection aisleDir, String[][] pattern) {
      public static final IOStreamCodec<MultiBlockFileReader.MbsData> CODEC = new IOStreamCodec<MultiBlockFileReader.MbsData>() {
         public void encode(DataIOStream stream, MultiBlockFileReader.MbsData data) throws IOException {
            MultiBlockFileReader.validateDirections(data.charDir(), data.stringDir(), data.aisleDir());
            stream.writeVarInt(-1);
            stream.writeVarInt(1);
            stream.writeEnum(data.charDir());
            stream.writeEnum(data.stringDir());
            stream.writeEnum(data.aisleDir());
            writeGrid(stream, data.pattern());
         }

         public MultiBlockFileReader.MbsData decode(DataIOStream stream) throws IOException {
            int first = stream.readVarInt();
            if (first != -1) {
               throw new IOException("unsupported .mbs payload without version marker");
            }

            int version = stream.readVarInt();
            if (version != 1) {
               throw new IOException("unsupported .mbs version: " + version);
            }

            RelativeDirection charDir = readDirectionOrdinal(stream, "charDir");
            RelativeDirection stringDir = readDirectionOrdinal(stream, "stringDir");
            RelativeDirection aisleDir = readDirectionOrdinal(stream, "aisleDir");
            MultiBlockFileReader.validateDirections(charDir, stringDir, aisleDir);
            return new MultiBlockFileReader.MbsData(version, charDir, stringDir, aisleDir, readGrid(stream, stream.readVarInt()));
         }

         private static String[][] readGrid(DataIOStream stream, int aisleCount) throws IOException {
            if (aisleCount < 0) {
               throw new IOException("negative aisle count: " + aisleCount);
            }

            String[][] pattern = new String[aisleCount][];

            for (int aisle = 0; aisle < aisleCount; aisle++) {
               int rowCount = stream.readVarInt();
               if (rowCount < 0) {
                  throw new IOException("negative row count in aisle " + aisle + ": " + rowCount);
               }

               pattern[aisle] = new String[rowCount];

               for (int row = 0; row < rowCount; row++) {
                  pattern[aisle][row] = stream.readUTF();
               }
            }

            return pattern;
         }

         private static void writeGrid(DataIOStream stream, String[][] pattern) throws IOException {
            stream.writeVarInt(pattern.length);

            for (String[] aisle : pattern) {
               stream.writeVarInt(aisle.length);

               for (String row : aisle) {
                  stream.writeUTF(row);
               }
            }
         }

         private static RelativeDirection readDirectionOrdinal(DataIOStream stream, String field) throws IOException {
            int ordinal = stream.readVarInt();
            RelativeDirection[] values = RelativeDirection.values();
            if (ordinal >= 0 && ordinal < values.length) {
               return values[ordinal];
            } else {
               throw new IOException("unknown .mbs " + field + " ordinal: " + ordinal);
            }
         }
      };
   }
}
