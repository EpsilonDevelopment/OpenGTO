package com.gtolib.api.ae2;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.networking.IGrid;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.crafting.pattern.AEPatternDecoder;
import appeng.crafting.pattern.AEProcessingPattern;
import appeng.crafting.pattern.ProcessingPatternItem;
import com.google.common.collect.ObjectArrays;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.ItemIngredient;
import com.gto.fastcollection.cache.WeakValueIdentityHashCache;
import com.hepdd.gtmthings.common.item.VirtualFluidProviderBehavior;
import com.hepdd.gtmthings.common.item.VirtualItemProviderBehavior;
import com.hepdd.gtmthings.common.item.VirtualProviderData;
import com.hepdd.gtmthings.data.CustomItems;
import java.util.List;
import java.util.function.Function;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public final class MyPatternDetailsHelper {
   private static final Function<AEItemKey, AEProcessingPattern> CREATE_FUNCTION = AEProcessingPattern::new;
   private static final WeakValueIdentityHashCache<AEItemKey, AEProcessingPattern> CACHE = new WeakValueIdentityHashCache<>();

   public static AEProcessingPattern decode(AEItemKey key) {
      return CACHE.getCache(key, CREATE_FUNCTION);
   }

   @Nullable
   public static IPatternDetails decodePattern(ItemStack stack, BlockEntity blockEntity, IGrid grid) {
      if (stack == null || stack.isEmpty()) {
         return null;
      } else {
         return stack.getItem() instanceof ProcessingPatternItem
            ? createMyAEProcessingPattern(stack.getTag(), grid)
            : AEPatternDecoder.INSTANCE.decodePattern(stack, blockEntity.getLevel(), false);
      }
   }

   public static AEProcessingPattern gtocoreDecode(AEItemKey what, IGrid grid) {
      return what == null ? null : createMyAEProcessingPattern(what.getTag(), grid);
   }

   private static AEProcessingPattern createMyAEProcessingPattern(CompoundTag tag, IGrid grid) {
      try {
         if (tag != null && !tag.isEmpty()) {
            GenericStack[] sparseInputs = ProcessingPatternEncoding.getProcessingInputs(tag, grid);
            GenericStack[] sparseOutputs = ProcessingPatternEncoding.getProcessingOutputs(tag);
            ItemStack newDefinition = PatternDetailsHelper.encodeProcessingPattern(sparseInputs, sparseOutputs);
            if (tag.tags.get("recipe") instanceof StringTag stringTag) {
               newDefinition.getOrCreateTag().put("recipe", stringTag);
            }

            AEItemKey key = AEItemKey.of(newDefinition);
            return CACHE.getCache(key, CREATE_FUNCTION);
         } else {
            return null;
         }
      } catch (Exception e) {
         return null;
      }
   }

   @Contract("null, _, _ -> null")
   public static AEProcessingPattern convertFromGTRecipe(GTRecipeDefinition gtRecipe, int maxItemOut, int maxFluidOut) {
      if (gtRecipe != null && maxFluidOut + maxItemOut != 0) {
         List<Content<ItemIngredient>> itemInputs = gtRecipe.itemInputs;
         List<Content<FluidIngredient>> fluidInputs = gtRecipe.fluidInputs;
         List<Content<ItemIngredient>> itemOutputs = gtRecipe.itemOutputs;
         if (itemOutputs.size() > maxItemOut) {
            itemOutputs = itemOutputs.subList(0, maxItemOut);
         }

         List<Content<FluidIngredient>> fluidsOutputs = gtRecipe.fluidOutputs;
         if (fluidsOutputs.size() > maxFluidOut) {
            fluidsOutputs = fluidsOutputs.subList(0, maxFluidOut);
         }

         GenericStack[] sparseItemsInputs = itemInputs.stream()
            .filter(c -> c.chance == 10000)
            .filter(i -> !i.isEmpty())
            .map(c -> c.inner)
            .map(i -> new GenericStack(AEItemKey.of(i.getInnerItemStack()), i.amount))
            .toArray(GenericStack[]::new);
         GenericStack[] sparseItemsCatalysts = itemInputs.stream().filter(c -> c.chance == 0).filter(c -> !c.isEmpty()).map(c -> c.inner).map(i -> {
            ItemStack s = VirtualItemProviderBehavior.setVirtualItem(CustomItems.VIRTUAL_ITEM_PROVIDER.asStack(), i.getInnerItemStack());
            VirtualProviderData.setLocked(s, true);
            return new GenericStack(AEItemKey.of(s), i.amount);
         }).toArray(GenericStack[]::new);
         GenericStack[] sparseItemsOutput = itemOutputs.stream()
            .map(c -> c.inner)
            .filter(i -> !i.isEmpty())
            .map(i -> new GenericStack(AEItemKey.of(i.getInnerItemStack()), i.amount))
            .toArray(GenericStack[]::new);
         GenericStack[] sparseFluidsInputs = fluidInputs.stream()
            .filter(c -> c.chance == 10000)
            .filter(i -> !i.isEmpty())
            .map(c -> c.inner)
            .map(i -> new GenericStack(AEFluidKey.of(i.getFluid(), i.nbt), i.amount))
            .toArray(GenericStack[]::new);
         GenericStack[] sparseFluidsCatalysts = fluidInputs.stream().filter(c -> c.chance == 0).filter(c -> !c.isEmpty()).map(c -> c.inner).map(i -> {
            ItemStack s = VirtualFluidProviderBehavior.setVirtualFluid(CustomItems.VIRTUAL_FLUID_PROVIDER.asStack(), i.getFluidStack());
            VirtualProviderData.setLocked(s, true);
            return new GenericStack(AEItemKey.of(s), i.amount);
         }).toArray(GenericStack[]::new);
         GenericStack[] sparseFluidsOutput = fluidsOutputs.stream()
            .filter(i -> !i.isEmpty())
            .map(c -> c.inner)
            .map(i -> new GenericStack(AEFluidKey.of(i.getFluid(), i.nbt), i.amount))
            .toArray(GenericStack[]::new);
         GenericStack[] sparseInputs = ObjectArrays.concat(sparseItemsInputs, sparseFluidsInputs, GenericStack.class);
         sparseInputs = ObjectArrays.concat(sparseInputs, sparseItemsCatalysts, GenericStack.class);
         GenericStack[] var17 = ObjectArrays.concat(sparseInputs, sparseFluidsCatalysts, GenericStack.class);
         GenericStack[] sparseOutputs = ObjectArrays.concat(sparseItemsOutput, sparseFluidsOutput, GenericStack.class);
         if (sparseOutputs.length == 0) {
            return null;
         }

         ItemStack newDefinition = PatternDetailsHelper.encodeProcessingPattern(var17, sparseOutputs);
         return CACHE.getCache(AEItemKey.of(newDefinition), CREATE_FUNCTION);
      } else {
         return null;
      }
   }
}
