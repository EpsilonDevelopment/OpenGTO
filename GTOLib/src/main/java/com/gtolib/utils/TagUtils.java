package com.gtolib.utils;

import com.gtolib.utils.iostream.DataIOStream;
import com.gtolib.utils.iostream.IOStreamCodec;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.io.IOException;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader.EntryWithSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public final class TagUtils {
   public static final IOStreamCodec<EntryWithSource> SOURCE_IO_CODEC = new IOStreamCodec<EntryWithSource>() {
      public void encode(DataIOStream stream, EntryWithSource obj) throws IOException {
         RLUtils.IO_CODEC.encode(stream, obj.entry().getId());
         stream.writeBoolean(obj.entry().isTag());
         stream.writeBoolean(obj.entry().isRequired());
         stream.writeBoolean(obj.remove());
      }

      public EntryWithSource decode(DataIOStream stream) throws IOException {
         return new EntryWithSource(new TagEntry(RLUtils.IO_CODEC.decode(stream), stream.readBoolean(), stream.readBoolean()), "", stream.readBoolean());
      }
   };
   public static boolean bind = true;
   public static Set<Runnable> bindedCallbacks = new ReferenceOpenHashSet<>();

   private TagUtils() {
   }

   public static TagKey<Block> createBlockTag(ResourceLocation id) {
      return TagKey.create(Registries.BLOCK, id);
   }

   public static TagKey<Fluid> createFluidTag(ResourceLocation id) {
      return TagKey.create(Registries.FLUID, id);
   }

   public static TagKey<Item> createItemTag(ResourceLocation id) {
      return TagKey.create(Registries.ITEM, id);
   }

   public static TagKey<Item> createItemTag(String tagString) {
      return createItemTag(RLUtils.parse(tagString));
   }

   public static TagKey<Item> createForgeItemTag(String path) {
      return createItemTag(RLUtils.fromNamespaceAndPath("forge", path));
   }

   public static TagKey<Item> createTGItemTag(String path) {
      return createItemTag(RLUtils.fromNamespaceAndPath("c", path));
   }
}
