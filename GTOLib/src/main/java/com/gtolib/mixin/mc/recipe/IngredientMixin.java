package com.gtolib.mixin.mc.recipe;

import com.gtolib.api.item.IItem;
import com.gtolib.mc.IIngredient;
import com.gtolib.mc.ITagKey;
import com.gtolib.utils.TagUtils;
import java.util.ArrayList;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.HolderSet.Named;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Ingredient.ItemValue;
import net.minecraft.world.item.crafting.Ingredient.TagValue;
import net.minecraft.world.item.crafting.Ingredient.Value;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import org.embeddedt.modernfix.forge.load.MinecraftServerReloadTracker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Ingredient.class, priority = 2000)
public abstract class IngredientMixin implements IIngredient {
   @Shadow
   @Final
   public Value[] values;
   @Final
   @Shadow(remap = false)
   private boolean isVanilla;
   @Shadow
   @Nullable
   public ItemStack[] itemStacks;
   @Unique
   private boolean gtolib$isCache;

   @Shadow
   public abstract boolean isEmpty();

   @Override
   public void gtolib$setCache() {
      this.gtolib$isCache = true;
   }

   @Overwrite
   public ItemStack[] getItems() {
      if (this.itemStacks == null) {
         this.itemStacks = this.gtolib$computeItemsArray();
      }

      return this.itemStacks;
   }

   @Unique
   private ItemStack[] gtolib$computeItemsArray() {
      if (this.values.length == 1) {
         Value var1 = this.values[0];
         if (var1 instanceof ItemValue var10) {
            return new ItemStack[]{this.gtolib$isCache ? var10.item.copy() : var10.item};
         }

         if (var1 instanceof TagValue var11) {
            if (TagUtils.bind && this.gtolib$isCache) {
               TagUtils.bindedCallbacks.add(() -> this.itemStacks = null);
            }

            Optional var12 = BuiltInRegistries.ITEM.getTag(var11.tag);
            if (!var12.isPresent()) {
               return new ItemStack[]{new ItemStack(Blocks.BARRIER).setHoverName(Component.literal("Empty Tag: " + var11.tag.location()))};
            }

            Named var13 = (Named)var12.get();
            int var6 = var13.size();
            ItemStack[] var7 = new ItemStack[var6];

            for (int var8 = 0; var8 < var6; var8++) {
               var7[var8] = new ItemStack(var13.get(var8));
            }

            return var7;
         }
      }

      ArrayList<ItemStack> var9 = new ArrayList<>(2);

      for (Value var5 : this.values) {
         var9.addAll(var5.getItems());
      }

      return var9.toArray(ItemStack[]::new);
   }

   @Overwrite
   public boolean test(ItemStack var1) {
      if (var1 == null) {
         return false;
      }

      Item var2 = var1.getItem();
      if (this.isEmpty()) {
         return var2 == Items.AIR;
      }

      if (this.isVanilla && this.values.length == 1) {
         Value var3 = this.values[0];
         if (var3 instanceof TagValue var4) {
            if (!MinecraftServerReloadTracker.isReloadActive()) {
               return var2.builtInRegistryHolder().is(var4.tag);
            }
         } else if (var3 instanceof ItemValue var9) {
            return var2 == var9.item.getItem();
         }
      }

      for (ItemStack var6 : this.getItems()) {
         if (var6.is(var2)) {
            return true;
         }
      }

      return false;
   }

   @Overwrite
   public static Ingredient of(TagKey<Item> var0) {
      return ((ITagKey)(Object)var0).gtolib$getIngredient();
   }

   @Inject(method = "of([Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/item/crafting/Ingredient;", at = @At("HEAD"), cancellable = true)
   private static void of(ItemLike[] var0, CallbackInfoReturnable<Ingredient> var1) {
      if (var0.length == 0) {
         var1.setReturnValue(Ingredient.EMPTY);
      } else if (var0.length == 1) {
         var1.setReturnValue(((IItem)var0[0].asItem()).gtolib$getIngredient());
      }
   }

   @Inject(method = "of([Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/crafting/Ingredient;", at = @At("HEAD"), cancellable = true)
   private static void of(ItemStack[] var0, CallbackInfoReturnable<Ingredient> var1) {
      if (var0.length == 0) {
         var1.setReturnValue(Ingredient.EMPTY);
      } else if (var0.length == 1 && !var0[0].hasTag() && var0[0].count == 1) {
         var1.setReturnValue(((IItem)var0[0].getItem()).gtolib$getIngredient());
      }
   }
}
