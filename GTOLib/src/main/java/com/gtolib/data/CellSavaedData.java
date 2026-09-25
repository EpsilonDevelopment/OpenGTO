package com.gtolib.data;

import com.gto.datasynclib.util.holder.BooleanHolder;
import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import com.gtolib.GTOCore;
import com.gtolib.api.ae2.storage.BigCellDataStorage;
import com.gtolib.api.ae2.storage.CellDataStorage;
import com.gtolib.api.misc.FastSavedData;
import com.gtolib.utils.FileUtils;
import com.gtolib.utils.ServerUtils;
import com.gtolib.utils.iostream.DataIOStream;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.TickTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CellSavaedData extends FastSavedData {
   public static final String NAME = "storage_cell_data";
   public static CellSavaedData INSTANCE = new CellSavaedData();
   private final Object2ObjectOpenHashMap<UUID, CellDataStorage> cells = new O2OOpenCacheHashMap<>();
   private final Object2ObjectOpenHashMap<UUID, BigCellDataStorage> bigCells = new O2OOpenCacheHashMap<>();
   private File a;
   private File b;
   @Nullable
   private File dataFile;

   public CellSavaedData() {
      this.setFile(GTOCore.getFile("cell_data"), null);
   }

   public CellSavaedData(CompoundTag nbt) {
      ListTag cellList = nbt.getList("l", 10);
      int size = cellList.size();

      for (int i = 0; i < size; i++) {
         CompoundTag cell = cellList.getCompound(i);
         this.cells.put(cell.getUUID("u"), CellDataStorage.fromNbt(cell.getCompound("d")));
      }

      ListTag bigCellList = nbt.getList("b", 10);
      size = bigCellList.size();

      for (int i = 0; i < size; i++) {
         CompoundTag cell = bigCellList.getCompound(i);
         this.bigCells.put(cell.getUUID("u"), BigCellDataStorage.fromNbt(cell.getCompound("d")));
      }

      this.setDirty(true);
   }

   public void setFile(File file, File dataFile) {
      this.a = new File(file, "a");
      this.b = new File(file, "b");
      this.dataFile = dataFile;
   }

   public static CellSavaedData read(DataIOStream stream) {
      try {
         byte[] data = new byte[1];
         stream.readFully(data);
         if (data[0] == 1) {
            return new CellSavaedData();
         }
      } catch (Throwable var2) {
      }

      return null;
   }

   @Override
   public void save(DataIOStream stream) throws IOException {
      stream.write(new byte[]{1});
   }

   @Override
   public void save(@NotNull File file) {
      if (this.isDirty()) {
         ObjectIterator<Entry<UUID, CellDataStorage>> ait = this.cells.object2ObjectEntrySet().fastIterator();

         while (ait.hasNext()) {
            Entry<UUID, CellDataStorage> entry = ait.next();
            CellDataStorage v = entry.getValue();
            if (v.getStoredMap() == null || v.getStoredMap().isEmpty()) {
               try {
                  Files.deleteIfExists(new File(this.a, entry.getKey().toString()).toPath());
               } catch (IOException var8) {
               }
            } else if (v.isDirty()) {
               FileUtils.saveToFile(v, new File(this.a, entry.getKey().toString()), CellDataStorage.IO_CODEC);
               v.setDirty(false);
            }
         }

         ObjectIterator<Entry<UUID, BigCellDataStorage>> bit = this.bigCells.object2ObjectEntrySet().fastIterator();

         while (bit.hasNext()) {
            Entry<UUID, BigCellDataStorage> entry = bit.next();
            BigCellDataStorage v = entry.getValue();
            if (v.getStoredMap() == null || v.getStoredMap().isEmpty()) {
               try {
                  Files.deleteIfExists(new File(this.b, entry.getKey().toString()).toPath());
               } catch (IOException var7) {
               }
            } else if (v.isDirty()) {
               FileUtils.saveToFile(v, new File(this.b, entry.getKey().toString()), BigCellDataStorage.IO_CODEC);
               v.setDirty(false);
            }
         }

         this.setDirty(false);
      }
   }

   public CellDataStorage getOrCreateCell(@NotNull UUID uuid) {
      BooleanHolder create = new BooleanHolder();
      CellDataStorage c = this.cells.computeIfAbsent(uuid, k -> {
         create.value = true;
         File fa = new File(this.a, uuid.toString());
         File fb = new File(this.b, uuid.toString());
         CellDataStorage dataA = CellDataStorage.loadOrCreate(fa);
         BigCellDataStorage b = this.bigCells.get(uuid);
         if (b != null) {
            dataA.importFromB(b);
         } else if (fb.exists()) {
            b = FileUtils.loadFromFile(fb, BigCellDataStorage.IO_CODEC);
            this.bigCells.put(uuid, b);
            dataA.importFromB(b);
         }

         return dataA;
      });
      if (create.value) {
         ServerUtils.getServer().tell(new TickTask(0, this::saveNow));
      }

      return c;
   }

   public BigCellDataStorage getOrCreateBigCell(@NotNull UUID uuid) {
      BooleanHolder create = new BooleanHolder(false);
      BigCellDataStorage c = this.bigCells.computeIfAbsent(uuid, k -> {
         create.value = false;
         File fa = new File(this.a, uuid.toString());
         File fb = new File(this.b, uuid.toString());
         BigCellDataStorage dataB = BigCellDataStorage.loadOrCreate(fb);
         CellDataStorage a = this.cells.get(uuid);
         if (a != null) {
            dataB.importFromA(a);
         } else if (fa.exists()) {
            a = FileUtils.loadFromFile(fa, CellDataStorage.IO_CODEC);
            this.cells.put(uuid, a);
            dataB.importFromA(a);
         }

         return dataB;
      });
      if (create.value) {
         ServerUtils.getServer().tell(new TickTask(0, this::saveNow));
      }

      return c;
   }

   public void saveNow() {
      if (this.dataFile != null) {
         this.save(this.dataFile);
      }
   }
}
