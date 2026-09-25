package com.gtolib.mixin.emi.plugin;

import java.util.Collections;
import java.util.List;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import mezz.jei.common.platform.IPlatformRegistry;
import mezz.jei.common.recipes.BrewingExtensionHelper;
import mezz.jei.common.util.StackHelper;
import mezz.jei.library.plugins.vanilla.VanillaPlugin;
import mezz.jei.library.plugins.vanilla.anvil.AnvilRecipeCategory;
import mezz.jei.library.plugins.vanilla.anvil.SmithingRecipeCategory;
import mezz.jei.library.plugins.vanilla.crafting.CraftingRecipeCategory;
import mezz.jei.library.plugins.vanilla.ingredients.ItemStackHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(VanillaPlugin.class)
public class VanillaPluginMixin {
   @Shadow(remap = false)
   @Nullable
   private CraftingRecipeCategory craftingCategory;
   @Shadow(remap = false)
   @Nullable
   private SmithingRecipeCategory smithingCategory;
   @Shadow(remap = false)
   @Nullable
   private BrewingExtensionHelper brewingExtensionHelper;

   @Overwrite(remap = false)
   public void registerItemSubtypes(ISubtypeRegistration var1) {
   }

   @Redirect(
      method = "registerIngredients",
      at = @At(
         value = "INVOKE",
         target = "Lmezz/jei/library/plugins/vanilla/ingredients/ItemStackListFactory;create(Lmezz/jei/common/util/StackHelper;Lmezz/jei/library/plugins/vanilla/ingredients/ItemStackHelper;)Ljava/util/List;"
      ),
      remap = false
   )
   private List<ItemStack> createItem(StackHelper var1, ItemStackHelper var2) {
      return Collections.emptyList();
   }

   @Redirect(
      method = "registerFluidIngredients",
      at = @At(
         value = "INVOKE",
         target = "Lmezz/jei/library/plugins/vanilla/ingredients/fluid/FluidStackListFactory;create(Lmezz/jei/common/platform/IPlatformRegistry;Lmezz/jei/api/helpers/IPlatformFluidHelper;)Ljava/util/List;"
      ),
      remap = false
   )
   private <T> List<T> createFluid(IPlatformRegistry<Fluid> var1, IPlatformFluidHelper<T> var2) {
      return Collections.emptyList();
   }

   @Overwrite(remap = false)
   public void registerCategories(IRecipeCategoryRegistration var1) {
      IJeiHelpers var2 = var1.getJeiHelpers();
      IGuiHelper var3 = var2.getGuiHelper();
      this.brewingExtensionHelper = new BrewingExtensionHelper();
      var1.addRecipeCategories(
         this.craftingCategory = new CraftingRecipeCategory(var3), this.smithingCategory = new SmithingRecipeCategory(var3), new AnvilRecipeCategory(var3)
      );
   }

   @Overwrite(remap = false)
   public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration var1) {
   }

   @Overwrite(remap = false)
   public void registerRecipes(IRecipeRegistration var1) {
   }

   @Overwrite(remap = false)
   public void registerGuiHandlers(IGuiHandlerRegistration var1) {
   }

   @Overwrite(remap = false)
   public void registerRecipeTransferHandlers(IRecipeTransferRegistration var1) {
   }

   @Overwrite(remap = false)
   public void registerRecipeCatalysts(IRecipeCatalystRegistration var1) {
   }
}
