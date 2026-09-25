package com.gtolib.mixin.mc;

import com.gtolib.api.fluid.IFluid;
import com.gtolib.api.recipe.lookup.MapIngredient;
import com.gtolib.utils.RegistriesUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = Fluid.class, priority = 0)
public class FluidMixin implements IFluid {
   @Unique
   private int gtolib$mapFluid;
   @Unique
   private FluidStack gtolib$readOnlyStack;
   @Unique
   private ResourceLocation gtolib$id;

   @NotNull
   @Override
   public ResourceLocation gtolib$getIdLocation() {
      ResourceLocation var1 = this.gtolib$id;
      if (var1 == null) {
         var1 = ForgeRegistries.FLUIDS.getKey((Fluid)(Object)this);
         if (RegistriesUtils.IDCache) {
            this.gtolib$id = var1;
         }
      }

      return var1;
   }

   @Override
   public FluidStack gtolib$getReadOnlyStack() {
      if (this.gtolib$readOnlyStack == null) {
         this.gtolib$readOnlyStack = new FluidStack((Fluid)(Object)this, 1000);
      }

      return this.gtolib$readOnlyStack;
   }

   @Override
   public int gtolib$getMapFluid() {
      return this.gtolib$mapFluid;
   }

   @Override
   public int gtolib$getOrCreateMapFluid() {
      if (this.gtolib$mapFluid == 0) {
         this.gtolib$mapFluid = MapIngredient.getCount(null);
      }

      return this.gtolib$mapFluid;
   }
}
