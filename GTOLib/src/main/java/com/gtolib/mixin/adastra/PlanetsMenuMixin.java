package com.gtolib.mixin.adastra;

import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import com.gtolib.api.adastra.IAdDisplayTagName;
import earth.terrarium.adastra.common.handlers.base.SpaceStation;
import earth.terrarium.adastra.common.menus.PlanetsMenu;
import earth.terrarium.adastra.common.recipes.SpaceStationRecipe;
import earth.terrarium.adastra.common.recipes.base.IngredientHolder;
import earth.terrarium.adastra.common.registry.ModRecipeTypes;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlanetsMenu.class)
public class PlanetsMenuMixin implements IAdDisplayTagName {
   @Shadow(remap = false)
   @Final
   protected Player player;
   @Shadow(remap = false)
   @Final
   protected int tier;
   @Final
   @Shadow(remap = false)
   protected Level level;
   @Final
   @Shadow(remap = false)
   protected Inventory inventory;
   @Unique
   protected Map<ResourceKey<Level>, List<IAdDisplayTagName.CountIngredient>> gtocore$planetItems = null;

   @Inject(method = "tier", at = @At("HEAD"), remap = false, cancellable = true)
   private void tier(CallbackInfoReturnable<Integer> var1) {
      if (this.tier == 100) {
         var1.setReturnValue(10);
      }
   }

   @Inject(
      method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Ljava/util/Set;Ljava/util/Map;Lit/unimi/dsi/fastutil/objects/Object2BooleanMap;Ljava/util/Set;)V",
      at = @At("TAIL"),
      remap = false
   )
   private void adastra$init(
      int var1,
      Inventory var2,
      Set<ResourceLocation> var3,
      Map<ResourceKey<Level>, Map<UUID, Set<SpaceStation>>> var4,
      Object2BooleanMap<ResourceKey<Level>> var5,
      Set<GlobalPos> var6,
      CallbackInfo var7
   ) {
      this.gtocore$planetItems = this.gtocore$getSpaceStationRecipes();
   }

   @Unique
   private Map<ResourceKey<Level>, List<IAdDisplayTagName.CountIngredient>> gtocore$getSpaceStationRecipes() {
      List<SpaceStationRecipe> var1 = this.level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.SPACE_STATION_RECIPE.get());
      O2OOpenCacheHashMap<ResourceKey<Level>, List<IAdDisplayTagName.CountIngredient>> var2 = new O2OOpenCacheHashMap<>(var1.size());

      for (SpaceStationRecipe var4 : var1) {
         for (IngredientHolder var6 : var4.ingredients()) {
            int var7 = 0;

            for (int var8 = 0; var8 < this.inventory.getContainerSize(); var8++) {
               ItemStack var9 = this.inventory.getItem(var8);
               if (var6.ingredient().test(var9)) {
                  var7 += var9.getCount();
               }
            }

            var2.computeIfAbsent(var4.dimension(), var0 -> new ArrayList<>()).add(new IAdDisplayTagName.CountIngredient(var6.ingredient(), var6.count(), var7));
         }
      }

      return var2;
   }

   @Override
   public Map<ResourceKey<Level>, List<IAdDisplayTagName.CountIngredient>> gtocore$getAdastraDisplayTagNames() {
      return this.gtocore$planetItems;
   }
}
