package com.gtolib.mixin.mc.recipe;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.gregtechceu.gtceu.common.data.GTRecipes;
import com.gregtechceu.gtceu.data.pack.GTDynamicDataPack;
import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import com.gtocore.common.data.GTORecipes;
import com.gtolib.GTOCore;
import com.gtolib.mixin.RecipeHelperAccessor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition.IContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RecipeManager.class, priority = 0)
public abstract class RecipeManagerMixin {
   @Shadow
   private Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes;
   @Shadow
   private boolean hasErrors;
   @Shadow
   private Map<ResourceLocation, Recipe<?>> byName;
   @Shadow(remap = false)
   @Final
   private IContext context;

   @Inject(
      method = "apply(Ljava/util/Map; Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
      at = @At("HEAD"),
      cancellable = true
   )
   private void apply(Map<ResourceLocation, JsonElement> var1, ResourceManager var2, ProfilerFiller var3, CallbackInfo var4) {
      var4.cancel();
      if (GTORecipes.cache) {
         this.recipes = GTORecipes.RECIPES_CACHE;
         this.byName = GTORecipes.BYNAME_CACHE;
      } else {
         GTORecipes.cache = true;
         long var5 = System.currentTimeMillis();
         this.hasErrors = false;
         O2OOpenCacheHashMap<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> var7 = new O2OOpenCacheHashMap<>();
         O2OOpenCacheHashMap<ResourceLocation, Recipe<?>> var8 = new O2OOpenCacheHashMap<>(16384);

         for (Entry var10 : var1.entrySet()) {
            ResourceLocation var11 = (ResourceLocation)var10.getKey();
            if (!var11.getPath().isEmpty() && var11.getPath().charAt(0) != '_') {
               try {
                  if (!((JsonElement)var10.getValue()).isJsonObject()
                     || CraftingHelper.processConditions(((JsonElement)var10.getValue()).getAsJsonObject(), "conditions", this.context)) {
                     Recipe var12 = GTORecipes.fromJson(var11, GsonHelper.convertToJsonObject((JsonElement)var10.getValue(), "top element"), this.context);
                     if (var12 != null) {
                        Recipe var14 = GTORecipes.convert(var12);
                        if (var14 != null) {
                           var7.computeIfAbsent(var14.getType(), var0 -> new HashMap<>()).put(var11, var14);
                           var8.put(var11, var14);
                        }
                     }
                  }
               } catch (IllegalArgumentException | JsonParseException var13) {
               }
            }
         }

         GTRecipes.RECIPE_MAP.forEach((var2x, var3x) -> {
            var7.computeIfAbsent(var3x.getType(), var0x -> new HashMap<>()).put(var2x, var3x);
            var8.put(var2x, var3x);
         });
         this.recipes = var7;
         this.byName = var8;
         RecipeHelperAccessor.addRecipes((RecipeManager)(Object)this);
         this.recipes = ImmutableMap.copyOf(this.recipes);
         this.byName = ImmutableMap.copyOf(this.byName);
         GTORecipes.RECIPES_CACHE = this.recipes;
         GTORecipes.BYNAME_CACHE = this.byName;
         GTOCore.LOGGER.info("Loaded {} recipes, took {}ms", GTORecipes.BYNAME_CACHE.size(), System.currentTimeMillis() - var5);
         GTDynamicDataPack.clearServer();
         RecipeHelperAccessor.getProviders().clear();
      }
   }

   @Inject(method = "replaceRecipes", at = @At("HEAD"), cancellable = true)
   private void replaceRecipes(Iterable<Recipe<?>> var1, CallbackInfo var2) {
      var2.cancel();
      if (GTORecipes.cache) {
         this.recipes = GTORecipes.RECIPES_CACHE;
         this.byName = GTORecipes.BYNAME_CACHE;
      } else {
         ArrayList<Recipe<?>> var3 = new ArrayList<>((Collection<Recipe<?>>)var1);
         var3.addAll(GTRecipes.RECIPE_MAP.values());
         this.hasErrors = false;
         O2OOpenCacheHashMap<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> var4 = new O2OOpenCacheHashMap<>();
         Builder var5 = ImmutableMap.builder();
         var3.forEach(var2x -> {
            Map<ResourceLocation, Recipe<?>> var3x = var4.computeIfAbsent(var2x.getType(), var0x -> new HashMap<>());
            ResourceLocation var4x = var2x.getId();
            Recipe var5x = var3x.put(var4x, var2x);
            var5.put(var4x, var2x);
            if (var5x != null) {
               throw new IllegalStateException("Duplicate recipe ignored with ID " + var4x);
            }
         });
         this.recipes = ImmutableMap.copyOf(var4);
         this.byName = var5.build();
      }
   }
}
