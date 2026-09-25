package com.gtolib.api.player;

import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.storage.StorageHelper;
import appeng.crafting.CraftingLink;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.common.data.GTDamageTypes;
import com.gto.datasynclib.datastream.codec.ByteStreamCodec;
import com.gto.datasynclib.util.holder.IntHolder;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.item.misc.OrganType;
import com.gtocore.config.GTOConfig;
import com.gtolib.GTOCore;
import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.capability.IHeatContainer;
import com.gtolib.api.capability.IWirelessChargerInteraction;
import com.gtolib.api.data.GTODimensions;
import com.gtolib.api.machine.impl.WirelessChargerMachine;
import com.gtolib.api.misc.PlanetManagement;
import com.gtolib.api.network.NetworkPack;
import com.gtolib.api.player.attribute.PlayerAttributes;
import com.gtolib.api.wireless.ExtendWirelessEnergyContainer;
import com.gtolib.utils.GTOUtils;
import com.gtolib.utils.RegistriesUtils;
import com.gtolib.utils.ServerUtils;
import com.lowdragmc.lowdraglib.LDLib;
import earth.terrarium.adastra.api.systems.GravityApi;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;
import lombok.Generated;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.HoverEvent.Action;
import net.minecraft.network.chat.HoverEvent.ItemStackInfo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

@DataGeneratorScanned
public final class PlayerData implements IWirelessChargerInteraction {
   public static final NetworkPack SHIFT_KEY = NetworkPack.registerC2S(
      "shiftKey", (p, b) -> IEnhancedPlayer.of(p).getPlayerData().getPlayerAttributes().setBooleanCurrent(PlayerAttributes.SHIFT_STATE, b.readBoolean())
   );
   private static final NetworkPack SYNC = NetworkPack.registerS2C(
      "playerDataSync", (args, buf) -> ((PlayerData)args[1]).writeToBuf(buf), (player, buf) -> ((IEnhancedPlayer)player).getPlayerData().syncFromBuf(buf)
   );
   @RegisterLanguage(cn = "负重过大，需要4级脊椎", en = "Too much weight, need 4th spinal cord")
   private static final String WEIGHT = "gtocore.organ.weight";
   public final Object2FloatOpenHashMap<String> floatCache = new Object2FloatOpenHashMap<>();
   public final Reference2IntOpenHashMap<OrganType> organTierCache = new Reference2IntOpenHashMap<>();
   public final List<ItemStack> organItemStacks = new ArrayList<>();
   public final Set<CraftingLink> craftingLinks = new ObjectOpenHashSet<>();
   private List<Double> clientElectricityHistoryCache;
   public final Int2ObjectOpenHashMap<BigInteger> clientElectricityCache = new Int2ObjectOpenHashMap<>();
   public BigInteger electricityCapacityCache = BigInteger.ZERO;
   public BigInteger electricityStorageCache = BigInteger.ZERO;
   private static final SplineInterpolator splineInterpolator = new SplineInterpolator();
   private final MEStorageInfoManager meStorageInfoManager;
   private WirelessChargerMachine netMachineCache;
   private final Player player;
   private final ExtendWirelessEnergyContainer wirelessEnergyContainer;
   private final PlayerAttributes playerAttributes;
   public boolean clientKnownAirSwimmingState;
   public boolean mythicBotKnowledgeState;
   public boolean mythicBotRelicState;

   public PlayerData(Player player) {
      this.player = player;
      this.meStorageInfoManager = new MEStorageInfoManager(this.player);
      this.wirelessEnergyContainer = (ExtendWirelessEnergyContainer)ExtendWirelessEnergyContainer.getOrCreateContainer(player.getUUID());
      this.playerAttributes = new PlayerAttributes(this);
   }

   public boolean canFly() {
      return this.playerAttributes.getBooleanCurrent(PlayerAttributes.CAN_FLY);
   }

