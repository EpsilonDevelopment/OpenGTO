package com.gtolib.utils.register;

import appeng.items.materials.StorageComponentItem;
import com.google.common.collect.ImmutableTable.Builder;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.tag.TagUtil;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.ITagPrefixItem;
import com.gregtechceu.gtceu.api.item.component.FoodStats;
import com.gregtechceu.gtceu.api.item.component.ICustomRenderer;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.item.CoverPlaceBehavior;
import com.gregtechceu.gtceu.common.item.TooltipBehavior;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import com.gto.registrate.Registrate;
import com.gto.registrate.providers.ProviderType;
import com.gto.registrate.util.entry.ItemEntry;
import com.gto.registrate.util.nullness.NonNullBiConsumer;
import com.gto.registrate.util.nullness.NonNullConsumer;
import com.gto.registrate.util.nullness.NonNullFunction;
import com.gtocore.api.data.Algae;
import com.gtocore.api.data.material.GTOMaterialIconSet;
import com.gtocore.common.data.GTOCovers;
import com.gtocore.common.data.GTOEffects;
import com.gtocore.common.item.KineticRotorItem;
import com.gtocore.common.item.misc.MysteriousBoostPotionBehaviour;
import com.gtolib.GTOCore;
import com.gtolib.api.GTOValues;
import com.gtolib.api.data.chemical.material.GTOMaterial;
import com.gtolib.api.registries.GTORegistration;
import com.gtolib.api.registries.ItemBuilder;
import com.gtolib.utils.ColorUtils;
import com.gtolib.utils.TintableModelUtils;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item.Properties;
import org.jetbrains.annotations.NotNull;

public final class ItemRegisterUtils {
   public static final Map<String, String> LANG = GTCEu.isDataGen() ? new O2OOpenCacheHashMap<>() : null;

   private ItemRegisterUtils() {
   }

   @NotNull
   public static <T extends Item> ItemBuilder<T, Registrate> item(String name, String cn, NonNullFunction<Properties, T> factory) {
      if (LANG != null) {
         if (LANG.containsKey(name)) {
            GTOCore.LOGGER.error("Repetitive Key: {}", name);
            throw new IllegalStateException();
         }

         if (LANG.containsValue(cn)) {
            GTOCore.LOGGER.error("Repetitive Value: {}", cn);
            throw new IllegalStateException();
         }

         LANG.put(name, cn);
      }

      return GTORegistration.GTO.item(name, factory);
   }

   @NotNull
   public static ItemBuilder<Item, Registrate> item(String name, String cn) {
      return item(name, cn, Item::new);
   }

   public static void generateMaterialItem(
      GTRegistrate registrate, TagPrefix tagPrefix, Material material, Builder<TagPrefix, Material, ItemEntry<Item>> MATERIAL_ITEMS_BUILDER
   ) {
      MATERIAL_ITEMS_BUILDER.put(
         tagPrefix,
         material,
         registrate.<Item>item(
                     tagPrefix.idPattern().formatted(material.getName()), properties -> tagPrefix.itemConstructor().create(properties, tagPrefix, material)
                  )
                  .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                  .transform(GTItems.unificationItem(tagPrefix, material))
               .properties(p -> {
                  p.stacksTo(tagPrefix.maxStackSize() == 1 ? 1 : 64);
                  ToIntFunction<Material> damageFunc = tagPrefix.getMaxDamageProvider();
                  if (damageFunc != null) {
                     int maxDamage = damageFunc.applyAsInt(material);
                     if (maxDamage > 0) {
                        p.durability(maxDamage);
                     }
                  }

                  if (material instanceof GTOMaterial mat) {
                     Rarity rarity = mat.gtolib$rarity();
                     if (rarity != null) {
                        p.rarity(rarity);
                     }
                  }

                  return p;
               })
               .model(
                  material.getMaterialIconSet() instanceof GTOMaterialIconSet i ? i.getModelProvider().create(tagPrefix, material) : NonNullBiConsumer.noop()
               )
               .color(() -> () -> ITagPrefixItem.tintColor(material))
               .onRegister(GTItems::cauldronInteraction)
            .register()
      );
   }

