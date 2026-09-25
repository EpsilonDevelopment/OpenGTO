package com.gtolib.mixin.mc.nbt;

import com.gto.datasynclib.util.holder.IntHolder;
import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CompoundTag.class, priority = 2000)
public abstract class CompoundTagMixin {
   @Shadow
   public Map<String, Tag> tags;

   @Overwrite
   public int sizeInBytes() {
      IntHolder var1 = new IntHolder(48);
      this.tags.forEach((var1x, var2) -> {
         var1.value = var1.value + 28 + 2 * var1x.length();
         var1.value += 36;
         var1.value = var1.value + var2.sizeInBytes();
      });
      return var1.value;
   }

   @Overwrite
   protected Map<String, Tag> entries() {
      return this.tags;
   }

   @Inject(method = "<init>(Ljava/util/Map;)V", at = @At("TAIL"))
   private void init(Map<String, Tag> var1, CallbackInfo var2) {
      if (!(var1 instanceof O2OOpenCacheHashMap)) {
         this.tags = new O2OOpenCacheHashMap<>(var1);
      }
   }

   @ModifyArg(method = "<init>()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;<init>(Ljava/util/Map;)V"))
   private static Map<String, Tag> useFasterCollection(Map<String, Tag> var0) {
      return new O2OOpenCacheHashMap<>();
   }

   @Redirect(method = "<init>()V", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Maps;newHashMap()Ljava/util/HashMap;", remap = false))
   private static HashMap<?, ?> removeOldMapAlloc() {
      return null;
   }

   @Overwrite
   public CompoundTag copy() {
      O2OOpenCacheHashMap var1 = new O2OOpenCacheHashMap(this.tags.size());
      ((Object2ObjectOpenHashMap<String, Tag>)this.tags).object2ObjectEntrySet().fastForEach(var1x -> var1.put(var1x.getKey(), var1x.getValue().copy()));
      return new CompoundTag(var1);
   }

   @Mixin(targets = "net.minecraft.nbt.CompoundTag$1")
   static class Type {
      @ModifyVariable(
         method = "load(Ljava/io/DataInput;ILnet/minecraft/nbt/NbtAccounter;)Lnet/minecraft/nbt/CompoundTag;",
         at = @At(value = "INVOKE_ASSIGN", target = "Lcom/google/common/collect/Maps;newHashMap()Ljava/util/HashMap;", remap = false)
      )
      private Map<String, Tag> useFasterCollection(Map<String, Tag> var1) {
         return new O2OOpenCacheHashMap<>();
      }

      @Redirect(
         method = "load(Ljava/io/DataInput;ILnet/minecraft/nbt/NbtAccounter;)Lnet/minecraft/nbt/CompoundTag;",
         at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Maps;newHashMap()Ljava/util/HashMap;", remap = false)
      )
      private HashMap<?, ?> removeOldMapAlloc() {
         return null;
      }
   }
}
