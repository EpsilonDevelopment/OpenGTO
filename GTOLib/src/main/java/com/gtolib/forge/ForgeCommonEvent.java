package com.gtolib.forge;

import appeng.core.settings.TickRates;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.gregtechceu.gtceu.utils.TaskHandler;
import com.gtocore.api.research.ExResearchManager;
import com.gtocore.common.item.DataStickExtension;
import com.gtocore.config.GTOConfig;
import com.gtocore.integration.jade.provider.AEGridProvider;
import com.gtocore.utils.OrganUtilsKt;
import com.gtolib.Client;
import com.gtolib.GTOCore;
import com.gtolib.MixinConfigPlugin;
import com.gtolib.ae2.crafting2.utils.PerfLogger;
import com.gtolib.api.ae2.wtlib.WUTHandlerExtended;
import com.gtolib.api.annotation.dynamic.DynamicInitialData;
import com.gtolib.api.misc.FastSavedData;
import com.gtolib.api.network.NetworkPack;
import com.gtolib.api.player.IEnhancedPlayer;
import com.gtolib.api.player.PlayerData;
import com.gtolib.api.player.attribute.PlayerAttributes;
import com.gtolib.api.recipe.RecipeType;
import com.gtolib.api.wireless.ReceiverTransmitterHandler;
import com.gtolib.cache.CacheManager;
import com.gtolib.cache.resourcestree.node.SaveableDirectoryNode;
import com.gtolib.data.CellSavaedData;
import com.gtolib.data.CommonSavaedData;
import com.gtolib.data.ExtendWirelessEnergySavaedData;
import com.gtolib.data.WirelessManaSavaedData;
import com.gtolib.gtm.AdvancedTerminalBehavior;
import com.gtolib.gtm.RecipeScript;
import com.gtolib.mc.IExtendedLevelSetting;
import com.gtolib.mc.ILevel;
import com.gtolib.mixin.BookContentResourceListenerLoaderAccessor;
import com.gtolib.mixin.forge.NetworkConstantsAccessor;
import com.gtolib.network.EmiNetwork;
import com.gtolib.utils.FileUtils;
import com.gtolib.utils.GTOUtils;
import com.gtolib.utils.PlayerUtils;
import com.gtolib.utils.RegistriesUtils;
import com.gtolib.utils.ServerUtils;
import com.gtolib.utils.SrmManager;
import com.gtolib.utils.reflect.FieldReference;
import com.hepdd.gtmthings.api.misc.WirelessEnergyContainer;
import com.hepdd.gtmthings.common.item.WirelessEnergyBindingToolBehavior;
import com.hepdd.gtmthings.data.CustomItems;
import com.hepdd.gtmthings.data.WirelessEnergySavaedData;
import com.mojang.brigadier.ParseResults;
import dev.emi.emi.runtime.EmiPersistentData;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntSupplier;
import java.util.stream.Collectors;
import jeresources.config.Settings;
import lombok.Generated;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.Clone;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.level.LevelEvent.Load;
import net.minecraftforge.event.level.LevelEvent.Unload;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.ListenerList;
import net.minecraftforge.eventbus.api.EventListenerHelper;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.moddiscovery.ExplodedDirectoryLocator;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import net.minecraftforge.fml.loading.moddiscovery.ExplodedDirectoryLocator.ExplodedMod;
import net.minecraftforge.forgespi.locating.IModLocator;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.ConnectionData.ModMismatchData;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.DataPackRegistryEvent.NewRegistry;
import org.apache.commons.lang3.tuple.Pair;
import vazkii.patchouli.client.book.BookContentResourceListenerLoader;
import vazkii.patchouli.client.book.ClientBookRegistry;

public final class ForgeCommonEvent {
   private static Throwable throwable;
   public static long tickCount;
   private static int tempDifficulty = GTOCore.difficulty;
   private static boolean wirelessTick;
   public static boolean isSrmMode = false;
   private static final NetworkPack LOGGED_IN = NetworkPack.registerS2C(
      "loginInitialize", (p, b) -> b.writeUUID(ServerUtils.getServerIdentifier()), (p, b) -> {
         Client.UNLOCKED_PLANET.clear();
         if (GTUtil.getClientLevel() != null && !Client.initializedBook) {
            Client.initializedBook = true;
            GTOUtils.asyncExecute(() -> {
               ClientBookRegistry.INSTANCE.reload();
               ((BookContentResourceListenerLoaderAccessor)BookContentResourceListenerLoader.INSTANCE).getData().clear();
            });
         }

         Client.SERVER_IDENTIFIER = b.readUUID();
         if (!GTOConfig.INSTANCE.misc.emiGlobalFavorites) {
            EmiPersistentData.load();
         }

         CacheManager.clearCache();
      }
   );

