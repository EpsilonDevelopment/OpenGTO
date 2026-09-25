package com.gtolib.mixin.mc;

import com.gtolib.api.item.IItem;
import com.gtolib.mc.IIngredient;
import com.gtolib.utils.EmptyStream;
import com.gtolib.utils.GTOUtils;
import com.gtolib.utils.RegistriesUtils;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Ingredient.ItemValue;
import net.minecraftforge.common.extensions.IForgeItem;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = Item.class, priority = 0)
public abstract class ItemMixin implements IItem, IForgeItem {
   @Shadow
   @Final
   private int maxDamage;
   @Shadow
   @Final
   private int maxStackSize;
   @Unique
   private ItemStack gtolib$readOnlyStack;
   @Unique
   private Ingredient gtolib$ingredient;
   @Unique
   private int[] gtolib$mapItem;
   @Unique
   private ResourceLocation gtolib$id;
   @Unique
   private Supplier<Component>[] gtolib$components;

   @Overwrite
   @Override
   public String toString() {
      return this.gtolib$getIdLocation().getPath();
   }

   @Override
   public boolean isDamageable(ItemStack var1) {
      return this.canBeDepleted();
   }

   @Shadow
   public abstract boolean canBeDepleted();

   @Override
   public int getMaxDamage(ItemStack var1) {
      return this.maxDamage;
   }

   @Override
   public void gtolib$setMapItem(int[] var1) {
      this.gtolib$mapItem = var1;
   }

   @SafeVarargs
   @Override
   public final void gtolib$setToolTips(Supplier<Component>... var1) {
      this.gtolib$components = var1;
   }

   @Override
   public int[] gtolib$getMapItem() {
      return this.gtolib$mapItem;
   }

   @Override
   public Ingredient gtolib$getIngredient() {
      if (this.gtolib$ingredient == null) {
         this.gtolib$ingredient = new Ingredient(EmptyStream.create(GTOUtils.array(new ItemValue(this.gtolib$getReadOnlyStack()))));
         ((IIngredient)this.gtolib$ingredient).gtolib$setCache();
      }

      return this.gtolib$ingredient;
   }

   @Override
   public Supplier<Component>[] gtolib$getToolTips() {
      return this.gtolib$components;
   }

   @Override
   public ItemStack gtolib$getReadOnlyStack() {
      if (this.gtolib$readOnlyStack == null) {
         this.gtolib$readOnlyStack = new ItemStack((Item)(Object)this);
      }

      this.gtolib$readOnlyStack.count = 1;
      return this.gtolib$readOnlyStack;
   }

   @Override
   public ResourceLocation gtolib$getIdLocation() {
      ResourceLocation var1 = this.gtolib$id;
      if (var1 == null) {
         var1 = ForgeRegistries.ITEMS.getKey((Item)(Object)this);
         if (RegistriesUtils.IDCache) {
            this.gtolib$id = var1;
         }
      }

      return var1;
   }

   @Override
   public int getMaxStackSize(ItemStack var1) {
      return this.maxStackSize;
   }

   @Shadow
   @Deprecated
   public abstract int getMaxStackSize();
}
