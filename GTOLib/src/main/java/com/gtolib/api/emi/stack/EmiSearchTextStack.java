package com.gtolib.api.emi.stack;

import com.gtolib.GTOCore;
import dev.emi.emi.api.stack.EmiStack;
import java.util.List;
import lombok.Generated;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class EmiSearchTextStack extends EmiStack {
   private static final ResourceLocation ID = GTOCore.id("search_text");
   private final String text;

   public EmiSearchTextStack(String text) {
      this.text = text;
   }

   @Override
   public EmiStack copy() {
      return new EmiSearchTextStack(this.text);
   }

   @Override
   public void render(GuiGraphics draw, int x, int y, float delta, int flags) {
      String first2Chars = this.text.length() > 2 ? this.text.substring(0, 2) : this.text;
      draw.drawString(Minecraft.getInstance().font, first2Chars, x, y, 16777215, false);
   }

   @Override
   public List<ClientTooltipComponent> getTooltip() {
      return List.of(ClientTooltipComponent.create(this.getName().getVisualOrderText()));
   }

   @Override
   public boolean isEmpty() {
      return this.text.isEmpty();
   }

   @Override
   public CompoundTag getNbt() {
      CompoundTag tag = new CompoundTag();
      tag.putString("text", this.text);
      return tag;
   }

   @Override
   public Object getKey() {
      return this.text;
   }

   @Override
   public ResourceLocation getId() {
      return ID;
   }

   @Override
   public List<Component> getTooltipText() {
      return List.of(Component.translatable("gtocore.emi.search_text", this.text), Component.translatable("gtocore.emi.search_text.how_to_use"));
   }

   @Override
   public Component getName() {
      return Component.literal(this.text);
   }

   @Override
   public boolean isEqual(EmiStack stack) {
      return stack instanceof EmiSearchTextStack other ? this.text.equals(other.text) : false;
   }

   @Generated
   public String getText() {
      return this.text;
   }
}
