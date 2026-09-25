package com.gtolib.api.annotation.component_builder.element;

import com.gtolib.api.annotation.component_builder.StyleBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import net.minecraft.network.chat.Component;

public class ContainerTextBlockElement implements ComponentElement {
   private final String[] cnLines;
   private final String[] enLines;
   private final UnaryOperator<StyleBuilder> styleOp;
   private final Object[] args;

   public ContainerTextBlockElement(String[] cnLines, String[] enLines, UnaryOperator<StyleBuilder> styleOp, Object... args) {
      this.cnLines = cnLines;
      this.enLines = enLines;
      this.styleOp = styleOp;
      this.args = args;
   }

   @Override
   public List<Component> build() {
      List<Component> components = new ArrayList<>();

      for (int i = 0; i < this.cnLines.length; i++) {
         List<Component> built = new LeafTranslatableElement(this.cnLines[i], this.enLines[i], this.styleOp, this.args).build();
         components.addAll(built);
      }

      return components;
   }
}
