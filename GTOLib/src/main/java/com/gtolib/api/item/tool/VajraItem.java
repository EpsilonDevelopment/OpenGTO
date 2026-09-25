package com.gtolib.api.item.tool;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.item.tool.GTToolItem;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.IGTToolDefinition;
import com.gregtechceu.gtceu.api.item.tool.MaterialToolTier;
import com.gtocore.client.KeyBind;
import com.mojang.blaze3d.platform.InputConstants.Key;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class VajraItem extends GTToolItem {
   protected VajraItem(GTToolType toolType, MaterialToolTier tier, Material material, IGTToolDefinition definition, Properties properties) {
      super(toolType, tier, material, definition, properties);
   }

   @Override
   public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
      super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
      if (level != null && level.isClientSide()) {
         Key keyBinding = KeyBind.vajraKey.getKey();
         tooltipComponents.add(Component.translatable("item.gtceu.tool.vajra.tooltip", keyBinding.getDisplayName()));
         tooltipComponents.add(Component.translatable("item.gtceu.tool.vajra.tooltip.shift", keyBinding.getDisplayName()));
         float maxSpeed = this.getMaterialToolSpeed(stack);
         float speed = stack.getOrCreateTag().contains("ToolSpeed") ? stack.getOrCreateTag().getFloat("ToolSpeed") : maxSpeed;
         tooltipComponents.add(Component.translatable("item.gtceu.tool.vajra.tooltip.max_speed", speed, maxSpeed));
      }
   }
}
