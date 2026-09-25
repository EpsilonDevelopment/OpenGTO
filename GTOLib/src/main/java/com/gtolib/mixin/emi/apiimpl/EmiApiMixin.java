package com.gtolib.mixin.emi.apiimpl;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.GenericStack;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.gtocore.integration.emi.GTEMIRecipe;
import com.gtolib.api.emi.stack.EmiTagprefixStack;
import com.gtolib.api.machine.MultiblockDefinition;
import dev.emi.emi.VanillaPlugin;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ListEmiIngredient;
import dev.emi.emi.api.stack.TagEmiIngredient;
import dev.emi.emi.bom.BoM;
import dev.emi.emi.recipe.EmiSyntheticIngredientRecipe;
import dev.emi.emi.recipe.EmiTagRecipe;
import dev.emi.emi.registry.EmiRecipes;
import dev.emi.emi.runtime.EmiFavorite;
import dev.emi.emi.runtime.EmiSidebars;
import dev.emi.emi.screen.BoMScreen;
import dev.emi.emi.screen.RecipeScreen;
import dev.ftb.mods.ftblibrary.ui.ScreenWrapper;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EmiApi.class)
public abstract class EmiApiMixin {
   @Shadow(remap = false)
   @Final
   private static Minecraft client;

   @Shadow(remap = false)
   private static void push() {
   }

   @Shadow(remap = false)
   public static AbstractContainerScreen<?> getHandledScreen() {
      return null;
   }

   @Shadow(remap = false)
   private static Map<EmiRecipeCategory, List<EmiRecipe>> mapRecipes(List<EmiRecipe> var0) {
      return null;
   }

   @Shadow(remap = false)
   private static List<EmiRecipe> pruneUses(List<EmiRecipe> var0, EmiIngredient var1) {
      return null;
   }

   @Shadow(remap = false)
   private static List<EmiRecipe> pruneSources(List<EmiRecipe> var0, EmiStack var1) {
      return null;
   }

   @Overwrite(remap = false)
   private static void setPages(Map<EmiRecipeCategory, List<EmiRecipe>> var0, EmiIngredient var1) {
      Map var6 = var0.entrySet().stream().filter(var0x -> !((List)var0x.getValue()).isEmpty()).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
      if (!var6.isEmpty()) {
         EmiSidebars.lookup(var1);
         if (client.screen instanceof ScreenWrapper) {
            RecipeScreen var7 = new RecipeScreen(new InventoryScreen(client.player), var6);
            client.setScreen(var7);
            return;
         }

         if (getHandledScreen() == null) {
            client.setScreen(new InventoryScreen(client.player));
         }

         if (client.screen instanceof AbstractContainerScreen var2) {
            push();
            client.setScreen(new RecipeScreen(var2, var6));
         } else if (client.screen instanceof BoMScreen var3) {
            push();
            client.setScreen(new RecipeScreen(var3.old, var6));
         } else if (client.screen instanceof RecipeScreen var4) {
            push();
            RecipeScreen var10 = new RecipeScreen(var4.old, var6);
            client.setScreen(var10);
            var10.focusCategory(var4.getFocusedCategory());
         }
      }
   }

