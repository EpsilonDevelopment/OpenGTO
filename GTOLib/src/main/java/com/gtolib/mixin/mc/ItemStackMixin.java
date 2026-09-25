package com.gtolib.mixin.mc;

import com.gtolib.utils.ItemUtils;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder.Reference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemStack.class, priority = 0)
public abstract class ItemStackMixin extends CapabilityProvider<ItemStack> {
   @Unique
   private boolean gtolib$forgeInit;
   @Unique
   private boolean gtolib$isEmpty;
   @Shadow
   @Nullable
   private CompoundTag tag;
   @Shadow
   public int count;
   @Mutable
   @Shadow
   @Final
   private Item item;
   @Shadow(remap = false)
   private CompoundTag capNBT;

   @Shadow
   public abstract void setCount(int var1);

   protected ItemStackMixin(Class<ItemStack> var1) {
      super(var1);
   }

   @Inject(method = "<init>(Ljava/lang/Void;)V", at = @At("TAIL"))
   private void gtolib$init(Void var1, CallbackInfo var2) {
      this.gtolib$isEmpty = true;
      this.item = Items.AIR;
   }

   @Redirect(
      method = "<init>(Lnet/minecraft/world/level/ItemLike;ILnet/minecraft/nbt/CompoundTag;)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraftforge/registries/IForgeRegistry;getDelegateOrThrow(Ljava/lang/Object;)Lnet/minecraft/core/Holder$Reference;",
         remap = false
      )
   )
   private <V> Reference<V> getDelegateOrThrow(IForgeRegistry var1, V var2) {
      return null;
   }

   @Redirect(
      method = "<init>(Lnet/minecraft/world/level/ItemLike;ILnet/minecraft/nbt/CompoundTag;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;forgeInit()V", remap = false)
   )
   private void forgeInit(ItemStack var1) {
      this.gtolib$isEmpty = this.item == Items.AIR;
   }

   @Redirect(
      method = "<init>(Lnet/minecraft/world/level/ItemLike;ILnet/minecraft/nbt/CompoundTag;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;isDamageable(Lnet/minecraft/world/item/ItemStack;)Z", remap = false)
   )
   private boolean isDamageable(Item var1, ItemStack var2) {
      return var1.canBeDepleted();
   }

   @Redirect(
      method = "<init>(Lnet/minecraft/nbt/CompoundTag;)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraftforge/registries/IForgeRegistry;getDelegateOrThrow(Ljava/lang/Object;)Lnet/minecraft/core/Holder$Reference;",
         remap = false
      )
   )
   private <V> Reference<V> getDelegateOrThrowTag(IForgeRegistry var1, V var2) {
      return null;
   }

   @Redirect(
      method = "<init>(Lnet/minecraft/nbt/CompoundTag;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;forgeInit()V", remap = false)
   )
   private void forgeInitTag(ItemStack var1) {
      this.gtolib$isEmpty = this.item == Items.AIR;
   }

   @Redirect(
      method = "<init>(Lnet/minecraft/nbt/CompoundTag;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;isDamageable(Lnet/minecraft/world/item/ItemStack;)Z", remap = false)
   )
   private boolean isDamageableTag(Item var1, ItemStack var2) {
      return false;
   }

   @Overwrite
   public boolean isEmpty() {
      return this.gtolib$isEmpty || this.count <= 0;
   }

   @Overwrite
   public Item getItem() {
      return this.count <= 0 ? Items.AIR : this.item;
   }

   @Overwrite
   public int getCount() {
      return this.gtolib$isEmpty ? 0 : this.count;
   }

   @Overwrite
   public void shrink(int var1) {
      this.setCount(this.count - var1);
   }

   @Overwrite
   public CompoundTag save(CompoundTag var1) {
      var1.putString("id", ItemUtils.getId(this.getItem()));
      var1.putByte("Count", (byte)Math.min(64, this.count));
      if (this.tag != null) {
         var1.put("tag", this.tag.copy());
      }

      CompoundTag var2 = this.serializeCaps();
      if (var2 != null && !var2.isEmpty()) {
         var1.put("ForgeCaps", var2);
      }

      return var1;
   }

   @Overwrite(remap = false)
   private void forgeInit() {
      if (!this.isEmpty()) {
         this.gatherCapabilities(() -> this.item.initCapabilities((ItemStack)(Object)this, this.capNBT));
         if (this.capNBT != null) {
            this.deserializeCaps(this.capNBT);
         }
      }
   }

   @NotNull
   @Override
   public <T> LazyOptional<T> getCapability(@NotNull Capability<T> var1, @Nullable Direction var2) {
      if (!this.gtolib$forgeInit) {
         this.gtolib$forgeInit = true;
         this.forgeInit();
      }

      return super.getCapability(var1, var2);
   }

   @Overwrite
   public static boolean isSameItemSameTags(ItemStack var0, ItemStack var1) {
      if (var0.getItem() == var1.getItem()) {
         CompoundTag var2 = var0.getTag();
         CompoundTag var3 = var1.getTag();
         return var2 != null && !var2.isEmpty() ? var2.equals(var3) : var3 == null || var3.isEmpty();
      } else {
         return false;
      }
   }
}
