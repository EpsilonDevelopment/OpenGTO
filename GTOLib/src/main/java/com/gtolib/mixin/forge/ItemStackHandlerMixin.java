package com.gtolib.mixin.forge;

import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = ItemStackHandler.class, priority = 0)
public class ItemStackHandlerMixin {
   @Overwrite(remap = false)
   protected void validateSlotIndex(int var1) {
   }
}
