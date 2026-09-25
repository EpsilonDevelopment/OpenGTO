package com.gtolib.api.annotation.component_builder.element;

import com.gtolib.api.annotation.component_builder.StyleBuilder;
import com.gtolib.api.annotation.component_builder.TranslationKeyProvider;
import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class LeafTranslatableElement implements ComponentElement {
   private final String cn;
   private final String en;
   private final UnaryOperator<StyleBuilder> styleOp;
   private final Object[] args;

   public LeafTranslatableElement(String cn, String en, UnaryOperator<StyleBuilder> styleOp, Object... args) {
      this.cn = cn;
      this.en = en;
      this.styleOp = styleOp;
      this.args = args;
   }

   @Override
   public List<Component> build() {
      String translationKey = TranslationKeyProvider.getTranslationKey(this.cn, this.en);
      MutableComponent component = Component.translatable(translationKey, this.args);
      if (this.styleOp != null) {
         StyleBuilder styleBuilder = new StyleBuilder();
         this.styleOp.apply(styleBuilder);
         component = styleBuilder.apply(component);
      }

      return Collections.singletonList(component);
   }
}
