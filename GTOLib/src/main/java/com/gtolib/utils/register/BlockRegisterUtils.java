package com.gtolib.utils.register;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable.Builder;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.ActiveBlock;
import com.gregtechceu.gtceu.api.block.IFusionCasingType;
import com.gregtechceu.gtceu.api.block.MaterialBlock;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix.OreType;
import com.gregtechceu.gtceu.api.item.MaterialBlockItem;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.common.block.CoilBlock;
import com.gregtechceu.gtceu.common.block.FusionCasingBlock;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTModels;
import com.gregtechceu.gtceu.core.mixins.BlockPropertiesAccessor;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import com.gto.registrate.Registrate;
import com.gto.registrate.builders.BlockBuilder;
import com.gto.registrate.providers.DataGenContext;
import com.gto.registrate.providers.ProviderType;
import com.gto.registrate.providers.RegistrateBlockstateProvider;
import com.gto.registrate.util.entry.BlockEntry;
import com.gto.registrate.util.entry.RegistryEntry;
import com.gto.registrate.util.nullness.NonNullBiConsumer;
import com.gto.registrate.util.nullness.NonNullFunction;
import com.gto.registrate.util.nullness.NonNullSupplier;
import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.client.renderer.machine.MonitorRenderer;
import com.gtocore.common.block.BlockMap;
import com.gtocore.common.block.CleanroomFilterType;
import com.gtocore.common.block.CoilType;
import com.gtocore.common.block.FusionCasings;
import com.gtocore.common.block.MEStorageCoreBlock;
import com.gtocore.common.block.WirelessEnergyUnitBlock;
import com.gtocore.common.item.WirelessEnergyUnitBlockItem;
import com.gtocore.common.machine.monitor.MonitorBlock;
import com.gtocore.common.machine.monitor.MonitorBlockItem;
import com.gtolib.GTOCore;
import com.gtolib.api.GTOValues;
import com.gtolib.api.annotation.NewDataAttributes;
import com.gtolib.api.registries.GTOMachineBuilder;
import com.gtolib.api.registries.GTORegistration;
import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraftforge.client.model.generators.ModelFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BlockRegisterUtils {
   public static BlockEntry<Block> REACTOR_CORE;
   public static final Map<String, String> LANG = GTCEu.isDataGen() ? new O2OOpenCacheHashMap<>() : null;
   private static ReferenceSet<BlockEntry<? extends Block>> CUSTOM_MODEL_DATA_BLOCKS_REG = new ReferenceOpenHashSet<>();
   public static Set<Block> CUSTOM_MODEL_DATA_BLOCKS;

   private BlockRegisterUtils() {
   }

   public static void addLang(String id, String cn) {
      if (LANG != null) {
         if (LANG.containsKey(id)) {
            GTOCore.LOGGER.error("Repetitive Key: {}", id);
            throw new IllegalStateException();
         }

         LANG.put(id, cn);
      }
   }

   public static void loadCustomModelDataBlocks() {
      CUSTOM_MODEL_DATA_BLOCKS = CUSTOM_MODEL_DATA_BLOCKS_REG.stream().map(RegistryEntry::get).collect(Collectors.toSet());
      CUSTOM_MODEL_DATA_BLOCKS_REG = null;
   }

   public static <T extends Block> BlockBuilder<T, Registrate> block(String id, String cn, NonNullFunction<Properties, T> factory) {
      addLang(id, cn);
      return GTORegistration.GTO.block(id, factory);
   }

   public static void registerOreBlock(
      Material material,
      GTRegistrate registrate,
      ImmutableMap<Material, Set<TagPrefix>> ORE_MAP,
      Set<TagPrefix> gtolib$DEEPSLATE,
      Builder<TagPrefix, Material, BlockEntry<? extends Block>> MATERIAL_BLOCKS_BUILDER
   ) {
      float destroyTime = (float)material.getMass() / 50.0F;
      float explosionResistance = material.getBlastTemperature() / 500.0F;

      for (Entry<TagPrefix, OreType> ore : TagPrefix.ORES.entrySet()) {
         if (!ore.getKey().isIgnored(material)) {
            TagPrefix oreTag = ore.getKey();
            if (oreTag != TagPrefix.ore) {
               Set<TagPrefix> tagPrefixes = ORE_MAP.get(material);
               if (tagPrefixes == null || !gtolib$DEEPSLATE.contains(oreTag) && !tagPrefixes.contains(oreTag)) {
                  continue;
               }
            }

            OreType oreType = ore.getValue();
            if (ore.getKey() instanceof GTOTagPrefix) {
               registrate = GTORegistration.GTO;
            }

            BlockEntry<MaterialBlock> entry = registrate.<MaterialBlock>block(
                        "%s%s_ore".formatted(oreTag != TagPrefix.ore ? FormattingUtil.toLowerCaseUnderscore(oreTag.name()) + "_" : "", material.getName()),
                        properties -> oreTag.blockConstructor().create(properties, oreTag, material)
                     )
                     .initialProperties(() -> oreType.stoneType().get().isAir() ? Blocks.IRON_ORE : oreType.stoneType().get().getBlock())
                     .properties(properties -> {
                        Properties p = GTBlocks.copy(oreType.template().get(), properties).noLootTable();
                        p.destroyTime(((BlockPropertiesAccessor)p).getDestroyTime() + destroyTime);
                        p.explosionResistance(((BlockPropertiesAccessor)p).getExplosionResistance() + explosionResistance);
                        return p;
                     })
                     .blockstate(NonNullBiConsumer.noop())
                     .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                     .setData(ProviderType.LOOT, NonNullBiConsumer.noop())
                     .transform(GTBlocks.unificationBlock(oreTag, material))
                  .color(() -> MaterialBlock::tintedColor)
                  .item((b, p) -> oreTag.blockItemConstructor().create(b, p, oreTag, material))
                  .model(NonNullBiConsumer.noop())
                  .color(() -> () -> MaterialBlockItem.tintColor(material))
                  .build()
               .register();
            MATERIAL_BLOCKS_BUILDER.put(oreTag, material, entry);
         }
      }
   }

   public static BlockEntry<Block> createTierCasings(String id, String cn, ResourceLocation texture, Int2ObjectMap<Supplier<?>> map, int tier) {
      return createTierCasings(id, null, cn, texture, map, tier);
   }

   public static BlockEntry<Block> createTierCasings(String id, String en, String cn, ResourceLocation texture, Int2ObjectMap<Supplier<?>> map, int tier) {
      NewDataAttributes.LEVEL.create(tier).get();
      return createTierCasings(id, en, cn, texture, () -> Blocks.IRON_BLOCK, p -> new Block(p) {
         @Override
         public void appendHoverText(@NotNull ItemStack stack, @Nullable BlockGetter level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
            tooltip.addAll(NewDataAttributes.LEVEL.create(tier).get());
         }
      }, () -> RenderType::cutoutMipped, map, tier);
   }

   public static BlockEntry<Block> createTierGlassCasings(String id, String cn, ResourceLocation texture, int tier) {
      return createTierGlassCasings(id, null, cn, texture, tier);
   }

   public static BlockEntry<Block> createTierGlassCasings(String id, String en, String cn, ResourceLocation texture, int tier) {
      NewDataAttributes.LEVEL.create(tier).get();
      return createTierCasings(id, en, cn, texture, () -> Blocks.GLASS, p -> new GlassBlock(p) {
         @Override
         public void appendHoverText(@NotNull ItemStack stack, @Nullable BlockGetter level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
            super.appendHoverText(stack, level, tooltip, flag);
            tooltip.addAll(NewDataAttributes.LEVEL.create(tier).get());
         }
      }, () -> RenderType::translucent, BlockMap.GLASSMAP, tier);
   }

   public static BlockEntry<Block> createTierCasings(
      String id,
      String en,
      String cn,
      ResourceLocation texture,
      NonNullSupplier<? extends Block> block,
      NonNullFunction<Properties, Block> blockSupplier,
      Supplier<Supplier<RenderType>> type,
      Int2ObjectMap<Supplier<?>> map,
      int tier
   ) {
      BlockBuilder<Block, Registrate> build = block(id, cn, blockSupplier)
         .initialProperties(block)
         .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
         .addLayer(type)
         .blockstate(GTModels.cubeAllModel(id, texture))
         .tag(GTToolType.WRENCH.harvestTags.getFirst(), BlockTags.MINEABLE_WITH_PICKAXE)
         .item(BlockItem::new)
         .build();
      if (en != null) {
         build.lang(en);
      }

      BlockEntry<Block> Block = build.register();
      map.put(tier, Block);
      return Block;
   }

   public static BlockEntry<ActiveBlock> createActiveCasing(String id, String cn, String baseModelPath) {
      return createActiveCasing(id, null, cn, baseModelPath);
   }

   public static BlockEntry<ActiveBlock> createActiveCasing(String id, String en, String cn, String baseModelPath) {
      BlockBuilder<ActiveBlock, Registrate> build = block(id, cn, ActiveBlock::new)
         .initialProperties(() -> Blocks.IRON_BLOCK)
         .addLayer(() -> RenderType::cutoutMipped)
         .blockstate(GTModels.createActiveModel(GTOCore.id(baseModelPath)))
         .tag(GTToolType.WRENCH.harvestTags.getFirst(), BlockTags.MINEABLE_WITH_PICKAXE)
         .item(BlockItem::new)
         .model((ctx, prov) -> prov.withExistingParent(prov.name(ctx), GTOCore.id(baseModelPath)))
         .build();
      if (en != null) {
         build.lang(en);
      }

      return build.register();
   }

   public static BlockEntry<ActiveBlock> createActiveTierCasing(String id, String cn, String baseModelPath, Int2ObjectMap<Supplier<?>> map, int tier) {
      return createActiveTierCasing(id, null, cn, baseModelPath, map, tier);
   }

   public static BlockEntry<ActiveBlock> createActiveTierCasing(String id, String en, String cn, String baseModelPath, Int2ObjectMap<Supplier<?>> map, int tier) {
      NewDataAttributes.LEVEL.create(tier).get();
      BlockBuilder<ActiveBlock, Registrate> build = BlockRegisterUtils.<ActiveBlock>block(id, cn, p -> new ActiveBlock(p) {
            @Override
            public void appendHoverText(@NotNull ItemStack stack, @Nullable BlockGetter level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
               super.appendHoverText(stack, level, tooltip, flag);
               tooltip.addAll(NewDataAttributes.LEVEL.create(tier).get());
            }
         })
         .initialProperties(() -> Blocks.IRON_BLOCK)
         .addLayer(() -> RenderType::cutoutMipped)
         .blockstate(GTModels.createActiveModel(GTOCore.id(baseModelPath)))
         .tag(GTToolType.WRENCH.harvestTags.getFirst(), BlockTags.MINEABLE_WITH_PICKAXE)
         .item(BlockItem::new)
         .model((ctx, prov) -> prov.withExistingParent(prov.name(ctx), GTOCore.id(baseModelPath)))
         .build();
      if (en != null) {
         build.lang(en);
      }

      BlockEntry<ActiveBlock> Block = build.register();
      map.put(tier, Block);
      return Block;
   }

   public static BlockEntry<Block> createCleanroomFilter() {
      BlockEntry<Block> filterBlock = block(((net.minecraft.util.StringRepresentable)CleanroomFilterType.FILTER_CASING_LAW).getSerializedName(), "绝对洁净过滤器机械方块", Block::new)
         .initialProperties(() -> Blocks.IRON_BLOCK)
         .properties(
            properties -> properties.strength(2.0F, 8.0F).sound(SoundType.METAL).isValidSpawn((blockState, blockGetter, blockPos, entityType) -> false)
         )
         .addLayer(() -> RenderType::solid)
         .blockstate(NonNullBiConsumer.noop())
         .tag(GTToolType.WRENCH.harvestTags.getFirst(), CustomTags.TOOL_TIERS[1])
         .item(BlockItem::new)
         .build()
         .register();
      GTCEuAPI.CLEANROOM_FILTERS.put(CleanroomFilterType.FILTER_CASING_LAW, filterBlock);
      return filterBlock;
   }

   public static BlockEntry<Block> createGlassCasingBlock(String id, String cn, ResourceLocation texture) {
      return createGlassCasingBlock(id, null, cn, texture);
   }

   public static BlockEntry<Block> createGlassCasingBlock(String id, String en, String cn, ResourceLocation texture) {
      return createCasingBlock(id, en, cn, GTModels.cubeAllModel(id, texture), GlassBlock::new, () -> Blocks.GLASS, () -> RenderType::translucent);
   }

   public static BlockEntry<Block> createCasingBlock(String id, String cn, ResourceLocation texture) {
      return createCasingBlock(id, null, cn, texture);
   }

   public static BlockEntry<Block> createCasingBlock(String id, String en, String cn, ResourceLocation texture) {
      return createCasingBlock(id, en, cn, GTModels.cubeAllModel(id, texture), Block::new, () -> Blocks.IRON_BLOCK, () -> RenderType::solid);
   }

   public static BlockEntry<Block> createCustomModelCasingBlock(String id, String cn) {
      return createCustomModelCasingBlock(id, null, cn);
   }

   public static BlockEntry<Block> createCustomModelCasingBlock(String id, String en, String cn) {
      return createCasingBlock(id, en, cn, NonNullBiConsumer.noop(), Block::new, () -> Blocks.IRON_BLOCK, () -> RenderType::solid);
   }

   public static BlockEntry<Block> createCustomModelCasingBlock(
      String id,
      String en,
      String cn,
      NonNullBiConsumer<DataGenContext<Block, Block>, RegistrateBlockstateProvider> modelCons,
      Supplier<Supplier<RenderType>> type
   ) {
      BlockEntry<Block> b = createCasingBlock(id, en, cn, modelCons, Block::new, () -> Blocks.IRON_BLOCK, type);
      CUSTOM_MODEL_DATA_BLOCKS_REG.add(b);
      return b;
   }

   public static BlockEntry<Block> createCasingBlock(
      String id,
      String cn,
      NonNullBiConsumer<DataGenContext<Block, Block>, RegistrateBlockstateProvider> cons,
      NonNullFunction<Properties, Block> blockSupplier,
      NonNullSupplier<? extends Block> properties,
      Supplier<Supplier<RenderType>> type
   ) {
      return createCasingBlock(id, null, cn, cons, blockSupplier, properties, type);
   }

   public static BlockEntry<Block> createCasingBlock(
      String id,
      String en,
      String cn,
      NonNullBiConsumer<DataGenContext<Block, Block>, RegistrateBlockstateProvider> cons,
      NonNullFunction<Properties, Block> blockSupplier,
      NonNullSupplier<? extends Block> properties,
      Supplier<Supplier<RenderType>> type
   ) {
      BlockBuilder<Block, Registrate> build = block(id, cn, blockSupplier)
         .initialProperties(properties)
         .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
         .addLayer(type)
         .blockstate(cons)
         .tag(GTToolType.WRENCH.harvestTags.getFirst(), BlockTags.MINEABLE_WITH_PICKAXE)
         .item(BlockItem::new)
         .build();
      if (en != null) {
         build.lang(en);
      }

      return build.register();
   }

   public static BlockEntry<Block> createSidedCasingBlock(String id, String cn, ResourceLocation texture) {
      return createSidedCasingBlock(id, null, cn, texture);
   }

   public static BlockEntry<Block> createSidedCasingBlock(String id, String en, String cn, ResourceLocation texture) {
      BlockBuilder<Block, Registrate> build = block(id, cn, Block::new)
         .initialProperties(() -> Blocks.IRON_BLOCK)
         .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
         .addLayer(() -> RenderType::solid)
         .blockstate(
            (ctx, prov) -> prov.simpleBlock(
               ctx.getEntry(), prov.models().cubeBottomTop(id, texture.withSuffix("/side"), texture.withSuffix("/top"), texture.withSuffix("/top"))
            )
         )
         .tag(GTToolType.WRENCH.harvestTags.getFirst(), BlockTags.MINEABLE_WITH_PICKAXE)
         .item(BlockItem::new)
         .build();
      if (en != null) {
         build.lang(en);
      }

      return build.register();
   }

   public static BlockEntry<Block> createStoneBlock(String id, String cn, ResourceLocation texture) {
      return block(id, cn, Block::new)
         .initialProperties(() -> Blocks.STONE)
         .addLayer(() -> RenderType::solid)
         .blockstate(GTModels.cubeAllModel(id, texture))
         .tag(BlockTags.MINEABLE_WITH_PICKAXE)
         .item(BlockItem::new)
         .build()
         .register();
   }

   public static BlockEntry<Block> createSandBlock(String id, String cn, ResourceLocation texture) {
      return block(id, cn, Block::new)
         .initialProperties(() -> Blocks.SAND)
         .addLayer(() -> RenderType::solid)
         .blockstate(GTModels.cubeAllModel(id, texture))
         .tag(BlockTags.MINEABLE_WITH_SHOVEL)
         .item(BlockItem::new)
         .build()
         .register();
   }

   public static BlockEntry<FusionCasingBlock> createFusionCasing(IFusionCasingType casingType, String cn) {
      return BlockRegisterUtils.<FusionCasingBlock>block(casingType.getSerializedName(), cn, p -> new FusionCasings(p, casingType))
         .initialProperties(() -> Blocks.IRON_BLOCK)
         .properties(properties -> properties.strength(5.0F, 10.0F).sound(SoundType.METAL))
         .addLayer(() -> RenderType::solid)
         .blockstate(
            (ctx, prov) -> {
               ActiveBlock block = ctx.getEntry();
               ModelFile inactive = prov.models().getExistingFile(GTOCore.id(casingType.getSerializedName()));
               ModelFile active = prov.models().getExistingFile(GTOCore.id(casingType.getSerializedName()).withSuffix("_active"));
               prov.getVariantBuilder(block)
                  .partialState()
                  .with(ActiveBlock.ACTIVE, false)
                  .modelForState()
                  .modelFile(inactive)
                  .addModel()
                  .partialState()
                  .with(ActiveBlock.ACTIVE, true)
                  .modelForState()
                  .modelFile(active)
                  .addModel();
            }
         )
         .tag(GTToolType.WRENCH.harvestTags.getFirst(), CustomTags.TOOL_TIERS[casingType.getHarvestLevel()])
         .item(BlockItem::new)
         .build()
         .register();
   }

   public static BlockEntry<Block> createHermeticCasing(int tier) {
      String tierName = GTValues.VN[tier].toLowerCase(Locale.ROOT);
      return block("%s_hermetic_casing".formatted(tierName), "%s密封机械方块".formatted(GTValues.VN[tier]), Block::new)
         .lang("Hermetic Casing %s".formatted(GTValues.LVT[tier]))
         .initialProperties(() -> Blocks.IRON_BLOCK)
         .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
         .addLayer(() -> RenderType::cutoutMipped)
         .blockstate(NonNullBiConsumer.noop())
         .tag(GTToolType.WRENCH.harvestTags.getFirst(), BlockTags.MINEABLE_WITH_PICKAXE)
         .item(BlockItem::new)
         .build()
         .register();
   }

   public static BlockEntry<CoilBlock> createCoilBlock(CoilType coilType) {
      BlockEntry<CoilBlock> coilBlock = block("%s_coil_block".formatted(coilType.getName()), coilType.getCnLang() + "线圈方块", p -> new CoilBlock(p, coilType))
         .initialProperties(() -> Blocks.IRON_BLOCK)
         .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
         .addLayer(() -> RenderType::cutoutMipped)
         .blockstate(
            (ctx, prov) -> {
               ActiveBlock block = ctx.getEntry();
               ModelFile inactive = prov.models().getExistingFile(coilType.getTexture());
               ModelFile active = prov.models().getExistingFile(coilType.getTexture().withSuffix("_bloom"));
               prov.getVariantBuilder(block)
                  .partialState()
                  .with(ActiveBlock.ACTIVE, false)
                  .modelForState()
                  .modelFile(inactive)
                  .addModel()
                  .partialState()
                  .with(ActiveBlock.ACTIVE, true)
                  .modelForState()
                  .modelFile(active)
                  .addModel();
            }
         )
         .tag(GTToolType.WRENCH.harvestTags.getFirst(), BlockTags.MINEABLE_WITH_PICKAXE)
         .item(BlockItem::new)
         .model((ctx, prov) -> prov.withExistingParent(prov.name(ctx), coilType.getTexture()))
         .build()
         .register();
      GTCEuAPI.HEATING_COILS.put(coilType, coilBlock);
      if (coilType == CoilType.INFINITY) {
         CUSTOM_MODEL_DATA_BLOCKS_REG.add(coilBlock);
      }

      return coilBlock;
   }

   public static BlockEntry<WirelessEnergyUnitBlock> createWirelessEnergyUnit(int tier) {
      String t = GTValues.VN[tier].toLowerCase(Locale.ROOT);
      String name = t + "_wireless_energy_unit";
      return ((BlockBuilder)block(name, GTOValues.VNFR[tier] + "无线能量单元", p -> new WirelessEnergyUnitBlock(p, tier))
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .addLayer(() -> RenderType::cutoutMipped)
            .blockstate(
               (ctx, prov) -> prov.simpleBlock((Block)ctx.getEntry(), prov.models().cubeAll(name, GTOCore.id("block/casings/wireless_energy_unit/" + t)))
            )
            .tag(GTToolType.WRENCH.harvestTags.getFirst())
            .item(WirelessEnergyUnitBlockItem::new)
            .build())
         .register();
   }

   public static BlockEntry<MEStorageCoreBlock> createMEStorageCore(int tier) {
      String t = "t" + tier;
      String name = t + "_me_storage_core";
      return ((BlockBuilder)block(name, "T" + tier + " ME存储核心", p -> new MEStorageCoreBlock(p, tier))
            .lang("T" + tier + " ME Storage Core")
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .addLayer(() -> RenderType::cutoutMipped)
            .blockstate((ctx, prov) -> prov.simpleBlock((Block)ctx.getEntry(), prov.models().cubeAll(name, GTOCore.id("block/casings/me_storage_core/" + t))))
            .tag(GTToolType.WRENCH.harvestTags.getFirst())
            .item(BlockItem::new)
            .build())
         .register();
   }

   public static BlockEntry<MEStorageCoreBlock> createCraftingStorageCore(int tier) {
      String t = "t" + tier;
      String name = t + "_crafting_storage_core";
      return ((BlockBuilder)block(name, "T" + tier + " 合成存储核心", p -> new MEStorageCoreBlock(p, tier))
            .lang("T" + tier + " Crafting Storage Core")
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .addLayer(() -> RenderType::cutoutMipped)
            .blockstate(
               (ctx, prov) -> prov.simpleBlock((Block)ctx.getEntry(), prov.models().cubeAll(name, GTOCore.id("block/casings/crafting_storage_core/" + t)))
            )
            .tag(GTToolType.WRENCH.harvestTags.getFirst())
            .item(BlockItem::new)
            .build())
         .register();
   }

   public static BlockEntry<Block> createTwoLayerCasingBlock(String id, String cn, ResourceLocation textureInner, ResourceLocation textureOuter) {
      return createTwoLayerCasingBlock(id, null, cn, textureInner, textureOuter);
   }

   public static BlockEntry<Block> createTwoLayerCasingBlock(String id, String en, String cn, ResourceLocation textureInner, ResourceLocation textureOuter) {
      return createCasingBlock(
         id,
         en,
         cn,
         (ctx, prov) -> prov.simpleBlock(
            ctx.getEntry(),
            prov.models().withExistingParent(id, GTCEu.id("block/cube_2_layer/all")).texture("bot_all", textureInner).texture("top_all", textureOuter)
         ),
         Block::new,
         () -> Blocks.IRON_BLOCK,
         () -> RenderType::cutoutMipped
      );
   }

   public static GTOMachineBuilder registerMonitor(String id, String cn, Function<MetaMachineBlockEntity, MetaMachine> monitorConstructor) {
      addLang(id, cn);
      MonitorBlockItem.addItem(GTOCore.id(id));
      return (GTOMachineBuilder)new GTOMachineBuilder(
            GTORegistration.GTO,
            id,
            MachineDefinition::createDefinition,
            monitorConstructor,
            MonitorBlock::new,
            MonitorBlockItem::new,
            MetaMachineBlockEntity::createBlockEntity
         )
         .rotationState(RotationState.NON_Y_AXIS)
         .renderer(MonitorRenderer::new)
         .hasTESR(true);
   }

   public static BlockEntry<ActiveBlock> createAcceleratorCoil(String name, String cnName, ResourceLocation texture) {
      return block(name, cnName, ActiveBlock::new)
         .initialProperties(() -> Blocks.IRON_BLOCK)
         .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
         .addLayer(() -> RenderType::cutoutMipped)
         .blockstate(
            (ctx, prov) -> {
               ActiveBlock block = ctx.getEntry();
               ModelFile inactive = prov.models().cubeAll(ctx.getName(), texture);
               ModelFile active = prov.models()
                  .withExistingParent(ctx.getName() + "_active", GTCEu.id("block/cube_2_layer/all"))
                  .texture("bot_all", texture)
                  .texture("top_all", texture.withSuffix("_bloom"));
               prov.getVariantBuilder(block)
                  .partialState()
                  .with(ActiveBlock.ACTIVE, false)
                  .modelForState()
                  .modelFile(inactive)
                  .addModel()
                  .partialState()
                  .with(ActiveBlock.ACTIVE, true)
                  .modelForState()
                  .modelFile(active)
                  .addModel();
            }
         )
         .tag(GTToolType.WRENCH.harvestTags.getFirst(), BlockTags.MINEABLE_WITH_PICKAXE)
         .item(BlockItem::new)
         .model((ctx, prov) -> prov.cubeAll(prov.name(ctx), texture))
         .build()
         .register();
   }
}
