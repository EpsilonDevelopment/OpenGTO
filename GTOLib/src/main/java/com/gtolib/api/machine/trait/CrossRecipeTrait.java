package com.gtolib.api.machine.trait;

import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.machine.feature.IOverclockMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.ItemIngredient;
import com.gto.datasynclib.util.holder.ObjHolder;
import com.gtolib.api.gui.ParallelConfigurator;
import com.gtolib.api.machine.feature.multiblock.ICrossRecipeMachine;
import com.gtolib.api.machine.feature.multiblock.IParallelMachine;
import com.gtolib.api.machine.impl.part.OverclockPartMachine;
import com.gtolib.api.machine.impl.part.ThreadPartMachine;
import com.gtolib.api.machine.impl.part.WirelessEnergyHatchPartMachine;
import com.gtolib.api.recipe.RecipeBuilder;
import com.gtolib.utils.MathUtil;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.ArrayList;
import java.util.function.BiPredicate;
import java.util.function.ToLongFunction;
import org.jetbrains.annotations.NotNull;

public class CrossRecipeTrait extends CustomParallelTrait {
   public final ReferenceOpenHashSet<GTRecipeDefinition> lastRecipes = new ReferenceOpenHashSet<>();
   public long availableParallel;
   public long maxParallel;
   public long lastParallel;
   public int useThread = 1;
   public int availableThread = 1;
   public boolean isSingleThread = true;
   public boolean isSeparateThread = true;
   public boolean duplicateCheck = true;
   public WirelessEnergyContainerTrait wirelessEnergyContainer;
   public OverclockPartMachine overclockHatchPartMachine;
   public ThreadPartMachine threadHatchPartMachine;
   private final ArrayList<Content<ItemIngredient>> items = new ArrayList<>();
   private final ArrayList<Content<FluidIngredient>> fluids = new ArrayList<>();
   private int outputColor = -1;
   private final boolean infinite;
   private final boolean isHatchParallel;
   private final ICrossRecipeMachine machine;

   public CrossRecipeTrait(ICrossRecipeMachine machine, boolean infinite, boolean isHatchParallel, @NotNull ToLongFunction<IParallelMachine> parallel) {
      super(machine, parallel);
      this.infinite = infinite;
      this.isHatchParallel = isHatchParallel;
      this.machine = machine;
   }

   @Override
   public void onPartScan(@NotNull IMultiPart part) {
      super.onPartScan(part);
      switch (part) {
         case ThreadPartMachine threadHatchPart:
            this.threadHatchPartMachine = threadHatchPart;
            break;
         case OverclockPartMachine hatchPartMachine:
            this.overclockHatchPartMachine = hatchPartMachine;
            break;
         case WirelessEnergyHatchPartMachine energyHatchPartMachine:
            this.wirelessEnergyContainer = energyHatchPartMachine.getEnergyContainer();
            break;
         default:
      }
   }

   @Override
   public void onStructureInvalid() {
      super.onStructureInvalid();
      this.wirelessEnergyContainer = null;
      this.threadHatchPartMachine = null;
      this.overclockHatchPartMachine = null;
   }

   @Override
   public long getParallel() {
      return this.isHatchParallel ? this.getMaxParallel() : super.getParallel();
   }

   public void attachConfigurators(@NotNull ConfiguratorPanel configuratorPanel) {
      if (!this.isHatchParallel) {
         configuratorPanel.attachConfigurators(new ParallelConfigurator(this.machine));
      }
   }

   public int getThread() {
      return this.infinite ? 128 : (this.threadHatchPartMachine == null ? 1 : this.threadHatchPartMachine.getCurrentThread());
   }

   public double getOverclockFactor() {
      return this.overclockHatchPartMachine == null ? 0.55 : this.overclockHatchPartMachine.getCurrentMultiplier();
   }

   public GTRecipe getRecipe() {
      this.outputColor = -1;
      this.isSeparateThread = false;
      this.duplicateCheck = true;
      this.lastRecipes.clear();
      double parallel = 0.0;
      int thread = this.machine.getThread();
      this.maxParallel = this.machine.getParallel();
      this.availableParallel = this.maxParallel * thread;
      this.items.clear();
      this.fluids.clear();
      double totalUnit = 0.0;

      while (this.availableParallel > 0L && thread > 0) {
         GTRecipe recipe = this.lookupRecipe((u, r) -> true);
         if (recipe == null) {
            break;
         }

         thread--;
         this.availableParallel = this.availableParallel - this.lastParallel;
         parallel += recipe.parallels;
         totalUnit += (double)recipe.duration * this.machine.getRecipeUnit(recipe);
         this.items.addAll(recipe.itemOutputs);
         this.fluids.addAll(recipe.fluidOutputs);
      }

      GTRecipe recipe = null;
      if (!this.lastRecipes.isEmpty()) {
         RecipeBuilder builder = this.machine.getRecipeBuilder();
         int limit = this.machine instanceof IOverclockMachine overclockMachine ? overclockMachine.getOverclockLimit() : 20;
         long maxUnit = this.machine.getOverclockMaxUnit();
         double d = totalUnit / maxUnit;
         this.machine.setRecipeUnit(d >= limit ? maxUnit : (long)(maxUnit * d / limit + 0.5), builder);
         builder.duration((int)Math.max(d + 0.5, limit));
         recipe = builder.buildRawRecipe();
         recipe.outputColor = this.outputColor;
         recipe.parallels = MathUtil.saturatedCast(parallel);
         recipe.itemOutputs = this.items;
         recipe.fluidOutputs = this.fluids;
      }

      return recipe;
   }

   public GTRecipe lookupRecipe(BiPredicate<RecipeHandlerUnit, GTRecipe> canHandle) {
      ObjHolder<GTRecipe> recipeObjectHolder = new ObjHolder<>();
      this.machine.findRecipe(this.machine.getRecipeType(), (u, r) -> {
         if (this.canHandle(u, r)) {
            this.lastParallel = 0L;
            GTRecipe modified = this.machine.fullModifyRecipe(u, r);
            if (modified != null && canHandle.test(u, modified) && this.machine.matchRecipeInput(u, modified) && this.machine.handleRecipeInput(u, modified)) {
               this.machine.beforeWorking(u, modified);
               if (this.duplicateCheck) {
                  this.lastRecipes.add(r);
               }

               if (modified.outputColor != -1) {
                  this.outputColor = modified.outputColor;
               }

               recipeObjectHolder.value = modified;
               return true;
            }
         }

         return false;
      });
      return recipeObjectHolder.value;
   }

   private boolean canHandle(RecipeHandlerUnit unit, GTRecipeDefinition recipe) {
      return (!this.duplicateCheck || !this.lastRecipes.contains(recipe))
         && recipe.tier <= this.machine.getRecipeTier()
         && this.machine.checkConditions(unit, recipe);
   }
}
