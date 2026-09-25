package com.gtolib.mixin.forge;

import io.netty.util.AttributeKey;
import net.minecraftforge.network.NetworkConstants;
import net.minecraftforge.network.ConnectionData.ModMismatchData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NetworkConstants.class)
public interface NetworkConstantsAccessor {
   @Accessor(remap = false)
   static AttributeKey<ModMismatchData> getFML_MOD_MISMATCH_DATA() {
      throw new UnsupportedOperationException();
   }
}
