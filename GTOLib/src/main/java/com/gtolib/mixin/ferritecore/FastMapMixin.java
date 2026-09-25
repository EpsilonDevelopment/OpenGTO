package com.gtolib.mixin.ferritecore;

import com.gto.fastcollection.fastutil.O2IOpenCacheHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.Collection;
import java.util.Map;
import malte0811.ferritecore.fastmap.FastMap;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FastMap.class)
public class FastMapMixin {
   @Mutable
   @Shadow(remap = false)
   @Final
   private Object2IntMap<Property<?>> toKeyIndex;

   @Inject(method = "<init>", at = @At("TAIL"), remap = false)
   private void init(Collection var1, Map var2, boolean var3, CallbackInfo var4) {
      this.toKeyIndex = new O2IOpenCacheHashMap<>(this.toKeyIndex);
      this.toKeyIndex.defaultReturnValue(-1);
   }
}
