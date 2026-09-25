package com.gtolib.mixin.emi.apiimpl;

import com.google.common.collect.ImmutableList;
import com.gtolib.api.GTOApi;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.registry.EmiPluginContainer;
import java.util.HashMap;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(EmiAgnos.class)
public abstract class EmiAgnosMixin {
   @Overwrite(remap = false)
   public static List<EmiPluginContainer> getPlugins() {
      HashMap var0 = new HashMap();
      GTOApi.EMI_PLUGIN_EVENT.call(var1 -> var0.put(var1.id(), var1));
      return ImmutableList.copyOf(var0.values());
   }
}
