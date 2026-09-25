package com.gtolib;

import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gto.fastcollection.fastutil.OpenCacheHashSet;
import com.gtocore.config.GTOConfig;
import com.gtolib.api.beam.BeamClientManager;
import com.gtolib.cache.CacheManager;
import com.gtolib.cache.resourcestree.SaveableResourcesTree;
import com.gtolib.utils.reflect.FieldReference;
import com.gtolib.utils.reflect.MethodReference;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.ScreenEvent.Init;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.GameShuttingDownEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.internal.BrandingControl;

public final class Client {
   public static final List<IGTAddon> ADDONS = Collections.singletonList(GTAddon.INSTANCE);
   public static boolean disableAdditionalEmiTooltip;
   public static final Set<ResourceKey<Level>> UNLOCKED_PLANET = new OpenCacheHashSet<>();
   public static boolean initializedBook;
   public static UUID SERVER_IDENTIFIER;
   public static String autoRenameName = GTOConfig.INSTANCE.gamePlay.renamePatternDefaultString;

   static void init() {
      BeamClientManager.init();
      Client.Wrapper.init();
   }

   private static class Wrapper {
      private static boolean init;

      private static void init() {
         MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, Client.Wrapper::gameShuttingDown);
         MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, Client.Wrapper::onScreenInit);
      }

      private static void gameShuttingDown(GameShuttingDownEvent event) {
         if (CacheManager.shouldCache()) {
            SaveableResourcesTree.save();
         }
      }

      private static void onScreenInit(Init event) {
         if (event.getScreen() instanceof TitleScreen && !init) {
            init = true;

            try {
               long secondsToStart = ManagementFactory.getRuntimeMXBean().getUptime() / 1000L;
               MethodReference.fromClass(BrandingControl.class, "computeBranding").invoke();
               FieldReference<List<String>> brandingsField = FieldReference.fromClass(BrandingControl.class, "brandings");
               List<String> brandings = brandingsField.get();
               if (brandings.size() > 1) {
                  List<String> newBrandings = new ArrayList<>(brandings);
                  brandingsField.set(newBrandings);
                  newBrandings.add("Launch took " + secondsToStart + "s");
               }
            } catch (Exception var6) {
            }
         }
      }
   }
}