   public void writeToBuf(FriendlyByteBuf buf) {
      this.playerAttributes.writeToBuf(buf);
      buf.writeVarInt(this.organItemStacks.size());

      for (ItemStack stack : this.organItemStacks) {
         buf.writeItem(stack);
      }

      boolean needWireless = this.wirelessEnergyContainer.getCapacity() != null && this.wirelessEnergyContainer.getCapacity().compareTo(BigInteger.ZERO) > 0;
      buf.writeBoolean(needWireless);
      if (needWireless) {
         this.writeWireless(buf);
      }

      buf.writeBoolean(this.clientKnownAirSwimmingState);
      if (this.player.getPersistentData().contains("MythicBotanyPlayerInfo", 10)) {
         buf.writeBoolean(this.player.getPersistentData().getCompound("MythicBotanyPlayerInfo").getBoolean("KvasirKnowledge"));
         buf.writeBoolean(this.player.getPersistentData().getCompound("MythicBotanyPlayerInfo").getBoolean("enterAlfheim"));
      } else {
         buf.writeBoolean(false);
         buf.writeBoolean(false);
      }
   }

   private void writeWireless(FriendlyByteBuf buf) {
      buf.writeInt(this.player.getServer().getTickCount());
      ByteStreamCodec.BIG_INTEGER_CODEC.encode(buf, this.wirelessEnergyContainer.getCapacity());
      ByteStreamCodec.BIG_INTEGER_CODEC.encode(buf, this.wirelessEnergyContainer.getStorage());
   }

   public void syncFromBuf(FriendlyByteBuf buf) {
      this.playerAttributes.readFromBuf(buf);
      int size = buf.readVarInt();
      this.organItemStacks.clear();

      for (int i = 0; i < size; i++) {
         this.organItemStacks.add(buf.readItem());
      }

      boolean needWireless = buf.readBoolean();
      if (needWireless) {
         this.readWireless(buf);
      } else {
         this.electricityCapacityCache = BigInteger.ZERO;
         this.electricityStorageCache = BigInteger.ZERO;
         this.clientElectricityCache.clear();
         this.clientElectricityHistoryCache = Collections.emptyList();
      }

      this.clientKnownAirSwimmingState = buf.readBoolean();
      this.mythicBotKnowledgeState = buf.readBoolean();
      this.mythicBotRelicState = buf.readBoolean();
   }

   private void readWireless(FriendlyByteBuf buf) {
      int tick = buf.readInt();
      BigInteger capacityStr = ByteStreamCodec.BIG_INTEGER_CODEC.decode(buf);
      BigInteger storageStr = ByteStreamCodec.BIG_INTEGER_CODEC.decode(buf);
      this.electricityCapacityCache = capacityStr;
      this.electricityStorageCache = storageStr;
      this.clientElectricityCache.put(tick, storageStr);
      this.clientElectricityCache.keySet().removeIf(t -> t < tick - 20 * GTOConfig.INSTANCE.client.hud.wirelessEnergyHUDHistorySeconds);
      int dataPointsMax = GTOConfig.INSTANCE.client.hud.wirelessEnergyHUDHistorySeconds;
      List<Integer> xs = new IntArrayList(dataPointsMax);
      List<Float> ys = new FloatArrayList(dataPointsMax);
      IntHolder minTick = new IntHolder(Integer.MAX_VALUE);
      IntHolder maxTick = new IntHolder(Integer.MIN_VALUE);
      this.clientElectricityCache.int2ObjectEntrySet().stream().sorted(Comparator.comparingInt(Entry::getIntKey)).forEach(entry -> {
         int tickCount = entry.getIntKey();
         BigInteger energy = entry.getValue();
         xs.add(tickCount);
         ys.add(energy.multiply(BigInteger.valueOf(10000L)).divide(this.electricityCapacityCache).floatValue() / 100.0F);
         if (tickCount < minTick.value) {
            minTick.value = tickCount;
         }

         if (tickCount > maxTick.value) {
            maxTick.value = tickCount;
         }
      });
      PolynomialSplineFunction fn = splineInterpolator.interpolate(
         xs.stream().mapToDouble(i -> i.intValue()).toArray(), ys.stream().mapToDouble(f -> f.floatValue()).toArray()
      );
      this.clientElectricityHistoryCache = IntStream.rangeClosed(minTick.value, maxTick.value)
         .mapToDouble(fn::value)
         .map(d -> Mth.clamp(d, 0.0, 100.0))
         .boxed()
         .toList();
   }

   public void setDrift(boolean drift) {
      if (this.player instanceof ServerPlayer serverPlayer) {
         this.playerAttributes.setBooleanCurrent(PlayerAttributes.DISABLE_DRIFT, drift);
         SYNC.send(serverPlayer, this);
      }
   }

