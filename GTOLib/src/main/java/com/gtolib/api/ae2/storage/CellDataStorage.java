package com.gtolib.api.ae2.storage;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyMap;
import appeng.api.storage.MEStorage.AvailableStacksCache;
import com.gtolib.data.CellSavaedData;
import com.gtolib.utils.FileUtils;
import com.gtolib.utils.iostream.DataIOStream;
import com.gtolib.utils.iostream.IOStreamCodec;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import lombok.Generated;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import org.jetbrains.annotations.NotNull;

public class CellDataStorage {
   public static final CellDataStorage EMPTY = new CellDataStorage() {
      @Override
      public double getBytes() {
         return 0.0;
      }

      @Override
      public AEKeyMap getStoredMap() {
         return AEKeyMap.EMPTY;
      }
   };
   private boolean dirty;
   private double bytes;
   private AEKeyMap<AEKey> storedMap;
   public final AvailableStacksCache cache = new AvailableStacksCache(out -> {
      AEKeyMap<AEKey> map = this.storedMap;
      if (map != null) {
         out.addAll(map);
      }
   });
   public static final IOStreamCodec<CellDataStorage> IO_CODEC = new IOStreamCodec<CellDataStorage>() {
      public void encode(DataIOStream stream, CellDataStorage obj) throws IOException {
         if (obj != CellDataStorage.EMPTY && obj.storedMap != null && !obj.storedMap.isEmpty()) {
            stream.writeInt(obj.storedMap.size());
            obj.storedMap.fastForEach((k, v) -> {
               try {
                  NbtIo.write(k.toTagGeneric(), stream);
                  stream.writeLong(v);
               } catch (IOException e) {
                  throw new RuntimeException(e);
               }
            });
         } else {
            stream.writeInt(0);
         }
      }

      public CellDataStorage decode(DataIOStream stream) throws IOException {
         CellDataStorage cellDataStorage = new CellDataStorage();
         int size = stream.readInt();
         if (size > 0) {
            cellDataStorage.storedMap = new AEKeyMap<>(size);

            for (int i = 0; i < size; i++) {
               cellDataStorage.storedMap.set(AEKey.fromTagGeneric(NbtIo.read(stream)), stream.readLong());
            }
         }

         return cellDataStorage;
      }
   };

   public CellDataStorage() {
      this.cache.setTickUpdate(false);
   }

   public static CellDataStorage get(@NotNull UUID uuid) {
      return CellSavaedData.INSTANCE.getOrCreateCell(uuid);
   }

   public void importFromB(BigCellDataStorage b) {
      if (b.getStoredMap() != null && !b.getStoredMap().isEmpty()) {
         if (this.storedMap == null) {
            this.storedMap = new AEKeyMap<>(b.getStoredMap().size());
         }

         b.getStoredMap().fastForEachLong((k, v) -> this.storedMap.insert(k, v));
         b.setStoredMap(null);
         this.setDirty();
      }
   }

   public static CellDataStorage loadOrCreate(File file) {
      return file.exists() ? FileUtils.loadFromFile(file, IO_CODEC) : new CellDataStorage();
   }

   public static CellDataStorage fromNbt(CompoundTag nbt) {
      ListTag stackKeys = nbt.getList("k", 10);
      long[] stackAmounts = nbt.getLongArray("a");
      CellDataStorage dataStorage = new CellDataStorage();
      dataStorage.storedMap = new AEKeyMap<>(stackAmounts.length);

      for (int i = 0; i < stackAmounts.length; i++) {
         dataStorage.storedMap.set(AEKey.fromTagGeneric((CompoundTag)stackKeys.get(i)), stackAmounts[i]);
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
   public void setStoredMap(AEKeyMap<AEKey> storedMap) {
      this.storedMap = storedMap;
   }

   @Generated
   public AEKeyMap<AEKey> getStoredMap() {
      return this.storedMap;
   }
}
