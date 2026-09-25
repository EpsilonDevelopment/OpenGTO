package com.gtolib.utils;

import com.gregtechceu.gtceu.api.capability.IParallelHatch;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton.Toggle;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IComputationContainerMachine;
import com.gregtechceu.gtceu.api.machine.feature.IElectricMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.IDummyEnergyMachine.DummyContainer;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IWorkableMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText.Builder;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.ChanceBoostFunction;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.handler.IRecipeHandlerHolder;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.ItemIngredient;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gto.datasynclib.util.holder.IntHolder;
import com.gtocore.api.gui.GTOGuiTextures;
import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.feature.multiblock.ICrossRecipeMachine;
import com.gtolib.api.machine.feature.multiblock.IParallelMachine;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

@ParametersAreNonnullByDefault
@DataGeneratorScanned
public final class MachineUtils {
   @RegisterLanguage(cn = "成型的模块数: %s / %s", en = "Formed modules: %s / %s")
   public static final String MODULES_AMOUNT = "gtocore.machine.modules_amount";
   @RegisterLanguage(cn = "线程%s：", en = "Thread %s: ")
   public static final String THREAD = "gtocore.machine.text.thread";

   private MachineUtils() {
   }

   public static boolean isUD(Direction facing) {
      return facing == Direction.UP || facing == Direction.DOWN;
   }

   public static BlockPos getOffsetPos(int a, int b, int c, Direction facing, BlockPos pos) {
      int x = 0;
      int z = 0;
      switch (facing) {
         case NORTH:
            z = a;
            x = c;
            break;
         case SOUTH:
            z = -a;
            x = -c;
            break;
         case WEST:
            x = a;
            z = c;
            break;
         case EAST:
            x = -a;
            z = -c;
      }

      return pos.offset(x, b, z);
   }

   public static BlockPos getOffsetPos(int a, int b, Direction facing, BlockPos pos) {
      int x = 0;
      int z = 0;
      switch (facing) {
         case NORTH:
            z = a;
            break;
         case SOUTH:
            z = -a;
            break;
         case WEST:
            x = a;
            break;
         case EAST:
            x = -a;
      }

      return pos.offset(x, b, z);
   }

   public static BlockPos getOffsetPos(int a, Direction facing, BlockPos pos) {
      int x = 0;
      int y = 0;
      int z = 0;
      switch (facing) {
         case NORTH:
            z = a;
            break;
         case SOUTH:
            z = -a;
            break;
         case WEST:
            x = a;
            break;
         case EAST:
            x = -a;
            break;
         case UP:
            y = -a;
            break;
         case DOWN:
            y = a;
      }

      return pos.offset(x, y, z);
   }

