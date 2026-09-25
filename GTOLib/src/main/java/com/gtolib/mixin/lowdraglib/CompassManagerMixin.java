package com.gtolib.mixin.lowdraglib;

import com.lowdragmc.lowdraglib.gui.compass.CompassManager;
import javax.annotation.Nonnull;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(CompassManager.class)
public class CompassManagerMixin {
   @Overwrite(remap = false)
   public void init() {
   }

   @Overwrite
   public void onResourceManagerReload(@Nonnull ResourceManager var1) {
   }
}
