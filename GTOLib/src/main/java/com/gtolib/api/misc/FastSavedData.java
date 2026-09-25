package com.gtolib.api.misc;

import com.gtolib.utils.FileUtils;
import com.gtolib.utils.iostream.DataIOStream;
import com.gtolib.utils.iostream.IOStreamDecoder;
import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class FastSavedData extends SavedData {
   public static <T extends FastSavedData> T get(String name, DimensionDataStorage dataStorage, IOStreamDecoder<T> loadFunction, Supplier<T> createFunction) {
      T cache = getFromFile(name, dataStorage, loadFunction);
      if (cache != null) {
         return cache;
      }

      cache = (T)createFunction.get();
      dataStorage.cache.put(name, cache);
      return cache;
   }

   @Nullable
   public static <T extends FastSavedData> T getFromFile(String name, DimensionDataStorage dataStorage, IOStreamDecoder<T> loadFunction) {
      SavedData cache = dataStorage.cache.get(name);
      if (cache != null) {
         return (T)cache;
      }

      File file = dataStorage.getDataFile(name);
      if (file.exists()) {
         cache = FileUtils.loadFromFile(file, loadFunction);
         if (cache != null) {
            dataStorage.cache.put(name, cache);
            return (T)cache;
         }
      }

      return null;
   }

   public abstract void save(DataIOStream var1) throws IOException;

   @NotNull
   @Override
   public final CompoundTag save(CompoundTag compoundTag) {
      return compoundTag;
   }

   @Override
   public void save(@NotNull File file) {
      if (this.isDirty()) {
         FileUtils.saveToFile(this, file, (stream, obj) -> obj.save(stream));
         this.setDirty(false);
      }
   }
}