   public static void addMachineText(List<Component> textList, WorkableMultiblockMachine machine, Consumer<List<Component>> customConsumer) {
      if (!machine.isRemote()) {
         Builder builder = MultiblockDisplayText.builder(textList, machine.isFormed())
            .addCustom(machine::addPatternText)
            .setWorkingStatus(machine.recipeLogic.isWorkingEnabled(), machine.recipeLogic.isActive());
         if (machine.isFormed()) {
            int numThread = 0;
            long numParallels;
            if (machine instanceof IParallelMachine parallelMachine && parallelMachine.getParallel() > 0L) {
               numParallels = parallelMachine.getParallel();
            } else {
               numParallels = getHatchParallel(machine);
            }

            if (machine instanceof IElectricMachine electricMultiblockMachine) {
               if (!(electricMultiblockMachine.getEnergyContainer() instanceof DummyContainer)) {
                  builder.addEnergyUsageLine(electricMultiblockMachine.getEnergyContainer());
               }

               if (electricMultiblockMachine instanceof ICrossRecipeMachine crossRecipeMachine) {
                  numThread = crossRecipeMachine.getThread();
               }
            }

            if (machine instanceof ITieredMachine tieredMachine) {
               builder.addEnergyTierLine(tieredMachine.getTier());
            }

            if (machine instanceof IComputationContainerMachine containerMachine) {
               builder.addComputationUsageLine(containerMachine.requestCWU(Long.MAX_VALUE, true));
            }

            builder.addMachineModeLine(machine.getRecipeType(), machine.getAvailableRecipeTypes().length > 1);
            int patternAmount = machine.getSubPatternAmount();
            if (patternAmount > 0) {
               builder.addCustom(
                  text -> textList.add(
                     Component.translatable("gtocore.machine.modules_amount", machine.getSubFormedAmount(), patternAmount).withStyle(ChatFormatting.GRAY)
                  )
               );
            }

            if (numThread > 1) {
               Component thread = Component.literal(String.valueOf(numThread)).withStyle(ChatFormatting.DARK_AQUA);
               Component parallels = Component.literal(FormattingUtil.formatNumbers(numParallels)).withStyle(ChatFormatting.DARK_PURPLE);
               builder.addCustom(text -> text.add(Component.translatable("gtocore.machine.thread.0", thread).withStyle(ChatFormatting.GRAY)));
               builder.addCustom(text -> text.add(Component.translatable("gtocore.machine.thread.1", parallels).withStyle(ChatFormatting.GRAY)));
               ICrossRecipeMachine crossRecipeMachine = (ICrossRecipeMachine)machine;
               if (crossRecipeMachine.isIndependentThread()) {
                  builder.addCustom(
                     text -> text.add(Component.translatable("gtocore.machine.independent_thread", Component.translatable("modernfix.option.enabled")))
                  );
               }
            } else if (numParallels > 1L) {
               builder.addCustom(
                  text -> textList.add(
                     Component.translatable(
                           "gtceu.multiblock.parallel", Component.literal(FormattingUtil.formatNumbers(numParallels)).withStyle(ChatFormatting.DARK_PURPLE)
                        )
                        .withStyle(ChatFormatting.GRAY)
                  )
               );
            }

            builder.addBatchModeLine(machine.isBatchEnabled(), Objects.requireNonNullElse(machine.recipeLogic.getLastRecipe(), GTRecipe.EMPTY).batchParallels);
            builder.addCustom(customConsumer);
            builder.addCustom(text -> machine.getDefinition().getAdditionalDisplay().accept(machine, text));
            builder.addCustom(text -> {
               if (machine.getRecipeLogic().isIdle() && machine.getRecipeLogic().showFancyTooltip()) {
                  textList.add(machine.getRecipeLogic().getIdleReason().copy().withStyle(ChatFormatting.GRAY));
               }
            });
            builder.addWorkingStatusLine();
            builder.addProgressLine(machine.recipeLogic.getProgress(), machine.recipeLogic.getMaxProgress(), machine.recipeLogic.getProgressPercent());
            builder.addCustom(
               text -> {
                  if (machine.isFormed() && machine.isActive()) {
                     if (machine instanceof ICrossRecipeMachine crossRecipeMachinex && !crossRecipeMachinex.getThreads().isEmpty()) {
                        IntHolder i = new IntHolder();
                        crossRecipeMachinex.getThreads()
                           .forEach(
                              threadx -> {
                                 textList.add(Component.translatable("gtocore.machine.text.thread", ICrossRecipeMachine.Thread.getID(i, threadx.getUse())));
                                 textList.add(
                                    Component.translatable(
                                       "gtceu.multiblock.progress",
                                       String.format(Locale.ROOT, "%.2f", threadx.getProgress() / 20.0F),
                                       String.format(Locale.ROOT, "%.2f", threadx.getDuration() / 20.0F),
                                       (int)(threadx.getProgressPercent() * 100.0)
                                    )
                                 );
                                 addRecipeText(machine, threadx.getRecipe(), textList);
                                 i.value++;
                              }
                           );
                     } else {
                        addRecipeText(machine, machine.recipeLogic.getLastRecipe(), textList);
                     }
                  }
               }
            );
         }
      }
   }

   public static void addRecipeText(IRecipeHandlerHolder machine, @Nullable GTRecipe recipe, List<Component> textList) {
      if (recipe != null) {
         List<Content<ItemIngredient>> itemInput = recipe.itemInputs;
         List<Content<FluidIngredient>> fluidInput = recipe.fluidInputs;
         if (!itemInput.isEmpty() || !fluidInput.isEmpty()) {
            textList.add(Component.translatable("gtceu.io.import").append(":"));
            addContentText(machine, true, textList, recipe, itemInput, fluidInput);
         }

         List<Content<ItemIngredient>> itemOutput = recipe.itemOutputs;
         List<Content<FluidIngredient>> fluidOutput = recipe.fluidOutputs;
         if (!itemOutput.isEmpty() || !fluidOutput.isEmpty()) {
            textList.add(Component.translatable("gtceu.io.export").append(":"));
            addContentText(machine, false, textList, recipe, itemOutput, fluidOutput);
         }
      }
   }

