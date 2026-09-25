package com.gtolib.mixin.emi;

import dev.emi.emi.data.EmiData;
import dev.emi.emi.data.EmiResourceReloadListener;
import java.util.function.Consumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(EmiData.class)
public final class EmiDataMixin {
   @Overwrite(remap = false)
   public static void init(Consumer<EmiResourceReloadListener> var0) {
   }
}
