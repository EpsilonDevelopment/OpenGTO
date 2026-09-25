package com.gtolib.mixin.placebo;

import dev.shadowsoffire.placebo.patreon.WingsManager;
import net.minecraftforge.client.event.EntityRenderersEvent.AddLayers;
import net.minecraftforge.client.event.InputEvent.Key;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(WingsManager.class)
public final class WingsManagerMixin {
   @Overwrite(remap = false)
   public static void init(FMLClientSetupEvent var0) {
   }

   @Overwrite(remap = false)
   public static void addLayers(AddLayers var0) {
   }

   @Overwrite(remap = false)
   public static void keys(Key var0) {
   }
}
