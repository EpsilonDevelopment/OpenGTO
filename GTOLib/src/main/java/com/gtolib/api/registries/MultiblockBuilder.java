package com.gtolib.api.registries;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.api.registry.registrate.MultiblockMachineBuilder;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import com.gto.registrate.Registrate;
import com.gtocore.common.machine.multiblock.steam.LargeSteamMultiblockMachine;
import com.gtocore.data.lang.LangHandler;
import com.gtolib.api.annotation.NewDataAttributes;
import com.gtolib.api.annotation.component_builder.ComponentBuilder;
import com.gtolib.api.annotation.dynamic.DynamicInitialData;
import com.gtolib.api.lang.CNEN;
import com.gtolib.api.lang.TooltipsSortedWrapper;
import com.gtolib.api.machine.IGTOMachineDefinition;
import com.gtolib.api.machine.MultiblockDefinition;
import com.gtolib.api.recipe.GTORecipeModifiers;
import com.gtolib.api.recipe.RecipeType;
import com.gtolib.utils.GTOUtils;
import com.gtolib.utils.register.BlockRegisterUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class MultiblockBuilder extends MultiblockMachineBuilder {
   public static final Map<String, CNEN> LANG = GTCEu.isDataGen() ? new O2OOpenCacheHashMap<>() : null;
   private int maxTier = -1;
   private boolean upgradable;
   private boolean workableInSpace = false;
   private Class<?> clazz;
   private int tooltipsIndex;
   private final List<GTRecipeType> recipes = new ArrayList<>();
   private final TooltipsSortedWrapper tooltipsWrapper = new TooltipsSortedWrapper();
   private BiConsumer<ItemStack, List<Component>> extraTooltipBuilder;

   MultiblockBuilder(
      Registrate registrate,
      String name,
      Function<MetaMachineBlockEntity, ? extends MultiblockControllerMachine> metaMachine,
      BiFunction<Properties, MultiblockMachineDefinition, MetaMachineBlock> blockFactory,
      BiFunction<MetaMachineBlock, net.minecraft.world.item.Item.Properties, MetaMachineItem> itemFactory,
      TriFunction<BlockEntityType<?>, BlockPos, BlockState, MetaMachineBlockEntity> blockEntityFactory
   ) {
      super(registrate, name, metaMachine, blockFactory, itemFactory, blockEntityFactory);
   }

   public MultiblockBuilder langValue(@Nullable String langValue) {
      return (MultiblockBuilder)super.langValue(langValue);
   }

   public MultiblockBuilder tier(int tier) {
      return (MultiblockBuilder)super.tier(tier);
   }

   public MultiblockBuilder noRecipeModifier() {
      return (MultiblockBuilder)super.noRecipeModifier();
   }

   public MultiblockBuilder recipeTypes(GTRecipeType... recipeTypes) {
      for (GTRecipeType recipeType : recipeTypes) {
         this.addRecipeType(recipeType);
      }

      return (MultiblockBuilder)super.recipeTypes(recipeTypes);
   }

   public MultiblockBuilder recipeType(GTRecipeType type) {
      this.addRecipeType(type);
      return (MultiblockBuilder)super.recipeType(type);
   }

   private void addRecipeType(GTRecipeType recipeType) {
      if (!Objects.equals(recipeType.group, "dummy")) {
         this.recipes.add(recipeType);
      }

      recipeType.getProxyRecipes().forEach(t -> {
         if (t instanceof GTRecipeType type) {
            this.recipes.add(type);
         }
      });
   }

   public MultiblockBuilder recipeModifier(RecipeModifier recipeModifier) {
      return (MultiblockBuilder)super.recipeModifier(recipeModifier);
   }

   public MultiblockBuilder recipeModifiers(RecipeModifier... recipeModifiers) {
      return recipeModifiers.length == 1 ? this.recipeModifier(recipeModifiers[0]) : (MultiblockBuilder)super.recipeModifiers(recipeModifiers);
   }

   public MultiblockBuilder generator() {
      return (MultiblockBuilder)this.generator(true);
   }

   public MultiblockBuilder tooltipsKey(String key, Object... args) {
      return this.tooltips(Component.translatable(key, args));
   }

   public MultiblockBuilder workableInSpace() {
      this.workableInSpace = true;
      this.tooltipsToTooltipsBuilder(NewDataAttributes.WORKABLE_IN_SPACE.create());
      return this;
   }

   public MultiblockBuilder tooltipsComponent(Component component) {
      return this.tooltips(component);
   }

   private MultiblockBuilder tooltipsText(CNEN cnen, Object... args) {
      String key = "gtocore.machine." + this.name + ".tooltip." + this.tooltipsIndex;
      if (LANG != null) {
         LANG.put(key, cnen);
      }

      this.tooltipsKey(key, args);
      this.tooltipsIndex++;
      return this;
   }

   public MultiblockBuilder tooltipsText(String cn, String en, Object... args) {
      return this.tooltipsText(new CNEN(cn, en), args);
   }

   public MultiblockBuilder tooltips(Component... components) {
      for (Component component : components) {
         if (component != null) {
            this.tooltipsWrapper.addTooltip(ComponentBuilder.create().addLines(new Component[]{component}, s -> s).build());
         }
      }

      return this;
   }

   public MultiblockBuilder tooltips(Supplier<List<Component>> list) {
      return this.tooltipsToTooltipsBuilder(list);
   }

   public MultiblockBuilder tooltipsSupplier(Supplier<List<Component>> supplier) {
      this.tooltipsWrapper.addSupplier(supplier);
      return this;
   }

   @NotNull
   private MultiblockBuilder tooltipsToTooltipsBuilder(Supplier<List<Component>> list) {
      this.tooltipsWrapper.addTooltip(list);
      return this;
   }

   public MultiblockBuilder addTooltipsFromClass(Class<?> clazz) {
      return this.addTooltipsFromClass(clazz, value -> true);
   }

   private MultiblockBuilder addTooltipsFromClass(Class<?> clazz, Predicate<DynamicInitialData.Value> filter) {
      this.clazz = clazz;
      return DynamicInitialData.addTooltipsText(this, clazz, filter);
   }

   public MultiblockBuilder addTooltipsFromClass(Class<?> clazz, String... nameFilter) {
      return this.addTooltipsFromClass(
         clazz, value1 -> Arrays.stream(nameFilter).anyMatch(s -> value1.field().getName().toLowerCase().contains(s.toLowerCase()))
      );
   }

   public MultiblockBuilder genLang(String cnLang) {
      if (BlockRegisterUtils.LANG == null) {
         return this;
      }

      if (!((GTORegistration)this.registrate).gtm) {
         throw new RuntimeException();
      }

      LangHandler.addCNEN(
         "block.gtceu." + this.name,
         cnLang,
         this.langValue() == null ? FormattingUtil.toEnglishName(GTORegistration.PATTERN.matcher(this.name).replaceAll("_")) : this.langValue()
      );
      return this;
   }

   @Override
   public MultiblockMachineDefinition register() {
      super.tooltipBuilder((stack, components) -> {
         for (Supplier<List<Component>> supplier : this.tooltipsWrapper.getTooltips()) {
            List<Component> list = supplier.get();
            if (list != null) {
               components.addAll(list);
            }
         }

         if (this.extraTooltipBuilder != null) {
            this.extraTooltipBuilder.accept(stack, components);
         }
      });
      this.tooltipsWrapper.setInitialized(true);
      MultiblockMachineDefinition definition = super.register();
      if (definition instanceof MultiblockDefinition multiblockDefinition) {
         multiblockDefinition.upgradable = this.upgradable;
         multiblockDefinition.maxTier = this.maxTier;
      }

      if (definition instanceof IGTOMachineDefinition workableInSpaceDefinition) {
         workableInSpaceDefinition.setCanWorkInSpaceIndependently(this.workableInSpace);
      }

      return definition;
   }

   public MultiblockBuilder tooltipBuilder(BiConsumer<ItemStack, List<Component>> tooltipBuilder) {
      if (this.extraTooltipBuilder == null) {
         this.extraTooltipBuilder = tooltipBuilder;
      } else {
         BiConsumer<ItemStack, List<Component>> previous = this.extraTooltipBuilder;
         this.extraTooltipBuilder = (stack, list) -> {
            previous.accept(stack, list);
            tooltipBuilder.accept(stack, list);
         };
      }

      return this;
   }

   public MultiblockBuilder workableTieredHullRenderer(ResourceLocation workableModel) {
      return (MultiblockBuilder)super.workableTieredHullRenderer(workableModel);
   }

   public MultiblockBuilder pattern(Function<MultiblockMachineDefinition, BlockPattern> pattern) {
      return (MultiblockBuilder)super.pattern(pattern);
   }

   public MultiblockBuilder addSubPattern(@NotNull Function<MultiblockMachineDefinition, BlockPattern> pattern) {
      return (MultiblockBuilder)super.addSubPattern(pattern);
   }

   public MultiblockBuilder workableCasingRenderer(ResourceLocation baseCasing, ResourceLocation overlayModel) {
      return (MultiblockBuilder)super.workableCasingRenderer(baseCasing, overlayModel);
   }

   public MultiblockBuilder workableCasingRenderer(ResourceLocation baseCasing, ResourceLocation overlayModel, boolean tint) {
      return (MultiblockBuilder)super.workableCasingRenderer(baseCasing, overlayModel, tint);
   }

   public MultiblockBuilder block(Supplier<? extends Block> block) {
      this.addRecipeTypeTooltips();
      return (MultiblockBuilder)this.appearanceBlock(block);
   }

   public MultiblockBuilder addRecipeTypeTooltips() {
      if (!this.recipes.isEmpty()) {
         MutableComponent root = null;

         for (GTRecipeType recipeType : this.recipes) {
            MutableComponent component = Component.translatable(recipeType.registryName.getNamespace() + "." + recipeType.registryName.getPath());
            if (root == null) {
               root = component;
            } else {
               root.append(", ").append(component);
            }
         }

         root.withStyle(ChatFormatting.WHITE);
         this.tooltips(NewDataAttributes.RECIPES_TYPE.create(root).get().toArray(new Component[0]));
      }

      return this;
   }

   public MultiblockBuilder nonYAxisRotation() {
      return (MultiblockBuilder)this.rotationState(RotationState.NON_Y_AXIS).allowExtendedFacing(false);
   }

   public MultiblockBuilder allRotation() {
      return (MultiblockBuilder)this.rotationState(RotationState.ALL);
   }

   public MultiblockBuilder noneRotation() {
      return (MultiblockBuilder)this.rotationState(RotationState.NONE).allowExtendedFacing(false).allowFlip(false);
   }

   public MultiblockBuilder fromSourceTooltips(String source) {
      return this.tooltipsKey("gtocore.source", source);
   }

   public MultiblockBuilder moduleTooltips(PartAbility[] abilities, RecipeType[] recipeType) {
      this.tooltipsToTooltipsBuilder(
         NewDataAttributes.ALLOW_MODULE
            .setComment(
               componentBuilder -> {
                  ComponentBuilder b = componentBuilder.addCommentLines(
                     "允许拓展结构提升机器效率。\n扩展结构可安装额外的仓，带来额外的加成",
                     "Allows the expansion of structures to improve machine efficiency.\nExpansion structures can install additional hatches to bring additional bonuses"
                  );
                  if (abilities.length > 0) {
                     b.addLines(
                        Component.translatable(
                           "gtocore.applicable_modules",
                           Arrays.stream(abilities)
                              .map(a -> "gtocore.part_ability." + a.getName())
                              .map(Component::translatable)
                              .map(c -> c.withStyle(ChatFormatting.AQUA))
                              .collect(GTOUtils.joiningComponent(Component.literal(", ").withStyle(ChatFormatting.GRAY)))
                        ),
                        styleBuilder -> styleBuilder.setGray().setPrefix(NewDataAttributes.PREFIX_TAB)
                     );
                  }

                  if (recipeType.length > 0) {
                     b.addLines(
                        Component.translatable(
                           "gtocore.applicable_recipes",
                           Arrays.stream(recipeType)
                              .map(rt -> "gtceu." + rt.registryName.getPath())
                              .map(Component::translatable)
                              .map(c -> c.withStyle(ChatFormatting.YELLOW))
                              .collect(GTOUtils.joiningComponent(Component.literal(", ").withStyle(ChatFormatting.GRAY)))
                        ),
                        styleBuilder -> styleBuilder.setGray().setPrefix(NewDataAttributes.PREFIX_TAB)
                     );
                  }

                  return b;
               }
            )
            .create()
      );
      return this;
   }

   public MultiblockBuilder moduleTooltips() {
      return this.moduleTooltips(new PartAbility[0], new RecipeType[0]);
   }

   public MultiblockBuilder moduleTooltips(PartAbility... abilities) {
      return this.moduleTooltips(abilities, new RecipeType[0]);
   }

   public MultiblockBuilder moduleTooltips(RecipeType... recipeType) {
      return this.moduleTooltips(new PartAbility[0], recipeType);
   }

   public MultiblockBuilder perfectOCTooltips() {
      this.tooltipsToTooltipsBuilder(NewDataAttributes.PREFECT_OVERCLOCK.create());
      return this;
   }

   public MultiblockBuilder lossyOCTooltips() {
      this.tooltipsToTooltipsBuilder(NewDataAttributes.LASER_LOSS_OVERCLOCKING.create());
      return this;
   }

   public MultiblockBuilder laserTooltips() {
      this.tooltipsToTooltipsBuilder(NewDataAttributes.LASER_ENERGY_HATCH.create());
      return this;
   }

   public MultiblockBuilder multipleRecipesTooltips() {
      this.tooltipsToTooltipsBuilder(NewDataAttributes.ALLOW_MULTI_RECIPE_PARALLEL.create());
      return this;
   }

   public MultiblockBuilder eutMultiplierTooltips(double multiplier) {
      this.tooltipsToTooltipsBuilder(NewDataAttributes.ENERGY_COST_MULTIPLY.create(multiplier));
      return this;
   }

   public MultiblockBuilder durationMultiplierTooltips(double multiplier) {
      this.tooltipsToTooltipsBuilder(NewDataAttributes.TIME_COST_MULTIPLY.create(multiplier));
      return this;
   }

   public MultiblockBuilder coilParallelTooltips() {
      this.specialParallelizableTooltips();
      this.tooltipsToTooltipsBuilder(
         NewDataAttributes.ALLOW_PARALLEL_NUMBER
            .create(
               b -> b.addLines("线圈温度每高出900K，并行数x2", "For every 900K increase in coil temperature, the parallel number doubles"),
               c -> c.addCommentLines("公式 : 2^(向下取整(温度 / 900)), 算去吧", "Formula: 2^(Round down(temperature / 900)), do the math")
            )
      );
      return this;
   }

   public MultiblockBuilder glassParallelTooltips() {
      this.specialParallelizableTooltips();
      this.tooltipsToTooltipsBuilder(
         NewDataAttributes.ALLOW_PARALLEL_NUMBER
            .create(h -> h.addLines("由玻璃等级决定", "Determined by glass tier"), c -> c.addCommentLines("公式 : 4^玻璃等级", "Formula: 4^(Glass Tier)"))
      );
      return this;
   }

   public MultiblockBuilder parallelizableTooltips() {
      return this.tooltipsToTooltipsBuilder(NewDataAttributes.ALLOW_PARALLEL.create());
   }

   public MultiblockBuilder specialParallelizableTooltips() {
      return this.tooltipsToTooltipsBuilder(NewDataAttributes.ALLOW_PARALLEL_SPECIAL.create());
   }

   public MultiblockBuilder overclock() {
      return this.recipeModifier(GTORecipeModifiers.UPGRADE_OVERCLOCK);
   }

   public MultiblockBuilder perfectOverclock() {
      return this.recipeModifier(GTORecipeModifiers.UPGRADE_PERFECT_OVERCLOCK);
   }

   public MultiblockBuilder parallelizableOverclock() {
      return this.recipeModifier(GTORecipeModifiers.UPGRADE_PARALLELIZABLE_OVERCLOCK);
   }

   public MultiblockBuilder parallelizablePerfectOverclock() {
      return this.recipeModifier(GTORecipeModifiers.UPGRADE_PARALLELIZABLE_PERFECT_OVERCLOCK);
   }

   public MultiblockBuilder parallelizableManaOverclock() {
      return this.recipeModifier(GTORecipeModifiers.PARALLELIZABLE_MANA_OVERCLOCK);
   }

   public MultiblockBuilder steamOverclock() {
      return this.steamOverclock(1);
   }

   public MultiblockBuilder steamOverclock(int tier) {
      this.maxTier(tier);
      this.tooltipsKey("gtocore.machine.steam.tooltip.1", GTValues.VNF[tier]);
      if (this.clazz == LargeSteamMultiblockMachine.class) {
         this.tooltipsKey("gtocore.machine.steam.tooltip.2");
      }

      this.specialParallelizableTooltips();
      return this;
   }

   public MultiblockBuilder upgradable() {
      this.upgradable = true;
      return this;
   }

   public MultiblockBuilder maxTier(int tier) {
      this.maxTier = tier;
      return this;
   }
}
