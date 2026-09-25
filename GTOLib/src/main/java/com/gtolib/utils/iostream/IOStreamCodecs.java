package com.gtolib.utils.iostream;

import com.gto.datasynclib.datastream.codec.DataCodec;
import com.gto.datasynclib.datastream.data.ByteArrayData;
import com.gto.datasynclib.datastream.data.Data;
import com.gtolib.utils.ItemUtils;
import com.gtolib.utils.RLUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import lombok.Generated;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public final class IOStreamCodecs {
   public static final IOStreamCodec<ResourceLocation> RESOURCE_LOCATION = new IOStreamCodec<ResourceLocation>() {
      public void encode(DataIOStream stream, ResourceLocation obj) throws IOException {
         stream.writeUTF(obj.getNamespace());
         stream.writeUTF(obj.getPath());
      }

      public ResourceLocation decode(DataIOStream stream) throws IOException {
         return new ResourceLocation(stream.readUTF().intern(), stream.readUTF(), null);
      }
   };
   public static final IOStreamCodec<CompoundTag> COMPOUND_TAG = new IOStreamCodec<CompoundTag>() {
      public void encode(DataIOStream stream, CompoundTag obj) throws IOException {
         NbtIo.write(obj, stream);
      }

      public CompoundTag decode(DataIOStream stream) throws IOException {
         return NbtIo.read(stream);
      }
   };
   public static final IOStreamCodec<Item> ITEM = new IOStreamCodec<Item>() {
      public void encode(DataIOStream stream, Item obj) throws IOException {
         RLUtils.IO_CODEC.encode(stream, ItemUtils.getIdLocation(obj));
      }

      public Item decode(DataIOStream stream) throws IOException {
         return ForgeRegistries.ITEMS.getValue(RLUtils.IO_CODEC.decode(stream));
      }
   };
   public static final IOStreamCodec<ItemStack> ITEM_STACK = new IOStreamCodec<ItemStack>() {
      public void encode(DataIOStream stream, ItemStack obj) throws IOException {
         Item item = obj.getItem();
         if (item == Items.AIR) {
            stream.writeVarInt(0);
         } else {
            stream.writeVarInt(obj.getCount());
            RLUtils.IO_CODEC.encode(stream, ItemUtils.getIdLocation(item));
            CompoundTag tag = obj.getTag();
            if (tag != null && !tag.isEmpty()) {
               stream.writeBoolean(true);
               NbtIo.write(tag, stream);
            } else {
               stream.writeBoolean(false);
            }
         }
      }

      public ItemStack decode(DataIOStream stream) throws IOException {
         int count = stream.readVarInt();
         if (count == 0) {
            return ItemStack.EMPTY;
         }

         Item item = ForgeRegistries.ITEMS.getValue(RLUtils.IO_CODEC.decode(stream));
         if (item == Items.AIR) {
            return ItemStack.EMPTY;
         }

         ItemStack stack = new ItemStack(item, count);
         if (stream.readBoolean()) {
            stack.setTag(NbtIo.read(stream));
         }

         return stack;
      }
   };

   public static <T> IOStreamCodec<T> of(final DataCodec<T> codec) {
      return new IOStreamCodec<T>() {
         @Override
         public void encode(DataIOStream stream, T obj) throws IOException {
            stream.writeByteArray(codec.encode(obj).writeToBytes());
         }

         @Override
         public T decode(DataIOStream stream) throws IOException {
            return codec.decode(Data.readData(stream.readByteArray()));
         }
      };
   }

   public static <T> DataCodec<T> toDataCodec(final IOStreamCodec<T> codec) {
      return new DataCodec<T>() {
         @NotNull
         @Override
         public Data encode(T t) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            try (DataOutputStreamWrapper stream = DataIOStream.of(bos)) {
               codec.encode(stream, t);
               stream.flush();
            } catch (IOException e) {
               throw new RuntimeException(e);
            }

            return ByteArrayData.valueOf(bos.toByteArray());
         }

         @Override
         public T decode(@NotNull Data data, int dataVersion) {
            try (DataInputStreamWrapper stream = DataIOStream.of(data.getByteArray())) {
               return codec.decode(stream);
            } catch (IOException e) {
               throw new RuntimeException(e);
            }
         }
      };
   }

   @Generated
   private IOStreamCodecs() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
