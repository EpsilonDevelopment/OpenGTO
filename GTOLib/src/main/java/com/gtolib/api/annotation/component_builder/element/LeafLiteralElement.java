package com.gtolib.api.annotation.component_builder.element;

import com.gtolib.api.annotation.component_builder.StyleBuilder;
import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class LeafLiteralElement implements ComponentElement {
   private final UnaryOperator<StyleBuilder> styleOp;
   private final String string;

   public LeafLiteralElement(String s, UnaryOperator<StyleBuilder> styleOp) {
      this.string = s;
      this.styleOp = styleOp;
   }

   @Override
   public List<Component> build() {
      MutableComponent component = Component.literal(this.string);
      if (this.styleOp != null) {
         StyleBuilder styleBuilder = new StyleBuilder();
         this.styleOp.apply(styleBuilder);
         component = styleBuilder.apply(component);
      }

      return Collections.singletonList(component);
   }
}
