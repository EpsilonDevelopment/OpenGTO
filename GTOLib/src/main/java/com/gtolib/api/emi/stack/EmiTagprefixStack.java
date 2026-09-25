package com.gtolib.api.emi.stack;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKey;
import com.gtolib.GTOCore;
import dev.emi.emi.api.stack.EmiStack;
import java.util.List;
import java.util.Set;
import lombok.Generated;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class EmiTagprefixStack extends EmiStack {
   private final TagPrefix tagPrefix;
   private final FluidStorageKey storageKey;
   private final boolean isFluidKey;
   private EmiStack firstValidStack;
   public static final Set<TagPrefix> filteredPrefixes = Set.of(
      TagPrefix.NULL_PREFIX,
      TagPrefix.dye,
      TagPrefix.rock,
      TagPrefix.surfaceRock,
      TagPrefix.log,
      TagPrefix.planks,
      TagPrefix.slab,
      TagPrefix.stairs,
      TagPrefix.fence,
      TagPrefix.fenceGate,
      TagPrefix.door
   );

   public EmiTagprefixStack(TagPrefix tagPrefix) {
      this.tagPrefix = tagPrefix;
      this.storageKey = null;
      this.isFluidKey = false;
   }

   public EmiTagprefixStack(FluidStorageKey storageKey) {
      this.tagPrefix = null;
      this.storageKey = storageKey;
      this.isFluidKey = true;
   }

   @Override
   public EmiStack copy() {
      return this.isFluidKey ? new EmiTagprefixStack(this.storageKey) : new EmiTagprefixStack(this.tagPrefix);
   }

   @Override
   public void render(GuiGraphics draw, int x, int y, float delta, int flags) {
      if (this.isFluidKey) {
         TagPrefixRenderer.render(draw, x, y, delta, this.storageKey);
      } else {
         TagPrefixRenderer.render(draw, x, y, delta, this.tagPrefix);
      }
   }

   @Override
   public boolean isEmpty() {
      return this.isFluidKey ? this.storageKey == null : this.tagPrefix == TagPrefix.NULL_PREFIX;
   }

   @Override
   public CompoundTag getNbt() {
      return null;
   }

   @Override
   public Object getKey() {
      return this.isFluidKey ? this.storageKey : this.tagPrefix;
   }

   @Override
   public ResourceLocation getId() {
      return this.isFluidKey ? this.storageKey.getResourceLocation() : GTOCore.id("item/" + this.tagPrefix.getLowerCaseName());
   }

   @Override
   public List<Component> getTooltipText() {
      return List.of(this.getName());
   }

   @Override
   public List<ClientTooltipComponent> getTooltip() {
      List<Component> texts = List.of(
         this.getName(),
         Component.translatable("gtocore.emi.tagprefix.tooltip").withStyle(ChatFormatting.DARK_GRAY),
         Component.translatable("gtocore.emi.tagprefix.tooltip.1").withStyle(ChatFormatting.DARK_GRAY),
         Component.translatable("gtocore.emi.tagprefix.tooltip.2").withStyle(ChatFormatting.DARK_GRAY)
      );
      return texts.stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).toList();
   }

   @Override
   public Component getName() {
      return TagPrefixNameUtil.getName(this.isFluidKey, this.tagPrefix, this.storageKey);
   }

   public EmiStack getFirstValidStack() {
      if (this.firstValidStack != null) {
         return this.firstValidStack;
      }

      Set<Material> allMats = GTCEuAPI.materialManager.getRegisteredMaterials();
      if (this.isFluidKey) {
         for (Material mat : allMats) {
            if (mat.hasFluid() && mat.getFluid(this.storageKey) != null) {
               this.firstValidStack = EmiStack.of(mat.getFluid(this.storageKey));
               break;
            }
         }
      } else {
         for (Material mat : allMats) {
            ItemStack i = ChemicalHelper.get(this.tagPrefix, mat);
            if (!i.isEmpty()) {
               this.firstValidStack = EmiStack.of(i);
               break;
            }
         }
      }

      return this.firstValidStack;
   }

   @Generated
   public TagPrefix getTagPrefix() {
      return this.tagPrefix;
   }

   @Generated
   public FluidStorageKey getStorageKey() {
      return this.storageKey;
   }

   @Generated
   public boolean isFluidKey() {
      return this.isFluidKey;
   }
}
