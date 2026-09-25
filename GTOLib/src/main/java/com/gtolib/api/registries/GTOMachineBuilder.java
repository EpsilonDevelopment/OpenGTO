package com.gtolib.api.registries;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.gui.editor.EditableMachineUI;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import com.gto.registrate.Registrate;
import com.gtocore.client.renderer.machine.WorkableManaTieredHullMachineRenderer;
import com.gtocore.data.lang.LangHandler;
import com.gtolib.api.annotation.NewDataAttributes;
import com.gtolib.api.annotation.dynamic.DynamicInitialData;
import com.gtolib.api.lang.CNEN;
import com.gtolib.api.lang.TooltipsSortedWrapper;
import com.gtolib.api.machine.IGTOMachineDefinition;
import com.gtolib.api.recipe.GTORecipeModifiers;
import com.gtolib.utils.register.BlockRegisterUtils;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
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
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import org.apache.commons.lang3.function.TriFunction;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class GTOMachineBuilder extends MachineBuilder<MachineDefinition> {
   private BiConsumer<ItemStack, List<Component>> extraTooltipBuilder;
   private boolean workableInSpace = false;
   public static final Map<String, CNEN> LANG = GTCEu.isDataGen() ? new O2OOpenCacheHashMap<>() : null;
   private int tooltipsIndex;
   private final TooltipsSortedWrapper tooltipsWrapper = new TooltipsSortedWrapper();

   public GTOMachineBuilder(
      Registrate registrate,
      String name,
      Function<ResourceLocation, MachineDefinition> definition,
      Function<MetaMachineBlockEntity, MetaMachine> machine,
      BiFunction<Properties, MachineDefinition, MetaMachineBlock> blockFactory,
      BiFunction<MetaMachineBlock, net.minecraft.world.item.Item.Properties, MetaMachineItem> itemFactory,
      TriFunction<BlockEntityType<?>, BlockPos, BlockState, MetaMachineBlockEntity> blockEntityFactory
   ) {
      super(registrate, name, definition, machine, blockFactory, itemFactory, blockEntityFactory);
   }

   @Override
   public MachineDefinition register() {
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
      MachineDefinition def = super.register();
      if (def instanceof IGTOMachineDefinition defSpace) {
         defSpace.setCanWorkInSpaceIndependently(this.workableInSpace);
      }

      return def;
   }

   public GTOMachineBuilder rotationState(RotationState rotationState) {
      return (GTOMachineBuilder)super.rotationState(rotationState);
   }

   public GTOMachineBuilder langValue(@Nullable String langValue) {
      return (GTOMachineBuilder)super.langValue(langValue);
   }

   public GTOMachineBuilder tier(int tier) {
      return (GTOMachineBuilder)super.tier(tier);
   }

   public GTOMachineBuilder workableInSpace() {
      this.workableInSpace = true;
      this.tooltips(NewDataAttributes.WORKABLE_IN_SPACE.create());
      return this;
   }

   public GTOMachineBuilder editableUI(@Nullable EditableMachineUI editableUI) {
      return (GTOMachineBuilder)super.editableUI(editableUI);
   }

   public GTOMachineBuilder noRecipeModifier() {
      return (GTOMachineBuilder)super.noRecipeModifier();
   }

   public GTOMachineBuilder recipeType(GTRecipeType type) {
      return (GTOMachineBuilder)super.recipeType(type);
   }

   public GTOMachineBuilder abilities(PartAbility... abilities) {
      return (GTOMachineBuilder)super.abilities(abilities);
   }

   public GTOMachineBuilder tooltips(Component... components) {
      this.tooltipsWrapper.addTooltip(Arrays.stream(components).filter(Objects::nonNull).toList());
      return this;
   }

   public GTOMachineBuilder tooltips(Supplier<List<Component>> supplier) {
      this.tooltipsWrapper.addTooltip(supplier);
      return this;
   }

   public GTOMachineBuilder renderer(@Nullable Supplier<IRenderer> renderer) {
      return (GTOMachineBuilder)super.renderer(renderer);
   }

   public GTOMachineBuilder tieredHullRenderer(ResourceLocation model) {
      return (GTOMachineBuilder)super.tieredHullRenderer(model);
   }

   public GTOMachineBuilder overlayTieredHullRenderer(String name) {
      return (GTOMachineBuilder)super.overlayTieredHullRenderer(name);
   }

   public GTOMachineBuilder workableTieredHullRenderer(ResourceLocation workableModel) {
      return (GTOMachineBuilder)super.workableTieredHullRenderer(workableModel);
   }

   public GTOMachineBuilder workableManaTieredHullRenderer(int tier, ResourceLocation workableModel) {
      return this.renderer(() -> new WorkableManaTieredHullMachineRenderer(tier, workableModel));
   }

   public GTOMachineBuilder genLang(String cnLang) {
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

   public GTOMachineBuilder tooltipBuilder(BiConsumer<ItemStack, List<Component>> tooltipBuilder) {
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

   public GTOMachineBuilder recipeModifier(RecipeModifier recipeModifier) {
      return (GTOMachineBuilder)super.recipeModifier(recipeModifier);
   }

   public GTOMachineBuilder recipeModifiers(RecipeModifier... recipeModifiers) {
      return (GTOMachineBuilder)super.recipeModifiers(recipeModifiers);
   }

   public GTOMachineBuilder nonYAxisRotation() {
      return this.rotationState(RotationState.NON_Y_AXIS);
   }

   public GTOMachineBuilder allRotation() {
      return this.rotationState(RotationState.ALL);
   }

   public GTOMachineBuilder addTooltipsFromClass(Class<?> clazz) {
      return this.addTooltipsFromClass(clazz, value -> true);
   }

   private GTOMachineBuilder addTooltipsFromClass(Class<?> clazz, Predicate<DynamicInitialData.Value> filter) {
      return DynamicInitialData.addTooltipsText(this, clazz, filter);
   }

   public GTOMachineBuilder addTooltipsFromClass(Class<?> clazz, String nameFilter) {
      return this.addTooltipsFromClass(clazz, value1 -> value1.field().getName().toLowerCase().contains(nameFilter));
   }

   public GTOMachineBuilder noneRotation() {
      return this.rotationState(RotationState.NONE);
   }

   public GTOMachineBuilder tooltipsKey(String key, Object... args) {
      return this.tooltips(Component.translatable(key, args));
   }

   private GTOMachineBuilder tooltipsText(CNEN cnen, Object... args) {
      String key = "gtocore.machine." + this.name + ".tooltip." + this.tooltipsIndex;
      if (LANG != null) {
         LANG.put(key, cnen);
      }

      this.tooltipsKey(key, args);
      this.tooltipsIndex++;
      return this;
   }

   public GTOMachineBuilder tooltipsText(String en, String cn, Object... args) {
      return this.tooltipsText(new CNEN(cn, en), args);
   }

   public GTOMachineBuilder notAllowSharedTooltips() {
      return this.tooltips(NewDataAttributes.NOT_ALLOW_SHARED.create().get().toArray(new Component[0]));
   }

   public GTOMachineBuilder overclock() {
      return this.recipeModifier(GTORecipeModifiers.UPGRADE_OVERCLOCK);
   }

   public GTOMachineBuilder perfectOverclock() {
      return this.recipeModifier(GTORecipeModifiers.UPGRADE_PERFECT_OVERCLOCK);
   }
}
