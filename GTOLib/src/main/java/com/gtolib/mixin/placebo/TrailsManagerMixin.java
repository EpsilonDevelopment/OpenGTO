package com.gtolib.mixin.placebo;

import dev.shadowsoffire.placebo.patreon.TrailsManager;
import net.minecraftforge.client.event.InputEvent.Key;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(TrailsManager.class)
public final class TrailsManagerMixin {
   @Overwrite(remap = false)
   public static void init() {
   }

   @Overwrite(remap = false)
   public static void clientTick(ClientTickEvent var0) {
   }

   @Overwrite(remap = false)
   public static void keys(Key var0) {
   }
}