   public void readAdditionalSaveData(CompoundTag compound) {
      if (compound.contains("gto_attributes", 7)) {
         this.playerAttributes.load(compound.getByteArray("gto_attributes"));
      } else {
         this.playerAttributes.loadLegacy(compound);
      }

      ListTag organItemStacksTag = compound.getList("organItemStacks", 10);
      this.organItemStacks.clear();

      for (int i = 0; i < organItemStacksTag.size(); i++) {
         this.organItemStacks.add(ItemStack.of(organItemStacksTag.getCompound(i)));
      }

      ListTag craftingLinksTag = compound.getList("craftingLinks", 10);
      this.craftingLinks.clear();

      for (int i = 0; i < craftingLinksTag.size(); i++) {
         CompoundTag n = craftingLinksTag.getCompound(i);
         this.craftingLinks.add((CraftingLink)StorageHelper.loadCraftingLink(n, (ICraftingRequester)this.player));
      }
   }

   public void addAdditionalSaveData(CompoundTag compound) {
      compound.putByteArray("gto_attributes", this.playerAttributes.save());
      ListTag organItemStacksTag = new ListTag();

      for (ItemStack stack : this.organItemStacks) {
         if (!stack.isEmpty()) {
            organItemStacksTag.add(stack.serializeNBT());
         }
      }

      compound.put("organItemStacks", organItemStacksTag);
      ListTag craftingLinksTag = new ListTag();

      for (CraftingLink link : this.craftingLinks) {
         CompoundTag n = new CompoundTag();
         link.writeToNBT(n);
         craftingLinksTag.add(n);
      }

      compound.put("craftingLinks", craftingLinksTag);
   }

