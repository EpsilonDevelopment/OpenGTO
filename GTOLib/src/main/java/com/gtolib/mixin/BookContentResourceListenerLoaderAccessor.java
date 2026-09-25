package com.gtolib.mixin;

import com.google.gson.JsonElement;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import vazkii.patchouli.client.book.BookContentResourceListenerLoader;

@Mixin(BookContentResourceListenerLoader.class)
public interface BookContentResourceListenerLoaderAccessor {
   @Accessor(value = "data", remap = false)
   @NotNull
   Map<ResourceLocation, Map<ResourceLocation, JsonElement>> getData();
}
