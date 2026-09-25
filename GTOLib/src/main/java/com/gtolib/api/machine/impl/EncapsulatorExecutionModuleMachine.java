package com.gtolib.api.machine.impl;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKeyMap;
import appeng.api.stacks.GenericStack;
import appeng.core.definitions.AEItems;
import appeng.crafting.pattern.ProcessingPatternItem;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.ChanceBoostFunction;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.handler.ICustomRecipeLogicHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.ItemIngredient;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.gtolib.api.gui.ParallelConfigurator;
import com.gtolib.api.machine.feature.multiblock.IParallelMachine;
import com.gtolib.api.machine.multiblock.StorageMultiblockMachine;
import com.gtolib.api.machine.trait.CustomParallelTrait;
import com.gtolib.api.recipe.RecipeBuilder;
import com.gtolib.api.recipe.RecipeHelper;
import com.gtolib.utils.MachineUtils;
import com.gtolib.utils.MathUtil;
import com.gtolib.utils.RLUtils;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2LongMap.Entry;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public final class EncapsulatorExecutionModuleMachine extends StorageMultiblockMachine implements IParallelMachine, ICustomRecipeLogicHolder {
   private GTRecipeType recipeTypeCache = GTRecipeTypes.DUMMY_RECIPES;
   ProcessingEncapsulatorMachine encapsulatorMachine;
   @SaveToDisk
   private GTRecipeDefinition finalRecipe;
   @SaveToDisk
   private final Reference2LongOpenHashMap<GTRecipeDefinition> packageRecipe = new Reference2LongOpenHashMap<>();
   private final List<GTRecipeDefinition> invalidRecipe = new ArrayList<>();
   @SaveToDisk
   private final AEKeyMap<AEItemKey> inputItemStackMap = new AEKeyMap<>();
   @SaveToDisk
   private final AEKeyMap<AEFluidKey> inputFluidStackMap = new AEKeyMap<>();
   @SaveToDisk
   private final AEKeyMap<AEItemKey> outputItemStackMap = new AEKeyMap<>();
   @SaveToDisk
   private final AEKeyMap<AEFluidKey> outputFluidStackMap = new AEKeyMap<>();
   @SaveToDisk
   private final CustomParallelTrait customParallelTrait = new CustomParallelTrait(
      this, machine -> MachineUtils.getHatchParallel(((EncapsulatorExecutionModuleMachine)machine).encapsulatorMachine)
   );

   public EncapsulatorExecutionModuleMachine(MetaMachineBlockEntity holder) {
      super(holder, 1, i -> true);
      this.customParallelTrait.setDefaultMax(false);
   }

   private void update() {
      this.recipeTypeCache = GTRecipeTypes.DUMMY_RECIPES;
      if (this.getStorageStack().getItem() instanceof MetaMachineItem metaMachineItem) {
         MachineDefinition definition = metaMachineItem.getDefinition();
         this.recipeTypeCache = definition.getRecipeTypes()[0];
      } else if (this.getStorageStack().getItem() instanceof ProcessingPatternItem) {
         CompoundTag tag = this.getStorageStack().getTag();
         if (tag != null && tag.tags.get("recipe") instanceof StringTag stringTag) {
            GTRecipeDefinition recipe = RecipeBuilder.get(RLUtils.parse(stringTag.getAsString()));
            if (recipe != null) {
               this.recipeTypeCache = recipe.recipeType;
            }
         }
      }
   }

   @Override
   public boolean checkConditions(RecipeHandlerUnit unit, GTRecipeDefinition recipe) {
      return this.encapsulatorMachine == null ? false : super.checkConditions(unit, recipe);
   }

   @Override
   public void onUnload() {
      super.onUnload();
      this.encapsulatorMachine = null;
   }

   @Override
   public void onStructureInvalid() {
      super.onStructureInvalid();
      this.encapsulatorMachine = null;
   }

   @Override
   public void onMachineChanged() {
      if (this.isFormed) {
         if (this.getRecipeLogic().getLastRecipe() != null) {
            this.getRecipeLogic().markLastRecipeDirty();
         }

         this.getRecipeLogic().updateTickSubscription();
         this.update();
      }
   }

   @Override
   public boolean storageFilter(ItemStack stack) {
      if (this.encapsulatorMachine == null) {
         return false;
      }

      if (stack.getItem() instanceof MetaMachineItem metaMachineItem) {
         MachineDefinition definition = metaMachineItem.getDefinition();
         if (definition.getTier() > this.tier) {
            return false;
         }

         GTRecipeType[] recipeTypes = definition.getRecipeTypes();
         if (recipeTypes != null && recipeTypes.length > 0) {
            return this.encapsulatorMachine.typeMap.containsKey(recipeTypes[0]);
         }
      } else if (stack.getItem() instanceof ProcessingPatternItem) {
         CompoundTag tag = stack.getTag();
         if (tag != null && tag.tags.get("recipe") instanceof StringTag stringTag) {
            GTRecipeDefinition recipe = RecipeBuilder.get(RLUtils.parse(stringTag.getAsString()));
            return recipe != null && this.encapsulatorMachine.typeMap.containsKey(recipe.recipeType);
         }
      }

      return false;
   }

   @Override
   public void onStructureFormed() {
      super.onStructureFormed();
      this.update();

      for (Entry<GTRecipeDefinition> recipe : this.packageRecipe.reference2LongEntrySet()) {
         boolean invalid = true;
         GTRecipeDefinition mapReicpe = RecipeBuilder.get(recipe.getKey().id);
         if (mapReicpe != null) {
            List<ItemStack> inputItem = RecipeHelper.getConsumeInputItems(recipe.getKey());
            List<ItemStack> mapInputItem = RecipeHelper.getConsumeInputItems(mapReicpe);
            if (inputItem.size() == mapInputItem.size()) {
               boolean itemMatch = true;

               for (int j = 0; j < inputItem.size(); j++) {
                  if (!ItemStack.isSameItemSameTags(inputItem.get(j), mapInputItem.get(j))) {
                     itemMatch = false;
                     break;
                  }
               }

               if (itemMatch) {
                  List<FluidStack> inputFluid = RecipeHelper.getConsumeInputFluids(recipe.getKey());
                  List<FluidStack> mapInputFluid = RecipeHelper.getConsumeInputFluids(mapReicpe);
                  if (inputFluid.size() == mapInputFluid.size()) {
                     boolean fluidMatch = true;

                     for (int j = 0; j < inputFluid.size(); j++) {
                        if (!inputFluid.get(j).equals(mapInputFluid.get(j))) {
                           fluidMatch = false;
                           break;
                        }
                     }

                     if (fluidMatch) {
                        invalid = false;
                     }
                  }
               }
            }
         }

         if (invalid) {
            this.invalidRecipe.add(recipe.getKey());
         }
      }

      if (!this.invalidRecipe.isEmpty()) {
         this.clean();
      }
   }

   private void clean() {
      this.finalRecipe = null;
      this.packageRecipe.clear();
      this.inputItemStackMap.clear();
      this.inputFluidStackMap.clear();
      this.outputItemStackMap.clear();
      this.outputFluidStackMap.clear();
      this.getRecipeLogic().markLastRecipeDirty();
   }

   @Override
   public void attachConfigurators(@NotNull ConfiguratorPanel configuratorPanel) {
      super.attachConfigurators(configuratorPanel);
      configuratorPanel.attachConfigurators(new ParallelConfigurator(this));
   }

   @Override
   public void customText(@NotNull List<Component> textList) {
      super.customText(textList);
      if (this.encapsulatorMachine != null) {
         textList.add(Component.translatable("gtocore.tooltip.item.craft_step", this.packageRecipe.size() + " / " + this.encapsulatorMachine.processingAmount));
         textList.add(
            ComponentPanelWidget.withButton(Component.literal("[").append(Component.translatable("gui.ae2.Clean")).append(Component.literal("]")), "clean")
         );
         if (this.finalRecipe == null && this.packageRecipe.size() < this.encapsulatorMachine.processingAmount) {
            textList.add(
               ComponentPanelWidget.withButton(
                  Component.literal("[").append(Component.translatable("gui.jade.search")).append(Component.literal("]")), "search"
               )
            );
         }

         if (this.finalRecipe == null && !this.packageRecipe.isEmpty()) {
            textList.add(
               ComponentPanelWidget.withButton(Component.literal("[").append(Component.translatable("gtocore.build")).append(Component.literal("]")), "build")
            );
         }

         if (this.finalRecipe != null) {
            textList.add(
               ComponentPanelWidget.withButton(
                  Component.literal("[").append(Component.translatable("gui.ae2.Patterns")).append(Component.literal("]")), "pattern"
               )
            );
         }
      }
   }

   @Override
   public void addDisplayText(@NotNull List<Component> textList) {
      super.addDisplayText(textList);
      if (!this.isRemote() && this.encapsulatorMachine != null) {
         if (this.finalRecipe != null) {
            long eut = this.finalRecipe.getInputEUt();
            textList.add(
               Component.literal(FormattingUtil.formatNumbers(eut)).append(" EU/t (").append(GTValues.VNF[GTUtil.getFloorTierByVoltage(eut)]).append(") ")
            );
            textList.add(Component.literal("Duration: ").append(String.valueOf(this.finalRecipe.duration)));
         } else if (!this.invalidRecipe.isEmpty()) {
            textList.add(Component.translatable("attributeslib.value.boolean.invalid"));

            for (GTRecipeDefinition recipe : this.invalidRecipe) {
               textList.add(Component.literal("Recipe: ").append(Component.translatable(recipe.id.toString())));
            }
         }

         if (!this.packageRecipe.isEmpty()) {
            textList.add(Component.translatable("gtceu.io.import"));
            ObjectIterator it = this.inputItemStackMap.reference2LongEntrySet().fastIterator();

            while (it.hasNext()) {
               Entry<AEItemKey> inputItem = (Entry<AEItemKey>)it.next();
               if (inputItem.getLongValue() > 0L) {
                  textList.add(inputItem.getKey().getDisplayName().copy().append(" x").append(String.valueOf(inputItem.getLongValue())));
               }
            }

            it = this.inputFluidStackMap.reference2LongEntrySet().fastIterator();

            while (it.hasNext()) {
               Entry<AEFluidKey> inputFluid = (Entry<AEFluidKey>)it.next();
               if (inputFluid.getLongValue() > 0L) {
                  textList.add(inputFluid.getKey().getDisplayName().copy().append(" x").append(String.valueOf(inputFluid.getLongValue())));
               }
            }

            textList.add(Component.translatable("gtceu.io.export"));
            it = this.outputItemStackMap.reference2LongEntrySet().fastIterator();

            while (it.hasNext()) {
               Entry<AEItemKey> outputItem = (Entry<AEItemKey>)it.next();
               if (outputItem.getLongValue() > 0L) {
                  textList.add(outputItem.getKey().getDisplayName().copy().append(" x").append(String.valueOf(outputItem.getLongValue())));
               }
            }

            it = this.outputFluidStackMap.reference2LongEntrySet().fastIterator();

            while (it.hasNext()) {
               Entry<AEFluidKey> outputFluid = (Entry<AEFluidKey>)it.next();
               if (outputFluid.getLongValue() > 0L) {
                  textList.add(outputFluid.getKey().getDisplayName().copy().append(" x").append(String.valueOf(outputFluid.getLongValue())));
               }
            }
         }
      }
   }

   @Override
   public void handleDisplayClick(String componentData, ClickData clickData) {
      if (!clickData.isRemote) {
         super.handleDisplayClick(componentData, clickData);
         switch (componentData) {
            case "clean":
               this.clean();
               this.invalidRecipe.clear();
               break;
            case "pattern":
               if (this.finalRecipe != null && this.inputItem(AEItems.BLANK_PATTERN.stack())) {
                  List<GenericStack> input = new ArrayList<>();
                  List<GenericStack> output = new ArrayList<>();
                  ObjectIterator it = this.inputItemStackMap.reference2LongEntrySet().fastIterator();

                  while (it.hasNext()) {
                     Entry<AEItemKey> inputItem = (Entry<AEItemKey>)it.next();
                     if (inputItem.getLongValue() > 0L) {
                        input.add(new GenericStack(inputItem.getKey(), inputItem.getLongValue()));
                     }
                  }

                  it = this.inputFluidStackMap.reference2LongEntrySet().fastIterator();

                  while (it.hasNext()) {
                     Entry<AEFluidKey> inputFluid = (Entry<AEFluidKey>)it.next();
                     if (inputFluid.getLongValue() > 0L) {
                        input.add(new GenericStack(inputFluid.getKey(), inputFluid.getLongValue()));
                     }
                  }

                  it = this.outputItemStackMap.reference2LongEntrySet().fastIterator();

                  while (it.hasNext()) {
                     Entry<AEItemKey> outputItem = (Entry<AEItemKey>)it.next();
                     if (outputItem.getLongValue() > 0L) {
                        output.add(new GenericStack(outputItem.getKey(), outputItem.getLongValue()));
                     }
                  }

                  it = this.outputFluidStackMap.reference2LongEntrySet().fastIterator();

                  while (it.hasNext()) {
                     Entry<AEFluidKey> outputFluid = (Entry<AEFluidKey>)it.next();
                     if (outputFluid.getLongValue() > 0L) {
                        output.add(new GenericStack(outputFluid.getKey(), outputFluid.getLongValue()));
                     }
                  }

                  this.outputItem(PatternDetailsHelper.encodeProcessingPattern(input.toArray(new GenericStack[0]), output.toArray(new GenericStack[0])));
               }
               break;
            case "build":
               if (this.finalRecipe != null || this.packageRecipe.isEmpty()) {
                  return;
               }

               RecipeBuilder recipeBuilder = this.getRecipeBuilder();
               double totalEU = 0.0;
               ObjectIterator<Entry<GTRecipeDefinition>> it = this.packageRecipe.reference2LongEntrySet().fastIterator();

               while (it.hasNext()) {
                  Entry<GTRecipeDefinition> recipe = (Entry<GTRecipeDefinition>)it.next();
                  totalEU += recipe.getKey().getInputEUt() * recipe.getKey().duration * recipe.getLongValue();
               }

               long maxEUt = this.getOverclockVoltage();
               double d = totalEU / maxEUt;
               int limit = this.getOverclockLimit();
               recipeBuilder.EUt(d >= limit ? maxEUt : Math.max(1L, (long)(maxEUt * d / limit))).duration((int)Math.max(Math.max(1.0, d), limit));
               ObjectIterator it2 = this.outputItemStackMap.reference2LongEntrySet().fastIterator();

               while (it2.hasNext()) {
                  Entry<AEItemKey> entry = (Entry<AEItemKey>)it2.next();
                  AEItemKey stack = entry.getKey();
                  long count = entry.getLongValue();
                  if (this.inputItemStackMap.containsKey(stack)) {
                     long inputCount = this.inputItemStackMap.getLong(stack);
                     if (inputCount >= count) {
                        long var44 = inputCount - count;
                        if (var44 > 0L) {
                           this.inputItemStackMap.put(stack, var44);
                        } else {
                           this.inputItemStackMap.removeLong(stack);
                        }

                        it2.remove();
                     } else {
                        count -= inputCount;
                        entry.setValue(count);
                        this.inputItemStackMap.removeLong(stack);
                     }
                  }
               }

               it2 = this.outputFluidStackMap.reference2LongEntrySet().fastIterator();

               while (it2.hasNext()) {
                  Entry<AEFluidKey> entry = (Entry<AEFluidKey>)it2.next();
                  AEFluidKey stack = entry.getKey();
                  long count = entry.getLongValue();
                  if (this.inputFluidStackMap.containsKey(stack)) {
                     long inputCount = this.inputFluidStackMap.getLong(stack);
                     if (inputCount >= count) {
                        long var46 = inputCount - count;
                        if (var46 > 0L) {
                           this.inputFluidStackMap.put(stack, var46);
                        } else {
                           this.inputFluidStackMap.removeLong(stack);
                        }

                        it2.remove();
                     } else {
                        count -= inputCount;
                        entry.setValue(count);
                        this.inputFluidStackMap.removeLong(stack);
                     }
                  }
               }

               it2 = this.inputItemStackMap.reference2LongEntrySet().fastIterator();

               while (it2.hasNext()) {
                  Entry<AEItemKey> inputItem = (Entry<AEItemKey>)it2.next();
                  if (inputItem.getLongValue() > 0L) {
                     recipeBuilder.inputItems(inputItem.getKey().toStack(MathUtil.saturatedCast(inputItem.getLongValue())));
                  }
               }

               it2 = this.inputFluidStackMap.reference2LongEntrySet().fastIterator();

               while (it2.hasNext()) {
                  Entry<AEFluidKey> inputFluid = (Entry<AEFluidKey>)it2.next();
                  if (inputFluid.getLongValue() > 0L) {
                     recipeBuilder.inputFluids(inputFluid.getKey().toStack(MathUtil.saturatedCast(inputFluid.getLongValue())));
                  }
               }

               it2 = this.outputItemStackMap.reference2LongEntrySet().fastIterator();

               while (it2.hasNext()) {
                  Entry<AEItemKey> outputItem = (Entry<AEItemKey>)it2.next();
                  if (outputItem.getLongValue() > 0L) {
                     recipeBuilder.outputItems(outputItem.getKey().toStack(MathUtil.saturatedCast(outputItem.getLongValue())));
                  }
               }

               it2 = this.outputFluidStackMap.reference2LongEntrySet().fastIterator();

               while (it2.hasNext()) {
                  Entry<AEFluidKey> outputFluid = (Entry<AEFluidKey>)it2.next();
                  if (outputFluid.getLongValue() > 0L) {
                     recipeBuilder.outputFluids(outputFluid.getKey().toStack(MathUtil.saturatedCast(outputFluid.getLongValue())));
                  }
               }

               this.finalRecipe = recipeBuilder.build();
               break;
            case "search":
               if (this.recipeTypeCache == GTRecipeTypes.DUMMY_RECIPES
                  || this.finalRecipe != null
                  || this.packageRecipe.size() >= this.encapsulatorMachine.processingAmount) {
                  return;
               }

               this.findRecipe(
                  this.recipeTypeCache,
                  (u, r) -> this.encapsulatorMachine != null
                     && this.encapsulatorMachine.isFormed()
                     && this.encapsulatorMachine.typeMap.getInt(r.recipeType) >= r.tier
                     && this.getOverclockTier() >= r.tier
                     && this.search(u, r)
               );
         }
      }
   }

   private boolean search(RecipeHandlerUnit unit, GTRecipeDefinition recipe) {
      if (!this.checkConditions(unit, recipe)) {
         return false;
      }

      long parallel = this.getParallel();
      this.packageRecipe.addTo(recipe, parallel);
      List<Content<ItemIngredient>> inputItem = recipe.itemInputs;
      if (inputItem != null) {
         for (Content<ItemIngredient> content : inputItem) {
            if (content.chance > 0 && content.inner instanceof ItemIngredient ingredient && !ingredient.isEmpty()) {
               this.forEachItems(true, (stack, amountx) -> {
                  if (ingredient.test(stack)) {
                     this.inputItemStackMap.addTo(AEItemKey.of(stack), parallel * ingredient.amount);
                     return true;
                  } else {
                     return false;
                  }
               });
            }
         }
      }

      List<Content<FluidIngredient>> inputFluid = recipe.fluidInputs;
      if (inputFluid != null) {
         for (Content<FluidIngredient> content : inputFluid) {
            if (content.chance > 0 && content.inner instanceof FluidIngredient ingredient && !ingredient.isEmpty()) {
               this.forEachFluids(true, (stack, amountx) -> {
                  if (ingredient.test(stack)) {
                     this.inputFluidStackMap.addTo(AEFluidKey.of(stack), parallel * ingredient.amount);
                     return true;
                  } else {
                     return false;
                  }
               });
            }
         }
      }

      List<Content<ItemIngredient>> outputItem = recipe.itemOutputs;
      if (outputItem != null) {
         for (Content<ItemIngredient> content : outputItem) {
            if (content.inner instanceof ItemIngredient ingredient) {
               int chance = ChanceBoostFunction.OVERCLOCK.getBoostedChance(content, recipe.tier, this.getOverclockTier());
               if (MathUtil.saturatedCast((double)parallel * chance) >= 10000L) {
                  long amount = parallel * ingredient.amount;
                  if (chance < 10000) {
                     amount = (long)((double)amount * chance / 10000.0);
                  }

                  if (!ingredient.isEmpty()) {
                     this.outputItemStackMap.addTo(AEItemKey.of(ingredient.getInnerItemStack()), amount);
                  }
               }
            }
         }
      }

      List<Content<FluidIngredient>> outputFluid = recipe.fluidOutputs;
      if (outputFluid != null) {
         for (Content<FluidIngredient> content : outputFluid) {
            if (content.inner instanceof FluidIngredient ingredient) {
               int chance = ChanceBoostFunction.OVERCLOCK.getBoostedChance(content, recipe.tier, this.getOverclockTier());
               if (MathUtil.saturatedCast((double)parallel * chance) >= 10000L) {
                  long amount = parallel * ingredient.amount;
                  if (chance < 10000) {
                     amount = (long)((double)amount * chance / 10000.0);
                  }

                  if (!ingredient.isEmpty()) {
                     this.outputFluidStackMap.addTo(AEFluidKey.of(ingredient.getFluid(), ingredient.nbt), amount);
                  }
               }
            }
         }
      }

      return true;
   }

   @Override
   public long getMaxParallel() {
      return this.customParallelTrait.getMaxParallel();
   }

   @Override
   public long getMinParallel() {
      return this.customParallelTrait.getMinParallel();
   }

   @Override
   public long getParallel() {
      return this.customParallelTrait.getParallel();
   }

   @Override
   public void setParallel(long number) {
      this.customParallelTrait.setParallel(number);
   }

   @Override
   public GTRecipeDefinition createCustomRecipe(RecipeHandlerUnit unit) {
      if (this.finalRecipe != null
         && this.encapsulatorMachine != null
         && this.encapsulatorMachine.isFormed()
         && this.encapsulatorMachine.getRecipeLogic().isWorking()) {
         ObjectIterator<Entry<GTRecipeDefinition>> it = this.packageRecipe.reference2LongEntrySet().fastIterator();

         while (it.hasNext()) {
            if (!this.encapsulatorMachine.typeMap.containsKey(it.next().getKey().recipeType)) {
               return null;
            }
         }

         return this.finalRecipe;
      } else {
         return null;
      }
   }
}
