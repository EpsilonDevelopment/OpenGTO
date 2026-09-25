package com.gtolib.api.ae2.storage;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyBigMap;
import appeng.api.storage.MEStorage.AvailableStacksCache;
import com.gtolib.data.CellSavaedData;
import com.gtolib.utils.FileUtils;
import com.gtolib.utils.iostream.DataIOStream;
import com.gtolib.utils.iostream.IOStreamCodec;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.UUID;
import lombok.Generated;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import org.jetbrains.annotations.NotNull;

public class BigCellDataStorage {
   public static final BigCellDataStorage EMPTY = new BigCellDataStorage() {
      @Override
      public double getBytes() {
         return 0.0;
      }

      @Override
      public AEKeyBigMap<AEKey> getStoredMap() {
         return AEKeyBigMap.EMPTY;
      }
   };
   private boolean dirty;
   private double bytes;
   private AEKeyBigMap<AEKey> storedMap;
   public final AvailableStacksCache cache = new AvailableStacksCache(out -> {
      AEKeyBigMap<AEKey> map = this.storedMap;
      if (map != null) {
         out.addAll(map);
      }
   });
   public static final IOStreamCodec<BigCellDataStorage> IO_CODEC = new IOStreamCodec<BigCellDataStorage>() {
      public void encode(DataIOStream stream, BigCellDataStorage obj) throws IOException {
         if (obj != BigCellDataStorage.EMPTY && obj.storedMap != null && !obj.storedMap.isEmpty()) {
            stream.writeInt(obj.storedMap.size());
            obj.storedMap.fastForEach((k, v) -> {
               try {
                  NbtIo.write(k.toTagGeneric(), stream);
                  FileUtils.OLD_BIG_INTEGER.encode(stream, v);
               } catch (IOException e) {
                  throw new RuntimeException(e);
               }
            });
         } else {
            stream.writeInt(0);
         }
      }

      public BigCellDataStorage decode(DataIOStream stream) throws IOException {
         BigCellDataStorage dataStorage = new BigCellDataStorage();
         int size = stream.readInt();
         if (size > 0) {
            dataStorage.storedMap = new AEKeyBigMap<>(size);

            for (int i = 0; i < size; i++) {
               AEKey key = AEKey.fromTagGeneric(NbtIo.read(stream));
               BigInteger bi = FileUtils.OLD_BIG_INTEGER.decode(stream);
               if (key != null) {
                  dataStorage.storedMap.set(key, bi);
               }
            }
         }

         return dataStorage;
      }
   };

   public static BigCellDataStorage get(@NotNull UUID uuid) {
      return CellSavaedData.INSTANCE.getOrCreateBigCell(uuid);
   }

   public BigCellDataStorage() {
      this.cache.setTickUpdate(false);
   }

   public void importFromA(CellDataStorage a) {
      if (a.getStoredMap() != null && !a.getStoredMap().isEmpty()) {
         if (this.storedMap == null) {
            this.storedMap = new AEKeyBigMap<>(a.getStoredMap().size());
         }

         a.getStoredMap().fastForEach((key, longValue) -> this.storedMap.compute(key, (k, v) -> {
            BigInteger value = BigInteger.valueOf(longValue);
            return v == null ? value : v.add(value);
         }));
         a.setStoredMap(null);
         this.setDirty();
      }
   }

   public static BigCellDataStorage loadOrCreate(File file) {
      return file.exists() ? FileUtils.loadFromFile(file, IO_CODEC) : new BigCellDataStorage();
   }

   public static BigCellDataStorage fromNbt(CompoundTag nbt) {
      ListTag stackKeys = nbt.getList("k", 10);
      ListTag stackAmounts = nbt.getList("a", 8);
      BigCellDataStorage dataStorage = new BigCellDataStorage();
      dataStorage.storedMap = new AEKeyBigMap<>();

      for (int i = 0; i < stackAmounts.size(); i++) {
         AEKey key = AEKey.fromTagGeneric((CompoundTag)stackKeys.get(i));
         if (key != null) {
            dataStorage.storedMap.set(key, new BigInteger(stackAmounts.get(i).getAsString()));
         }
      }

      dataStorage.dirty = true;
      return dataStorage;
   }

   public void setDirty() {
      CellSavaedData.INSTANCE.setDirty(true);
      this.dirty = true;
      this.cache.markAsDirty();
   }

   @Generated
   public void setDirty(boolean dirty) {
      this.dirty = dirty;
   }

   @Generated
   public boolean isDirty() {
      return this.dirty;
   }

   @Generated
   public void setBytes(double bytes) {
      this.bytes = bytes;
   }

   @Generated
   public double getBytes() {
      return this.bytes;
   }

   @Generated
   public void setStoredMap(AEKeyBigMap<AEKey> storedMap) {
      this.storedMap = storedMap;
   }

   @Generated
   public AEKeyBigMap<AEKey> getStoredMap() {
      return this.storedMap;
   }
}
