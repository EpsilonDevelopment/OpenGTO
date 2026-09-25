package com.gtolib.api.ae2.stacks;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKey;
import com.gtolib.GTOCore;
import com.gtolib.utils.MapValueCache;
import corgitaco.corgilib.shadow.blue.endless.jankson.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class TagPrefixKeyType extends AEKeyType {
   public static final AEKeyType TYPE = new TagPrefixKeyType();
   public static final MapValueCache<TagPrefix, AEKey> map = new MapValueCache<>(TagPrefixKey::new);
   public static final MapValueCache<FluidStorageKey, AEKey> fluidMap = new MapValueCache<>(TagPrefixKey::new);

   private TagPrefixKeyType() {
      super(GTOCore.id("key_type"), TagPrefixKey.class, Component.translatable("gtocore.aekey.tag_prefix"));
   }

   @Nullable
   @Override
   public AEKey readFromPacket(FriendlyByteBuf input) {
      boolean isFluid = input.readBoolean();
      return isFluid ? fluidMap.getCache(FluidStorageKey.getByName(input.readResourceLocation())) : map.getCache(TagPrefix.getPrefix(input.readUtf()));
   }

   @Nullable
   @Override
   public AEKey loadKeyFromTag(CompoundTag tag) {
      return tag.getBoolean("isFluid")
         ? fluidMap.getCache(FluidStorageKey.getByName(ResourceLocation.parse(tag.getString("storageKey"))))
         : map.getCache(TagPrefix.getPrefix(tag.getString("prefix")));
   }

   @Nullable
   @Override
   public String getUnitSymbol() {
      return Component.translatable("gtocore.aekey.tag_prefix.unit").getString();
   }
}
