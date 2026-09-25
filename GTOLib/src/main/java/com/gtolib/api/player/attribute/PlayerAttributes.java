package com.gtolib.api.player.attribute;

import com.google.common.collect.ImmutableMap;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;
import com.gregtechceu.gtceu.api.registry.GTRegistry;
import com.gregtechceu.gtceu.api.registry.GTRegistry.Str;
import com.gto.datasynclib.datastream.data.Data;
import com.gto.datasynclib.datastream.data.ListData;
import com.gto.datasynclib.datastream.data.StringMapData;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.item.misc.OrganType;
import com.gtolib.GTOCore;
import com.gtolib.api.lang.CNEN;
import com.gtolib.api.network.NetworkPack;
import com.gtolib.api.player.IEnhancedPlayer;
import com.gtolib.api.player.PlayerData;
import dev.shadowsoffire.attributeslib.api.ALObjects.Attributes;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.Objects;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeMod;
import org.jetbrains.annotations.Nullable;

public final class PlayerAttributes {
   public static final Str<AttributeDefinition<?, ?>> REGISTRY = new Str<>(GTOCore.id("player_attribute"), false);
   public static final ImmutableMap<AttributeDefinition<?, ?>, CNEN> NAMES;
   public static final BooleanAttribute CAN_FLY = REGISTRY.register("canFly", new BooleanAttribute("canFly", false, false, PlayerAttributes::applyCanFly));
   public static final FloatAttribute FLY_SPEED_ABLE = REGISTRY.register(
      "flySpeedAble",
      new FloatAttribute(
         "flySpeedAble",
         false,
         0.0F,
         Float.MAX_VALUE,
         0.0F,
         (plr, attrs, val) -> attrs.getNumeric(PlayerAttributes.FLY_SPEED).setRange(0.0F, val.getCurrentFloat())
      )
   );
   public static final FloatAttribute FLY_SPEED = REGISTRY.register(
      "flySpeed", new FloatAttribute("flySpeed", true, 0.0F, 0.0F, 0.0F, PlayerAttributes::applyFlightSpeed)
   );
   public static final FloatAttribute SPEED = REGISTRY.register(
      "speed", new FloatAttribute("speed", true, 0.0F, 0.0F, 0.0F, PlayerAttributes::applyMovementSpeed)
   );
   public static final FloatAttribute SPEED_ABLE = REGISTRY.register(
      "speedAble",
      new FloatAttribute("speedAble", false, 0.0F, Float.MAX_VALUE, 0.0F, (plr, attrs, val) -> attrs.getNumeric(SPEED).setRange(0.0F, val.getCurrentFloat()))
   );
   public static final FloatAttribute BLOCK_REACH = REGISTRY.register(
      "blockReach", new FloatAttribute("blockReach", false, 0.0F, Float.MAX_VALUE, 0.0F, PlayerAttributes::applyBlockReach)
   );
   public static final FloatAttribute ARMOR = REGISTRY.register(
      "armor", new FloatAttribute("armor", false, 0.0F, Float.MAX_VALUE, 0.0F, PlayerAttributes::applyArmor)
   );
   public static final FloatAttribute ARMOR_TOUGHNESS = REGISTRY.register(
      "armorToughness", new FloatAttribute("armorToughness", false, 0.0F, Float.MAX_VALUE, 0.0F, PlayerAttributes::applyArmorToughness)
   );
   public static final BooleanAttribute SHIFT_STATE = REGISTRY.register("shiftState", new BooleanAttribute("shiftState", false, false));
   public static final BooleanAttribute NO_GRAVITY = REGISTRY.register("noGravity", new BooleanAttribute("noGravity", false, false));
   public static final BooleanAttribute DISABLE_DRIFT = REGISTRY.register("disableDrift", new BooleanAttribute("disableDrift", true, false));
   public static final BooleanAttribute SPACE_STATE = REGISTRY.register("spaceState", new BooleanAttribute("spaceState", false, false));
   public static final BooleanAttribute WARDEN_STATE = REGISTRY.register("wardenState", new BooleanAttribute("wardenState", false, false));
   public static final BooleanAttribute WING_STATE = REGISTRY.register("wingState", new BooleanAttribute("wingState", false, false));
   public static final BooleanAttribute CMD_STATE = REGISTRY.register("cmdState", new BooleanAttribute("cmdState", false, false));
   public static final BooleanAttribute FREE_MOV_STATE = REGISTRY.register(
      "freeMovState", new BooleanAttribute("freeMovState", false, false, (plr, attrs, val) -> {
         if (!val.isAvailable()) {
            val.setCurrent(false);
         }
      })
   );
   public static final BooleanAttribute NIGHT_VISION = REGISTRY.register("nightvision", new BooleanAttribute("nightvision", true, false, (plr, attrs, val) -> {
      if (val.getCurrent()) {
         plr.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, true, false));
      }
   }));
   public static final IntAttribute MASS_CAP = REGISTRY.register("massCap", new IntAttribute("massCap", false, 0.0F, 2.1474836E9F, 2.1474836E9F));
   public static final FloatAttribute HOT_INGOT_RESISTANCE = REGISTRY.register(
      "hotIngotResistance", new FloatAttribute("hotIngotResistance", false, 0.0F, Float.MAX_VALUE, 1.0F)
   );
   private final PlayerData playerData;
   private final Reference2ReferenceOpenHashMap<AttributeDefinition<?, ?>, AttributeValue<?, ?>> attributes = new Reference2ReferenceOpenHashMap<>();
   private final ReferenceOpenHashSet<AttributeDefinition<?, ?>> dirtyAttributes = new ReferenceOpenHashSet<>();
   private static final NetworkPack TO_SERVER_PACK;

   public static void init() {
   }

   public PlayerAttributes(PlayerData playerData) {
      this.playerData = playerData;
      REGISTRY.forEach(
         attribute -> this.attributes
            .put((AttributeDefinition<?, ?>)attribute, attribute.createValue(() -> this.markDirty((AttributeDefinition<?, ?>)attribute)))
      );
   }

   public static void syncToServer(Player p, AttributeDefinition<?, ?> attribute) {
      PlayerAttributes attributes = IEnhancedPlayer.of(p).getPlayerData().getPlayerAttributes();
      TO_SERVER_PACK.send(buf -> {
         REGISTRY.streamCodec().encode(buf, attribute);
         attributes.get(attribute).write(buf);
      }, p);
   }

   public <T extends AttributeValue<T, D>, D extends AttributeDefinition<T, D>> T get(AttributeDefinition<T, D> definition) {
      return (T)this.attributes.get(definition);
   }

   @Nullable
   public static <T extends AttributeValue<T, D>, D extends AttributeDefinition<T, D>> AttributeDefinition<T, D> query(String name) {
      return (AttributeDefinition<T, D>)REGISTRY.get(name);
   }

   public BooleanValue getBoolean(BooleanAttribute attribute) {
      BooleanValue value = this.get(attribute);
      if (value == null) {
         throw new IllegalStateException("Unknown boolean player attribute: " + attribute.name);
      } else {
         return value;
      }
   }

   public boolean getBooleanCurrent(BooleanAttribute attribute) {
      return this.getBoolean(attribute).getCurrent();
   }

   public void setBooleanAvailable(BooleanAttribute attribute, boolean available) {
      this.getBoolean(attribute).setAvailable(available);
   }

   public void setBooleanCurrent(BooleanAttribute attribute, boolean current) {
      this.getBoolean(attribute).setCurrent(current);
   }

   public void orBooleanCurrent(BooleanAttribute attribute, boolean current) {
      if (current) {
         this.getBoolean(attribute).setCurrent(true);
      }
   }

   public NumericValue getNumeric(NumericAttribute<?> attribute) {
      NumericValue value = this.get(attribute);
      if (value == null) {
         throw new IllegalStateException("Unknown numeric player attribute: " + attribute.name);
      } else {
         return value;
      }
   }

   public float getNumericCurrentFloat(NumericAttribute<?> attribute) {
      return this.getNumeric(attribute).getCurrentFloat();
   }

   public int getNumericCurrentInt(NumericAttribute<?> attribute) {
      return this.getNumeric(attribute).getCurrentInt();
   }

   public void setNumericCurrent(NumericAttribute<?> attribute, float current) {
      this.getNumeric(attribute).setCurrent(current);
   }

   public void addNumericCurrent(NumericAttribute<?> attribute, float delta) {
      if (delta != 0.0F) {
         NumericValue value = this.getNumeric(attribute);
         value.setCurrent(value.getCurrentFloat() + delta);
      }
   }

   public void tick(ServerPlayer serverPlayer) {
      this.resetAccumulatedNumeric(SPEED_ABLE);
      this.resetAccumulatedNumeric(BLOCK_REACH);
      this.resetAccumulatedNumeric(ARMOR);
      this.resetAccumulatedNumeric(ARMOR_TOUGHNESS);
      this.setBooleanAvailable(FREE_MOV_STATE, false);
      this.setBooleanCurrent(CAN_FLY, false);
      boolean noGravity = false;

      for (ItemStack stack : serverPlayer.getInventory().items) {
         if (!stack.isEmpty()) {
            MaterialEntry materialEntry = ChemicalHelper.getMaterialEntry(stack.getItem());
            if (materialEntry.material() == GTOMaterials.Amprosium) {
               noGravity = true;
               break;
            }
         }
      }

      this.setBooleanCurrent(NO_GRAVITY, noGravity);
      boolean wardenState = !serverPlayer.gameMode.isSurvival()
         || Objects.equals(serverPlayer.getArmorSlots().toString(), "[1 warden_boots, 1 warden_leggings, 1 warden_chestplate, 1 warden_helmet]");
      this.setBooleanCurrent(WARDEN_STATE, wardenState);
      this.setBooleanAvailable(FREE_MOV_STATE, wardenState);
      this.setBooleanCurrent(WING_STATE, false);
      NumericValue massCap = this.getNumeric(MASS_CAP);
      boolean limitedByMass = this.playerData.organTierCache.getInt(OrganType.Spine) < 4;
      if (limitedByMass) {
         massCap.setCurrent(10000.0F);
      } else {
         massCap.setCurrent(2.1474836E9F);
      }

      float hotIngotResistance = 1.0F;
      ItemStack chestArmor = serverPlayer.getItemBySlot(EquipmentSlot.CHEST);
      if (!chestArmor.isEmpty() && chestArmor.getItem() instanceof ArmorComponentItem armorItem) {
         hotIngotResistance = armorItem.getArmorLogic().getHeatResistance();
      }

      NumericValue resistance = this.getNumeric(HOT_INGOT_RESISTANCE);
      resistance.setCurrent(hotIngotResistance);
      NumericValue flySpeed = this.getNumeric(FLY_SPEED_ABLE);
      float baseMax = wardenState ? 0.3F : flySpeed.getCurrentFloat();
      flySpeed.setCurrent(baseMax);
   }

   public void applyAll(ServerPlayer serverPlayer) {
      REGISTRY.forEach(attribute -> this.apply(serverPlayer, (AttributeDefinition<?, ?>)attribute));
   }

   public void apply(ServerPlayer serverPlayer, AttributeDefinition<?, ?> attribute) {
      attribute.apply(serverPlayer, this);
   }

   public void markDirty(AttributeDefinition<?, ?> attribute) {
      this.dirtyAttributes.add(attribute);
   }

   private void resetAccumulatedNumeric(NumericAttribute<?> attribute) {
      NumericValue value = this.getNumeric(attribute);
      value.setRange(0.0F, Float.MAX_VALUE);
      value.setCurrent(0.0F);
   }

   private static void applyCanFly(ServerPlayer serverPlayer, PlayerAttributes playerAttributes, BooleanValue value) {
      boolean canFly = value.getCurrent();
      applyAdditiveModifier(serverPlayer.getAttribute(Attributes.CREATIVE_FLIGHT.get()), "gtocore:fly", canFly ? 1.0 : 0.0);
      playerAttributes.getNumeric(FLY_SPEED).setAvailable(canFly);
   }

   private static void applyFlightSpeed(ServerPlayer serverPlayer, PlayerAttributes playerAttributes, NumericValue value) {
      float currentValue = value.getCurrentFloat();
      float flyingSpeed = Mth.clamp(currentValue, 0.0F, playerAttributes.getNumericCurrentFloat(FLY_SPEED_ABLE));
      if (Float.compare(serverPlayer.getAbilities().getFlyingSpeed(), flyingSpeed + 0.1F) != 0) {
         serverPlayer.getAbilities().setFlyingSpeed(flyingSpeed + 0.1F);
         serverPlayer.onUpdateAbilities();
      }

      if (currentValue <= 0.0F) {
         serverPlayer.getPersistentData().remove("fly_speed");
      }
   }

   private static void applyMovementSpeed(ServerPlayer serverPlayer, PlayerAttributes playerAttributes, NumericValue value) {
      float speed = Mth.clamp(value.getCurrentFloat(), 0.0F, playerAttributes.getNumericCurrentFloat(SPEED_ABLE));
      AttributeInstance attribute = serverPlayer.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
      removeLegacyTierModifiers(attribute, "gtocore:organ_speed_tier_", 0, 4);
      applyAdditiveModifier(attribute, "gtolib:player_attribute_speed", speed);
   }

   private static void applyBlockReach(ServerPlayer serverPlayer, PlayerAttributes playerAttributes, NumericValue value) {
      applyAdditiveModifier(serverPlayer.getAttribute(ForgeMod.BLOCK_REACH.get()), "gtocore:organ_reach", value.getCurrentFloat());
   }

   private static void applyArmor(ServerPlayer serverPlayer, PlayerAttributes playerAttributes, NumericValue value) {
      AttributeInstance attribute = serverPlayer.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR);
      removeLegacyTierModifiers(attribute, "gtocore:organ_armor_tier_", 1, 4);
      applyAdditiveModifier(attribute, "gtolib:player_attribute_armor", value.getCurrentFloat());
   }

   private static void applyArmorToughness(ServerPlayer serverPlayer, PlayerAttributes playerAttributes, NumericValue value) {
      AttributeInstance attribute = serverPlayer.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS);
      removeLegacyTierModifiers(attribute, "gtocore:organ_toughness_tier_", 1, 4);
      applyAdditiveModifier(attribute, "gtolib:player_attribute_armor_toughness", value.getCurrentFloat());
   }

   private static void applyAdditiveModifier(@Nullable AttributeInstance attribute, String modifierName, double amount) {
      if (attribute != null) {
         UUID modifierUUID = UUID.nameUUIDFromBytes(modifierName.getBytes());
         if (amount > 0.0) {
            if (attribute.getModifier(modifierUUID) == null) {
               attribute.addPermanentModifier(new AttributeModifier(modifierUUID, modifierName, amount, Operation.ADDITION));
            }
         } else {
            attribute.removeModifier(modifierUUID);
         }
      }
   }

   private static void removeLegacyTierModifiers(@Nullable AttributeInstance attribute, String baseModifierName, int minTier, int maxTier) {
      if (attribute != null) {
         for (int tier = minTier; tier <= maxTier; tier++) {
            attribute.removeModifier(UUID.nameUUIDFromBytes((baseModifierName + tier).getBytes()));
         }
      }
   }

   public void writeToBuf(FriendlyByteBuf buf) {
      buf.writeVarInt(this.dirtyAttributes.size());
      this.dirtyAttributes.forEach(attribute -> {
         REGISTRY.streamCodec().encode(buf, (AttributeDefinition<?, ?>)attribute);
         this.get((AttributeDefinition<?, ?>)attribute).write(buf);
      });
      this.dirtyAttributes.clear();
   }

   public void readFromBuf(FriendlyByteBuf buf) {
      int size = buf.readVarInt();

      for (int i = 0; i < size; i++) {
         this.get(REGISTRY.streamCodec().decode(buf)).read(buf);
      }
   }

   public byte[] save() {
      StringMapData root = new StringMapData();
      root.putInt("d_v", 1);
      REGISTRY.forEach(attribute -> {
         ListData tag = new ListData();
         this.get((AttributeDefinition<?, ?>)attribute).write(tag);
         root.put(attribute.name, tag);
      });
      return root.writeToBytes();
   }

   public void load(byte[] bytes) {
      this.resetToDefaults();
      Data root = Data.readData(bytes);
      int dataVersion = root.asStringMapData().getInt("d_v");

      for (Entry<String, Data> key : root.getStringMap().entrySet()) {
         AttributeDefinition<? extends AttributeValue<?, ? extends AttributeDefinition<?, ?>>, ? extends AttributeDefinition<? extends AttributeValue<?, ?>, ?>> attribute = query(
            key.getKey()
         );
         if (attribute != null) {
            this.get(attribute).read(key.getValue().asListData(), dataVersion);
         }
      }
   }

   public void loadLegacy(CompoundTag root) {
      this.resetToDefaults();
      this.setBooleanCurrent(SHIFT_STATE, root.getBoolean("shiftState"));
      this.setBooleanCurrent(NO_GRAVITY, root.getBoolean("noGravity"));
      this.setBooleanCurrent(DISABLE_DRIFT, root.getBoolean("disableDrift"));
      this.setBooleanCurrent(SPACE_STATE, root.getBoolean("spaceState"));
      this.setBooleanCurrent(WARDEN_STATE, root.getBoolean("wardenState"));
      this.setBooleanCurrent(WING_STATE, root.getBoolean("wingState"));
      this.setBooleanCurrent(CAN_FLY, this.getBooleanCurrent(WARDEN_STATE) || this.getBooleanCurrent(WING_STATE));
      this.setBooleanCurrent(CMD_STATE, root.getBoolean("cmdState"));
      this.setBooleanCurrent(FREE_MOV_STATE, root.getBoolean("freeMovState"));
      this.setBooleanCurrent(NIGHT_VISION, root.getBoolean("night_vision"));
   }

   public void copyFrom(PlayerAttributes other) {
      REGISTRY.forEach(attribute -> this.get((AttributeDefinition)attribute).copyFrom(other.get((AttributeDefinition)attribute)));
   }

   private void resetToDefaults() {
      REGISTRY.forEach(attribute -> this.get((AttributeDefinition<?, ?>)attribute).reset());
   }

   static {
      GTRegistry.REGISTERED.putIfAbsent(REGISTRY.getRegistryName(), REGISTRY);
      REGISTRY.unfreeze();
      REGISTRY.freeze();
      if (GTCEu.isDataGen()) {
         NAMES = ImmutableMap.<AttributeDefinition<?, ?>, CNEN>builder()
            .put(CAN_FLY, new CNEN("飞行能力", "Fly Ability"))
            .put(FLY_SPEED, new CNEN("飞行速度", "Fly Speed"))
            .put(FLY_SPEED_ABLE, new CNEN("飞行速度上限", "Fly Speed Cap"))
            .put(SPEED, new CNEN("移动速度", "Movement Speed"))
            .put(SPEED_ABLE, new CNEN("移动速度上限", "Movement Speed Cap"))
            .put(BLOCK_REACH, new CNEN("方块交互范围", "Block Reach"))
            .put(ARMOR, new CNEN("护甲值", "Armor"))
            .put(ARMOR_TOUGHNESS, new CNEN("护甲韧性", "Armor Toughness"))
            .put(SHIFT_STATE, new CNEN("下蹲状态", "Shift State"))
            .put(NO_GRAVITY, new CNEN("无重状态", "No Gravity"))
            .put(DISABLE_DRIFT, new CNEN("禁用漂移", "Disable Drift"))
            .put(SPACE_STATE, new CNEN("太空状态", "Space State"))
            .put(WARDEN_STATE, new CNEN("守卫状态", "Warden State"))
            .put(WING_STATE, new CNEN("翅膀状态", "Wing State"))
            .put(CMD_STATE, new CNEN("指令状态", "CMD State"))
            .put(FREE_MOV_STATE, new CNEN("飞行穿墙", "Free Movement State"))
            .put(NIGHT_VISION, new CNEN("夜视", "Night Vision"))
            .put(MASS_CAP, new CNEN("承重上限", "Mass Cap"))
            .put(HOT_INGOT_RESISTANCE, new CNEN("热锭抗性", "Hot Ingot Resistance"))
            .build();
      } else {
         NAMES = null;
      }

      TO_SERVER_PACK = NetworkPack.registerC2S("player_attributes_sync_to_server", (sp, buf) -> {
         AttributeDefinition<?, ?> attribute = REGISTRY.streamCodec().decode(buf);
         IEnhancedPlayer.of(sp).getPlayerData().getPlayerAttributes().get(attribute).read(buf);
      });
   }
}