   public static ItemEntry<StorageComponentItem> registerStorageComponentItem(int tier) {
      return item("cell_component_" + tier + "m", tier + "M ME存储组件", p -> new StorageComponentItem(p, 1048576 * tier)).register();
   }

   public static ItemEntry<ComponentItem> registerTieredCover(int amperage) {
      return item(
            GTValues.VN[14].toLowerCase(Locale.ROOT) + "_" + (amperage == 1 ? "" : amperage + "a_") + "wireless_energy_receive_cover",
            (amperage == 1 ? "" : amperage + "安") + GTValues.VN[14] + "无线能源接收器",
            ComponentItem::create
         )
         .lang(GTValues.VNF[14] + " " + (amperage == 1 ? "" : amperage + "A ") + "Wireless Energy Receive Cover")
         .onRegister(GTItems.attach(new TooltipBehavior(lines -> {
            lines.add(Component.translatable("item.gtmthings.wireless_energy_receive_cover.tooltip.1"));
            lines.add(Component.translatable("item.gtmthings.wireless_energy_receive_cover.tooltip.2"));
            lines.add(Component.translatable("item.gtmthings.wireless_energy_receive_cover.tooltip.3", GTValues.V[14] * amperage));
         }), new CoverPlaceBehavior(amperage == 1 ? GTOCovers.MAX_WIRELESS_ENERGY_RECEIVE : GTOCovers.MAX_WIRELESS_ENERGY_RECEIVE_4A)))
         .register();
   }

   public static <T extends ComponentItem> NonNullConsumer<T> attachRenderer(ICustomRenderer customRenderer) {
      return !GTCEu.isClientSide() ? NonNullConsumer.noop() : item -> item.attachComponents(customRenderer);
   }

   public static ItemEntry<KineticRotorItem> registerRotor(String id, String cn, int durability, int min, int max, int material) {
      return item(id, cn + "动力转子", p -> new KineticRotorItem(p, durability, min, max, material)).register();
   }

   public static ItemEntry<Item>[] registerCircuits(String name, String cn, int[] tiers, Function<Integer, Component> componentFunction) {
      ItemEntry<Item>[] entries = new ItemEntry[GTValues.TIER_COUNT];

      for (int tier : tiers) {
         String id = name + "_" + GTValues.VN[tier].toLowerCase();
         ItemEntry<Item> register = item(id, GTOValues.VOLTAGE_NAMESCN[tier] + cn, Item::new)
            .toolTips(() -> Component.translatable("gtocore.tooltip.item." + name).withStyle(ChatFormatting.GRAY), () -> componentFunction.apply(tier))
            .model(NonNullBiConsumer.noop())
            .lang(GTValues.VOLTAGE_NAMES[tier] + " " + FormattingUtil.toEnglishName(name))
            .tag(CustomTags.CIRCUITS_ARRAY[tier])
            .register();
         entries[tier] = register;
      }

      return entries;
   }

