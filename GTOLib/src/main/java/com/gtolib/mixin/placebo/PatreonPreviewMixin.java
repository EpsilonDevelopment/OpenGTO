package com.gtolib.mixin.placebo;

import dev.shadowsoffire.placebo.patreon.PatreonPreview;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(PatreonPreview.class)
public final class PatreonPreviewMixin {
   @Overwrite(remap = false)
   public static void tick(PlayerTickEvent var0) {
   }
}