   public static void syncCommonConfig() {
      TickRates.Interface.setMin(20);
      TickRates.ImportBus.setMin(20);
      TickRates.ImportBus.setMax(80);
      TickRates.ExportBus.setMin(20);
      TickRates.ExportBus.setMax(80);
      TickRates.AnnihilationPlane.setMin(20);
      TickRates.METunnel.setMin(10);
      TickRates.METunnel.setMax(40);
      TickRates.IOPort.setMin(5);
      TickRates.IOPort.setMax(40);
      TickRates.StorageBus.setMin(20);
      TickRates.LightTunnel.setMin(20);
   }

   public static void init() {
      IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
      eventBus.addListener(EventPriority.HIGHEST, ForgeCommonEvent::commonSetup);
      MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, ForgeCommonEvent::onServerTickEvent);
      MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, ForgeCommonEvent::onServerAboutToStartEvent);
      MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, ForgeCommonEvent::onServerStarted);
      MinecraftForge.EVENT_BUS.addListener(ForgeCommonEvent::onLevelLoad);
      MinecraftForge.EVENT_BUS.addListener(ForgeCommonEvent::onLevelUnload);
      MinecraftForge.EVENT_BUS.addListener(ForgeCommonEvent::onServerStopping);
      MinecraftForge.EVENT_BUS.addListener(ForgeCommonEvent::onServerStoppedEvent);
      MinecraftForge.EVENT_BUS.addListener(ForgeCommonEvent::onClone);
      MinecraftForge.EVENT_BUS.addListener(ForgeCommonEvent::onPlayerLogin);
      MinecraftForge.EVENT_BUS.addListener(ForgeCommonEvent::onPlayerDimensionChange);
      MinecraftForge.EVENT_BUS.addListener(ForgeCommonEvent::cmdExecute);
      initHandshake();
      GTOUtils.asyncExecute(() -> {
         File folder = GTOCore.getFile("cache");
         File[] files = folder.listFiles();
         if (files != null) {
            Set<String> mods = ModList.get().getModFiles().stream().map(m -> m.getFile().getFileName() + ".bin").collect(Collectors.toUnmodifiableSet());

            for (File file : files) {
               String fileName = file.getName();
               if (fileName.endsWith(".bin") && !mods.contains(fileName)) {
                  if (file.delete()) {
                     GTOCore.LOGGER.info("Deleted file: {}", file.getName());
                  } else {
                     GTOCore.LOGGER.warn("Failed to delete file: {}", file.getName());
                  }
               }
            }
         }
      });
   }

   public static void clear() {
      GTOUtils.asyncExecute(
         () -> {
            try {
               CacheManager.save();
               boolean destroy = SaveableDirectoryNode.paths != 2 || PerfLogger.Companion.getDestroy();
               ModFileInfo gtm = FMLLoader.getLoadingModList().getModFileById("gtceu");
               if (FMLLoader.isProduction() && !gtm.getFile().getFilePath().toString().isBlank()) {
                  destroy = true;
               }

               FieldReference<List<ListenerList>> allListsField = FieldReference.fromClass(ListenerList.class, "allLists");
               List<ListenerList> allLists = allListsField.get();
               FieldReference<Map<Class<?>, ListenerList>> mapField = FieldReference.fromInstance(
                  FieldReference.fromClass(EventListenerHelper.class, "listeners").get(), "map"
               );
               Map<Class<?>, ListenerList> map = mapField.get();
               FieldReference<List<IModLocator>> modLocatorListField = FieldReference.fromInstance(
                  FieldReference.fromClass(FMLLoader.class, "modDiscoverer").get(), "modLocatorList"
               );

               label56:
               for (IModLocator locator : modLocatorListField.get()) {
                  if (locator instanceof ExplodedDirectoryLocator directoryLocator) {
                     FieldReference<List<ExplodedMod>> explodedModsField = FieldReference.fromInstance(directoryLocator, "explodedMods");

                     for (ExplodedMod explodedMod : explodedModsField.get()) {
                        if (explodedMod.modid().equals("gtocore")) {
                           destroy = true;
                           break label56;
                        }
                     }
                  }
               }

               if (destroy) {
                  map.replaceAll((k, v) -> new ListenerList());
                  allLists.clear();
               } else {
                  for (Object o : new Object[]{
                     FMLClientSetupEvent.class,
                     FMLCommonSetupEvent.class,
                     FMLConstructModEvent.class,
                     FMLLoadCompleteEvent.class,
                     RegisterKeyMappingsEvent.class,
                     NewRegistry.class,
                     NewRegistryEvent.class,
                     RegisterEvent.class
                  }) {
                     ListenerList list = map.remove(o);
                     if (list != null) {
                        allLists.remove(list);
                     }
                  }
               }
            } catch (Throwable e) {
               throwable = e;
            }
         }
      );
   }

   public static void commonSetup(FMLCommonSetupEvent event) {
      RegistriesUtils.IDCache = true;
      if (GTCEu.isModLoaded("jei")) {
         Settings.disableLootManagerReloading = true;
         Settings.useDIYdata = false;
      }

      ((ComponentItem)CustomItems.ADVANCED_TERMINAL.get()).getComponents().clear();
      ((ComponentItem)CustomItems.ADVANCED_TERMINAL.get()).getComponents().add(new AdvancedTerminalBehavior());
      GTItems.TOOL_DATA_STICK.get().attachComponents(DataStickExtension.INSTANCE);
      EmiNetwork.init();
      WUTHandlerExtended.init();
   }

   private static void onServerTickEvent(ServerTickEvent event) {
      if (event.phase == Phase.START) {
         WirelessEnergyContainer.observed = false;
         AEGridProvider.OBSERVE = false;
         tickCount++;
      } else if (wirelessTick) {
         int tick = event.getServer().getTickCount();
         if (tick % 20 == 0) {
            boolean refreshBinding = tick % 200 == 0;

            for (WirelessEnergyContainer container : WirelessEnergySavaedData.INSTANCE.containerMap.values()) {
               if (refreshBinding) {
                  long rate = 0L;
                  GlobalPos pos = container.getBindPos();
                  if (pos != null) {
                     ServerLevel level = event.getServer().getLevel(pos.dimension());
                     if (level != null) {
                        if (!level.isLoaded(pos.pos())) {
                           continue;
                        }

                        rate = WirelessEnergyBindingToolBehavior.getRate(level, pos.pos());
                     }
                  }

                  container.setRate(rate);
               }

               container.getEnergyStat().tick();
            }
         }
      }
   }

   private static void onServerAboutToStartEvent(ServerAboutToStartEvent event) {
      Thread.currentThread().setPriority(10);
      WirelessEnergyContainer.server = event.getServer();
      RecipeType.initSearch();
   }

   private static void onServerStarted(ServerStartedEvent event) {
      CacheManager.clearCache();
   }

   private static void onLevelLoad(Load event) {
      if (event.getLevel() instanceof ServerLevel l && l.getServer().getLevel(Level.OVERWORLD) instanceof ServerLevel level) {
         if (throwable != null) {
            try {
               throw throwable;
            } catch (Throwable e) {
               throw new RuntimeException(throwable);
            }
         }

         DimensionDataStorage dataStorage = level.getDataStorage();
         CommonSavaedData.INSTANCE = FastSavedData.getFromFile("common_data", dataStorage, CommonSavaedData::read);
         if (CommonSavaedData.INSTANCE == null) {
            CommonSavaedData.INSTANCE = dataStorage.computeIfAbsent(CommonSavaedData::new, CommonSavaedData::new, "common_data");
         }

         if (ServerUtils.getPersistentData().getBoolean("srm")) {
            isSrmMode = true;
         } else if (GTOConfig.INSTANCE.gamePlay.selfRestraint) {
            ServerUtils.getPersistentData().putBoolean("srm", true);
            CommonSavaedData.INSTANCE.setDirty();
            isSrmMode = true;
         }

         if (GTOConfig.INSTANCE.devMode.dev && !ServerUtils.getPersistentData().getBoolean("dev")) {
            ServerUtils.getPersistentData().putBoolean("dev", true);
            CommonSavaedData.INSTANCE.setDirty();
         }

         int difficulty = ServerUtils.getPersistentData().getInt("difficulty");
         if (difficulty == 0) {
            difficulty = GTOCore.difficulty;
            ServerUtils.getPersistentData().putInt("difficulty", difficulty);
            CommonSavaedData.INSTANCE.setDirty();
         } else if (difficulty != GTOCore.difficulty) {
            if (!GTCEu.isClientSide()) {
               throw new RuntimeException("Current difficulty: " + GTOCore.difficulty + " | World difficulty: " + difficulty);
            }

            GTOCore.LOGGER.error("Current difficulty: {} | World difficulty: {}", GTOCore.difficulty, difficulty);
            tempDifficulty = difficulty;
         }

         if (level.getLevelData() instanceof PrimaryLevelData primaryLevelData) {
            IExtendedLevelSetting s = (IExtendedLevelSetting)(Object)primaryLevelData.getLevelSettings();
            s.gto$setGTODifficulty(difficulty);
            s.gto$setDevMode(ServerUtils.getPersistentData().getBoolean("dev"));
            s.gto$setSrm(ServerUtils.getPersistentData().getBoolean("srm"));
         }

         CellSavaedData.INSTANCE = FastSavedData.getFromFile("storage_cell_data", dataStorage, CellSavaedData::read);
         if (CellSavaedData.INSTANCE == null) {
            try {
               CellSavaedData.INSTANCE = dataStorage.computeIfAbsent(CellSavaedData::new, CellSavaedData::new, "storage_cell_data");
            } catch (Exception e) {
               CellSavaedData.INSTANCE = new CellSavaedData();
            }

            FileUtils.saveToFile(CellSavaedData.INSTANCE, dataStorage.getDataFile("storage_cell_data"), (stream, obj) -> obj.save(stream));
         }

         CellSavaedData.INSTANCE.setFile(new File(dataStorage.dataFolder, "storage_data"), dataStorage.getDataFile("storage_cell_data"));
         WirelessEnergySavaedData.INSTANCE = dataStorage.computeIfAbsent(
            ExtendWirelessEnergySavaedData::new, ExtendWirelessEnergySavaedData::new, "wireless_energy_data"
         );
         WirelessManaSavaedData.INSTANCE = dataStorage.computeIfAbsent(WirelessManaSavaedData::new, WirelessManaSavaedData::new, "wireless_mana_data");
         CommonSavaedData.INSTANCE.getPlanetUnlocked().forEach((key, value) -> value.forEach(dim -> ExResearchManager.triggerPlanetaryResearch(key, dim)));
      }
   }

   private static void onLevelUnload(Unload event) {
      if (!event.getLevel().isClientSide()) {
         CellSavaedData.INSTANCE.saveNow();
         ((ILevel)event.getLevel()).gtolib$getMachineNet().clear();
      }
   }

   private static void onServerStopping(ServerStoppingEvent event) {
      for (ServerLevel level : event.getServer().getAllLevels()) {
         ((ILevel)level).gtolib$getMachineNet().clear();
      }
   }

   private static void onServerStoppedEvent(ServerStoppedEvent event) {
      tempDifficulty = GTOCore.difficulty;
      WirelessEnergyContainer.server = null;
      CellSavaedData.INSTANCE = new CellSavaedData();
      WirelessEnergySavaedData.INSTANCE = new ExtendWirelessEnergySavaedData();
      WirelessManaSavaedData.INSTANCE = new WirelessManaSavaedData();
      wirelessTick = false;
      isSrmMode = false;
      ReceiverTransmitterHandler.unload();
   }

   private static void onClone(Clone event) {
      PlayerData old = IEnhancedPlayer.of(event.getOriginal()).getPlayerData();
      PlayerData new_ = IEnhancedPlayer.of(event.getEntity()).getPlayerData();
      new_.getPlayerAttributes().copyFrom(old.getPlayerAttributes());
      new_.organItemStacks.addAll(old.organItemStacks);
      new_.organTierCache.putAll(old.organTierCache);
      PlayerUtils.refreshPlayerAttributes(event.getEntity());
   }

   private static void onPlayerLogin(PlayerLoggedInEvent event) {
      Player player = event.getEntity();
      PlayerUtils.refreshPlayerAttributes(player);
      if (player instanceof ServerPlayer serverPlayer && player instanceof IEnhancedPlayer enhancedPlayer) {
         LOGGED_IN.send(serverPlayer);
         ReceiverTransmitterHandler.syncToPlayer(serverPlayer);
         OrganUtilsKt.ktFreshOrganState(enhancedPlayer.getPlayerData());
         if (!wirelessTick) {
            TaskHandler.enqueueTask(serverPlayer.serverLevel(), () -> wirelessTick = true, 200);
         }
      }
   }

   private static void onPlayerDimensionChange(PlayerChangedDimensionEvent event) {
      PlayerUtils.refreshPlayerAttributes(event.getEntity());
      if (event.getEntity() instanceof ServerPlayer serverPlayer) {
         ReceiverTransmitterHandler.syncToPlayer(serverPlayer);
      }
   }

   private static void initHandshake() {
      class diffPacket implements IntSupplier {
         final int innerValue;
         final String innerHash;
         final int recipeScriptsHash;
         int loginIndex;

         diffPacket() {
            this(ForgeCommonEvent.tempDifficulty, MixinConfigPlugin.hash, RecipeScript.LOADED_FILE_HASH);
         }

         diffPacket(int v, String innerHash, int recipeScriptsHash) {
            this.innerValue = v;
            this.innerHash = innerHash == null ? "?" : innerHash;
            this.recipeScriptsHash = recipeScriptsHash;
         }

         @Override
         public int getAsInt() {
            return this.innerValue;
         }

         @Generated
         public void setLoginIndex(int loginIndex) {
            this.loginIndex = loginIndex;
         }

         @Generated
         public int getLoginIndex() {
            return this.loginIndex;
         }
      }

      SimpleChannel networkChannel = NetworkRegistry.newSimpleChannel(GTOCore.id("handshake"), () -> "1", s -> true, s -> true);
      networkChannel.messageBuilder(diffPacket.class, 1, NetworkDirection.LOGIN_TO_CLIENT)
         .encoder((i, buf) -> {
            buf.writeOptional(Optional.of(i.innerValue), FriendlyByteBuf::writeVarInt);
            buf.writeOptional(Optional.of(i.innerHash), FriendlyByteBuf::writeUtf);
            buf.writeInt(i.recipeScriptsHash);
         })
         .decoder(
            buf -> new diffPacket(
               buf.<Integer>readOptional(FriendlyByteBuf::readVarInt).orElse(GTOCore.difficulty),
               buf.<String>readOptional(FriendlyByteBuf::readUtf).orElse(null),
               buf.readInt()
            )
         )
         .consumerNetworkThread(
            (i, ctx) -> {
               if (GTOCore.difficulty != i.innerValue) {
                  ctx.get()
                     .getNetworkManager()
                     .disconnect(
                        Component.translatable(
                           "message.gtocore.difficulty_mismatch",
                           DynamicInitialData.getDifficultyComponent(i.innerValue),
                           DynamicInitialData.getDifficultyComponent(GTOCore.difficulty)
                        )
                     );
               }

               if (i.recipeScriptsHash != RecipeScript.LOADED_FILE_HASH) {
                  ctx.get().getNetworkManager().disconnect(Component.translatable("message.gtocore.custom_recipe.mismatch", i.recipeScriptsHash));
               }

               if (!Objects.equals(MixinConfigPlugin.hash, i.innerHash) && FMLLoader.isProduction()) {
                  ctx.get()
                     .getNetworkManager()
                     .channel()
                     .attr(NetworkConstantsAccessor.getFML_MOD_MISMATCH_DATA())
                     .set(new ModMismatchData(Map.of(GTOCore.id(""), MixinConfigPlugin.hash), Map.of(GTOCore.id(""), Pair.of("gtocore", i.innerHash)), false));
                  ctx.get().getNetworkManager().disconnect(Component.translatable("fml.modmismatchscreen.mismatchedmods"));
               }
            }
         )
         .loginIndex(diffPacket::getLoginIndex, diffPacket::setLoginIndex)
         .noResponse()
         .buildLoginPacketList(isLocal -> List.of(Pair.of(diffPacket.class.getName(), new diffPacket())))
         .add();
   }

   private static void cmdExecute(CommandEvent event) {
      if (SrmManager.isSrmMode()) {
         ParseResults<CommandSourceStack> result = event.getParseResults();
         String cmd = result.getContext().getNodes().getFirst().getNode().getName();
         CommandSourceStack source = result.getContext().getSource();
         if (source.hasPermission(4) && !SrmManager.isWhiteList(cmd)) {
            boolean isServerSource = source.getServer() == source.source;
            boolean isPlayerWithCmd = source.isPlayer()
               && IEnhancedPlayer.of(source.getPlayer()).getPlayerData().getPlayerAttributes().getBooleanCurrent(PlayerAttributes.CMD_STATE);
            if (isServerSource || isPlayerWithCmd) {
               GTOCore.LOGGER.warn("command {} is not in whitelist", cmd);
               if (source.isPlayer()) {
                  Objects.requireNonNull(source.getPlayer())
                     .sendSystemMessage(Component.literal(cmd).append(Component.translatable("item.apotheosis.potion_charm.disabled")));
               }

               event.setCanceled(true);
            }
         }
      }
   }
}
