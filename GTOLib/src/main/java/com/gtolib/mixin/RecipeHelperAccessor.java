package com.gtolib.mixin;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import dev.shadowsoffire.placebo.recipe.RecipeHelper;
import dev.shadowsoffire.placebo.recipe.RecipeHelper.RecipeFactory;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.function.Consumer;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RecipeHelper.class)
public interface RecipeHelperAccessor {
   @Accessor(value = "PROVIDERS", remap = false)
   @NotNull
   static Multimap<String, Consumer<RecipeFactory>> getProviders() {
      return Multimaps.newSetMultimap(new O2OOpenCacheHashMap<>(), ReferenceOpenHashSet::new);
   }

   @Invoker(value = "addRecipes", remap = false)
   static void addRecipes(RecipeManager var0) {
   }
}
