package com.gtolib.api.data.chemical.material;

import com.gregtechceu.gtceu.api.data.chemical.Element;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.Material.Builder;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlag;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.MaterialProperties;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty.GasTier;
import com.gregtechceu.gtceu.api.data.chemical.material.registry.MaterialRegistry;
import com.gregtechceu.gtceu.common.unification.material.MaterialRegistryManager;
import com.gtolib.api.registries.GTORegistration;
import net.minecraft.world.item.Rarity;

public class GTOMaterialBuilder extends Builder {
   public static final MaterialRegistry REGISTRY = MaterialRegistryManager.getInstance().createRegistry(GTORegistration.GTO);
   private Rarity rarity;
   private boolean glow;
   private int temp;

   public static MaterialProperties getProperties(Material material) {
      return ((GTOMaterial)material).gtolib$getProperties();
   }

   public static int getTemp(Material material) {
      return ((GTOMaterial)material).gtolib$temp();
   }

   public GTOMaterialBuilder(String mane) {
      super(REGISTRY, mane);
   }

   public GTOMaterialBuilder wood() {
      return (GTOMaterialBuilder)super.wood();
   }

   public GTOMaterialBuilder fluid() {
      return (GTOMaterialBuilder)super.fluid();
   }

   public GTOMaterialBuilder liquid() {
      return (GTOMaterialBuilder)super.liquid();
   }

   public GTOMaterialBuilder plasma() {
      return (GTOMaterialBuilder)super.plasma();
   }

   public GTOMaterialBuilder gas() {
      return (GTOMaterialBuilder)super.gas();
   }

   public GTOMaterialBuilder dust() {
      return (GTOMaterialBuilder)super.dust();
   }

   public GTOMaterialBuilder gem() {
      return (GTOMaterialBuilder)super.gem();
   }

   public GTOMaterialBuilder ingot() {
      return (GTOMaterialBuilder)super.ingot();
   }

   public GTOMaterialBuilder polymer() {
      return (GTOMaterialBuilder)super.polymer();
   }

   public GTOMaterialBuilder ore() {
      return (GTOMaterialBuilder)super.ore();
   }

   public GTOMaterialBuilder addOreByproducts(Material... byproducts) {
      return (GTOMaterialBuilder)super.addOreByproducts(byproducts);
   }

   public GTOMaterialBuilder color(int color) {
      return (GTOMaterialBuilder)super.color(color);
   }

   public GTOMaterialBuilder secondaryColor(int color) {
      return (GTOMaterialBuilder)super.secondaryColor(color);
   }

   public GTOMaterialBuilder iconSet(MaterialIconSet iconSet) {
      return (GTOMaterialBuilder)super.iconSet(iconSet);
   }

   public GTOMaterialBuilder components(Object... components) {
      return (GTOMaterialBuilder)super.components(components);
   }

   public GTOMaterialBuilder flags(MaterialFlag... flags) {
      return (GTOMaterialBuilder)super.flags(flags);
   }

   public GTOMaterialBuilder element(Element element) {
      return (GTOMaterialBuilder)super.element(element);
   }

   public GTOMaterialBuilder blastTemp(int temp, GasTier gasTie) {
      return (GTOMaterialBuilder)super.blastTemp(temp, gasTie);
   }

   public GTOMaterialBuilder blastTemp(int temp, GasTier gasTie, int eutOverride) {
      return (GTOMaterialBuilder)super.blastTemp(temp, gasTie, eutOverride);
   }

   public GTOMaterialBuilder blastTemp(int temp, GasTier gasTie, int eutOverride, int durationOverride) {
      return (GTOMaterialBuilder)super.blastTemp(temp, gasTie, eutOverride, durationOverride);
   }

   public GTOMaterialBuilder blastTemp(int temp) {
      return (GTOMaterialBuilder)super.blast(temp);
   }

   public GTOMaterialBuilder cableProperties(long voltage, int amperage, int loss, boolean isSuperCon) {
      return (GTOMaterialBuilder)super.cableProperties(voltage, amperage, loss, isSuperCon);
   }

   @Override
   public Material buildAndRegister() {
      Material mat = super.buildAndRegister();
      if (mat instanceof GTOMaterial material) {
         if (this.rarity != null) {
            material.gtolib$setRarity(this.rarity);
         }

         if (this.glow) {
            material.gtolib$setGlow();
         }

         if (this.temp > 0) {
            material.gtolib$setTemp(this.temp);
         }
      }

      return mat;
   }

   public GTOMaterialBuilder rarity(Rarity rarity) {
      this.rarity = rarity;
      return this;
   }

   public GTOMaterialBuilder glow(boolean glow) {
      this.glow = glow;
      return this;
   }

   public GTOMaterialBuilder temp(int temp) {
      this.temp = temp;
      return this;
   }
}
