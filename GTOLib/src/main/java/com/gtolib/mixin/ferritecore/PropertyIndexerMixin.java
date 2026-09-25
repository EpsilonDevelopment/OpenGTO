package com.gtolib.mixin.ferritecore;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.Map;
import malte0811.ferritecore.fastmap.PropertyIndexer;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PropertyIndexer.class)
public class PropertyIndexerMixin {
   @Mutable
   @Shadow(remap = false)
   @Final
   private static Map<Property<?>, PropertyIndexer<?>> KNOWN_INDEXERS;

   @Inject(method = "<clinit>", at = @At("TAIL"), remap = false)
   private static void init(CallbackInfo var0) {
      KNOWN_INDEXERS = new Reference2ObjectOpenHashMap<>(KNOWN_INDEXERS);
   }
}
