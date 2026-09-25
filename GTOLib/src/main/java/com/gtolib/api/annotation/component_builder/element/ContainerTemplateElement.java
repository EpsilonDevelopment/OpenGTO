package com.gtolib.api.annotation.component_builder.element;

import com.gtolib.api.annotation.component_builder.ComponentBuilder;
import com.gtolib.api.annotation.component_builder.ComponentTemplate;
import com.gtolib.api.annotation.component_builder.StyleBuilder;
import com.gtolib.api.annotation.component_builder.TranslationKeyProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ContainerTemplateElement implements ComponentElement {
   private final ComponentTemplate template;
   private final Object value;
   private final UnaryOperator<StyleBuilder> styleOpFromCreate;

   public ContainerTemplateElement(ComponentTemplate template, Object value, UnaryOperator<StyleBuilder> styleOpFromCreate) {
      this.template = template;
      if (value instanceof ComponentBuilder builder && builder.isEmpty()) {
         this.value = null;
      } else {
         this.value = value;
      }

      this.styleOpFromCreate = styleOpFromCreate;
   }

   @Override
   public List<Component> build() {
      List<Component> components = new ArrayList<>();
      Object var6;
      if (this.value instanceof ComponentBuilder builder) {
         var6 = builder.buildSingle();
      } else {
         var6 = this.value != null ? this.value : this.template.getDefaultValue();
      }

      String translationKey = TranslationKeyProvider.getTranslationKey(
         this.template.getCnTemplate(), this.template.getEnTemplate(), "gtocore.lang.template." + this.template.getKey()
      );
      MutableComponent mainComponent = Component.translatable(translationKey, var6);
      if (this.template.getStyleOpFromRegister() != null) {
         StyleBuilder styleBuilder = new StyleBuilder();
         this.template.getStyleOpFromRegister().apply(styleBuilder);
         mainComponent = styleBuilder.apply(mainComponent);
      }

      if (this.styleOpFromCreate != null) {
         StyleBuilder styleBuilder = new StyleBuilder();
         this.styleOpFromCreate.apply(styleBuilder);
         mainComponent = styleBuilder.apply(mainComponent);
      }

      components.add(mainComponent);
      return components;
   }
}