   private static void addContentText(
      IRecipeHandlerHolder machine,
      boolean input,
      List<Component> textList,
      GTRecipe recipe,
      List<Content<ItemIngredient>> items,
      List<Content<FluidIngredient>> fluids
   ) {
      int recipeTier = recipe.tier;
      int chanceTier = recipeTier + recipe.ocLevel;
      ChanceBoostFunction function = ChanceBoostFunction.OVERCLOCK;
      double maxDurationSec = recipe.duration / 20.0;

      for (Content<ItemIngredient> item : items) {
         ItemIngredient ingredient = item.inner;
         ItemStack stack = ItemStack.EMPTY;
         if (input) {
            ItemStack[] stacks = ingredient.inner.getItems();
            if (stacks.length > 1) {
               for (ItemStack i : stacks) {
                  if (machine.getItemAmount(true, i.getItem())[0] > 0L) {
                     stack = i;
                     break;
                  }
               }
            }
         }

         if (stack.isEmpty()) {
            stack = ingredient.getInnerItemStack();
            if (stack.isEmpty()) {
               continue;
            }
         }

         long count = item.amount;
         double countD = count;
         if (item.chance < 10000) {
            countD = countD * function.getBoostedChance(item, recipeTier, chanceTier) / 10000.0;
            count = countD < 1.0 ? 1L : Math.round(countD);
         }

         if (count < maxDurationSec) {
            String key = "gtceu.multiblock.output_line." + (item.chance < 10000 ? "2" : "0");
            textList.add(
               Component.translatable(key, stack.getHoverName(), NumberUtils.formatLong(count), NumberUtils.formatLong((long)(maxDurationSec / countD)))
            );
         } else {
            String key = "gtceu.multiblock.output_line." + (item.chance < 10000 ? "3" : "1");
            textList.add(
               Component.translatable(key, stack.getHoverName(), NumberUtils.formatLong(count), NumberUtils.formatLong((long)(countD / maxDurationSec)))
            );
         }
      }

      for (Content<FluidIngredient> fluid : fluids) {
         FluidIngredient ingredient = fluid.inner;
         FluidStack stack = ingredient.getFluidStack();
         if (!stack.isEmpty()) {
            long amount = fluid.amount;
            double amountD = amount;
            if (fluid.chance < 10000) {
               amountD = amountD * function.getBoostedChance(fluid, recipeTier, chanceTier) / 10000.0;
               amount = amountD < 1.0 ? 1L : Math.round(amountD);
            }

            if (amount < maxDurationSec) {
               String key = "gtceu.multiblock.output_line." + (fluid.chance < 10000 ? "2" : "0");
               textList.add(
                  Component.translatable(
                     key,
                     stack.getDisplayName(),
                     FluidUtils.getUnicodeMillibuckets(amount),
                     FluidUtils.getUnicodeMillibuckets((long)(maxDurationSec / amountD))
                  )
               );
            } else {
               String key = "gtceu.multiblock.output_line." + (fluid.chance < 10000 ? "3" : "1");
               textList.add(
                  Component.translatable(
                     key,
                     stack.getDisplayName(),
                     FluidUtils.getUnicodeMillibuckets(amount),
                     FluidUtils.getUnicodeMillibuckets((long)(amountD / maxDurationSec))
                  )
               );
            }
         }
      }
   }

   public static void addRecipeTypeText(List<Component> textList, IRecipeLogicMachine machine) {
      GTRecipeType type = machine.getRecipeType();
      if (type != GTRecipeTypes.DUMMY_RECIPES) {
         textList.add(Component.translatable("gtceu.gui.machinemode", Component.translatable(type.registryName.toLanguageKey())).withStyle(ChatFormatting.AQUA));
      }
   }

   public static long getHatchParallel(MetaMachine machine) {
      if (machine instanceof IWorkableMultiController controller && controller.isFormed()) {
         IParallelHatch parallelHatch = controller.getParallelHatch();
         if (parallelHatch != null) {
            return Math.max(1L, parallelHatch.getCurrentParallel());
         }
      }

      return 1L;
   }

   public static void attachStructureCheckConfigurators(ConfiguratorPanel configuratorPanel, IMultiController controller) {
      configuratorPanel.attachConfigurators(
         new Toggle(
               GTOGuiTextures.STRUCTURE_CHECK.getSubTexture(0.0, 0.0, 1.0, 0.5),
               GTOGuiTextures.STRUCTURE_CHECK.getSubTexture(0.0, 0.5, 1.0, 0.5),
               () -> controller.getWaitingTime() < 1,
               (clickData, pressed) -> {
                  if (!clickData.isRemote) {
                     if (controller.getWaitingTime() > 0) {
                        controller.setWaitingTime(0);
                     } else if (clickData.isShiftClick) {
                        controller.requestCheck();
                     }
                  }
               }
            )
            .setTooltipsSupplier(
               pressed -> List.of(Component.translatable("gtocore.machine.structure_check"), Component.translatable("gtocore.machine.structure_check.shift"))
            )
      );
   }

   public static void attachBatchConfigurators(ConfiguratorPanel configuratorPanel, BooleanSupplier getter, BiConsumer<ClickData, Boolean> setter) {
      configuratorPanel.attachConfigurators(
         new Toggle(GuiTextures.BUTTON_BATCH.getSubTexture(0.0, 0.0, 1.0, 0.5), GuiTextures.BUTTON_BATCH.getSubTexture(0.0, 0.5, 1.0, 0.5), getter, setter)
            .setTooltipsSupplier(p -> List.of(Component.translatable("gtceu.machine.batch_" + (p ? "enabled" : "disabled"))))
      );
   }
}
