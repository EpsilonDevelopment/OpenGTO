package com.gtolib.mixin.placebo;

import dev.shadowsoffire.placebo.recipe.RecipeHelper;
import dev.shadowsoffire.placebo.util.RunnableReloader;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(RecipeHelper.class)
public final class RecipeHelperMixin {
   @Overwrite(remap = false)
   @Internal
   public static PreparableReloadListener getReloader(RecipeManager var0) {
      return RunnableReloader.of(() -> {});
   }
}
