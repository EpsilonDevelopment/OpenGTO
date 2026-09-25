package com.gtolib.data;

import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import com.gto.fastcollection.fastutil.OpenCacheHashSet;
import com.gtolib.api.data.GTODimensions;
import com.gtolib.api.misc.FastSavedData;
import com.gtolib.utils.FileUtils;
import com.gtolib.utils.RLUtils;
import com.gtolib.utils.iostream.DataIOStream;
import com.gtolib.utils.iostream.IOStreamCodec;
import com.gtolib.utils.iostream.IOStreamDecoder;
import com.gtolib.utils.iostream.IOStreamEncoder;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.Generated;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public final class CommonSavaedData extends FastSavedData {
   private static final IOStreamCodec<String> STRING = new IOStreamCodec<String>() {
      public String decode(DataIOStream stream) throws IOException {
         byte[] bytes = new byte[stream.readInt()];
         stream.readFully(bytes);
         return new String(CommonSavaedData.xor(bytes), StandardCharsets.UTF_8);
      }

      public void encode(DataIOStream stream, String obj) throws IOException {
         byte[] bytes = CommonSavaedData.xor(obj.getBytes(StandardCharsets.UTF_8));
         stream.writeInt(bytes.length);
         stream.write(bytes);
      }
   };
   public static CommonSavaedData INSTANCE = new CommonSavaedData();
   private final Map<UUID, Set<ResourceKey<Level>>> planetUnlocked = new O2OOpenCacheHashMap<>();
   private CompoundTag data = new CompoundTag();
   private boolean serverLangInitialized;

   public static void setData(CompoundTag data) {
      INSTANCE.data = data == null ? new CompoundTag() : data;
      INSTANCE.setDirty(true);
   }

   public static CompoundTag getData() {
      if (INSTANCE.data == null) {
         INSTANCE.data = new CompoundTag();
         INSTANCE.setDirty(true);
      }

      return INSTANCE.data;
   }

   public CommonSavaedData() {
   }

   public CommonSavaedData(CompoundTag compoundTag) {
      this();
      this.data = compoundTag.getCompound("data");

      for (Tag tag : compoundTag.getList("l", 10)) {
         CompoundTag playerTag = (CompoundTag)tag;
         Set<ResourceKey<Level>> set = new ReferenceOpenHashSet<>();

         for (Tag planetTag : playerTag.getList("p", 10)) {
            set.add(GTODimensions.getDimensionKey(RLUtils.parse(((CompoundTag)planetTag).getString("n"))));
         }

         this.planetUnlocked.put(playerTag.getUUID("u"), set);
      }

      this.setDirty(true);
   }

   private static byte[] xor(byte[] input) {
      int i = 0;

      for (int len = input.length; i < len; i++) {
         input[i] = (byte)(input[i] ^ 67);
      }

      return input;
   }

   public static CommonSavaedData read(DataIOStream stream) {
      CommonSavaedData data = new CommonSavaedData();

      try {
         data.planetUnlocked
            .putAll(
               IOStreamDecoder.old_map(
                     FileUtils.OLD_UUID,
                     IOStreamDecoder.old_collection(OpenCacheHashSet::new, IOStreamDecoder.convert(RLUtils.IO_CODEC, GTODimensions::getDimensionKey))
                  )
                  .decode(stream)
            );
         data.data = TagParser.parseTag(STRING.decode(stream));

         try {
            data.serverLangInitialized = IOStreamCodec.BOOLEAN_CODEC.decode(stream);
         } catch (Throwable var3) {
         }

         return data;
      } catch (Throwable ignored) {
         return null;
      }
   }

   @Override
   public void save(@NotNull DataIOStream stream) throws IOException {
      IOStreamEncoder.old_map(FileUtils.OLD_UUID, IOStreamEncoder.old_collection(IOStreamEncoder.convert(RLUtils.IO_CODEC, GTODimensions::getLocation)))
         .encode(stream, this.planetUnlocked);
      STRING.encode(stream, this.data.toString());
      IOStreamCodec.BOOLEAN_CODEC.encode(stream, this.serverLangInitialized);
      IOStreamCodec.INT_CODEC.encode(stream, 1);
   }

   public void markServerLangInitialized() {
      this.serverLangInitialized = true;
      this.setDirty(true);
   }

   @Generated
   public Map<UUID, Set<ResourceKey<Level>>> getPlanetUnlocked() {
      return this.planetUnlocked;
   }

   @Generated
   public boolean isServerLangInitialized() {
      return this.serverLangInitialized;
   }
}
