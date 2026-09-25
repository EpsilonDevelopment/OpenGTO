package com.gtolib.api.item;

import com.gtolib.utils.ItemUtils;
import com.gtolib.utils.RegistriesUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record NBTItem(Item item, CompoundTag nbt) {
   public static NBTItem of(ItemStack stack) {
      CompoundTag tag = stack.getTag();
      boolean hasTag = tag != null && !tag.isEmpty();
      return new NBTItem(stack.getItem(), hasTag ? tag : null);
   }

   public static NBTItem of(CompoundTag tag) {
      return new NBTItem(RegistriesUtils.getItem(tag.getString("id")), tag.getCompound("tag"));
   }

   public ItemStack toStack() {
      return this.toStack(1);
   }

   public ItemStack toStack(int count) {
      return new ItemStack(this.item, count, this.nbt);
   }

   public CompoundTag serializeNBT() {
      CompoundTag tag = new CompoundTag();
      tag.putString("id", ItemUtils.getId(this.item));
      if (this.nbt != null) {
         tag.put("tag", this.nbt);
      }

      return tag;
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof NBTItem(Item item1, CompoundTag nbt1) && this.item == item1)) {
         return false;
      } else {
         return this.nbt == null ? nbt1 == null : this.nbt.equals(nbt1);
      }
   }

   @Override
   public int hashCode() {
      int result = 17;
      result = 31 * result + (this.item == null ? 0 : this.item.hashCode());
      return 31 * result + (this.nbt == null ? 0 : this.nbt.hashCode());
   }
}
