package com.gtolib.api.annotation.component_builder.element;

import com.gtolib.api.annotation.component_builder.StyleBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class LeafComponentElement implements ComponentElement {
   private final UnaryOperator<StyleBuilder> styleOp;
   private final Supplier<List<Component>> component;

   public LeafComponentElement(Supplier<List<Component>> component, UnaryOperator<StyleBuilder> styleOp) {
      this.component = component;
      this.styleOp = styleOp;
   }

   public LeafComponentElement(Component component, UnaryOperator<StyleBuilder> styleOp) {
      this(() -> Collections.singletonList(component), styleOp);
   }

   public static LeafComponentElement create(Supplier<Component> component, UnaryOperator<StyleBuilder> styleOp) {
      return new LeafComponentElement(() -> component.get().toFlatList(), styleOp);
   }

   @Override
   public List<Component> build() {
      List<MutableComponent> components = this.component.get().stream().map(Component::copy).toList();
      List<Component> result = new ArrayList<>();

      for (MutableComponent mutableComponent : components) {
         if (this.styleOp != null) {
            StyleBuilder styleBuilder = new StyleBuilder();
            this.styleOp.apply(styleBuilder);
            result.add(styleBuilder.apply(mutableComponent));
         }
      }

      return result;
   }
}