   public static ItemEntry<ComponentItem>[] registerMysteriousBoostDrink() {
      ItemEntry[] entries = new ItemEntry[GTValues.TIER_COUNT];

      for (int tier : GTValues.ALL_TIERS) {
         entries[tier] = item("gto_overseer_coke_" + GTValues.VN[tier].toLowerCase(), "GTO牌 " + GTOValues.VNFR[tier] + " 监工可乐", ComponentItem::create)
            .model(
               (ctx, prov) -> TintableModelUtils.createTintableModel(
                  ctx,
                  prov,
                  "item/skill/normal/normal_border",
                  "item/skill/normal/tier_border",
                  "item/skill/mysterious_boost_medicine/0_bottle",
                  "item/skill/mysterious_boost_medicine/1_face",
                  "item/skill/mysterious_boost_medicine/2_liquid1",
                  "item/skill/mysterious_boost_medicine/3_liquid2",
                  "item/skill/mysterious_boost_medicine/4_liquid3",
                  "item/skill/mysterious_boost_medicine/5_gtologo",
                  "item/skill/mysterious_boost_medicine/6_gtologobase",
                  "item/skill/mysterious_boost_medicine/7_straw"
               )
            )
            .color(() -> () -> (stack, tintIndex) -> {
               if (tintIndex == 1) {
                  float light_factor = 0.3F + (tier + 1) * (0.7F / GTValues.TIER_COUNT);
                  int a = 255;
                  int r = Math.min(255, (int)(97.0F * light_factor));
                  int g = Math.min(255, (int)(252.0F * light_factor));
                  int b = Math.min(255, (int)(6375932.0F * light_factor));
                  return a << 24 | r << 16 | g << 8 | b;
               }

               RandomSource rng = GTValues.RNG;
               int[] stepGradient = ColorUtils.generateStepGradient(rng.nextInt(16777216), rng.nextInt(16777216), 70, 3);

               return switch (tintIndex) {
                  case 4 -> stepGradient[0];
                  case 5 -> stepGradient[1];
                  case 6 -> stepGradient[2];
                  default -> -1;
               };
            })
            .onRegister(GTItems.attach(new MysteriousBoostPotionBehaviour()))
            .onRegister(
               GTItems.attach(
                  new FoodStats(
                     new net.minecraft.world.food.FoodProperties.Builder()
                        .effect(() -> new MobEffectInstance((MobEffect)GTOEffects.MYSTERIOUS_BOOST.get(), 1200 * (5 + tier), tier), 1.0F)
                        .alwaysEat()
                        .nutrition(tier)
                        .saturationMod(tier / 10.0F)
                        .build(),
                     true,
                     null
                  )
               )
            )
            .register();
      }

      return entries;
   }

   public static ItemEntry<Item> registerCircuit(String id, String cn, TagKey<Item> tagKey, Supplier<String> componentSupplier) {
      return item(id, cn, Item::new)
         .toolTips(() -> Component.literal(componentSupplier.get()))
         .model((ctx, prov) -> prov.generated(ctx, GTOCore.id("item/circuit/" + id)))
         .tag(tagKey)
         .model(NonNullBiConsumer.noop())
         .register();
   }

   public static ItemEntry<Item> registerEssence(String id, String cn) {
      return item(id + "_essence", cn + "精华", Item::new)
         .model((ctx, prov) -> prov.generated(ctx, GTOCore.id("item/essence/" + id)))
         .tag(TagUtil.optionalTag(BuiltInRegistries.ITEM, GTOCore.id("vein_essence")))
         .register();
   }

   public static ItemEntry<Item> registerAlgae(Algae algae) {
      String id = algae.en.toLowerCase(Locale.ROOT);
      ItemEntry<Item> i = item(id + "_algae", algae.cnFull, Item::new)
         .model((ctx, prov) -> prov.generated(ctx, GTOCore.id("item/algae/" + id)))
         .tag(TagUtil.optionalTag(BuiltInRegistries.ITEM, GTOCore.id("algae")))
         .register();
      algae.itemSupplier = i;
      return i;
   }

   public static ItemEntry<Item> registerAlgaeFiber(Algae algae) {
      String id = algae.en.toLowerCase(Locale.ROOT);
      return item(id + "_algae_fiber", algae.cnFull + "纤维", Item::new).tag(TagUtil.optionalTag(BuiltInRegistries.ITEM, GTOCore.id("algae_fiber"))).register();
   }

   public static ItemEntry<Item> registerCustomModel(String id, String cn) {
      return item(id, cn, Item::new).model(NonNullBiConsumer.noop()).register();
   }

   public static ItemEntry<Item> register(String id, String cn) {
      return item(id, cn, Item::new).register();
   }

   public static ItemEntry<Item> registerTexture(String id, String cn, String texture) {
      return item(id, cn, Item::new).model((ctx, prov) -> prov.generated(ctx, GTOCore.id("item/" + texture))).register();
   }

   public static ItemEntry<Item> registerLang(String id, String en, String cn) {
      return item(id, cn, Item::new).lang(en).register();
   }

   public static ItemEntry<Item> registerTooltip(String id, String cn, Supplier<Component> componentSupplier) {
      return item(id, cn, Item::new).toolTips(componentSupplier).register();
   }
}
