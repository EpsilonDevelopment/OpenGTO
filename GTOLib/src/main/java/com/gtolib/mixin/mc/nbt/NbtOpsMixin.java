package com.gtolib.mixin.mc.nbt;

import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import com.mojang.serialization.DataResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(targets = "net.minecraft.nbt.NbtOps.NbtRecordBuilder", priority = 2000)
public class NbtOpsMixin {
   @Overwrite
   protected DataResult<Tag> build(CompoundTag var1, Tag var2) {
      if (var2 == null || var2 == EndTag.INSTANCE) {
         return DataResult.success(var1);
      } else if (var2 instanceof CompoundTag var3) {
         CompoundTag var4 = new CompoundTag(new O2OOpenCacheHashMap<>(var3.tags));
         var1.tags.forEach(var4::put);
         return DataResult.success(var4);
      } else {
         return DataResult.error(() -> "mergeToMap called with not a map: " + var2, var2);
      }
   }
}