   public void onPlayerTick() {
      if (this.player instanceof ServerPlayer serverPlayer) {
         IEnhancedPlayer.onTick(serverPlayer);
         if (serverPlayer.tickCount % 20 == 12) {
            this.playerAttributes.tick(serverPlayer);
            IEnhancedPlayer.organService.tick(serverPlayer);
            this.playerAttributes.applyAll(serverPlayer);
            Level level = this.getLevel();
            MinecraftServer server = level.getServer();
            if (serverPlayer.getFoodData().getFoodLevel() > (GTOCore.difficulty == 1 ? 5 : 15)
               && serverPlayer.getHealth() < serverPlayer.getMaxHealth() - 4.0F
               && serverPlayer.tickCount % 80 == 0
               && serverPlayer.getRandom().nextBoolean()) {
               serverPlayer.heal(Math.max(1, (int)Math.log(serverPlayer.getMaxHealth() * Math.max(1, 4 - GTOCore.difficulty) / 4.0F)));
            }

            boolean hasHotIronIngot = false;
            if (!this.player.isCreative()) {
               List<BlockPos> neighbors = new ArrayList<>();

               for (Direction direction : Direction.values()) {
                  neighbors.add(serverPlayer.getOnPos().above().relative(direction));
               }

               for (BlockPos neighbor : neighbors) {
                  BlockEntity blockEntity = level.getBlockEntity(neighbor);
                  IHeatContainer heat = GTCapabilityHelper.getBlockEntityGTCapability(IHeatContainer.class, blockEntity, null);
                  if (heat != null) {
                     int threshold = 333;
                     if (heat.getTemperature() > threshold) {
                        serverPlayer.hurt(GTDamageTypes.HEAT.source(level), Math.min((float)(heat.getTemperature() - threshold) / 10.0F, 20.0F));
                        this.player.setDeltaMovement(this.player.getDeltaMovement());
                     }
                  }
               }
            }

            long mass = 0L;
            WirelessChargerMachine machine = this.getNetMachine();

            for (ItemStack stack : serverPlayer.getInventory().items) {
               Item item = stack.getItem();
               if (item != Items.AIR) {
                  if (item == GTOItems.HOT_IRON_INGOT.asItem()) {
                     hasHotIronIngot = true;
                  } else {
                     IWirelessChargerInteraction.charge(machine, stack);
                     if (!this.player.isCreative()) {
                        MaterialEntry mat = ChemicalHelper.getMaterialEntry(item);
                        long ma = mat.material().getMass();
                        if (ma > 400L) {
                           mass += ma;
                        }
                     }
                  }
               }
            }

            if (mass > this.playerAttributes.getNumericCurrentInt(PlayerAttributes.MASS_CAP)) {
               this.player.displayClientMessage(Component.translatable("gtocore.organ.weight"), true);
               this.player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 5, false, false));
            }

            for (ItemStack stack : serverPlayer.getInventory().armor) {
               if (!stack.isEmpty()) {
                  IWirelessChargerInteraction.charge(machine, stack);
               }
            }

            if (hasHotIronIngot && !this.player.isCreative()) {
               float heatDamage = 1.373F * this.playerAttributes.getNumericCurrentFloat(PlayerAttributes.HOT_INGOT_RESISTANCE);
               if (heatDamage > 0.0) {
                  serverPlayer.hurt(GTDamageTypes.HEAT.source(level), heatDamage);
               }
            }

            boolean wardenState = this.playerAttributes.getBooleanCurrent(PlayerAttributes.WARDEN_STATE);
            if (!this.player.isCreative() && !this.player.isSpectator()) {
               ResourceKey<Level> dimension = level.dimension();
               if (dimension == GTODimensions.CREATE) {
                  this.discard(server);
                  serverPlayer.displayClientMessage(
                     Component.translatable("gtceu.creative_tooltip.1").append(Component.translatable("gtceu.creative_tooltip.2")), false
                  );
               } else if (dimension != GTODimensions.OTHERSIDE) {
                  if (GTCEu.isProd() && GTODimensions.isPlanet(dimension) && !PlanetManagement.isUnlocked(serverPlayer, dimension)) {
                     serverPlayer.displayClientMessage(Component.translatable("gtocore.ununlocked"), false);
                     this.discard(server);
                  }
               } else if (!wardenState) {
                  this.discard(server);
                  MutableComponent component = Component.empty();
                  String[] wardenArmorIds = new String[]{"warden_helmet", "warden_chestplate", "warden_leggings", "warden_boots"};

                  for (String id : wardenArmorIds) {
                     ItemStack item = RegistriesUtils.getItem("deeperdarker", id).getDefaultInstance();
                     component.append(
                           item.getDisplayName().copy().withStyle(style -> style.withHoverEvent(new HoverEvent(Action.SHOW_ITEM, new ItemStackInfo(item))))
                        )
                        .append(" ");
                  }

                  serverPlayer.displayClientMessage(Component.translatable("gtceu.creative_tooltip.1").append(component), false);
               }
            }

            this.craftingLinks.removeIf(link -> link.isCanceled() || link.isDone());
            SYNC.send(serverPlayer, this);
         }
      }

      if (this.player.tickCount % 20 == 2) {
         this.meStorageInfoManager.updateSeconds();
      }
   }

   @Override
   public BlockPos getPos() {
      return this.player.getOnPos();
   }

   @Override
   public Level getLevel() {
      return this.player.level();
   }

   private void discard(MinecraftServer server) {
      ServerUtils.teleportToDimension(server, this.player, GTODimensions.OVERWORLD, new Vec3(0.0, 100.0, 0.0));
      this.player.kill();
   }

   public UUID getUUID() {
      return this.player.getUUID();
   }

   @Override
   public boolean testMachine(WirelessChargerMachine machine) {
      return machine.isFormed()
         && machine.isWorkingEnabled()
         && machine.getRate() > 0
         && (machine.isInfinite() || GTOUtils.calculateDistance(machine.getPos(), this.getPos()) < machine.getRange());
   }

   public boolean isNoGravity() {
      return LDLib.isRemote()
         ? this.clientKnownAirSwimmingState
         : (
            this.clientKnownAirSwimmingState = this.playerAttributes.getBooleanCurrent(PlayerAttributes.NO_GRAVITY)
               || GravityApi.API.getGravity(this.player) < 0.05F
               || this.player.isNoGravity()
         );
   }

   @Generated
   public List<Double> getClientElectricityHistoryCache() {
      return this.clientElectricityHistoryCache;
   }

   @Generated
   public MEStorageInfoManager getMeStorageInfoManager() {
      return this.meStorageInfoManager;
   }

   @Generated
   public WirelessChargerMachine getNetMachineCache() {
      return this.netMachineCache;
   }

   @Generated
   public void setNetMachineCache(WirelessChargerMachine netMachineCache) {
      this.netMachineCache = netMachineCache;
   }

   @Generated
   public ExtendWirelessEnergyContainer getWirelessEnergyContainer() {
      return this.wirelessEnergyContainer;
   }

   @Generated
   public PlayerAttributes getPlayerAttributes() {
      return this.playerAttributes;
   }
}
