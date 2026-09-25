package com.gtolib;

import com.gto.datasynclib.DataSyncCodec;
import com.gtocore.config.GTOConfig;
import com.gtolib.api.beam.BeamManager;
import com.gtolib.api.machine.feature.multiblock.ICrossRecipeMachine;
import com.gtolib.api.registries.ScanningClass;
import com.gtolib.api.wireless.ReceiverTransmitterHandler;
import com.gtolib.forge.ForgeCommonEvent;
import com.lowdragmc.lowdraglib.Platform;
import dev.architectury.networking.simple.SimpleNetworkManager;
import java.io.File;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

public final class GTOCore {
   public static final String MOD_ID = "gtocore";
   public static final String NAME = "GTO Core";
   public static final Logger LOGGER = MixinConfigPlugin.LOGGER;
   public static final int difficulty = GTOConfig.INSTANCE.gamePlay.difficulty.ordinal() + 1;
   private static File location;
   private static boolean bootstrapped;
   public static final SimpleNetworkManager NETWORK_MANAGER = SimpleNetworkManager.create("gtocore");

   private GTOCore() {
   }

   public static void bootstrap() {
      if (!bootstrapped) {
         bootstrapped = true;
         if (FMLEnvironment.dist.isClient()) {
            Client.init();
         }

         ScanningClass.init();
         BeamManager.init();
         ReceiverTransmitterHandler.init();
         ForgeCommonEvent.init();
         DataSyncCodec.register(ICrossRecipeMachine.Thread.class, ICrossRecipeMachine.Thread.DATA_CODECS);
      }
   }

   public static ResourceLocation id(String name) {
      return new ResourceLocation("gtocore", name, null);
   }

   public static boolean isEasy() {
      return difficulty == 1;
   }

   public static boolean isNormal() {
      return difficulty == 2;
   }

   public static boolean isExpert() {
      return difficulty == 3;
   }

   public static File getFile() {
      if (location == null) {
         location = new File(Platform.getGamePath().toFile(), "gtocore");
         if (location.mkdir()) {
            LOGGER.info("create gtocore folder");
         }
      }

      return location;
   }

   public static File getFile(String child) {
      return new File(getFile(), child);
   }
}
