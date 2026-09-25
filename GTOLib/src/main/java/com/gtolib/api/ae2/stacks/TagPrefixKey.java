package com.gtolib.api.ae2.stacks;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKey;
import com.gtolib.GTOCore;
import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.emi.stack.TagPrefixNameUtil;
import java.util.List;
import lombok.Generated;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

@DataGeneratorScanned
public class TagPrefixKey extends AEKey {
   @RegisterLanguage(cn = "单位", en = "Unit")
   static final String UNIT = "gtocore.aekey.tag_prefix.unit";
   private static final ResourceLocation ID = GTOCore.id("tag_prefix");
   private final TagPrefix prefix;
   private final FluidStorageKey storageKey;
   private final boolean isFluidKey;

   TagPrefixKey(TagPrefix prefix) {
      this.prefix = prefix;
      this.storageKey = null;
      this.isFluidKey = false;
   }

   TagPrefixKey(FluidStorageKey storageKey) {
      this.prefix = null;
      this.storageKey = storageKey;
      this.isFluidKey = true;
   }

   public static TagPrefixKey of(TagPrefix prefix) {
      return (TagPrefixKey)TagPrefixKeyType.map.getCache(prefix);
   }

   @Override
   public AEKeyType getType() {
      return TagPrefixKeyType.TYPE;
   }

   @Override
   public AEKey dropSecondary() {
      return this;
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putBoolean("isFluid", this.isFluidKey);
      if (this.isFluidKey) {
         tag.putString("storageKey", this.storageKey.getResourceLocation().toString());
      } else {
         tag.putString("prefix", this.prefix.name);
      }

      return tag;
   }

   @Override
   public Object getPrimaryKey() {
      return this;
   }

   @Override
   public ResourceLocation getId() {
      return ID;
   }

   @Override
   public void writeToPacket(FriendlyByteBuf data) {
      data.writeBoolean(this.isFluidKey);
      if (this.isFluidKey) {
         data.writeResourceLocation(this.storageKey.getResourceLocation());
      } else {
         data.writeUtf(this.prefix.name);
      }
   }

   @Override
   protected Component computeDisplayName() {
      return TagPrefixNameUtil.getName(this.isFluidKey, this.prefix, this.storageKey);
   }

   @Override
   public void addDrops(long amount, List<ItemStack> drops, Level level, BlockPos pos) {
   }

   @Nullable
   public AEKey getFromMaterial(Material material) {
      if (this.isFluidKey) {
         FluidProperty fluidProp = material.getProperty(PropertyKey.FLUID);
         if (fluidProp == null) {
            return null;
         }

         Fluid result = fluidProp.getStorage().get(this.storageKey);
         return result != null ? AEFluidKey.of(result) : null;
      } else {
         ItemStack item = ChemicalHelper.get(this.prefix, material, 1);
         return AEItemKey.of(item);
      }
   }

   @Generated
   public TagPrefix getPrefix() {
      return this.prefix;
   }

   @Generated
   public FluidStorageKey getStorageKey() {
      return this.storageKey;
   }

   @Generated
   public boolean isFluidKey() {
      return this.isFluidKey;
   }
}