   @Overwrite(remap = false)
   public static void displayUses(EmiIngredient var0) {
      if (!var0.isEmpty()) {
         int var1;
         ReferenceOpenHashSet var2;
         if (var0 instanceof EmiStack var3
            && !GTUtil.isShiftDown()
            && var3.getKey() instanceof MetaMachineItem var4
            && var4.getDefinition().getRecipeTypes() != null
            && var4.getDefinition().getRecipeTypes().length > 0) {
            MachineDefinition var13 = var4.getDefinition();
            var2 = new ReferenceOpenHashSet<>(var13.getRecipeTypes());
            if (var13 instanceof MultiblockDefinition var6) {
               var1 = var6.maxTier;
            } else {
               var1 = var13.getTier();
            }
         } else {
            var1 = -1;
            var2 = null;
         }

         EmiStack var10 = var0.getEmiStacks().getFirst();
         if (var10 instanceof EmiTagprefixStack var11) {
            var10 = var11.getFirstValidStack();
            if (var10 == null) {
               return;
            }
         }

         ReferenceOpenHashSet var12 = new ReferenceOpenHashSet();
         List<EmiRecipe> var14 = pruneUses(EmiApi.getRecipeManager().getRecipesByInput(var10), var0);
         if (var14 == null) {
            var14 = new ArrayList<>();
         } else {
            var14 = new ArrayList<>(var14);
         }

         EmiIngredient var16 = gtolib$getBucketFluid(var0);
         if (var16 != null) {
            List var7 = pruneUses(EmiApi.getRecipeManager().getRecipesByInput(var16.getEmiStacks().getFirst()), var16);
            if (var7 != null) {
               var14.addAll(var7);
            }
         }

         List var17 = EmiRecipes.byWorkstation.getOrDefault(var10, Collections.emptyList());
         var12.addAll(var14);
         var12.addAll(var17);
         Stream var8 = var12.stream();
         if (var1 >= 0) {
            var8 = var8.filter(var2x -> !(var2x instanceof GTEMIRecipe var3x && var3x.getTier() > var1 && var2.contains(var3x.getRecipeType())));
         }

         Map var9 = mapRecipes(var8.toList());
         setPages(var9, var0);
      }
   }

   @Overwrite(remap = false)
   public static void displayRecipes(EmiIngredient var0) {
      if (var0 instanceof EmiFavorite var1) {
         var0 = var1.getStack();
      }

      if (var0 instanceof EmiTagprefixStack var6) {
         var0 = var6.getFirstValidStack();
         if (var0 == null) {
            return;
         }
      }

      if (var0 instanceof TagEmiIngredient var7) {
         for (EmiRecipe var3 : EmiApi.getRecipeManager().getRecipes(VanillaPlugin.TAG)) {
            if (var3 instanceof EmiTagRecipe var4 && var4.key.equals(var7.key)) {
               setPages(Map.of(VanillaPlugin.TAG, List.of(var3)), var0);
               break;
            }
         }
      } else if (var0 instanceof ListEmiIngredient) {
         setPages(Map.of(VanillaPlugin.INGREDIENT, List.of(new EmiSyntheticIngredientRecipe(var0))), var0);
      } else {
         ArrayList var8 = new ArrayList();
         EmiIngredient var9 = gtolib$getBucketFluid(var0);
         EmiStack var10 = var0.getEmiStacks().getFirst();
         if (var9 != null) {
            EmiStack var5 = var9.getEmiStacks().getFirst();
            var8.addAll(pruneSources(EmiApi.getRecipeManager().getRecipesByOutput(var5), var5));
         }

         if (var0.getEmiStacks().size() == 1) {
            var8.addAll(pruneSources(EmiApi.getRecipeManager().getRecipesByOutput(var10), var10));
         } else if (!var8.isEmpty()) {
            var10 = var9.getEmiStacks().getFirst();
         }

         if (var8.isEmpty()) {
            return;
         }

         setPages(mapRecipes(var8), var0);
         EmiApi.focusRecipe(BoM.getRecipe(var10));
      }
   }

   @Unique
   private static EmiIngredient gtolib$getBucketFluid(EmiIngredient var0) {
      if (var0 instanceof EmiStack var1) {
         AtomicReference var2 = new AtomicReference<>(Fluids.EMPTY);
         if (var1.getKey() instanceof BucketItem var3) {
            var2.set(var3.getFluid());
         } else if (Optional.ofNullable(GenericStack.fromItemStack(var1.getItemStack()))
            .map(GenericStack::what)
            .stream()
            .anyMatch(AEFluidKey.class::isInstance)) {
            AEFluidKey var5 = (AEFluidKey)GenericStack.fromItemStack(var1.getItemStack()).what();
            var2.set(var5.getFluid());
         } else if (var1.hasNbt()) {
            var1.getItemStack().getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(var1x -> var2.set(var1x.getFluidInTank(0).getFluid()));
         }

         return var2.get() == Fluids.EMPTY ? null : EmiStack.of((Fluid)var2.get());
      } else {
         return null;
      }
   }
}
