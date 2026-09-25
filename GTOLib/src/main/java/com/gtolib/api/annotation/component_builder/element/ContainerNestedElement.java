package com.gtolib.api.annotation.component_builder.element;

import com.gtolib.api.annotation.component_builder.ComponentBuilder;
import java.util.List;
import net.minecraft.network.chat.Component;

public class ContainerNestedElement implements ComponentElement {
   private final ComponentBuilder nestedBuilder;

   public ContainerNestedElement(ComponentBuilder nestedBuilder) {
      this.nestedBuilder = nestedBuilder;
   }

   @Override
   public List<Component> build() {
      return this.nestedBuilder.build().get();
   }
}
