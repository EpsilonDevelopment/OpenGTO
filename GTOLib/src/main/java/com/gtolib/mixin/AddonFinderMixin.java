package com.gtolib.mixin;

import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gtolib.Client;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(AddonFinder.class)
public class AddonFinderMixin {
   @Overwrite(remap = false)
   public static List<IGTAddon> getAddons() {
      return Client.ADDONS;
   }
}
