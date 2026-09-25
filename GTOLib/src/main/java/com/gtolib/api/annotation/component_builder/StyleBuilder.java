package com.gtolib.api.annotation.component_builder;

import com.gregtechceu.gtceu.client.util.TooltipHelper;
import com.gregtechceu.gtceu.client.util.TooltipHelper.GTFormattingCode;
import com.gtolib.api.annotation.NewDataAttributes;
import java.util.function.UnaryOperator;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class StyleBuilder {
   private Style style = Style.EMPTY;
   private Component prefixComponent = Component.empty();
   private int cacheColor;

   public StyleBuilder setPrefix(Component prefixComponent) {
      this.prefixComponent = prefixComponent;
      return this;
   }

   public StyleBuilder setOneTab() {
      return this.setPrefix(NewDataAttributes.PREFIX_TAB);
   }

   public StyleBuilder setTwoTabs() {
      return this.setPrefix(NewDataAttributes.PREFIX_TAB.copy().append(NewDataAttributes.PREFIX_TAB));
   }

   public StyleBuilder setThreeTabs() {
      return this.setPrefix(NewDataAttributes.PREFIX_TAB.copy().append(NewDataAttributes.PREFIX_TAB).append(NewDataAttributes.PREFIX_TAB));
   }

   public StyleBuilder setColor(int rgb) {
      this.style = this.style.withColor(rgb);
      return this;
   }

   public StyleBuilder setGray() {
      this.style = this.style.withColor(ChatFormatting.GRAY);
      return this;
   }

   public StyleBuilder setWarm() {
      if (this.cacheColor == 0) {
         this.cacheColor = StyleCycleColor.WARM.getNextColor();
      }

      this.style = this.style.withColor(this.cacheColor);
      return this;
   }

   public StyleBuilder setProgressive() {
      if (this.cacheColor == 0) {
         this.cacheColor = StyleCycleColor.PROGRESSIVE.getNextColor();
      }

      this.style = this.style.withColor(this.cacheColor);
      return this;
   }

   public StyleBuilder setAqua() {
      this.style = this.style.withColor(ChatFormatting.AQUA);
      return this;
   }

   public StyleBuilder setOrange() {
      this.style = this.style.withColor(16753920);
      return this;
   }

   public StyleBuilder setWhite() {
      this.style = this.style.withColor(ChatFormatting.WHITE);
      return this;
   }

   public StyleBuilder setRed() {
      this.style = this.style.withColor(ChatFormatting.RED);
      return this;
   }

   private StyleBuilder setStyle(UnaryOperator<Style> op) {
      this.style = op.apply(this.style);
      return this;
   }

   private StyleBuilder setGtFormattingCode(GTFormattingCode gtFormat) {
      this.setStyle(style -> style.withColor(gtFormat.getCurrent()));
      return this;
   }

   public StyleBuilder setBlinkingCyan() {
      this.setGtFormattingCode(TooltipHelper.BLINKING_CYAN);
      return this;
   }

   public StyleBuilder setBlinkingRed() {
      return this.setGtFormattingCode(TooltipHelper.BLINKING_RED);
   }

   public StyleBuilder setBlinkingOrange() {
      return this.setGtFormattingCode(TooltipHelper.BLINKING_ORANGE);
   }

   public StyleBuilder setRainbow() {
      return this.setGtFormattingCode(TooltipHelper.RAINBOW);
   }

   public StyleBuilder setRainbowSlow() {
      return this.setGtFormattingCode(TooltipHelper.RAINBOW_SLOW);
   }

   public StyleBuilder setRainbowFast() {
      return this.setGtFormattingCode(TooltipHelper.RAINBOW_FAST);
   }

   public StyleBuilder setGreen() {
      this.style = this.style.withColor(ChatFormatting.GREEN);
      return this;
   }

   public StyleBuilder setBlue() {
      this.style = this.style.withColor(ChatFormatting.BLUE);
      return this;
   }

   public StyleBuilder setYellow() {
      this.style = this.style.withColor(ChatFormatting.YELLOW);
      return this;
   }

   public StyleBuilder setGold() {
      this.style = this.style.withColor(ChatFormatting.GOLD);
      return this;
   }

   public StyleBuilder setLightPurple() {
      this.style = this.style.withColor(ChatFormatting.LIGHT_PURPLE);
      return this;
   }

   public StyleBuilder setMixedRedPurple() {
      this.style = this.style.withColor(14381203);
      return this;
   }

   public StyleBuilder setBold() {
      this.style = this.style.withBold(true);
      return this;
   }

   public StyleBuilder setItalic() {
      this.style = this.style.withItalic(true);
      return this;
   }

   public StyleBuilder setUnderline() {
      this.style = this.style.withUnderlined(true);
      return this;
   }

   public StyleBuilder setStrikethrough() {
      this.style = this.style.withStrikethrough(true);
      return this;
   }

   public StyleBuilder setObfuscated() {
      this.style = this.style.withObfuscated(true);
      return this;
   }

   public MutableComponent apply(MutableComponent component) {
      return Component.empty().append(this.prefixComponent).append(component.withStyle(this.style));
   }
}
